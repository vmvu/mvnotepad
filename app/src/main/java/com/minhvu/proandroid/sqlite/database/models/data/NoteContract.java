package com.minhvu.proandroid.sqlite.database.models.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by vomin on 8/1/2017.
 */

public class NoteContract  {
    public static final String AUTHORITY = "com.minhvu.proandroid.sqlite.database";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY );

    public static final String path_tnote ="note";
    public static final String path_ttypeoftext ="typeoftext";
    public static  final String path_account = "account";
    public static final String path_images = "images";

    private NoteContract(){}

    public static final class NoteEntry implements BaseColumns{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(path_tnote).build();

        public static final String DATABASE_TABLE = "v_note";
        public static final String COL_TITLE = "title";
        public static final String COL_CONTENT = "content";
        public static final String COL_DATE_CREATED = "date_created";
        public static final String COL_LAST_ON = "last_on";
        public static final String COL_PASSWORD = "pass";
        public static final String COL_PASSWORD_SALT = "pass_key";
        public static final String COL_COLOR = "id_color";
        public static final String COL_TYPE_OF_TEXT = "id_typeoftext";
        public static final String COL_ACCOUNT ="account";
        public static final String COL_DELETE = "isdelete";

        public static final String DEFAULT_SORT_ORDER = COL_DATE_CREATED + " DESC";
        private NoteEntry(){}

        public static String[] getColumnNames(){
            return new String[]{_ID, COL_TITLE, COL_CONTENT, COL_DATE_CREATED, COL_LAST_ON,
                    COL_PASSWORD, COL_PASSWORD_SALT, COL_COLOR, COL_TYPE_OF_TEXT, COL_ACCOUNT, COL_DELETE};
        }

        public static String[] getColumnNamesForNote(){
            return new String[]{_ID, COL_TITLE,
                    COL_CONTENT,
                    COL_DATE_CREATED,
                    COL_LAST_ON,
                    COL_COLOR,
                    COL_TYPE_OF_TEXT};
        }
    }

    public static final class ImageEntry{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(path_images).build();
        public static final String DATABASE_TABLE = "v_images";
        public static final String COL_NAME_PATH = "name_path";
        public static final String COL_NOTE_ID = "note_id";

        public static String[] getColumnNames(){
            return new String[]{COL_NAME_PATH, COL_NOTE_ID};
        }
    }

    public static final class TypeOfTextEntry implements  BaseColumns{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(path_ttypeoftext).build();

        public static final String DATABASE_TABLE ="v_typeoftext";
        public static final String COL_NAME = "name";

        public static final String DEFAULT_SORT_ORDER = TypeOfTextEntry._ID + " ASC";

        public static String[] getColumnsName(){
            return new String[]{_ID, COL_NAME};
        }

    }

    public static final class AccountEntry {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(path_account).build();

        public static final String DATABASE_TABLE = "v_account";
        public static final String COL_ID = "id_account";

        public static String[] getColumnsName(){
            return new String[] {COL_ID};
        }
    }
}
