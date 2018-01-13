package com.minhvu.proandroid.sqlite.database.models.entity;

import android.net.Uri;

import com.google.firebase.database.Exclude;

import java.io.File;

/**
 * Created by vomin on 12/4/2017.
 */

public class Image {

    private String path;
    //-1: deleted   - 0: normal/default   -    1:uploaded
    private int isSync = 0;

    private long noteID = -1;

    public Image() {
    }

    public Image(String path, int isSync) {
        this.path = path;
        this.isSync = isSync;
    }

    public Image(String path, long noteID) {
        this.path = path;
        this.noteID = noteID;
    }

    public Image(String path, int isSync, long noteID) {
        this.path = path;
        this.isSync = isSync;
        this.noteID = noteID;
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

    public void setNoteID(long noteID) {
        this.noteID = noteID;
    }
    public long getNoteID() {
        return noteID;
    }
}
