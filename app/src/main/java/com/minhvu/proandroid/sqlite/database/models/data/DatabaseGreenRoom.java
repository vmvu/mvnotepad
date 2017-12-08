package com.minhvu.proandroid.sqlite.database.models.data;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;

/**
 * Created by vomin on 12/1/2017.
 */

public class DatabaseGreenRoom {
    private int count = 0;
    private int client = 0;
    private NoteDBHelper helper;

    private static DatabaseGreenRoom s_self;

    private DatabaseGreenRoom() {
    }


    synchronized NoteDBHelper enter(Context context) {
        count++;
        openDB(context);
        return helper;
    }

    synchronized void leave() {
        if (count == 0)
            return;
        count--;
        if (count == 0) {
            closeDB();
        }
    }

    public static NoteDBHelper s_enter(Context ctx) {
        if (s_self == null) {
            s_self = new DatabaseGreenRoom();
        }
        return s_self.enter(ctx);
    }

    public static void s_leave() {
        if (s_self == null) {
            return;
        }
        s_self.leave();
    }

    private void registerClient() {
        client++;
    }

    private void unregisterClient() {
        if (client == 0) {
            return;
        }
        client--;
        if (client == 0) {
            closeDB();
        }
    }

    public static void s_registerClient() {
        if (s_self == null) {
            s_self = new DatabaseGreenRoom();
        }
        s_self.registerClient();
    }

    public static void s_unregisterClient() {
        if (s_self == null) {
            return;
        }
        s_self.unregisterClient();
    }


    private void openDB(Context context) {
        if (helper == null) {
            helper = NoteDBHelper.getInstance(context);
        }
    }

    private void closeDB() {
        if (helper != null) {
            helper.close();
        }
    }
}
