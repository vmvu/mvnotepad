package com.minhvu.proandroid.sqlite.database.main.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.minhvu.proandroid.sqlite.database.main.model.view.IGetShareModel;
import com.minhvu.proandroid.sqlite.database.main.presenter.view.IGetSharePresenter;
import com.minhvu.proandroid.sqlite.database.models.DAO.ImageDAO;
import com.minhvu.proandroid.sqlite.database.models.DAO.NoteDAO;
import com.minhvu.proandroid.sqlite.database.models.data.NoteContract.NoteEntry;
import com.minhvu.proandroid.sqlite.database.models.data.ImageContract.ImageEntry;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;

/**
 * Created by vomin on 9/12/2017.
 */

public class GetShareModel implements IGetShareModel {
    private IGetSharePresenter presenter;
    private Note mNote;
    private NoteDAO mNoteDAO;

    public GetShareModel(Context context){
        mNote = new Note();

    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        if(!isChangingConfiguration){
            presenter = null;
            mNote = null;
        }
    }

    @Override
    public void setPresenter(IGetSharePresenter presenter) {
        this.presenter = presenter;
    }

    private void setup(){
        if(mNoteDAO == null)
            mNoteDAO = new NoteDAO(presenter.getActivityContext());
    }

    @Override
    public Note loadNote(String noteId) {
        if(TextUtils.isEmpty(noteId)){
            return null;
        }
        setup();
        long noteIDLongType = Long.parseLong(noteId.trim());
        mNote = mNoteDAO.getItemAt(noteIDLongType);
        return mNote;
    }



    @Override
    public long getCountImages(long noteId) {
        ImageDAO dao =  new ImageDAO(presenter.getActivityContext());
        return dao.getCountOfNote(noteId);
    }

    @Override
    public boolean insertNote(String title, String content) {
        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        note.setIdColor(0);
        note.setIdTypeOfText(1);
        note.setDateCreated(System.currentTimeMillis());
        note.setLastOn(System.currentTimeMillis());
        setup();
        return mNoteDAO.insertNote(note);
    }

    @Override
    public boolean updateNote(String noteId,String title, String content) {

        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        note.setLastOn(System.currentTimeMillis());
        note.setDelete(false);
        setup();
        return mNoteDAO.updateNote(note);


    }

    @Override
    public Note getmNote() {
        return mNote;
    }

}
