package com.minhvu.proandroid.sqlite.database.models.DAO;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.minhvu.proandroid.sqlite.database.models.data.DBSchema;

/**
 * Created by vomin on 1/12/2018.
 */

public abstract class BaseDAO {
    private DBSchema helper;
    protected Context context;

    public BaseDAO(Context context){
        this.context = context;
        helper = DBSchema.getInstance(context);
    }

    protected SQLiteDatabase getReadDB(){
        return helper.getReadableDatabase();
    }

    protected SQLiteDatabase getWriteDB(){
        return helper.getWritableDatabase();
    }
}
