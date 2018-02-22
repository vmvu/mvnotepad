package com.minhvu.proandroid.sqlite.database.models.data;

import android.net.Uri;
import android.provider.BaseColumns;
import android.text.style.BackgroundColorSpan;

/**
 * Created by vomin on 8/1/2017.
 */

public class NoteContract implements Contract {

    public static final String path_tnote = "note";

    private NoteContract() {
    }

    public static final class NoteEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(path_tnote).build();

        public static final String DATABASE_TABLE = "note";
        public static final String COL_KEY_SYNC = "synch_keys";
        public static final String COL_TITLE = "title";
        public static final String COL_CONTENT = "content";
        public static final String COL_DATE_CREATED = "creation_time";
        public static final String COL_LAST_ON = "last_edit_time";
        public static final String COL_PASSWORD = "pass";
        public static final String COL_PASSWORD_SALT = "pass_salt";
        public static final String COL_COLOR = "id_color";
        public static final String COL_TYPE_OF_TEXT = "id_text_style";
        public static final String COL_DELETE = "is_delete";

        public static final String DEFAULT_SORT_ORDER = COL_DATE_CREATED + " DESC";

        private NoteEntry() {
        }

        public static String[] getColumnNames() {
            return new String[]{_ID, COL_TITLE, COL_CONTENT, COL_DATE_CREATED, COL_LAST_ON,
                    COL_PASSWORD, COL_PASSWORD_SALT, COL_COLOR, COL_DELETE, COL_KEY_SYNC};
        }

        public static String[] getColumnNamesForNote() {
            return new String[]{_ID, COL_TITLE,
                    COL_CONTENT,
                    COL_DATE_CREATED,
                    COL_LAST_ON,
                    COL_COLOR,
                    COL_TYPE_OF_TEXT};
        }
    }
}
