package com.minhvu.proandroid.sqlite.database.models.entity;

import android.net.Uri;

import com.google.firebase.database.Exclude;

import java.io.File;

/**
 * Created by vomin on 12/4/2017.
 */

public class Image {

    public Image() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getSync() {
        return isSync;
    }

    public void setSync(int sync) {
        isSync = sync;
    }

    public Image(String path, int isSync) {
        this.path = path;
        this.isSync = isSync;
    }

    private String path;
    //-1: deleted   - 0: normal   -    1:uploaded
    private int isSync;


}
