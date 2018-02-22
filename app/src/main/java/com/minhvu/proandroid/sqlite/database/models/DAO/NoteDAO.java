package com.minhvu.proandroid.sqlite.database.models.DAO;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.minhvu.proandroid.sqlite.database.models.data.NoteContract;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vomin on 1/12/2018.
 */

public class NoteDAO extends BaseDAO {

    public NoteDAO(Context context) {
        super(context);
    }

    public List<Note> loadDataByDELETE(int DELETE) {
        SQLiteDatabase db = getReadDB();
        List<Note> noteList = new ArrayList<>();
        String selection = NoteContract.NoteEntry.COL_DELETE + "=?";
        String[] selectionArgs = new String[]{String.valueOf(DELETE)};
        @SuppressLint("Recycle") Cursor c = db.query(
                NoteContract.NoteEntry.DATABASE_TABLE,
                NoteContract.NoteEntry.getColumnNames(),
                selection,
                selectionArgs,
                null,
                null,
                null);
        if (c != null && c.moveToFirst()) {
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
                noteList.add(note);
            } while (c.moveToNext());
        }
        return noteList;
    }

    public List<Note> loadData(String orderBy) {

        SQLiteDatabase db = getReadDB();
        List<Note> noteList = new ArrayList<>();
        String selection = NoteContract.NoteEntry.COL_DELETE + "=?";
        @SuppressLint("Recycle") Cursor c = db.query(
                NoteContract.NoteEntry.DATABASE_TABLE,
                null,
                null,
                null,
                null,
                null,
                orderBy);
        if (c != null && c.moveToFirst()) {
            int idIndex = c.getColumnIndex(NoteContract.NoteEntry._ID);
            int titleIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_TITLE);
            int contentIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_CONTENT);
            int colorIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_COLOR);
            int passwordIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_PASSWORD);
            int keyIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_PASSWORD_SALT);
            int dateCreatedIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_DATE_CREATED);
            int lastUpdateIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_LAST_ON);
            int keySyncIndex = c.getColumnIndex(NoteContract.NoteEntry.COL_KEY_SYNC);
            do {
                Note note = new Note();
                note.setId(c.getLong(idIndex));
                note.setTitle(c.getString(titleIndex));
                note.setContent(c.getString(contentIndex));
                note.setIdColor(c.getInt(colorIndex));
                note.setPassword(c.getString(passwordIndex));
                note.setPassSalt(c.getString(keyIndex));
                note.setDateCreated(Long.parseLong(c.getString(dateCreatedIndex)));
                note.setLastOn(Long.parseLong(c.getString(lastUpdateIndex)));
                note.setKeySync(c.getString(keySyncIndex));
                noteList.add(note);
            } while (c.moveToNext());
        }
        return noteList;
    }

    public boolean UpdateDeleteCol(long noteID, int DELETE) {
        SQLiteDatabase db = getWriteDB();
        ContentValues cv = new ContentValues();
        cv.put(NoteContract.NoteEntry.COL_DELETE, DELETE);

        int success = db.update(
                NoteContract.NoteEntry.DATABASE_TABLE,
                cv,
                NoteContract.NoteEntry._ID + "=?",
                new String[]{String.valueOf(noteID)});
        return success > 0;
    }

    public Note getItemAt(long noteID) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(NoteContract.NoteEntry.CONTENT_URI, noteID);
        String[] projection = NoteContract.NoteEntry.getColumnNames();
        String selection = NoteContract.NoteEntry.COL_DELETE + "=?";
        String[] selectionArgs = new String[]{"0"};
        Cursor c = null;
        try {
            c = cr.query(uri, projection, selection, selectionArgs, null, null);
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
                return note;
            } else {
                return null;
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public long getCount(int DELETE) {
        SQLiteDatabase db = getReadDB();
        String selection = NoteContract.NoteEntry.COL_DELETE + "=?";
        return DatabaseUtils.queryNumEntries(db, NoteContract.NoteEntry.DATABASE_TABLE, selection, new String[]{"" + DELETE});
    }

    public Note getLastNote() {

        SQLiteDatabase db = getReadDB();
        Cursor c = null;
        try {
            c = db.query(
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
                return note;
            } else {
                return null;
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public long insertNote(Note note) {

        ContentValues cv = new ContentValues();
        cv.put(NoteContract.NoteEntry.COL_TITLE, note.getTitle());
        cv.put(NoteContract.NoteEntry.COL_CONTENT, note.getContent());
        cv.put(NoteContract.NoteEntry.COL_COLOR, note.getIdColor());
        cv.put(NoteContract.NoteEntry.COL_DATE_CREATED, note.getDateCreated());
        cv.put(NoteContract.NoteEntry.COL_LAST_ON, note.getLastOn());
        cv.put(NoteContract.NoteEntry.COL_PASSWORD, note.getPassword());
        cv.put(NoteContract.NoteEntry.COL_TYPE_OF_TEXT, note.getIdTypeOfText());
        cv.put(NoteContract.NoteEntry.COL_PASSWORD_SALT, note.getPassSalt());
        cv.put(NoteContract.NoteEntry.COL_DELETE, note.isDelete() ? 1 : 0);
        cv.put(NoteContract.NoteEntry.COL_KEY_SYNC, note.getKeySync());

        SQLiteDatabase db = getWriteDB();
        try {
            return db.insertOrThrow(NoteContract.NoteEntry.DATABASE_TABLE, null, cv);
        } catch (SQLException e) {
            return -1;
        }
    }

    public boolean updateNote(Note note) {
        ContentValues cv = new ContentValues();
        cv.put(NoteContract.NoteEntry.COL_TITLE, note.getTitle());
        cv.put(NoteContract.NoteEntry.COL_CONTENT, note.getContent());
        if (note.getIdColor() != -1)
            cv.put(NoteContract.NoteEntry.COL_COLOR, note.getIdColor());
        cv.put(NoteContract.NoteEntry.COL_LAST_ON, note.getLastOn());
        if (!TextUtils.isEmpty(getLastNote().getPassword())) {
            cv.put(NoteContract.NoteEntry.COL_PASSWORD, note.getPassword());
            cv.put(NoteContract.NoteEntry.COL_PASSWORD_SALT, note.getPassSalt());
        }

        cv.put(NoteContract.NoteEntry.COL_DELETE, note.isDelete() ? 1 : 0);
        if (!TextUtils.isEmpty(note.getKeySync()))
            cv.put(NoteContract.NoteEntry.COL_KEY_SYNC, note.getKeySync());

        SQLiteDatabase db = getWriteDB();
        long success = db.update(
                NoteContract.NoteEntry.DATABASE_TABLE,
                cv,
                NoteContract.NoteEntry._ID + "=?",
                new String[]{String.valueOf(note.getId())});
        return success > 0;
    }

    public void UpdateSynchronousList(long noteID, String SynchroKey) {
        SQLiteDatabase db = getWriteDB();
        ContentValues cv = new ContentValues();
        cv.put(NoteContract.NoteEntry.COL_KEY_SYNC, SynchroKey);
        db.update(
                NoteContract.NoteEntry.DATABASE_TABLE,
                cv,
                NoteContract.NoteEntry._ID + "=" + noteID,
                null);

    }

    public boolean deleteNote(long noteID) {
        SQLiteDatabase db = getWriteDB();
        String whereClause = NoteContract.NoteEntry._ID + "=?";
        int success = db.delete(NoteContract.NoteEntry.DATABASE_TABLE, whereClause, new String[]{String.valueOf(noteID)});
        return success > 0;
    }

    public void deleteALlItems() {
        SQLiteDatabase db = getWriteDB();
        db.execSQL("delete from " + NoteContract.NoteEntry.DATABASE_TABLE);
    }

    public boolean isSyncChecksExist(String keySync) {
        SQLiteDatabase db = getReadDB();
        String selection = NoteContract.NoteEntry.COL_KEY_SYNC + "=?";
        Cursor c = db.query(NoteContract.NoteEntry.DATABASE_TABLE, null, selection,
                new String[]{keySync}, null, null, null);
        return c != null && c.getCount() > 0;
    }

}
