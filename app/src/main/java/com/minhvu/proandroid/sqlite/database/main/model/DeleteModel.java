package com.minhvu.proandroid.sqlite.database.main.model;

import android.content.Context;

import com.minhvu.proandroid.sqlite.database.main.model.view.IDeleteModel;
import com.minhvu.proandroid.sqlite.database.main.presenter.view.IDeletePresenter;
import com.minhvu.proandroid.sqlite.database.models.DAO.NoteDAO;
import com.minhvu.proandroid.sqlite.database.models.DAO.NoteDeletedDAO;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;
import com.minhvu.proandroid.sqlite.database.models.entity.NoteDeleted;

import java.util.ArrayList;
import java.util.List;

public class DeleteModel implements IDeleteModel {
    private List<Note> listNote = null;
    private IDeletePresenter presenter;
    private NoteDAO mNoteDAO;
    private Context context;


    public DeleteModel(Context context) {
        listNote = new ArrayList<>();
        mNoteDAO = new NoteDAO(context);
        this.context = context;
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        if (!isChangingConfiguration) {
            listNote.clear();
            presenter = null;
            context = null;
        }
    }

    @Override
    public void setPresenter(IDeletePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void loadData(Context context) {
        listNote = mNoteDAO.loadDataByDELETE(1);
    }


    @Override
    public long getCount(Context context) {
        return mNoteDAO.getCount(1);
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
    public boolean restoreNote(int position) {
        Note note = listNote.get(position);
        if(mNoteDAO.UpdateDeleteCol(note.getId(), 0)){
            listNote.remove(position);
            return true;
        }
        return false;
    }


    @Override
    public boolean deleteNote(long noteID, String noteKeySync) {

        boolean isDeleted = mNoteDAO.deleteNote(noteID);
        if (isDeleted) {
            for (Note n : listNote) {
                if (n.getId() == noteID) {
                    listNote.remove(n);
                    break;
                }
            }
            noteReadyDeleted(noteKeySync, noteID);
            return true;
        }
        return false;
    }


    private void noteReadyDeleted(String keySync, long noteID) {
        NoteDeletedDAO dao = new NoteDeletedDAO(context);
        dao.insert(new NoteDeleted(noteID, keySync));
    }

}


