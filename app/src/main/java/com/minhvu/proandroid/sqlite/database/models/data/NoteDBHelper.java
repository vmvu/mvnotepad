package com.minhvu.proandroid.sqlite.database.models.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vomin on 8/4/2017.
 */

public class NoteDBHelper extends SQLiteOpenHelper {
    private static final String LOGTAG = "NoteDBHelper";
    private static final String DATABASE_NAME = "v_notepad";
    private static final String PATH = "/data/data/com.minhvu.proandroid.sqlite.database/databases/";
    private static final String SQL_FILE_NAME = "v_notepad.db.sql";
    private static int DATABASE_VERSION = 1;
    private Context ctx ;

    private static volatile NoteDBHelper mHelper = null;

    public static synchronized NoteDBHelper getInstance(Context context){
        if(mHelper == null){
            mHelper = new NoteDBHelper(context);
        }
        return mHelper;
    }


    private NoteDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.ctx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try{
            loadSQLFromFile(SQL_FILE_NAME, db);
        }catch (Throwable t){
            throw new RuntimeException(t);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private boolean checkDatabase(){
        SQLiteDatabase check = null;
        try{
            String myPath = NoteDBHelper.PATH + NoteDBHelper.DATABASE_NAME;
            check = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        }catch (SQLiteException e){

        }
        if(check != null){
            Log.d("Pin", "check != null");
            check.close();
        }

        return check != null;
    }

    private void loadSQLFromFile(String assetsFile, SQLiteDatabase db){
        Log.d("Pin", "Khong vao day");
        List<String> listStatement = getDDLStatementsFrom(assetsFile);
        if(listStatement == null){
            Log.d(LOGTAG, "Problem creating database");
            return;
        }
        for(String sttm: listStatement){
           // Log.d("Pin","db: " +sttm);
            db.execSQL(sttm);
        }
    }

    private List<String> getDDLStatementsFrom(String assetsFile){
        ArrayList<String> list = new ArrayList<>();
        String s = getStringFromAssetFile(assetsFile);
        if(TextUtils.isEmpty(s)){
            return null;
        }
        for(String sttm: s.split(";")){
            if(isValid(sttm)){
                list.add(sttm);
            }
        }
        return list;
    }

    private boolean isValid(String s){
        if(TextUtils.isEmpty(s))
            return false;
        if(s.trim().equals("")){
            return false;
        }
        //this is commend
        if(s.startsWith("//"))
            return false;
        return true;
    }

    private String getStringFromAssetFile(String assetsFile){
        String s = "";
        try{
            InputStream inputStream = ctx.getAssets().open(assetsFile);
            s = convertStreamToString(inputStream);
        }catch (IOException e){

        }
        return s;
    }

    private String convertStreamToString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i = inputStream.read();
        while(i != -1){
            baos.write(i);
            i = inputStream.read();
        }
        return baos.toString();
    }





}
