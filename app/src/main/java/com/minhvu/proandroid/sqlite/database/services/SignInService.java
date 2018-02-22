package com.minhvu.proandroid.sqlite.database.services;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.Utils.NoteUtils;
import com.minhvu.proandroid.sqlite.database.models.DAO.ImageDAO;
import com.minhvu.proandroid.sqlite.database.models.DAO.LastSyncDAO;
import com.minhvu.proandroid.sqlite.database.models.DAO.NoteDAO;
import com.minhvu.proandroid.sqlite.database.models.entity.Image;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by vomin on 1/9/2018.
 */

public class SignInService extends ALongRunningNonStickyBroadcastService {

    private String mUser;
    private ValueEventListener mValueEventListener;
    private StorageReference mStorage;
    private DatabaseReference mDb;

    private NoteDAO mNoteDAO;
    private ImageDAO mImageDAO;

    public SignInService() {
        super(SignInService.class.getSimpleName());
    }

    @Override
    public void onDestroy() {
        /*if(mValueEventListener != null){
            mDb.child(getString(R.string.users_list_firebase)).removeEventListener(mValueEventListener);
            Intent intent = new Intent(getString(R.string.broadcast_sign_out));
            intent.putExtra(getString(R.string.sign_in_flag), "yes");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }*/
        super.onDestroy();
    }

    @Override
    public void handIntentBroadcast(Intent intentBroadcast) {
        mUser = intentBroadcast.getStringExtra(getString(R.string.user_token));
        if (TextUtils.isEmpty(mUser)) {
            return;
        }
        mNoteDAO = new NoteDAO(this);
        mImageDAO = new ImageDAO(this);
        mainHandle();
        GetLastSyncFromServer();
    }



    private void mainHandle() {
        mStorage = FirebaseStorage.getInstance().getReference();
        mDb = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mRef = mDb.child(getString(R.string.users_list_firebase));
        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(mUser)) {
                    for (DataSnapshot ds : dataSnapshot.child(mUser).getChildren()) {
                        handleNote(ds);
                    }
                }
                completeTask();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mRef.addValueEventListener(mValueEventListener);
    }


    private void completeTask(){
        mNoteDAO = null;
        mDb.child(getString(R.string.users_list_firebase)).removeEventListener(mValueEventListener);
        //active notification to MainFragment
        Intent intent = new Intent(getString(R.string.broadcast_sign_out));
        intent.putExtra(getString(R.string.sign_in_flag), "yes");
        LocalBroadcastManager.getInstance(SignInService.this).sendBroadcast(intent);

        //close Sign In Window
        Intent closeWindowIntent = new Intent(getString(R.string.close_sign_in_when_complete_task));
        LocalBroadcastManager.getInstance(this).sendBroadcast(closeWindowIntent);
    }

    private void GetLastSyncFromServer(){
        final DatabaseReference mRef = mDb.child(getString(R.string.users_list_firebase));
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(mUser)){
                    if (dataSnapshot.child(mUser).hasChild(getString(R.string.last_update_ones_account))){
                        Long time = dataSnapshot.child(mUser)
                                .child(getString(R.string.last_update_ones_account))
                                .getValue(Long.class);
                        LastSyncDAO dao = new LastSyncDAO(SignInService.this);
                        dao.InsertLastSyncTime(time);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //[start handle a note]
    private void handleNote(DataSnapshot mDataSnapshot) {
        if (!mDataSnapshot.hasChild(getString(R.string.note_directory))) {
            return;
        }
        String mKey = mDataSnapshot.getKey();
        DataSnapshot dataSnapshot = mDataSnapshot.child(getString(R.string.note_directory));
        Note mNote = dataSnapshot.getValue(Note.class);

        if (!mNoteDAO.isSyncChecksExist(mKey)) {
            mNote.setKeySync(mKey);
            long noteID = mNoteDAO.insertNote(mNote);
            handleDocument(mDataSnapshot, noteID);
        }

    }
    //[end note]


    //[start handle a document]
    private void handleDocument(DataSnapshot mDataSnapshot, long noteID) {
        if (!mDataSnapshot.hasChild(getString(R.string.document_directory))) {
            return;
        }
        List<String> imageList = new ArrayList<>();
        for (DataSnapshot ds : mDataSnapshot.child(getString(R.string.document_directory)).getChildren()) {
            imageList.add(ds.getValue(String.class));
        }
        /*new Thread() {
            @Override
            public void run() {
                super.run();
                getImageFromServer(imageList, noteID);
            }
        }.start();*/
        getImageFromServer(imageList, noteID);

    }

    private void getImageFromServer(List<String> imageList, final long noteID) {
        if (imageList == null || imageList.size() == 0)
            return;
        StorageReference ref = mStorage.child(mUser);
        for (String path : imageList) {
            ref.child(path).getDownloadUrl().addOnSuccessListener(uri -> {
                saveImage(uri, path, noteID);
            });



                    /*.getFile(file)
                    .addOnSuccessListener(taskSnapshot -> {
                        insertImageIntoDatabase("file:" + file.getAbsolutePath(), noteID);
                    })
                    .addOnFailureListener(e -> {
                    });*/


        }
    }

    private Bitmap imageStream(Uri uri) {
        HttpURLConnection connection = null;
        final String methodGET = "GET";
        try {
            URL url = new URL(uri.toString());

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(methodGET);
            connection.setDoInput(true);
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }
            connection.connect();
            InputStream is = new BufferedInputStream(connection.getInputStream());
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            String error = e.getMessage();
            return null;
        }
    }

    private void saveImage(Uri uri, final String fileName, final long noteID) {
        new AsyncTask<Uri, Void, String>() {

            @Override
            protected String doInBackground(Uri... uris) {
                if (uris.length == 0)
                    return null;
                Bitmap bitmap = imageStream(uris[0]);
                if (bitmap == null)
                    return null;
                File file = getOutputMediaFile(fileName);
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    return file.getPath();
                } catch (FileNotFoundException e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String path) {
                super.onPostExecute(path);
                if (TextUtils.isEmpty(path)) return;
                insertImageIntoDatabase("file:" + path, noteID);
            }
        }.execute(uri);
    }

    private void insertImageIntoDatabase(String path, long noteID) {
        mImageDAO.insertItem(new Image(path, 1, noteID));
    }


    private File getOutputMediaFile(String fileName) {
        File root = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), getString(R.string.my_storage));
        if(!root.exists()){
            root.mkdirs();
        }
        return new File(root.getPath() + File.separator + fileName);
    }
    //[end document]
}
