package com.minhvu.proandroid.sqlite.database.services;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.models.data.ImageContract;
import com.minhvu.proandroid.sqlite.database.models.data.NoteContract;
import com.minhvu.proandroid.sqlite.database.models.data.NoteDBHelper;
import com.minhvu.proandroid.sqlite.database.models.data.NoteReadyDeletedContract;
import com.minhvu.proandroid.sqlite.database.models.entity.Image;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by vomin on 11/28/2017.
 */

public class SyncService extends ALongRunningNonStickyBroadcastService {

    private final IBinder mBinder = new LocalBinder();


    private String user;


    public class LocalBinder extends Binder{
        public SyncService getService(){
            return SyncService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public SyncService() {
        super("SyncService");
    }

    @Override
    public void handIntentBroadcast(Intent intentBroadcast) {
        String intentAction = intentBroadcast.getAction();
        if (TextUtils.isEmpty(intentAction) || !intentAction.equals(getString(R.string.broadcast_sync))) {
            return;
        }
        user = intentBroadcast.getStringExtra(this.getString(R.string.user_token));
        if(TextUtils.isEmpty(user)){
            return;
        }
        int mode = intentBroadcast.getIntExtra(this.getString(R.string.user_sign_out_delete_data), 0);
        if(mode == 1){
            deleteAllDataInDatabase();
        }else{
            syncFirebase();
        }

    }


    private void deleteAllDataInDatabase(){
        NoteDBHelper helper = NoteDBHelper.getInstance(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        if(db == null)
            return;
        db.execSQL("delete from " + NoteContract.NoteEntry.DATABASE_TABLE);
        db.execSQL("delete from " + ImageContract.ImageEntry.DATABASE_TABLE);
        db.execSQL("delete from " + NoteReadyDeletedContract.NoteReadyDeletedEntry.DATABASE_TABLE);
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
        List<Note> noteList = NoteSync();
        if(noteList == null){
            noteList = new ArrayList<>();
        }
        List<Note> notes = removeNote();
        if(notes != null){
            noteList.addAll(notes);
        }
        DocumentSync(noteList);
    }

    @Nullable
    private List<Note> NoteSync() {
        if (TextUtils.isEmpty(user)) {
            return null;
        }
        final List<Note> noteList = getListNote();
        if (noteList == null || noteList.size() == 0) {
            return null;
        }

        final DatabaseReference db = FirebaseDatabase.getInstance()
                .getReference(getString(R.string.users_list_firebase))
                .child(user);

        for (Note note : noteList) {
            if (TextUtils.isEmpty(note.getKeySync())) {
                DatabaseReference mRef = db.push();
                note.setKeySync(mRef.getKey());
                mRef.child(getString(R.string.note_directory)).setValue(note);
            } else {
                db.child(note.getKeySync())
                        .child(getString(R.string.note_directory)).setValue(note);
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
        return noteList;
    }

    private List<Note> removeNote() {

        NoteDBHelper helper = NoteDBHelper.getInstance(this);
        SQLiteDatabase db = helper.getReadableDatabase();
        List<Note> list = new ArrayList<>();
        try {
            final String DATA_TABLE = NoteReadyDeletedContract.NoteReadyDeletedEntry.DATABASE_TABLE;
            Cursor c = db.query(DATA_TABLE, null, null, null, null, null, null);
            if (c != null && c.moveToFirst()) {
                int keySyncIndex = c.getColumnIndex(NoteReadyDeletedContract.NoteReadyDeletedEntry.NOTE_KEY_SYNC);
                int noteIDIndex = c.getColumnIndex(NoteReadyDeletedContract.NoteReadyDeletedEntry.NOTE_ID);
                do {
                    String keySync = c.getString(keySyncIndex);
                    long noteID = c.getLong(noteIDIndex);
                    list.add(new Note(keySync, noteID));

                } while (c.moveToNext());
            }
            if (c != null) {
                c.close();
            }
            db.delete(NoteReadyDeletedContract.NoteReadyDeletedEntry.DATABASE_TABLE, null, null);
        } catch (IllegalStateException e) {
            e.getMessage();
        }
        final DatabaseReference dbFirebase = FirebaseDatabase.getInstance()
                .getReference(getString(R.string.users_list_firebase)).child(user);
        for (Note keySync : list) {
            if(!TextUtils.isEmpty(keySync.getKeySync()))
                dbFirebase.child(keySync.getKeySync()).setValue(null);
        }
        return list;
    }

    private void DocumentSync(List<Note> noteList) {
        //getUser();
        if (TextUtils.isEmpty(user)) {
            return;
        }
        if (noteList == null || noteList.size() == 0) {
            return;
        }
        HashMap<String, ArrayList<Image>> idImageList = getImageList();
        if (idImageList == null || idImageList.size() == 0) {
            return;
        }
        //  [begin: connect Firebase]
        final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        final DatabaseReference db = FirebaseDatabase.getInstance()
                .getReference(getString(R.string.users_list_firebase))
                .child(user);
        //  [end: connect Firebase]
        ArrayList<Image> tempList = new ArrayList<>();
        for (Note note : noteList) {
            ArrayList<Image> imagePathList = idImageList.get(note.getId() + "");
            if (imagePathList == null) {
                continue;
            }
            if (imagePathList.size() == 0) {
                continue;
            }
            if (TextUtils.isEmpty(note.getKeySync())) {
                tempList.addAll(imagePathList);
                continue;
            }
            StorageReference mRef = storageRef.child(user);
            DatabaseReference dbRef = db.child(note.getKeySync()).child(getString(R.string.document_directory));

            for (Image image : imagePathList) {
                int stage = image.getSync();
                Uri uri = Uri.parse(image.getPath());
                if (stage == 0) {
                    Uri file = Uri.fromFile(new File(uri.getPath()));
                    mRef.child(uri.getLastPathSegment()).putFile(file);
                    dbRef.child(getNameFileRemoveDot(file.getLastPathSegment())).setValue(file.getLastPathSegment());
                    image.setSync(1);
                    tempList.add(image);
                }
                if (stage == -1) {
                    mRef.child(uri.getLastPathSegment()).delete();
                    dbRef.child(getNameFileRemoveDot(uri.getLastPathSegment())).removeValue();
                    tempList.add(image);
                }
            }
        }
        updateImageToSync(tempList);
    }

    private String getNameFileRemoveDot(String path){
        String[] list = path.split("\\.");
        int a = 4;
        return list[0];
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private List<Note> getListNote() {
        List<Note> noteList = null;
        NoteDBHelper helper = NoteDBHelper.getInstance(this);
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
            Log.d("service-error:[1]", e.toString());
        }
        return noteList;
    }

    private HashMap<String, ArrayList<Image>> getImageList() {
        HashMap<String, ArrayList<Image>> idImageList = new HashMap<>();
        NoteDBHelper helper = NoteDBHelper.getInstance(this);
        SQLiteDatabase db = helper.getReadableDatabase();

        String[] selection = ImageContract.ImageEntry.getColumnNames();
        Cursor c;
        try {
            c = db.query(
                    ImageContract.ImageEntry.DATABASE_TABLE,
                    selection,
                    null, null, null, null, null);
            if (c == null) {
                return idImageList;
            }
            if (c != null && c.moveToFirst()) {
                do {
                    String path = c.getString(c.getColumnIndex(ImageContract.ImageEntry.COL_NAME_PATH));
                    long id = c.getLong(c.getColumnIndex(ImageContract.ImageEntry.COL_NOTE_ID));
                    int stage = c.getInt(c.getColumnIndex(ImageContract.ImageEntry.COL_SYNC));
                    ArrayList<Image> temp = idImageList.get(id + "");
                    if (temp == null) {
                        temp = new ArrayList<>();
                    }
                    temp.add(new Image(path, stage));
                    idImageList.put(id + "", temp);
                } while (c.moveToNext());
            }
            if (c != null) {
                c.close();
            }

        } catch (IllegalStateException e) {
            Log.d("service-error[2]", e.toString());
        }
        return idImageList;
    }

    private void updateNoteWithKeySync(List<Note> noteList) {
        NoteDBHelper helper = NoteDBHelper.getInstance(this);
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
            Log.d("service-error[3]", e.toString());
        }

    }

    private void updateImageToSync(List<Image> imageList) {
        if (imageList == null || imageList.size() == 0) {
            return;
        }
        NoteDBHelper helper = NoteDBHelper.getInstance(this);
        try (SQLiteDatabase db = helper.getWritableDatabase()) {
            String selection = ImageContract.ImageEntry.COL_NAME_PATH + "=?";
            ContentValues cv = new ContentValues();
            for (Image image : imageList) {
                try {
                    if (image.getSync() == 1 || image.getSync() == -1) {
                        cv.clear();
                        cv.put(ImageContract.ImageEntry.COL_SYNC, image.getSync());
                        db.update(
                                ImageContract.ImageEntry.DATABASE_TABLE,
                                cv,
                                selection,
                                new String[]{image.getPath()});
                    }

                } catch (IllegalStateException e) {
                    Log.d("service-error[4]", e.getMessage());
                }
            }
            db.delete(
                    ImageContract.ImageEntry.DATABASE_TABLE,
                    ImageContract.ImageEntry.COL_SYNC + "=?",
                    new String[]{"-1"});
        }
    }

}
