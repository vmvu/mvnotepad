package com.minhvu.proandroid.sqlite.database.main.model;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.minhvu.proandroid.sqlite.database.main.model.view.IDeleteModel;
import com.minhvu.proandroid.sqlite.database.main.presenter.view.IDeletePresenter;
import com.minhvu.proandroid.sqlite.database.models.data.NoteContract;
import com.minhvu.proandroid.sqlite.database.models.data.NoteDBHelper;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vomin on 10/7/2017.
 */

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
        ContentResolver cr = context.getContentResolver();
        String selection = NoteContract.NoteEntry.COL_DELETE + "=?";
        String[] selectionArgs = new String[]{"1"};
        Cursor c = cr.query(NoteContract.NoteEntry.CONTENT_URI, NoteContract.NoteEntry.getColumnNames(), selection, selectionArgs, null, null);
        if (c != null && c.moveToFirst()) {
            listNote.clear();
            int idPos = c.getColumnIndex(NoteContract.NoteEntry._ID);
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
                listNote.add(note);
            } while (c.moveToNext());
        }
        c.close();
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
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv =new ContentValues();
        cv.put(NoteContract.NoteEntry.COL_DELETE, 0);
        int success = db.update(
                NoteContract.NoteEntry.DATABASE_TABLE,
                cv,
                NoteContract.NoteEntry._ID + "=?",
                new String[]{String.valueOf(noteID)}
                );
        db.close();
        if(success > 0){
            for(Note note: listNote){
                if(note.getId() == noteID){
                    listNote.remove(note);
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public boolean deleteNote(Context context, long noteID) {
        NoteDBHelper helper = NoteDBHelper.getInstance(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(NoteContract.NoteEntry.COL_DELETE, 1);

        int success = db.delete(
                NoteContract.NoteEntry.DATABASE_TABLE,
                NoteContract.NoteEntry._ID + "=?",
                new String[]{String.valueOf(noteID)});
        db.close();
        if (success > 0) {
            for (Note n : listNote) {
                if (n.getId() == noteID) {
                    listNote.remove(n);
                    break;
                }
            }
            return true;
        }
        return false;
    }

}


