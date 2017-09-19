package com.minhvu.proandroid.sqlite.database.main.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.minhvu.proandroid.sqlite.database.main.presenter.IGetSharePresenter;
import com.minhvu.proandroid.sqlite.database.models.data.NoteContract.NoteEntry;
import com.minhvu.proandroid.sqlite.database.models.data.NoteContract.ImageEntry;
import com.minhvu.proandroid.sqlite.database.models.data.NoteDBHelper;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;

/**
 * Created by vomin on 9/12/2017.
 */

public class GetShareModel implements IGetShareModel{
    private IGetSharePresenter presenter;
    private Note note = new Note();


    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        if(!isChangingConfiguration){
            presenter = null;
            note = null;
        }
    }

    @Override
    public void setPresenter(IGetSharePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Note loadNote(String noteId) {
        Object ac = presenter.getActivityContext();
       // Object app = presenter.getAppContext();
        NoteDBHelper helper = NoteDBHelper.getInstance(presenter.getActivityContext());
        SQLiteDatabase db = helper.getReadableDatabase();

        String[] projection = new String[]{NoteEntry.COL_TITLE, NoteEntry.COL_CONTENT,
                NoteEntry.COL_PASSWORD, NoteEntry.COL_PASSWORD_SALT};
        String selection = NoteEntry._ID  + "=?";
        String[] selectionArgs = new String[]{noteId};

        Cursor c = db.query(NoteEntry.DATABASE_TABLE, projection,selection, selectionArgs, null, null, null);
        if(c.moveToFirst()){
            note.setTitle(c.getString(c.getColumnIndex(NoteEntry.COL_TITLE)));
            note.setContent(c.getString(c.getColumnIndex(NoteEntry.COL_CONTENT)));
            note.setPassword(c.getString(c.getColumnIndex(NoteEntry.COL_PASSWORD)));
            note.setPassSalt(c.getString(c.getColumnIndex(NoteEntry.COL_PASSWORD_SALT)));
            c.close();
            db.close();
            return note;
        }
        db.close();
        return null;
    }

    @Override
    public int loadImage(String noteId) {
        NoteDBHelper helper = NoteDBHelper.getInstance(presenter.getActivityContext());
        SQLiteDatabase db = helper.getReadableDatabase();

        String query = "SELECT * FROM " + ImageEntry.DATABASE_TABLE + " WHERE " + ImageEntry.COL_NOTE_ID + "=" + noteId;
        Cursor c = db.rawQuery(query, null);
        if(c != null){
            return c.getCount();
        }
        return 0;
    }

    @Override
    public boolean insertNote(String title, String content) {
        NoteDBHelper helper = NoteDBHelper.getInstance(presenter.getActivityContext());
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(NoteEntry.COL_TITLE, title );
        cv.put(NoteEntry.COL_CONTENT, content );
        cv.put(NoteEntry.COL_COLOR, 0);
        cv.put(NoteEntry.COL_TYPE_OF_TEXT, 1);
        cv.put(NoteEntry.COL_DATE_CREATED, System.currentTimeMillis() + "");
        cv.put(NoteEntry.COL_LAST_ON, System.currentTimeMillis() + "");

        long success = db.insert(NoteEntry.DATABASE_TABLE, null, cv);
        db.close();
        if(success > 0){
            return true;
        }
        return false;
    }

    @Override
    public boolean updateNote(String noteId,String title, String content) {
        NoteDBHelper helper = NoteDBHelper.getInstance(presenter.getActivityContext());
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(NoteEntry.COL_TITLE, title );
        cv.put(NoteEntry.COL_CONTENT, content );
        cv.put(NoteEntry.COL_LAST_ON, System.currentTimeMillis() + "");

        String selection = NoteEntry._ID  + "=?";
        String[] selectionArgs = new String[]{noteId};

        int success = db.update(NoteEntry.DATABASE_TABLE, cv, selection, selectionArgs);
        db.close();
        if(success > 0){
            return true;
        }
        return false;
    }

    @Override
    public Note getNote() {
        return note;
    }

}
