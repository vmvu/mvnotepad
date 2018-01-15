package com.minhvu.proandroid.sqlite.database.main.view.Fragment.view;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.widget.Toast;

/**
 * Created by vomin on 10/16/2017.
 */

public interface IDeleteView {
    Context getActivityContext();
    Context getBaseContext();
    Context getAppContext();
    void startActivityResult(Intent intent, int requestCode);
    void showToast(Toast toast);
    void showDialog(AlertDialog dialog);
    void updateAdapter();
    void updateViewAtPosition(int position);

    DisplayMetrics getDimensionOnScreen();
}
