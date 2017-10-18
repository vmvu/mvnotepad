package com.minhvu.proandroid.sqlite.database.main.view.Fragment.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.widget.Toast;

/**
 * Created by vomin on 10/7/2017.
 */

public interface IDetailFragment {

    interface View {
        Context getAppContext();
        Context getActivityContext();
        void showToast(Toast toast);
        void showAlert(AlertDialog dialog);
        void showDateTimePicker(DatePickerDialog dialog);
        void showAlarmSpecial(final boolean isAllDayType, final SwitchCompat[] sc, final String switchType);
        void finishIfSelf();
    }

    interface ImageView {
        Context getActivityContext();
        void notifyUpdate();
    }

}
