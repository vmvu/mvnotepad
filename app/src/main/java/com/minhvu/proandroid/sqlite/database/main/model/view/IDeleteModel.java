package com.minhvu.proandroid.sqlite.database.main.model.view;

import android.content.Context;

import com.minhvu.proandroid.sqlite.database.main.presenter.view.IDeletePresenter;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;

import java.util.List;

/**
 * Created by vomin on 10/16/2017.
 */

public interface  IDeleteModel {
    void onDestroy(boolean isChangingConfiguration);
    void setPresenter(IDeletePresenter presenter);

    void loadData(Context context);
    int getCount();
    List<Note> getNoteList();
    Note getNote(int index);
    long getCount(Context context);

    boolean restoreNote(Context context, long noteID);

    boolean deleteNote(Context context, long noteID);





}
