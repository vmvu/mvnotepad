package com.minhvu.proandroid.sqlite.database.main.model;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.net.Uri;

import com.minhvu.proandroid.sqlite.database.main.model.view.IDetailModel;
import com.minhvu.proandroid.sqlite.database.main.presenter.view.IDetailPresenter;

/**
 * Created by vomin on 8/26/2017.
 */

public class DetailModel implements IDetailModel {
    private IDetailPresenter mMainPresenter;
    private SharedPreferences mPreferences = null;

    public DetailModel(SharedPreferences preferences){
        this.mPreferences = preferences;
    }

    @Override
    public void setPresenter(IDetailPresenter presenter) {
        mMainPresenter = presenter;
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        if(!isChangingConfiguration){
            mMainPresenter = null;
            mPreferences = null;
        }
    }

    @Override
    public void setDataSharePreference(String key, String content) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(key, content);
        editor.apply();
    }

    @Override
    public String getDataSharePreference( String key) {
        return mPreferences.getString(key, "");
    }

    @Override
    public Uri insertData(Uri uri, ContentValues cv) {
        ContentResolver contentResolver = mMainPresenter.getActivityContext().getContentResolver();
        Uri uriInsert = null;
        uriInsert = contentResolver.insert(uri, cv);
        return uriInsert;
    }

    @Override
    public boolean update(Uri uri, ContentValues cv, String where, String[] selectionArgs) {
        ContentResolver contentResolver = mMainPresenter.getActivityContext().getContentResolver();
        int success = contentResolver.update(uri, cv, where, selectionArgs);
        return success > 0;
    }

    @Override
    public boolean delete(Uri uri, String where, String[] selectionArgs) {
        ContentResolver contentResolver = mMainPresenter.getActivityContext().getContentResolver();
        int success = contentResolver.delete(uri, where, selectionArgs);
        if(success > 0){
            return true;
        }
        return false;
    }









}
