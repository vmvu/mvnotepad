package com.minhvu.proandroid.sqlite.database.services;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.minhvu.proandroid.sqlite.database.models.data.NoteContract;
import com.minhvu.proandroid.sqlite.database.models.data.NoteDBHelper;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vomin on 11/28/2017.
 */

public class SyncService extends ALongRunningNonStickyBroadcastService {

    private FirebaseDatabase mFirebaseDB;

    public SyncService() {
        super("SyncService");
    }

    @Override
    public void handIntentBroadcast(Intent intentBroadcast) {
        Toast.makeText(getApplicationContext(), "vao broadcast", Toast.LENGTH_SHORT).show();
        getInstance();
        Sync();
    }


    public void getInstance(){
        if(mFirebaseDB == null){
            mFirebaseDB = FirebaseDatabase.getInstance();
        }
    }

    public void Sync(){
        String userId = "shshhs";
        if(TextUtils.isEmpty(userId)){
            return;
        }
        List<Note> noteList = getListNote();
        if(noteList == null || noteList.size() == 0){
            return;
        }
        DatabaseReference db  = mFirebaseDB.getReference("UserList").child(userId);
        /*for(int i = 0 ; i < noteList.size(); i++){
            Note note = noteList.get(i);
            DatabaseReference mRef = db.child(note.getId() + "");
            mRef.child(NoteContract.NoteEntry.COL_TITLE).setValue(note.getTitle());
            mRef.child(NoteContract.NoteEntry.COL_CONTENT).setValue(note.getContent());
        }*/
        db.child("2").setValue(null);
    }
    private String getUser(){
        String user = null;
        try(SQLiteDatabase db = NoteDBHelper.getInstance(this).getReadableDatabase()){
            Cursor c = null;
            try{
                c = db.query(
                        NoteContract.AccountEntry.DATABASE_TABLE,
                        new String[]{NoteContract.AccountEntry.COL_ID},
                        null, null, null, null, null
                );

                if(c != null && c.moveToNext()){
                    user = c.getString(c.getColumnIndex(NoteContract.AccountEntry.COL_ID));
                }
            }finally {
                if(c != null){
                    c.close();
                }
                if(db != null){
                    db.close();
                }
            }
        }
        return user;
    }

    private List<Note> getListNote(){
        List<Note> noteList = null;
        try(SQLiteDatabase db = NoteDBHelper.getInstance(this).getReadableDatabase()){
            String []selection = new String[]{
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
            };

            String orderBy = NoteContract.NoteEntry._ID + " ASC";

            Cursor c = null;
            try{
                c  = db.query(
                        NoteContract.NoteEntry.DATABASE_TABLE,
                        selection,
                        null,
                        null,
                        null,
                        null,
                        orderBy);
                noteList = new ArrayList<>();
                if(c != null && c.moveToFirst()){
                    int idIndex = c.getColumnIndex(NoteContract.NoteEntry._ID);
                    int titleIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_TITLE);
                    int contentIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_CONTENT);
                    int deleteIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_DELETE);
                    int colorIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_COLOR);
                    int passwordIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_PASSWORD);
                    int passwordSaltIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_PASSWORD_SALT);
                    int dateCreateIndex=  c.getColumnIndex(NoteContract.NoteEntry.COL_DATE_CREATED);
                    int lastOnIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_LAST_ON);
                    int typeOfTextIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_TYPE_OF_TEXT);
                    Note note;
                    do{
                        note = new Note();
                        note.setId(c.getLong(idIndex));
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
                    }while(c.moveToNext());
                }
            }finally {
                if(c != null){
                    c.close();
                }
                if(db != null){
                    db.close();
                }
            }
        }
        return noteList;
    }
}
