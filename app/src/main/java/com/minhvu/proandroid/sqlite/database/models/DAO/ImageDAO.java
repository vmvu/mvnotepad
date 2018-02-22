package com.minhvu.proandroid.sqlite.database.models.DAO;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.minhvu.proandroid.sqlite.database.models.data.ImageContract;
import com.minhvu.proandroid.sqlite.database.models.data.NoteContract;
import com.minhvu.proandroid.sqlite.database.models.data.NoteDeletedContract;
import com.minhvu.proandroid.sqlite.database.models.entity.Image;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vomin on 1/13/2018.
 */

public class ImageDAO extends BaseDAO {

    public ImageDAO(Context context) {
        super(context);
    }

    public List<Image> loadData(){
        SQLiteDatabase db = getReadDB();
        Cursor c = db.query(ImageContract.ImageEntry.DATABASE_TABLE,
                null, null, null, null, null, null);
        if(c != null && c.moveToFirst()){
            List<Image> imageList = new ArrayList<>();
            do{
                String path = c.getString(c.getColumnIndex(ImageContract.ImageEntry.COL_NAME_PATH));
                long noteID = c.getLong(c.getColumnIndex(ImageContract.ImageEntry.COL_NOTE_ID));
                int state = c.getInt(c.getColumnIndex(ImageContract.ImageEntry.COL_SYNC));
                imageList.add(new Image(path, state, noteID));
            }while (c.moveToNext());
            c.close();
            return imageList;
        }
        return  null;
    }
    public List<String> loadImagePathListOfNote(long noteID) {
        SQLiteDatabase db = getReadDB();

        String selection = ImageContract.ImageEntry.COL_NOTE_ID + "=?";
        Cursor c = db.query(ImageContract.ImageEntry.DATABASE_TABLE,
                null,
                selection,
                new String[]{String.valueOf(noteID)},
                null,
                null,
                null);
        if (c != null && c.moveToFirst()) {
            List<String> imageList = new ArrayList<>();
            int pathIndex = c.getColumnIndex(ImageContract.ImageEntry.COL_NAME_PATH);
            int deletedIndex = c.getColumnIndex(ImageContract.ImageEntry.COL_SYNC);
            do {
                String path = c.getString(pathIndex);
                int deleted = c.getInt(deletedIndex);
                if (deleted != -1) {
                    imageList.add(path);
                }
            } while (c.moveToNext());
            c.close();
            return imageList;
        }
        return null;
    }

    public boolean insertItem(Image image) {
        SQLiteDatabase db = getWriteDB();
        ContentValues cv = new ContentValues();
        cv.put(ImageContract.ImageEntry.COL_NAME_PATH, image.getPath());
        cv.put(ImageContract.ImageEntry.COL_NOTE_ID, image.getNoteID());
        cv.put(ImageContract.ImageEntry.COL_SYNC, image.getSync());
        try {
            long insertOrThrow = db.insertOrThrow(ImageContract.ImageEntry.DATABASE_TABLE, null, cv);
            return insertOrThrow > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean updateByPath(Image image) {
        SQLiteDatabase db = getWriteDB();

        String selection = ImageContract.ImageEntry.COL_NAME_PATH + "=?";
        ContentValues cv = new ContentValues();
        if (!TextUtils.isEmpty(image.getPath()))
            cv.put(ImageContract.ImageEntry.COL_NAME_PATH, image.getPath());
        if (image.getNoteID() != -1)
            cv.put(ImageContract.ImageEntry.COL_NOTE_ID, image.getNoteID());
        cv.put(ImageContract.ImageEntry.COL_SYNC, image.getSync());
        int success = db.update(
                ImageContract.ImageEntry.DATABASE_TABLE,
                cv,
                selection,
                new String[]{image.getPath()});
        return success > 0;
    }
    public void updatesByPath(List<Image> imageList) {
        SQLiteDatabase db = getWriteDB();

        String selection = ImageContract.ImageEntry.COL_NAME_PATH + "=?";
        ContentValues cv = new ContentValues();
        for(Image image: imageList){
            if (!TextUtils.isEmpty(image.getPath()))
                cv.put(ImageContract.ImageEntry.COL_NAME_PATH, image.getPath());
            if (image.getNoteID() != -1)
                cv.put(ImageContract.ImageEntry.COL_NOTE_ID, image.getNoteID());
            cv.put(ImageContract.ImageEntry.COL_SYNC, image.getSync());
            int success = db.update(
                    ImageContract.ImageEntry.DATABASE_TABLE,
                    cv,
                    selection,
                    new String[]{image.getPath()});
        }
    }

    public boolean updateByNoteID(Image image) {
        SQLiteDatabase db = getWriteDB();

        String selection = ImageContract.ImageEntry.COL_NOTE_ID + "=?";
        ContentValues cv = new ContentValues();
        if (!TextUtils.isEmpty(image.getPath()))
            cv.put(ImageContract.ImageEntry.COL_NAME_PATH, image.getPath());
        if (image.getNoteID() != -1)
            cv.put(ImageContract.ImageEntry.COL_NOTE_ID, image.getNoteID());
        cv.put(ImageContract.ImageEntry.COL_SYNC, image.getSync());
        int success = db.update(
                ImageContract.ImageEntry.DATABASE_TABLE,
                cv,
                selection,
                new String[]{String.valueOf(image.getNoteID())});
        return success > 0;
    }

    public void deleteAllItems(){
        SQLiteDatabase db = getWriteDB();
        db.execSQL("delete from " + ImageContract.ImageEntry.DATABASE_TABLE);
    }


    public void deleteAllItemsBySyncState(int SYNC_STATE){
        SQLiteDatabase db = getWriteDB();
        db.delete(
                ImageContract.ImageEntry.DATABASE_TABLE,
                ImageContract.ImageEntry.COL_SYNC + "=?",
                new String[]{SYNC_STATE + ""});
    }

    public long getCountOfNote(long noteID){
        SQLiteDatabase db = getReadDB();
        String sql = "SELECT * FROM " +
                ImageContract.ImageEntry.DATABASE_TABLE + " WHERE " + ImageContract.ImageEntry.COL_NOTE_ID + "=" + noteID;

        Cursor cursor = db.rawQuery(sql, null);
        if(cursor != null && cursor.moveToFirst()){
            return cursor.getCount();
        }
        return 0;
    }

    public boolean updateAllBySyncState(int STATE){
        ContentValues cv = new ContentValues();
        cv.put(ImageContract.ImageEntry.COL_SYNC, STATE);
        SQLiteDatabase db = getWriteDB();
        long success = db.update(ImageContract.ImageEntry.DATABASE_TABLE, cv, null, null);
        return success > 0;
    }
}
