package com.minhvu.proandroid.sqlite.database.models.data;

/**
 * Created by vomin on 1/12/2018.
 */

public class NoteReadyDeletedContract implements Contract {

    public static final class NoteReadyDeletedEntry {
        public static final String DATABASE_TABLE = "note_ready_deleted";
        public static final String NOTE_KEY_SYNC = "key_sync";
        public static final String NOTE_ID = "note_id";
    }
}
