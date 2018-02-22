package com.minhvu.proandroid.sqlite.database.main.view.Fragment.view;

import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.widget.Toast;

/**
 * Created by vomin on 10/7/2017.
 */

public interface IMainView {
    interface View {
        Context getActivityContext();

        Context getAppContext();

        void startActivityResult(Intent intent, int requestCode);

        void showToast(Toast toast);

        void updateAdapter();

        DisplayMetrics getDimensionOnScreen();

    }

}
