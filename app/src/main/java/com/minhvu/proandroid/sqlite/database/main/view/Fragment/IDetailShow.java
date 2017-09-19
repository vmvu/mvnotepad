package com.minhvu.proandroid.sqlite.database.main.view.Fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.Toast;

/**
 * Created by vomin on 8/24/2017.
 */

public interface IDetailShow {
    Context getAppContext();
    Context getActivityContext();
    void showToast(Toast toast);
    void showAlert(AlertDialog dialog);
    void showDateTimePicker(DatePickerDialog dialog);
    void showAlarmSpecial(final boolean isAllDayType, final SwitchCompat[] sc, final String switchType);
    void finishIfSelf();
}
