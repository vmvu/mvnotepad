package com.minhvu.proandroid.sqlite.database.main.model;

import android.content.Context;

import com.minhvu.proandroid.sqlite.database.main.model.view.IMainModel;
import com.minhvu.proandroid.sqlite.database.main.presenter.view.IMainPresenter;
import com.minhvu.proandroid.sqlite.database.models.DAO.NoteDAO;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vomin on 10/7/2017.
 */

public class MainModel implements IMainModel {
    private List<Note> listNote = new ArrayList<>();
    private IMainPresenter presenter;
    private NoteDAO mNoteDAO;

    public MainModel(Context context) {
        mNoteDAO = new NoteDAO(context);
        loadData(context);
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        if (!isChangingConfiguration) {
            listNote.clear();
            presenter = null;
        }
    }

    @Override
    public void setPresenter(IMainPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public boolean loadData(Context context) {
        if(listNote == null){
            listNote = new ArrayList<>();
        }else
            listNote.clear();

        listNote = mNoteDAO.loadDataByDELETE(0);
        return listNote.size() != 0;
    }


    @Override
    public int getCount() {
        return listNote == null ? 0 : listNote.size();
    }

    @Override
    public List<Note> getNoteList() {
        return listNote;
    }

    @Override
    public Note getNote(int index) {
        return listNote.get(index);
    }

    @Override
    public boolean deleteNote(Context ctx, long noteID) {
         boolean state = mNoteDAO.UpdateDeleteCol(noteID, 1);
         if(state){
             for (Note n : listNote) {
                 if (n.getId() == noteID) {
                     listNote.remove(n);
                     break;
                 }
             }
             return true;
         }
         return false;
    }

    @Override
    public void updateNote(Context context, int position) {
        Note note = mNoteDAO.getItemAt(listNote.get(position).getId());
        if(note == null){
            listNote.remove(position);
        }else{
            listNote.set(position, note);
        }
    }

    @Override
    public boolean isCheckCount(Context context) {


        long count = mNoteDAO.getCount(0);
        return count > this.getCount();

    }

    @Override
    public void getNewNote(Context context) {
        Note note = mNoteDAO.getLastNote();
        if(note != null){
            listNote.add(note);
        }
    }
}
