package com.minhvu.proandroid.sqlite.database.models.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by vomin on 2/22/2018.
 */

public class LastSyncDAO extends BaseDAO {
    private final String DATABASE_NAME = "last_sync";
    private final String COL_LONGTIME = "long_time";
    public LastSyncDAO(Context context) {
        super(context);
    }

    public boolean InsertLastSyncTime(long time){
        SQLiteDatabase db = getWriteDB();
        ContentValues cv = new ContentValues();
        cv.put(COL_LONGTIME, time);
        long success = db.insert(DATABASE_NAME, null, cv);
        return success > 0;
    }

    public long LastSyncTime(){
        SQLiteDatabase db = getReadDB();
        Cursor c =db.query(DATABASE_NAME, null, null, null,null, null, null);
        if(c == null || !c.moveToNext()){
            return 0;
        }
        int longTimeIndex=  c.getColumnIndex(COL_LONGTIME);
        long time = c.getLong(longTimeIndex);
        c.close();
        return time;
    }
}
