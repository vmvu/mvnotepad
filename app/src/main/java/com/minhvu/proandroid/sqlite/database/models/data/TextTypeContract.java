package com.minhvu.proandroid.sqlite.database.models.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by vomin on 1/12/2018.
 */

public class TextTypeContract implements Contract {
    public static final String path_ttypeoftext = "typeoftext";
    public static final class TextTypeEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(path_ttypeoftext).build();

        public static final String DATABASE_TABLE = "v_typeoftext";
        public static final String COL_NAME = "name";

        public static final String DEFAULT_SORT_ORDER = _ID + " ASC";

        public static String[] getColumnsName() {
            return new String[]{_ID, COL_NAME};
        }

    }
}
