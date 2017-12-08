package com.minhvu.proandroid.sqlite.database.Utils;

import android.content.ContentValues;
import android.text.TextUtils;

import com.minhvu.proandroid.sqlite.database.models.data.NoteContract;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;

/**
 * Created by vomin on 11/29/2017.
 */

public class NoteUtils {

    public static ContentValues getContentValues(Note note){
        ContentValues cv = new ContentValues();
        if(note.getId() != -1) cv.put(NoteContract.NoteEntry._ID, note.getId());
        if(!TextUtils.isEmpty(note.getTitle())) cv.put(NoteContract.NoteEntry.COL_TITLE, note.getTitle());
        if(!TextUtils.isEmpty(note.getContent())) cv.put(NoteContract.NoteEntry.COL_CONTENT, note.getContent());
        if(note.getDateCreated() > 0)
            cv.put(NoteContract.NoteEntry.COL_DATE_CREATED, String.valueOf(note.getDateCreated()));
        cv.put(NoteContract.NoteEntry.COL_LAST_ON, String.valueOf(note.getLastOn()));
        if(!TextUtils.isEmpty(note.getPassword())){
            if( !TextUtils.isEmpty(note.getPassSalt())){
                cv.put(NoteContract.NoteEntry.COL_PASSWORD_SALT, note.getPassSalt());
                cv.put(NoteContract.NoteEntry.COL_PASSWORD, note.getPassword());
            }
        }
        cv.put(NoteContract.NoteEntry.COL_COLOR, note.getIdColor());
        cv.put(NoteContract.NoteEntry.COL_TYPE_OF_TEXT, note.getIdTypeOfText());
        cv.put(NoteContract.NoteEntry.COL_DELETE, note.isDelete() ? 1: 0);
        return cv;
    }
}
