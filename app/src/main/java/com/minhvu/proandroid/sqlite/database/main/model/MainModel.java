package com.minhvu.proandroid.sqlite.database.main.model;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.minhvu.proandroid.sqlite.database.main.model.view.IMainModel;
import com.minhvu.proandroid.sqlite.database.main.presenter.view.IMainPresenter;
import com.minhvu.proandroid.sqlite.database.models.data.NoteContract;
import com.minhvu.proandroid.sqlite.database.models.data.NoteDBHelper;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vomin on 10/7/2017.
 */

public class MainModel implements IMainModel {
    private ArrayList<Note> listNote = null;
    private IMainPresenter presenter;

    public MainModel(Context context) {
        listNote = new ArrayList<>();
        try{
            loadData(context);
        }finally {

        }

    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        if (!isChangingConfiguration) {
            listNote.clear();
            presenter = null;
        }
    }

    @Override
    public void setPresenter(IMainPresenter presenter) {
        this.presenter = presenter;
    }


    public void loadData(Context context) {
        ContentResolver cr = context.getContentResolver();
        String selection = NoteContract.NoteEntry.COL_DELETE + "=?";
        String[] selectionArgs = new String[]{"0"};
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
    public boolean deleteNote(Context ctx, long noteID) {
        ContentResolver cr = ctx.getContentResolver();
        Uri uri = ContentUris.withAppendedId(NoteContract.NoteEntry.CONTENT_URI, noteID);

        ContentValues cv = new ContentValues();
        cv.put(NoteContract.NoteEntry.COL_DELETE, 1);

        int success = cr.update(uri, cv, NoteContract.NoteEntry._ID + "=?", new String[]{String.valueOf(noteID)});
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

    @Override
    public void updateNote(Context context, int position) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(NoteContract.NoteEntry.CONTENT_URI, listNote.get(position).getId());
        String[] projection = NoteContract.NoteEntry.getColumnNames();
        String selection = NoteContract.NoteEntry.COL_DELETE + "=?";
        String[] selectionArgs = new String[]{"0"};

        Cursor c = cr.query(uri, projection, selection, selectionArgs, null, null);

        if (c != null && c.moveToFirst()) {
            int idPos = c.getColumnIndex(NoteContract.NoteEntry._ID);
            int titlePos = c.getColumnIndex(NoteContract.NoteEntry.COL_TITLE);
            int contentPos = c.getColumnIndex(NoteContract.NoteEntry.COL_CONTENT);
            int colorPos = c.getColumnIndex(NoteContract.NoteEntry.COL_COLOR);
            int passwordPos = c.getColumnIndex(NoteContract.NoteEntry.COL_PASSWORD);
            int keyPos = c.getColumnIndex(NoteContract.NoteEntry.COL_PASSWORD_SALT);
            int dateCreatedPos = c.getColumnIndex(NoteContract.NoteEntry.COL_DATE_CREATED);
            int lastUpdatePos = c.getColumnIndex(NoteContract.NoteEntry.COL_LAST_ON);

            Note note = new Note();
            note.setId(c.getLong(idPos));
            note.setTitle(c.getString(titlePos));
            note.setContent(c.getString(contentPos));
            note.setIdColor(c.getInt(colorPos));
            note.setPassword(c.getString(passwordPos));
            note.setPassSalt(c.getString(keyPos));
            note.setDateCreated(Long.parseLong(c.getString(dateCreatedPos)));
            note.setLastOn(Long.parseLong(c.getString(lastUpdatePos)));
            listNote.set(position, note);
        }else{
            listNote.remove(position);
        }
        c.close();
    }

    @Override
    public boolean isCheckCount(Context context) {
        NoteDBHelper helper = NoteDBHelper.getInstance(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        String selection = NoteContract.NoteEntry.COL_DELETE +  "=?";
        long count = DatabaseUtils.queryNumEntries(db, NoteContract.NoteEntry.DATABASE_TABLE, selection, new String[]{"0"});
        db.close();
        return count > this.getCount();
    }

    @Override
    public void getNewNote(Context context) {
        NoteDBHelper helper = NoteDBHelper.getInstance(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor c = db.query(
                NoteContract.NoteEntry.DATABASE_TABLE,
                NoteContract.NoteEntry.getColumnNames(),
                NoteContract.NoteEntry.COL_DELETE + "=?",
                new String[]{"0"},
                null,
                null,
                NoteContract.NoteEntry._ID + " DESC",
                "1");

        if (c != null && c.moveToFirst()) {
            int idPos = c.getColumnIndex(NoteContract.NoteEntry._ID);
            int titlePos = c.getColumnIndex(NoteContract.NoteEntry.COL_TITLE);
            int contentPos = c.getColumnIndex(NoteContract.NoteEntry.COL_CONTENT);
            int colorPos = c.getColumnIndex(NoteContract.NoteEntry.COL_COLOR);
            int passwordPos = c.getColumnIndex(NoteContract.NoteEntry.COL_PASSWORD);
            int keyPos = c.getColumnIndex(NoteContract.NoteEntry.COL_PASSWORD_SALT);
            int dateCreatedPos = c.getColumnIndex(NoteContract.NoteEntry.COL_DATE_CREATED);
            int lastUpdatePos = c.getColumnIndex(NoteContract.NoteEntry.COL_LAST_ON);

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
        }
        c.close();
        db.close();
    }
}
