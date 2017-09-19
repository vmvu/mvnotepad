package com.minhvu.proandroid.sqlite.database.main.model;

import android.net.Uri;

import com.minhvu.proandroid.sqlite.database.main.presenter.IGetSharePresenter;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;

/**
 * Created by vomin on 9/12/2017.
 */

public interface IGetShareModel {
     void onDestroy(boolean isChangingConfiguration);
     void setPresenter(IGetSharePresenter presenter);
     Note loadNote(String noteId);
     int loadImage(String noteId);
     boolean insertNote(String title, String content);
     boolean updateNote(String noteId, String title, String content);
     Note getNote();
}
