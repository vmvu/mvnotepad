package com.minhvu.proandroid.sqlite.database.models.entity;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.text.TextUtils;
import android.util.Log;

import com.minhvu.proandroid.sqlite.database.models.data.NoteContract.NoteEntry;


/**
 * Created by vomin on 8/24/2017.
 */

public class Note {
    private long id = -1;
    private String title;
    private String content;
    private long date_created;
    private long last_on;
    private String password = null;
    private String pass_salt = null;
    private int id_color = 0;
    private int id_typeoftext;
    private String account = null;
    private boolean delete = false;

    public ContentValues getValues(){
        ContentValues cv = new ContentValues();
        if(id != -1) cv.put(NoteEntry._ID, id);
        if(!TextUtils.isEmpty(getTitle())) cv.put(NoteEntry.COL_TITLE, getTitle());
        if(!TextUtils.isEmpty(getContent())) cv.put(NoteEntry.COL_CONTENT, getContent());
        if(date_created > 0)
            cv.put(NoteEntry.COL_DATE_CREATED, String.valueOf(getDateCreated()));
        cv.put(NoteEntry.COL_LAST_ON, String.valueOf(getLastOn()));
        if(!TextUtils.isEmpty(getPassword())){
            if( !TextUtils.isEmpty(getPassSalt())){
                cv.put(NoteEntry.COL_PASSWORD_SALT, getPassSalt());
                cv.put(NoteEntry.COL_PASSWORD, getPassword());
            }
        }
        cv.put(NoteEntry.COL_COLOR, getIdColor());
        cv.put(NoteEntry.COL_TYPE_OF_TEXT, getIdTypeOfText());
        if(!TextUtils.isEmpty(getAccount())){
            cv.put(NoteEntry.COL_ACCOUNT, getAccount());
        }
        cv.put(NoteEntry.COL_DELETE, isDelete() ? 1: 0);
        return cv;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getDateCreated() {
        return date_created;
    }

    public void setDateCreated(long date_created) {
        this.date_created = date_created;
    }

    public long getLastOn() {
        return last_on;
    }

    public void setLastOn(long last_on) {
        this.last_on = last_on;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassSalt() {
        return pass_salt;
    }

    public void setPassSalt(String pass_salt) {
        this.pass_salt = pass_salt;
    }

    public int getIdColor() {
        return id_color;
    }

    public void setIdColor(int id_color) {
        this.id_color = id_color;
    }

    public int getIdTypeOfText() {
        return id_typeoftext;
    }

    public void setIdTypeOfText(int id_typeoftext) {
        this.id_typeoftext = id_typeoftext;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    @Override
    public String toString() {

        Log.d("Note string", "id" + id);
        Log.d("Note string", "title:" + title);
        Log.d("Note string", "content:" + content);
        Log.d("Note string", "color:" + id_color);
        Log.d("Note string", "password:" + password);
        Log.d("Note string", "salt:" + pass_salt);
        Log.d("Note string", "delete:" + delete);
        Log.d("Note string", "-----------------");
       return super.toString();
    }
}
