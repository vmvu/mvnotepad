package com.minhvu.proandroid.sqlite.database.Utils;

import android.util.Log;

import com.minhvu.proandroid.sqlite.database.models.entity.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vomin on 10/30/2017.
 */

public class Sort {

    public static void colorSort(List<Note> notes, int colorPos){
        if(notes == null){
            return;
        }
        if(colorPos == -1){
            for(Note note: notes){
                note.setDelete(false);
            }
            return;
        }
        int k = 0;
        for(int i = 0; i < notes.size(); i++){
            Note note = notes.get(i);
            if(i == k && note.getIdColor() == colorPos && !note.isDelete()){
                k++;
                continue;
            }
            if(note.getIdColor() == colorPos){
                note.setDelete(false);
                if(i == 0) {
                    k++;
                    continue;
                }
                if(i != k){
                    Note temp = notes.get(k);
                    notes.set(k,notes.get(i));
                    notes.set(i, temp);
                    k++;
                }
            }else{
                note.setDelete(true);
            }
        }
    }

}
