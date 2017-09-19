package com.minhvu.proandroid.sqlite.database.main.presenter;

import android.content.Context;
import android.net.Uri;
import android.widget.EditText;
import android.widget.TextView;

import com.minhvu.proandroid.sqlite.database.main.model.IGetShareModel;
import com.minhvu.proandroid.sqlite.database.main.view.Activity.GetShareView;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;

/**
 * Created by vomin on 9/12/2017.
 */

public interface IGetSharePresenter {
    Context getActivityContext();
    Context getAppContext();
    void bindView(GetShareView view);
    void setModel(IGetShareModel model);
    void onDestroy(boolean isChangingConfiguration);
    void setCurrentUri(Uri uri);
    Uri getCurrentUri();
    void loadNote();
    void updateView(Note note);
    void onDetailOnClick();

    void saveNote(EditText title, EditText content);
}
