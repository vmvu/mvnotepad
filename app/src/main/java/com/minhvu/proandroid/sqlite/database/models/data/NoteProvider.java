package com.minhvu.proandroid.sqlite.database.models.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.minhvu.proandroid.sqlite.database.MyApplication;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;

import java.util.HashMap;

/**
 * Created by vomin on 8/1/2017.
 */

public class NoteProvider extends ContentProvider {
    //tnote table
    public static final int INCOMING_NOTE_COLLECTION_URI_INDICATOR = 1;
    public static final int INCOMING_SINGLE_NOTE_URI_INDICATOR = 100;
    //ttypeoftext table
    public static final int INCOMING_TYPEOFTEXT_COLLECTION_URI_INDICATOR = 3;
    public  static final int INCOMING_SINGLE_TYPEOFTEXT_URI_INDICATOR = 98;
    //taccount table
    public static final int INCOMING_ACCOUNT_COLLECTION_URI_INDICATOR = 4;
    //v_images
    public static final int INCOMING_IMAGES_COLLECTION_URI_INDICATOR = 5;
    public static final int INCOMING_SINGLE_IMAGES_URI_INDICATOR = 97;

    private NoteDBHelper mOpenHelper = null;

    private static UriMatcher uriMatcher = null;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(NoteContract.AUTHORITY, NoteContract.path_tnote,
                INCOMING_NOTE_COLLECTION_URI_INDICATOR);
        uriMatcher.addURI(NoteContract.AUTHORITY, NoteContract.path_tnote + "/#",
                INCOMING_SINGLE_NOTE_URI_INDICATOR);

        uriMatcher.addURI(NoteContract.AUTHORITY, NoteContract.path_ttypeoftext,
                INCOMING_TYPEOFTEXT_COLLECTION_URI_INDICATOR);
        uriMatcher.addURI(NoteContract.AUTHORITY, NoteContract.path_ttypeoftext + "/#",
                INCOMING_SINGLE_TYPEOFTEXT_URI_INDICATOR);

        uriMatcher.addURI(NoteContract.AUTHORITY, NoteContract.path_account,
                INCOMING_ACCOUNT_COLLECTION_URI_INDICATOR);

