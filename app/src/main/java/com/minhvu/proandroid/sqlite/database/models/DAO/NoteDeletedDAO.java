package com.minhvu.proandroid.sqlite.database.models.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.minhvu.proandroid.sqlite.database.models.data.NoteDeletedContract;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;
import com.minhvu.proandroid.sqlite.database.models.entity.NoteDeleted;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vomin on 1/14/2018.
 */

public class NoteDeletedDAO extends BaseDAO {
    public NoteDeletedDAO(Context context) {
        super(context);
    }


    public List<NoteDeleted> loadData(){
        SQLiteDatabase db = getReadDB();
        Cursor c = db.query(NoteDeletedContract.NoteDeletedEntry.DATABASE_TABLE,
                null, null, null, null, null, null);
        if(c != null && c.moveToFirst()){

            List<NoteDeleted> noteDeletedList = new ArrayList<>();
            int synchroKeyIndex = c.getColumnIndex(NoteDeletedContract.NoteDeletedEntry.NOTE_KEY_SYNC);
            int noteIDIndex = c.getColumnIndex(NoteDeletedContract.NoteDeletedEntry.NOTE_ID);
            do {
                String keySync = c.getString(synchroKeyIndex);
                long noteID = c.getLong(noteIDIndex);
                noteDeletedList.add(new NoteDeleted(noteID, keySync));
            } while (c.moveToNext());
            c.close();
            return noteDeletedList;
        }
        return null;
    }

    public boolean insert(NoteDeleted noteDeleted){
        ContentValues cv = new ContentValues();
        cv.put(NoteDeletedContract.NoteDeletedEntry.NOTE_ID, noteDeleted.getNoteID());
        cv.put(NoteDeletedContract.NoteDeletedEntry.NOTE_KEY_SYNC, noteDeleted.getKeySync());

        SQLiteDatabase db = getWriteDB();
        long success = db.insert(NoteDeletedContract.NoteDeletedEntry.DATABASE_TABLE, null, cv);
        return success > 0;
    }

    public void deleteAllItems(){
        SQLiteDatabase db = getWriteDB();
        db.execSQL("delete from " + NoteDeletedContract.NoteDeletedEntry.DATABASE_TABLE);
    }


}
