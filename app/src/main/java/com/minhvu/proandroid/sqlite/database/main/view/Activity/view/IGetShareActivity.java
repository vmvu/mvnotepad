package com.minhvu.proandroid.sqlite.database.main.view.Activity.view;

import android.app.Dialog;
import android.content.Context;
import android.widget.Toast;

/**
 * Created by vomin on 10/7/2017.
 */

public interface IGetShareActivity {
     interface  View {
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

}