        uriMatcher.addURI(NoteContract.AUTHORITY, NoteContract.path_images,
                INCOMING_IMAGES_COLLECTION_URI_INDICATOR);
        uriMatcher.addURI(NoteContract.AUTHORITY, NoteContract.path_images + "/*",
                INCOMING_SINGLE_IMAGES_URI_INDICATOR);
    }
    private static HashMap<String, String> sNoteProjectMap;
    private static HashMap<String, String> sTypeOfTextProjectMap;
    private static HashMap<String, String> sImagesProjectMap;
    static{
        sNoteProjectMap = new HashMap<>();
        sNoteProjectMap.put(NoteContract.NoteEntry._ID, NoteContract.NoteEntry._ID);
        sNoteProjectMap.put(NoteContract.NoteEntry.COL_TITLE, NoteContract.NoteEntry.COL_TITLE);
        sNoteProjectMap.put(NoteContract.NoteEntry.COL_CONTENT, NoteContract.NoteEntry.COL_CONTENT);
        sNoteProjectMap.put(NoteContract.NoteEntry.COL_DATE_CREATED, NoteContract.NoteEntry.COL_DATE_CREATED);
        sNoteProjectMap.put(NoteContract.NoteEntry.COL_LAST_ON, NoteContract.NoteEntry.COL_LAST_ON);
        sNoteProjectMap.put(NoteContract.NoteEntry.COL_PASSWORD, NoteContract.NoteEntry.COL_PASSWORD);
        sNoteProjectMap.put(NoteContract.NoteEntry.COL_PASSWORD_SALT, NoteContract.NoteEntry.COL_PASSWORD_SALT);
        sNoteProjectMap.put(NoteContract.NoteEntry.COL_COLOR, NoteContract.NoteEntry.COL_COLOR);
        sNoteProjectMap.put(NoteContract.NoteEntry.COL_TYPE_OF_TEXT, NoteContract.NoteEntry.COL_TYPE_OF_TEXT);
        sNoteProjectMap.put(NoteContract.NoteEntry.COL_ACCOUNT, NoteContract.NoteEntry.COL_ACCOUNT);
        sNoteProjectMap.put(NoteContract.NoteEntry.COL_DELETE, NoteContract.NoteEntry.COL_DELETE);

        sTypeOfTextProjectMap = new HashMap<>();
        sTypeOfTextProjectMap.put(NoteContract.TypeOfTextEntry._ID, NoteContract.TypeOfTextEntry._ID);
        sTypeOfTextProjectMap.put(NoteContract.TypeOfTextEntry.COL_NAME, NoteContract.TypeOfTextEntry.COL_NAME);

        sImagesProjectMap = new HashMap<>();
        sImagesProjectMap.put(NoteContract.ImageEntry.COL_NAME_PATH,NoteContract.ImageEntry.COL_NAME_PATH );
        sImagesProjectMap.put(NoteContract.ImageEntry.COL_NOTE_ID,NoteContract.ImageEntry.COL_NOTE_ID);

    }
    @Override
    public boolean onCreate() {
        mOpenHelper = NoteDBHelper.getInstance(getContext());
        return true;
    }


    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder qb =new SQLiteQueryBuilder();
        String orderBy =null;
        switch (uriMatcher.match(uri)){
            case INCOMING_NOTE_COLLECTION_URI_INDICATOR:
                qb.setTables(NoteContract.NoteEntry.DATABASE_TABLE);
                qb.setProjectionMap(sNoteProjectMap);
                orderBy = NoteContract.NoteEntry.DEFAULT_SORT_ORDER;
                break;
            case INCOMING_SINGLE_NOTE_URI_INDICATOR:
                qb.setTables(NoteContract.NoteEntry.DATABASE_TABLE);
                qb.setProjectionMap(sNoteProjectMap);
                qb.appendWhere(NoteContract.NoteEntry._ID + "=" + uri.getPathSegments().get(1));
                break;
            case INCOMING_TYPEOFTEXT_COLLECTION_URI_INDICATOR:
                qb.setTables(NoteContract.TypeOfTextEntry.DATABASE_TABLE);
                qb.setProjectionMap(sTypeOfTextProjectMap);
                orderBy = NoteContract.TypeOfTextEntry.DEFAULT_SORT_ORDER;
                break;
            case  INCOMING_SINGLE_TYPEOFTEXT_URI_INDICATOR:
                qb.setTables(NoteContract.TypeOfTextEntry.DATABASE_TABLE);
                qb.setProjectionMap(sTypeOfTextProjectMap);
                qb.appendWhere(NoteContract.TypeOfTextEntry._ID + "=" + uri.getPathSegments().get(1));
                break;
            case INCOMING_IMAGES_COLLECTION_URI_INDICATOR:
                qb.setTables(NoteContract.ImageEntry.DATABASE_TABLE);
                qb.setProjectionMap(sImagesProjectMap);
                qb.appendWhere(selection);
                orderBy = null;
                break;
            case INCOMING_SINGLE_IMAGES_URI_INDICATOR:
                qb.setTables(NoteContract.ImageEntry.DATABASE_TABLE);
                qb.setProjectionMap(sImagesProjectMap);
                qb.appendWhere(selection);
                orderBy = null;
                break;
            case  INCOMING_ACCOUNT_COLLECTION_URI_INDICATOR:
                qb.setTables(NoteContract.AccountEntry.DATABASE_TABLE);
                orderBy = null;
                break;

        }
        if(!TextUtils.isEmpty(sortOrder)){
            orderBy = sortOrder;
        }
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db,projection, selection, selectionArgs, null, null, orderBy);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        ContentValues cv ;
        // xu ly du lieu nguon khong bi anh huong
        if(values != null){
            cv = new ContentValues(values);
        }else {
            cv = new ContentValues();
        }

        switch (uriMatcher.match(uri)){
            case INCOMING_NOTE_COLLECTION_URI_INDICATOR:
                SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                long rowSuccess = db.insert(NoteContract.NoteEntry.DATABASE_TABLE, null, cv);
                if(rowSuccess <= 0){
                    return null;
                }
                Uri insertUri = ContentUris.withAppendedId(NoteContract.NoteEntry.CONTENT_URI, rowSuccess);
                getContext().getContentResolver().notifyChange(insertUri, null);
                return insertUri;
            case INCOMING_IMAGES_COLLECTION_URI_INDICATOR:
                SQLiteDatabase dbImage = mOpenHelper.getWritableDatabase();
                long s = dbImage.insert(NoteContract.ImageEntry.DATABASE_TABLE, null, cv);
                if(s < 0){
                    return  null;
                }
                Uri imageUri = Uri.withAppendedPath(NoteContract.ImageEntry.CONTENT_URI,
                        cv.getAsString(NoteContract.ImageEntry.COL_NAME_PATH));
                getContext().getContentResolver().notifyChange(imageUri, null);
                return imageUri;

            case INCOMING_ACCOUNT_COLLECTION_URI_INDICATOR:
                if(!cv.containsKey(NoteContract.AccountEntry.COL_ID)){
                    return null;
                }
                SQLiteDatabase database = mOpenHelper.getWritableDatabase();
                long successful = database.insert(NoteContract.AccountEntry.DATABASE_TABLE, null, cv);
                if(successful <= 0){
                    return null;
                }
                Uri accountUri = Uri.withAppendedPath(NoteContract.AccountEntry.CONTENT_URI,
                        cv.getAsString(NoteContract.AccountEntry.COL_ID));
                getContext().getContentResolver().notifyChange(accountUri, null);
                return accountUri;
            default:
                throw new IllegalArgumentException("Unknown URI:"+uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        String where ;
        String DBName = "";
        switch (uriMatcher.match(uri)){
            case INCOMING_NOTE_COLLECTION_URI_INDICATOR:
                where = selection;
                DBName = NoteContract.NoteEntry.DATABASE_TABLE;
                break;
            case INCOMING_SINGLE_NOTE_URI_INDICATOR:
                String rowID = uri.getPathSegments().get(1);
                where = NoteContract.NoteEntry._ID + "=" + rowID
                        + (!TextUtils.isEmpty(selection) ? "AND (" + selection + ")" : "");
                DBName = NoteContract.NoteEntry.DATABASE_TABLE;
                break;
            case INCOMING_IMAGES_COLLECTION_URI_INDICATOR:
                where = selection;
                DBName = NoteContract.ImageEntry.DATABASE_TABLE;
                break;
            case INCOMING_SINGLE_IMAGES_URI_INDICATOR:
                where = selection;
                DBName = NoteContract.ImageEntry.DATABASE_TABLE;
                break;
            case INCOMING_ACCOUNT_COLLECTION_URI_INDICATOR:
                where = selection;
                DBName = NoteContract.AccountEntry.DATABASE_TABLE;
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri:"+ uri);
        }
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int deletes = db.delete(DBName, where, selectionArgs);
        if(deletes > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deletes;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        String where;
        String DBName = "";
        switch (uriMatcher.match(uri)){
            case INCOMING_NOTE_COLLECTION_URI_INDICATOR:
                where = selection;
                DBName = NoteContract.NoteEntry.DATABASE_TABLE;
                break;
            case  INCOMING_SINGLE_NOTE_URI_INDICATOR:
                Log.d("Password", "vao day");
                String rowID = uri.getPathSegments().get(1);
                where = NoteContract.NoteEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                DBName = NoteContract.NoteEntry.DATABASE_TABLE;
                break;
            case INCOMING_ACCOUNT_COLLECTION_URI_INDICATOR:
                where = selection;
                DBName = NoteContract.AccountEntry.DATABASE_TABLE;
                break;
            default:
                return 0;
        }
        Log.d("Loaf", "where:" + where);
        Log.d("Loaf", "where Uri:" + uri.toString());
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int updates = db.update(DBName, values, where, selectionArgs);
        Log.d("Loaf", "updates:" + updates);
        if(updates > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updates;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        /*switch (uriMatcher.match(uri)){
            case INCOMING_NOTE_COLLECTION_URI_INDICATOR:
                return ContentResolver.CURSOR_DIR_BASE_TYPE +"/" + NoteContract.AUTHORITY + "."+ BookContract.BookEntry.DATABASE_TABLE;
            case INCOMING_SINGLE_NOTE_URI_INDICATOR:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"+ BookContract.AUTHORITY + "."+ BookContract.BookEntry.DATABASE_TABLE;
            default:
                throw new IllegalArgumentException("Unknown Uri"+ uri);
        }*/
        return null;
    }


}
