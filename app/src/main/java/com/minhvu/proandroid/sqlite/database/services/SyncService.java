package com.minhvu.proandroid.sqlite.database.services;

import android.content.ContentValues;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.models.DAO.ImageDAO;
import com.minhvu.proandroid.sqlite.database.models.DAO.LastSyncDAO;
import com.minhvu.proandroid.sqlite.database.models.DAO.NoteDAO;
import com.minhvu.proandroid.sqlite.database.models.DAO.NoteDeletedDAO;
import com.minhvu.proandroid.sqlite.database.models.data.ImageContract;
import com.minhvu.proandroid.sqlite.database.models.data.NoteContract;
import com.minhvu.proandroid.sqlite.database.models.entity.Image;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;
import com.minhvu.proandroid.sqlite.database.models.entity.NoteDeleted;

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
    private NoteDAO dao;


    public class LocalBinder extends Binder {
        public SyncService getService() {
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
        if (TextUtils.isEmpty(user)) {
            return;
        }
        int mode = intentBroadcast.getIntExtra(this.getString(R.string.user_sign_out_delete_data), 0);
        if (mode == 1) {
            deleteAllDataInDatabase();
        } else {
            dao = new NoteDAO(this);//can delete dao object
            syncFirebase();
        }

    }


    private void deleteAllDataInDatabase() {
        NoteDAO noteDAO = new NoteDAO(this);
        noteDAO.deleteALlItems();

        ImageDAO imageDAO = new ImageDAO(this);
        imageDAO.deleteAllItems();

        NoteDeletedDAO noteDeletedDAO = new NoteDeletedDAO(this);
        noteDeletedDAO.deleteAllItems();
    }


    private void syncFirebase() {
        Sync();
    }

    private void Sync() {
        List<NoteDeleted> noteList = SynchronousNote();
        if (noteList == null) {
            noteList = new ArrayList<>();
        }
        List<NoteDeleted> notes = DeleteOldNotes();
        if (notes != null) {
            noteList.addAll(notes);
        }
        SynchronousDocument(noteList);
        LastSync();
    }

    @Nullable
    private List<NoteDeleted> SynchronousNote() {
        if (TextUtils.isEmpty(user)) {
            return null;
        }
        final List<Note> noteList = GetListNote();
        if (noteList == null || noteList.size() == 0) {
            return null;
        }

        final DatabaseReference db = FirebaseDatabase.getInstance()
                .getReference(getString(R.string.users_list_firebase))
                .child(user);
        List<NoteDeleted> noteDeletedList = new ArrayList<>();
        for (Note note : noteList) {
            if (TextUtils.isEmpty(note.getKeySync())) {
                DatabaseReference mRef = db.push();
                note.setKeySync(mRef.getKey());
                mRef.child(getString(R.string.note_directory)).setValue(note);
                UpdateNoteWithSynchroKey(note.getId(), note.getKeySync());
            } else {
                db.child(note.getKeySync())
                        .child(getString(R.string.note_directory)).setValue(note);
            }
            noteDeletedList.add(new NoteDeleted(note.getId(), note.getKeySync()));
        }
        return noteDeletedList;
    }

    private List<NoteDeleted> DeleteOldNotes() {

        NoteDeletedDAO noteDeletedDAO = new NoteDeletedDAO(this);
        List<NoteDeleted> noteDeletedList = noteDeletedDAO.loadData();
        if (noteDeletedList == null || noteDeletedList.size() == 0)
            return null;
        noteDeletedDAO.deleteAllItems();
        final DatabaseReference mDBFireBase = FirebaseDatabase.getInstance()
                .getReference(getString(R.string.users_list_firebase)).child(user);
        for (NoteDeleted note : noteDeletedList) {
            if (!TextUtils.isEmpty(note.getKeySync()))
                mDBFireBase.child(note.getKeySync()).setValue(null);
        }
        return noteDeletedList;
    }

    private void SynchronousDocument(List<NoteDeleted> noteList) {
        //getUser();
        if (TextUtils.isEmpty(user)) {
            return;
        }
        if (noteList == null || noteList.size() == 0) {
            return;
        }
        HashMap<String, ArrayList<Image>> ImageListWithID = GetImageList();
        if (ImageListWithID == null || ImageListWithID.size() == 0) {
            return;
        }
        //  [begin: connect Firebase]
        final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        final DatabaseReference db = FirebaseDatabase.getInstance()
                .getReference(getString(R.string.users_list_firebase))
                .child(user);
        //  [end: connect Firebase]
        ArrayList<Image> tempList = new ArrayList<>();
        for (NoteDeleted note : noteList) {
            ArrayList<Image> pathOfImageList = ImageListWithID.get(note.getNoteID() + "");
            if (pathOfImageList == null ||pathOfImageList.size() == 0) {
                continue;
            }

            if (TextUtils.isEmpty(note.getKeySync())) {
                tempList.addAll(pathOfImageList);
                continue;
            }
            StorageReference mSRef = storageRef.child(user);
            DatabaseReference mDBRef = db.child(note.getKeySync()).child(getString(R.string.document_directory));

            for (Image image : pathOfImageList) {
                int stage = image.getSync();
                Uri uri = Uri.parse(image.getPath());
                if (stage == 0) {
                    Uri file = Uri.fromFile(new File(uri.getPath()));
                    mSRef.child(uri.getLastPathSegment())
                            .putFile(file)
                            .addOnSuccessListener(
                                    taskSnapshot ->
                                            mDBRef.child(getNameFileRemoveDot(file.getLastPathSegment()))
                                                    .setValue(file.getLastPathSegment())
                            );
                    image.setSync(1);
                    tempList.add(image);
                }
                if (stage == -1) {
                    mSRef.child(uri.getLastPathSegment()).delete();
                    mDBRef.child(getNameFileRemoveDot(uri.getLastPathSegment())).removeValue();
                    tempList.add(image);
                }
            }
        }
        AddSynchronousKeyForImage(tempList);
    }

    private void LastSync(){
        if(TextUtils.isEmpty(user)){
            return;
        }
        final DatabaseReference db = FirebaseDatabase.getInstance()
                .getReference(getString(R.string.users_list_firebase))
                .child(user);

        long currentTime = System.currentTimeMillis();
        db.child(getString(R.string.last_update_ones_account)).setValue(currentTime);
        LastSyncDAO dao = new LastSyncDAO(this);
        dao.InsertLastSyncTime(currentTime);
    }

    private String getNameFileRemoveDot(String path) {
        String[] list = path.split("\\.");
        int a = 4;
        return list[0];
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private List<Note> GetListNote() {
        NoteDAO dao = new NoteDAO(this);
        String orderBy = NoteContract.NoteEntry._ID + " ASC";

        return dao.loadData(orderBy);
    }

    private HashMap<String, ArrayList<Image>> GetImageList() {
        HashMap<String, ArrayList<Image>> idImageList = new HashMap<>();
        ImageDAO dao = new ImageDAO(this);
        List<Image> imageList = dao.loadData();
        if (imageList != null && imageList.size() != 0) {
            for (Image image : imageList) {
                ArrayList<Image> temp = idImageList.get(image.getNoteID() + "");
                if (temp == null) {
                    temp = new ArrayList<>();
                }
                temp.add(new Image(image.getPath(), image.getSync(), image.getNoteID()));
                idImageList.put(image.getNoteID() + "", temp);
            }
        }
        return idImageList;
    }

    private void UpdateNoteWithSynchroKey(long noteID, String SynchroKey) {
        Thread threadUpdateKeySync = new Thread() {
            @Override
            public void run() {
                super.run();
                dao.UpdateSynchronousList(noteID, SynchroKey);
            }
        };
        threadUpdateKeySync.start();
    }

    private void AddSynchronousKeyForImage(List<Image> imageList) {
        if (imageList == null || imageList.size() == 0) {
            return;
        }
        String selection = ImageContract.ImageEntry.COL_NAME_PATH + "=?";
        ContentValues cv = new ContentValues();
        for (Image image : imageList) {
            if (image.getSync() == 0) {
                imageList.remove(image);
            }
        }
        ImageDAO dao = new ImageDAO(this);
        dao.updatesByPath(imageList);
        dao.deleteAllItemsBySyncState(-1);
    }
}


