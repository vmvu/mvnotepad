package com.minhvu.proandroid.sqlite.database.main.model.view;

import android.content.Context;

import com.minhvu.proandroid.sqlite.database.main.presenter.view.IMainPresenter;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;

import java.util.List;

/**
 * Created by vomin on 10/7/2017.
 */

public interface IMainModel {
    void onDestroy(boolean isChangingConfiguration);
    void setPresenter(IMainPresenter presenter);

    void loadData(Context context);
    int getCount();
    List<Note> getNoteList();
    Note getNote(int index);

    boolean deleteNote(Context context, long noteID);

    void updateNote(Context context, int position);

    void getNewNote(Context context);

    boolean isCheckCount(Context context);

}
