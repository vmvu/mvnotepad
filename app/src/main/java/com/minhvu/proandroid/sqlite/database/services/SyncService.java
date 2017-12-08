package com.minhvu.proandroid.sqlite.database.services;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.OnDisconnect;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.models.data.DatabaseGreenRoom;
import com.minhvu.proandroid.sqlite.database.models.data.NoteContract;
import com.minhvu.proandroid.sqlite.database.models.data.NoteDBHelper;
import com.minhvu.proandroid.sqlite.database.models.entity.Image;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by vomin on 11/28/2017.
 */

public class SyncService extends ALongRunningNonStickyBroadcastService {

    private String user;
    private NoteDBHelper helper;

    public SyncService() {
        super("SyncService");
    }

    @Override
    public void handIntentBroadcast(Intent intentBroadcast) {
        String intentAction = intentBroadcast.getAction();
        if (TextUtils.isEmpty(intentAction) || !intentAction.equals(getString(R.string.broadcast_sync))) {
            return;
        }
        DatabaseGreenRoom.s_registerClient();
        syncFirebase();
    }

    private void syncFirebase() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                Sync();
            }
        };
        thread.start();
    }


    private void Sync() {
        //getUser();
        helper = DatabaseGreenRoom.s_enter(this);
        user = "shshhs";
        if (TextUtils.isEmpty(user)) {
            return;
        }
        final List<Note> noteList = getListNote();
        if (noteList == null || noteList.size() == 0) {
            return;
        }
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("UserList").child(user);
        for (Note note : noteList) {
            if (TextUtils.isEmpty(note.getKeySync())) {
                DatabaseReference mRef = db.push();
                note.setKeySync(mRef.getKey());
                mRef.setValue(note);
            } else {
                db.child(note.getKeySync()).setValue(note);
            }
        }
        Thread threadUpdateKeySync = new Thread() {
            @Override
            public void run() {
                super.run();
                updateNoteWithKeySync(noteList);
            }
        };
        threadUpdateKeySync.start();
        DocumentSync(noteList);
        DatabaseGreenRoom.s_leave();
        DatabaseGreenRoom.s_unregisterClient();
    }

    private void DocumentSync(List<Note> noteList) {
        //getUser();
        if (TextUtils.isEmpty(user)) {
            return;
        }
        HashMap<String, ArrayList<Image>> idImageList = getImageList(noteList);
        if (idImageList == null || idImageList.size() == 0) {
            return;
        }
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        ArrayList<Image> tempList = new ArrayList<>();
        for (Note note : noteList) {
            ArrayList<Image> imagePathList = idImageList.get(note.getId() + "");
            if (imagePathList == null) {
                continue;
            }
            if (imagePathList.size() == 0) {
                continue;
            }
            StorageReference mRef = storageRef.child(user).child(note.getKeySync());
            for (Image image : imagePathList) {
                int stage = image.getSync();
                Uri uri = Uri.parse(image.getPath());
                if (stage == 0) {
                    Uri file = Uri.fromFile(new File(uri.getPath()));
                    mRef.child(uri.getLastPathSegment()).putFile(file);
                    image.setSync(1);
                    tempList.add(image);
                }
                if (stage == -1) {
                    mRef.child(uri.getLastPathSegment()).delete();
                    tempList.add(image);
                }
            }
        }
        updateImageToSync(tempList);
    }


    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void getUser() {
        if (!TextUtils.isEmpty(user)) {
            return;
        }
        try (SQLiteDatabase db = helper.getReadableDatabase()) {
            Cursor c = null;

            c = db.query(
                    NoteContract.AccountEntry.DATABASE_TABLE,
                    new String[]{NoteContract.AccountEntry.COL_ID},
                    null, null, null, null, null
            );

            if (c != null && c.moveToNext()) {
                user = c.getString(c.getColumnIndex(NoteContract.AccountEntry.COL_ID));
            }

            if (c != null) {
                c.close();
            }
        } catch (IllegalStateException e) {
            Log.d("service-error", e.toString());
        }
    }

    private List<Note> getListNote() {
        List<Note> noteList = null;
        try (SQLiteDatabase db = helper.getReadableDatabase()) {
            String[] selection = new String[]{
                    NoteContract.NoteEntry._ID,
                    NoteContract.NoteEntry.COL_TITLE,
                    NoteContract.NoteEntry.COL_CONTENT,
                    NoteContract.NoteEntry.COL_DELETE,
                    NoteContract.NoteEntry.COL_COLOR,
                    NoteContract.NoteEntry.COL_PASSWORD,
                    NoteContract.NoteEntry.COL_PASSWORD_SALT,
                    NoteContract.NoteEntry.COL_DATE_CREATED,
                    NoteContract.NoteEntry.COL_LAST_ON,
                    NoteContract.NoteEntry.COL_TYPE_OF_TEXT,
                    NoteContract.NoteEntry.COL_KEY_SYNC
            };

            String orderBy = NoteContract.NoteEntry._ID + " ASC";

            Cursor c = null;
            try {
                c = db.query(
                        NoteContract.NoteEntry.DATABASE_TABLE,
                        selection,
                        null,
                        null,
                        null,
                        null,
                        orderBy);
                noteList = new ArrayList<>();
                if (c != null && c.moveToFirst()) {
                    int idIndex = c.getColumnIndex(NoteContract.NoteEntry._ID);
                    int keySyncIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_KEY_SYNC);
                    int titleIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_TITLE);
                    int contentIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_CONTENT);
                    int deleteIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_DELETE);
                    int colorIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_COLOR);
                    int passwordIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_PASSWORD);
                    int passwordSaltIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_PASSWORD_SALT);
                    int dateCreateIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_DATE_CREATED);
                    int lastOnIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_LAST_ON);
                    int typeOfTextIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_TYPE_OF_TEXT);
                    Note note;
                    do {
                        note = new Note();
                        note.setId(c.getLong(idIndex));
                        note.setKeySync(c.getString(keySyncIndex));
                        note.setTitle(c.getString(titleIndex));
                        note.setContent(c.getString(contentIndex));
                        note.setDelete(c.getInt(deleteIndex) == 1);
                        note.setIdColor(c.getInt(colorIndex));
                        note.setPassword(c.getString(passwordIndex));
                        note.setPassSalt(c.getString(passwordSaltIndex));
                        note.setDateCreated(Long.parseLong(c.getString(dateCreateIndex)));
                        note.setLastOn(Long.parseLong(c.getString(lastOnIndex)));
                        note.setIdTypeOfText(c.getInt(typeOfTextIndex));
                        noteList.add(note);
                    } while (c.moveToNext());
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        } catch (IllegalStateException e) {
            Log.d("service-error", e.toString());
        }
        return noteList;
    }


    private HashMap<String, ArrayList<Image>> getImageList(List<Note> noteList) {
        ArrayList<String> imageList = new ArrayList<>();
        HashMap<String, ArrayList<Image>> idImageList = new HashMap<>();
        SQLiteDatabase db = helper.getReadableDatabase();

        String[] selection = NoteContract.ImageEntry.getColumnNames();
        Cursor c = null;
        try {
            c = db.query(
                    NoteContract.ImageEntry.DATABASE_TABLE,
                    selection,
                    null, null, null, null, null);
            if (c == null) {
                return idImageList;
            }
            if (c != null && c.moveToFirst()) {
                do {
                    String path = c.getString(c.getColumnIndex(NoteContract.ImageEntry.COL_NAME_PATH));
                    long id = c.getLong(c.getColumnIndex(NoteContract.ImageEntry.COL_NOTE_ID));
                    int stage = c.getInt(c.getColumnIndex(NoteContract.ImageEntry.COL_SYNC));
                    ArrayList<Image> temp = idImageList.get(id + "");
                    if (temp == null) {
                        temp = new ArrayList<>();
                    }
                    temp.add(new Image(path, stage));
                    idImageList.put(id + "", temp);
                } while (c.moveToNext());
            }


        } catch (IllegalStateException e) {
            Log.d("service-error", e.toString());
        }
        return idImageList;
    }

    private void updateNoteWithKeySync(List<Note> noteList) {
        try (SQLiteDatabase db = helper.getWritableDatabase()) {
            ContentValues cv = new ContentValues();
            for (Note note : noteList) {
                cv.put(NoteContract.NoteEntry.COL_KEY_SYNC, note.getKeySync());
                db.update(
                        NoteContract.NoteEntry.DATABASE_TABLE,
                        cv,
                        NoteContract.NoteEntry._ID + "=" + note.getId(),
                        null);
                cv.clear();
            }

        } catch (IllegalStateException e) {
            Log.d("service-error", e.toString());
        }
    }

    private void updateImageToSync(List<Image> imageList) {
        if (imageList == null || imageList.size() == 0) {
            return;
        }
        try (SQLiteDatabase db = helper.getWritableDatabase()) {
            String selection = NoteContract.ImageEntry.COL_NAME_PATH + "=?";
            ContentValues cv = new ContentValues();
            for (Image image : imageList) {
                try {
                    if (image.getSync() == 1) {
                        cv.clear();
                        cv.put(NoteContract.ImageEntry.COL_SYNC, 1);
                        db.update(
                                NoteContract.ImageEntry.DATABASE_TABLE,
                                cv,
                                selection,
                                new String[]{image.getPath()});
                    }
                    if (image.getSync() == -1) {
                        db.delete(
                                NoteContract.ImageEntry.DATABASE_TABLE,
                                selection,
                                new String[]{image.getPath()});
                    }
                } catch (IllegalStateException e) {
                    Log.d("image-fix", e.getMessage());
                }
            }
        }
    }

}
