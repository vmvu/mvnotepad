package com.minhvu.proandroid.sqlite.database.main.view.Activity;

import android.app.Dialog;
import android.content.Context;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.minhvu.proandroid.sqlite.database.main.model.IGetShareModel;

/**
 * Created by vomin on 9/12/2017.
 */

public interface  GetShareView {
    Context getActivityContext();
    Context getAppContext();
    void visibleView();
    void invisibleView();
    void updateView(String title, String content);
    void finishThis();
    void showToast(Toast toast);
    void showDialog(Dialog dialog);
    void updateImageCount(int count);
    void lockContent();

}
