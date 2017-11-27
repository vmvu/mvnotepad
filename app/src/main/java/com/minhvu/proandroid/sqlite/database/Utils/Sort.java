package com.minhvu.proandroid.sqlite.database.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vomin on 10/30/2017.
 */

public class Sort {

    public static void colorSort(List<Note> notes, int colorPos) {
        if (notes == null || notes.size() <= 1) {
            return;
        }
        if (colorPos == -1) {
            for (Note note : notes) {
                note.setDelete(false);
            }
            return;
        }
        int k = 0;
        for (int i = 0; i < notes.size(); i++) {
            Note note = notes.get(i);
            if (i == k && note.getIdColor() == colorPos && !note.isDelete()) {
                k++;
                continue;
            }
            if (note.getIdColor() == colorPos) {
                note.setDelete(false);
                if (i != k) {
                    Note temp = notes.get(k);
                    notes.set(k, notes.get(i));
                    notes.set(i, temp);
                    k++;
                }
            } else {
                note.setDelete(true);
            }
        }
    }

    public static void alphaSort(List<Note> notes) {
        if (notes == null || notes.size() <= 1) {
            return;
        }
        ShakerSort(notes);
    }

    private static void ShakerSort(List<Note> notes) {
        int mLength = notes.size();
        int mLeft = 0, mRight = mLength - 1;
        int k = mRight;
        int j = mRight;
        while (mLeft < mRight) {
            while (j > mLeft) {
                String firstVal = notes.get(j - 1).getTitle();
                String secondVal = notes.get(j).getTitle();
                if (greater(firstVal, secondVal)) {
                    k = j;
                    swap(notes, j, j - 1);
                }
                j--;
            }
            mLeft = k;
            j = k;
            while (j < mRight) {
                String firstVal = notes.get(j).getTitle();
                String secondVal = notes.get(j + 1).getTitle();
                if (greater(firstVal, secondVal)) {
                    k = j;
                    swap(notes, j, j + 1);
                }
                j++;
            }
            mRight = k;
            j = k;
        }
    }

    private static void swap(List<Note> notes, int a, int b) {
        Note temp = notes.get(a);
        notes.set(a, notes.get(b));
        notes.set(b, temp);
    }

    private static boolean greater(String a, String b) {
        if (TextUtils.isEmpty(a)) {
            return true;
        }
        if (TextUtils.isEmpty(b)) {
            return false;
        }
        a = replaceAccents(a);
        b = replaceAccents(b);
        return a.compareToIgnoreCase(b) > 0;
    }

    private static String replaceAccents(String string) {
        String result = null;

        if (string != null) {
            result = string;

            result = result.replaceAll("[àáâãåäặăấ]", "a");
            result = result.replaceAll("[ç]", "c");
            result = result.replaceAll("[èéêëẹệ]", "e");
            result = result.replaceAll("[ìíîïị]", "i");
            result = result.replaceAll("[ñ]", "n");
            result = result.replaceAll("[òóôõöơồớợ]", "o");
            result = result.replaceAll("[ùúûüụ]", "u");
            result = result.replaceAll("[ÿý]", "y");
            result = result.replaceAll("[đ]", "d");

            result = result.replaceAll("[ÀÁÂÃÅÄẬĂẶẮ]", "A");
            result = result.replaceAll("[Ç]", "C");
            result = result.replaceAll("[ÈÉÊËẸỆ]", "E");
            result = result.replaceAll("[ÌÍÎÏỊ]", "I");
            result = result.replaceAll("[Ñ]", "N");
            result = result.replaceAll("[ÒÓỌÔÕÖƠỜỢỒỐỘ]", "O");
            result = result.replaceAll("[ÙÚÛÜỤ]", "U");
            result = result.replaceAll("[Ý]", "Y");
            result = result.replaceAll("[Đ]", "D");
            result = result.replaceAll("[ ]", "");
        }

        return result;
    }

    public static void colorOrderSort(List<Note> notes) {
        int length = notes.size();
        if (length <= 1) {
            return;
        }
        int k = 0;
        int j = 0;
        for (int i = 0; i <= 8; i++) {
            while (j < length) {
                Note note = notes.get(j);
                if (note.getIdColor() == i && !note.isDelete()) {
                    if (k != j) {
                        swap(notes, k, j);
                    }
                    k++;
                }
                j++;
            }
            j = k;
        }

    }

    public static void modifiedTimeSort(List<Note> notes){
        for(int i = 0 ; i < notes.size() - 1; i++)
            for(int j = i + 1; j < notes.size() ;j++){
                if(notes.get(i).getLastOn() < notes.get(j).getLastOn()){
                    swap(notes, i, j);
                }
            }
    }

    public static void sortByImportant(Context ctx, List<Note> notes){
        if(notes.size() <= 1){
            return;
        }
        SharedPreferences preferences = ctx.getSharedPreferences(
                ctx.getString(R.string.PREFS_ALARM_FILE), Context.MODE_PRIVATE);
        int k = 0;
        for(int i = 0; i < notes.size(); i++){
            if(isImportantNote(ctx, preferences, notes.get(i).getId())){
                if(k != i) {
                    swap(notes, k, i);
                }
                k++;
            }
        }
    }

    private static boolean isImportantNote(Context ctx, SharedPreferences preferences,long noteID) {
        String key = ctx.getString(R.string.PREFS_ALARM_SWITCH_KEY) + noteID;
        String switchType = preferences.getString(key, "");
        return switchType.trim().equals("scPin");
    }

}
