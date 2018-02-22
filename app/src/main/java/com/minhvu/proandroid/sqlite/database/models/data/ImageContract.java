package com.minhvu.proandroid.sqlite.database.models.data;

import android.net.Uri;
import android.provider.BaseColumns;
import android.text.style.BackgroundColorSpan;

/**
 * Created by vomin on 8/1/2017.
 */

public class ImageContract implements Contract {

    public static final String path_images = "images";

    private ImageContract() {
    }

    public static final class ImageEntry {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(path_images).build();

        public static final String DATABASE_TABLE = "images";
        public static final String COL_NAME_PATH = "path_name";
        public static final String COL_NOTE_ID = "id_note";
        public static final String COL_SYNC = "sync_state";

        public static String[] getColumnNames() {
            return new String[]{COL_NAME_PATH, COL_NOTE_ID, COL_SYNC};
        }
    }
}
