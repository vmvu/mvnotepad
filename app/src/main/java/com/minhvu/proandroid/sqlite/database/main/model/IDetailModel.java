package com.minhvu.proandroid.sqlite.database.main.model;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.minhvu.proandroid.sqlite.database.main.presenter.IDetailPresenter;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vomin on 8/26/2017.
 */

public interface IDetailModel {
    void setPresenter(IDetailPresenter presenter);
    void onDestroy(boolean isChangingConfiguration);
    void setDataSharePreference(String key, String content);
    String getDataSharePreference(String key);


    Uri insertData(Uri uri, ContentValues cv);
    boolean update(Uri uri, ContentValues cv, String where, String[] selectionArgs);
    boolean delete(Uri uri, String where, String[] selectionArgs);

}
