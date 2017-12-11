package com.minhvu.proandroid.sqlite.database.main.model;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;
import android.util.Log;

import com.minhvu.proandroid.sqlite.database.main.model.view.IDeleteModel;
import com.minhvu.proandroid.sqlite.database.main.presenter.view.IDeletePresenter;
import com.minhvu.proandroid.sqlite.database.models.data.NoteContract;
import com.minhvu.proandroid.sqlite.database.models.data.NoteDBHelper;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;

import java.util.ArrayList;
import java.util.List;

public class DeleteModel implements IDeleteModel {
    private ArrayList<Note> listNote = null;
    private IDeletePresenter presenter;

    public DeleteModel() {
        listNote = new ArrayList<>();
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        if (!isChangingConfiguration) {
            listNote.clear();
            presenter = null;
        }
    }

    @Override
    public void setPresenter(IDeletePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void loadData(Context context) {
        NoteDBHelper helper = NoteDBHelper.getInstance(context);
        String selection = NoteContract.NoteEntry.COL_DELETE + "=?";
        String[] selectionArgs = new String[]{"1"};

        try (SQLiteDatabase db = helper.getReadableDatabase()) {
            Cursor c = null;
            if(db.isOpen()){
                c = db.query(
                        NoteContract.NoteEntry.DATABASE_TABLE,
                        NoteContract.NoteEntry.getColumnNames(),
                        selection,
                        selectionArgs,
                        null, null, null
                );
            }else{
                return;
            }
            if(c == null){
                return;
            }
            try{
                if (db.isOpen() && c!= null && !c.isClosed() && c.moveToFirst()) {
                    listNote.clear();
                    int idPos = c.getColumnIndex(NoteContract.NoteEntry._ID);
                    int keySyncIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_KEY_SYNC);
                    int titlePos = c.getColumnIndex(NoteContract.NoteEntry.COL_TITLE);
                    int contentPos = c.getColumnIndex(NoteContract.NoteEntry.COL_CONTENT);
                    int colorPos = c.getColumnIndex(NoteContract.NoteEntry.COL_COLOR);
                    int passwordPos = c.getColumnIndex(NoteContract.NoteEntry.COL_PASSWORD);
                    int keyPos = c.getColumnIndex(NoteContract.NoteEntry.COL_PASSWORD_SALT);
                    int dateCreatedPos = c.getColumnIndex(NoteContract.NoteEntry.COL_DATE_CREATED);
                    int lastUpdatePos = c.getColumnIndex(NoteContract.NoteEntry.COL_LAST_ON);
                    do {
                        Note note = new Note();
                        note.setId(c.getLong(idPos));
                        note.setTitle(c.getString(titlePos));
                        note.setContent(c.getString(contentPos));
                        note.setIdColor(c.getInt(colorPos));
                        note.setPassword(c.getString(passwordPos));
                        note.setPassSalt(c.getString(keyPos));
                        note.setDateCreated(Long.parseLong(c.getString(dateCreatedPos)));
                        note.setLastOn(Long.parseLong(c.getString(lastUpdatePos)));
                        note.setKeySync(c.getString(keySyncIndex));
                        int a = 5;
                        listNote.add(note);
                    } while (c.moveToNext());
                }
            }catch (IllegalStateException e){
                if( c != null)
                    c.close();
            }


        }
    }


    @Override
    public long getCount(Context context) {
        NoteDBHelper helper = NoteDBHelper.getInstance(context);
        String selection = NoteContract.NoteEntry.COL_DELETE + "=1";
        try (SQLiteDatabase db = helper.getReadableDatabase()) {
            if(db.isOpen()){
                try{
                    return DatabaseUtils.queryNumEntries(db, NoteContract.NoteEntry.DATABASE_TABLE, selection);
                }catch (IllegalStateException e){
                    return 0;
                }
            }
            return 0;
        }
    }

    @Override
    public int getCount() {
        return listNote == null ? 0 : listNote.size();
    }

    @Override
    public List<Note> getNoteList() {
        return listNote;
    }

    @Override
    public Note getNote(int index) {
        return listNote.get(index);
    }

    @Override
    public boolean restoreNote(Context context, long noteID) {
        NoteDBHelper helper = NoteDBHelper.getInstance(context);
        ContentValues cv = new ContentValues();
        cv.put(NoteContract.NoteEntry.COL_DELETE, 0);
        try (SQLiteDatabase db = helper.getWritableDatabase()) {
            int success = db.update(
                    NoteContract.NoteEntry.DATABASE_TABLE,
                    cv,
                    NoteContract.NoteEntry._ID + "=?",
                    new String[]{String.valueOf(noteID)}
            );
            if (success > 0) {
                for (Note note : listNote) {
                    if (note.getId() == noteID) {
                        listNote.remove(note);
                        return true;
                    }
                }
            }
        }

        return false;
    }


    @Override
    public boolean deleteNote(Context context, long noteID, String noteKeySync) {
        NoteDBHelper helper = NoteDBHelper.getInstance(context);
        ContentValues cv = new ContentValues();
        cv.put(NoteContract.NoteEntry.COL_DELETE, 1);
        try (SQLiteDatabase db = helper.getWritableDatabase()) {
            int success = db.delete(
                    NoteContract.NoteEntry.DATABASE_TABLE,
                    NoteContract.NoteEntry._ID + "=?",
                    new String[]{String.valueOf(noteID)});
            if (success > 0) {
                for (Note n : listNote) {
                    if (n.getId() == noteID) {
                        listNote.remove(n);
                        break;
                    }
                }
               /* if(!TextUtils.isEmpty(noteKeySync)){*/
                    noteReadyDeleted(context, noteKeySync, noteID);
               /* }*/
                return true;
            }
        }
        return false;
    }

    private void noteReadyDeleted(Context context, String keySync, long noteID){
        ContentValues cv = new ContentValues();
        cv.put(NoteContract.NoteReadyDeletedEntry.NOTE_KEY_SYNC, keySync);
        cv.put(NoteContract.NoteReadyDeletedEntry.NOTE_ID, noteID);
        NoteDBHelper helper = NoteDBHelper.getInstance(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.insert(NoteContract.NoteReadyDeletedEntry.DATABASE_TABLE, null, cv);
        db.close();
    }

}


