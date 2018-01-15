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
        import com.minhvu.proandroid.sqlite.database.models.DAO.ImageDAO;
        import com.minhvu.proandroid.sqlite.database.models.DAO.NoteDAO;
        import com.minhvu.proandroid.sqlite.database.models.DAO.NoteDeletedDAO;
        import com.minhvu.proandroid.sqlite.database.models.data.ImageContract;
        import com.minhvu.proandroid.sqlite.database.models.data.NoteContract;
        import com.minhvu.proandroid.sqlite.database.models.data.DBSchema;
        import com.minhvu.proandroid.sqlite.database.models.data.NoteDeletedContract;
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
        List<NoteDeleted> noteList = NoteSync();
        if (noteList == null) {
            noteList = new ArrayList<>();
        }
        List<NoteDeleted> notes = removeNote();
        if (notes != null) {
            noteList.addAll(notes);
        }
        DocumentSync(noteList);
    }

    @Nullable
    private List<NoteDeleted> NoteSync() {
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
        List<NoteDeleted> noteDeletedList = new ArrayList<>();
        for (Note note : noteList) {
            if (TextUtils.isEmpty(note.getKeySync())) {
                DatabaseReference mRef = db.push();
                note.setKeySync(mRef.getKey());
                mRef.child(getString(R.string.note_directory)).setValue(note);
            } else {
                db.child(note.getKeySync())
                        .child(getString(R.string.note_directory)).setValue(note);
            }
            noteDeletedList.add(new NoteDeleted(note.getId(), note.getKeySync()));
        }
        Thread threadUpdateKeySync = new Thread() {
            @Override
            public void run() {
                super.run();
                updateNoteWithKeySync(noteList);
            }
        };
        threadUpdateKeySync.start();
        return noteDeletedList;
    }

    private List<NoteDeleted> removeNote() {

        NoteDeletedDAO noteDeletedDAO = new NoteDeletedDAO(this);
        List<NoteDeleted> noteDeletedList = noteDeletedDAO.loadData();
        if(noteDeletedList == null || noteDeletedList.size() == 0)
            return null;
        noteDeletedDAO.deleteAllItems();
        final DatabaseReference dbFirebase = FirebaseDatabase.getInstance()
                .getReference(getString(R.string.users_list_firebase)).child(user);
        for (NoteDeleted keySync : noteDeletedList) {
            if (!TextUtils.isEmpty(keySync.getKeySync()))
                dbFirebase.child(keySync.getKeySync()).setValue(null);
        }
        return noteDeletedList;
    }

    private void DocumentSync(List<NoteDeleted> noteList) {
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
        for (NoteDeleted note : noteList) {
            ArrayList<Image> imagePathList = idImageList.get(note.getNoteID() + "");
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

    private List<Note> getListNote() {
        NoteDAO dao = new NoteDAO(this);
        String orderBy = NoteContract.NoteEntry._ID + " ASC";

        return dao.loadData(orderBy);
    }

    private HashMap<String, ArrayList<Image>> getImageList() {
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

    private void updateNoteWithKeySync(List<Note> noteList) {
        NoteDAO dao = new NoteDAO(this);
        dao.updateSyncList(noteList);
    }

    private void updateImageToSync(List<Image> imageList) {
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


