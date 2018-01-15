package com.minhvu.proandroid.sqlite.database.models.entity;

/**
 * Created by vomin on 1/14/2018.
 */

public class NoteDeleted {
    private long noteID;
    private String keySync;

    public NoteDeleted() {
    }

    public NoteDeleted(long noteID, String keySync) {
        this.noteID = noteID;
        this.keySync = keySync;
    }

    public long getNoteID() {
        return noteID;
    }

    public void setNoteID(long noteID) {
        this.noteID = noteID;
    }

    public String getKeySync() {
        return keySync;
    }

    public void setKeySync(String keySync) {
        this.keySync = keySync;
    }
}
