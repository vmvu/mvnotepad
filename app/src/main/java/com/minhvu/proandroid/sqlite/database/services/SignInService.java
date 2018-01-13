package com.minhvu.proandroid.sqlite.database.services;

import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vomin on 1/9/2018.
 */

public class SignInService extends ALongRunningNonStickyBroadcastService {

    private String mUser;
    private ValueEventListener mValueEventListener;
    private StorageReference mStorage;
    private DatabaseReference mDb;

    public SignInService() {
        super(SignInService.class.getSimpleName());
    }

    @Override
    public void onDestroy() {
        mDb.child(getString(R.string.users_list_firebase)).removeEventListener(mValueEventListener);
        super.onDestroy();
    }

    @Override
    public void handIntentBroadcast(Intent intentBroadcast) {
        mUser = intentBroadcast.getStringExtra(getString(R.string.user_token));
        setup();
        mainHandle();

    }

    private void setup() {

        mDb = FirebaseDatabase.getInstance().getReference();

        mStorage = FirebaseStorage.getInstance().getReference();

        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (TextUtils.isEmpty(mUser)) {
                    return;
                }
                if (TextUtils.isEmpty(mUser)) {
                    return;
                }

                if (dataSnapshot.hasChild(mUser)) {
                    for (DataSnapshot ds : dataSnapshot.child(mUser).getChildren()) {
                        handleNote(ds);
                        handleDocument(ds);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }


    private void mainHandle() {
        DatabaseReference dbRef = mDb.child(getString(R.string.users_list_firebase));
        if (dbRef == null) {
            return;
        }
        dbRef.addValueEventListener(mValueEventListener);
    }

    //[start handle a note]
    private void handleNote(DataSnapshot mDataSnapshot) {
        if (!mDataSnapshot.hasChild(getString(R.string.note_directory))) {
            return;
        }
        Note mNote = mDataSnapshot.child(getString(R.string.note_directory)).getValue(Note.class);

    }
    //[end note]


    //[start handle a document]
    private void handleDocument(DataSnapshot mDataSnapshot) {
        if (!mDataSnapshot.hasChild(getString(R.string.document_directory))) {
            return;
        }
        List<String> imageList = new ArrayList<>();
        for (DataSnapshot ds : mDataSnapshot.child(getString(R.string.document_directory)).getChildren()) {
            imageList.add(ds.getValue(String.class));
        }
        new Thread() {
            @Override
            public void run() {
                super.run();
                getImage(imageList);
            }
        }.start();

    }

    private void getImage(List<String> imageList) {
        if (imageList == null || imageList.size() == 0)
            return;
        StorageReference ref = mStorage.child(mUser);
        for (String path : imageList) {
            File file = getOutputMediaFile(path);
            if (file == null)
                continue;
            ref.child(path)
                    .getFile(file)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        }

                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });


        }
    }


    private File getOutputMediaFile(String fileName) {
        File file = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), getString(R.string.my_storage));
        boolean success = false;
        if (!file.exists()) {
            success = file.mkdirs();
        }
        if (!success) {
            return null;
        }
        return new File(file.getPath() + File.separator + fileName);
    }
    //[end document]
}
