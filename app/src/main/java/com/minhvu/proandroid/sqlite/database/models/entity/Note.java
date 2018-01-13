package com.minhvu.proandroid.sqlite.database.models.entity;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.minhvu.proandroid.sqlite.database.models.data.NoteContract.NoteEntry;


/**
 * Created by vomin on 8/24/2017.
 */
@IgnoreExtraProperties
public class Note {

    private String keySync = null;/**/
    private long id = -1;/**/
    private String title;/**/
    private String content;/**/
    private long date_created = 0;/**/
    private long last_on = 0;/**/
    private String password = null;
    private String pass_salt = null;
    private int id_color = -1;/**/
    private int id_typeoftext;/**/
    private boolean delete = false;/**/

    public Note() {
    }

    public Note(String keySync, long id) {
        this.keySync = keySync;
        this.id = id;
    }

    public Note(long id, String title, String content, long date_created, long last_on,
                String password, String pass_salt, int id_color, int id_typeoftext, boolean delete) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date_created = date_created;
        this.last_on = last_on;
        this.password = password;
        this.pass_salt = pass_salt;
        this.id_color = id_color;
        this.id_typeoftext = id_typeoftext;
        this.delete = delete;
    }
    @Exclude
    public String getKeySync() {
        return keySync;
    }
    @Exclude
    public void setKeySync(String keySync) {
        this.keySync = keySync;
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


    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

}
