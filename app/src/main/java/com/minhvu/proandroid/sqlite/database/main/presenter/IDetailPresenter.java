package com.minhvu.proandroid.sqlite.database.main.presenter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.minhvu.proandroid.sqlite.database.main.model.IDetailModel;
import com.minhvu.proandroid.sqlite.database.main.view.Fragment.IDetailShow;

/**
 * Created by vomin on 8/24/2017.
 */

public interface IDetailPresenter {
    Context getAppContext();
    Context getActivityContext();
    void bindView(IDetailShow view);
    void setModel(IDetailModel model);
    void onDestroy(boolean isChangeConfiguration);
    void setCurrentUri(Uri currentUri);
    Uri getCurrentUri();
    void onViewHasChanged();

    void showTableSetting(View layout, View parent);

    void deleteOnClick(EditText title, EditText content,ImageButton color);
    void setAlarmOnClick(EditText title, EditText content,ImageButton color, View alarm);
    void lockOnClick(EditText title, EditText content,ImageButton color, EditText password, View layout);
    void unLockOnClick(EditText title, EditText content,ImageButton color, View layout);
    void onPause(EditText title, EditText content, ImageButton color, int typeOfText, boolean isCheck);

    void handleForAlarms(SwitchCompat[] switchCompatArray, View layout);
    void switchCompatOnClick(View view, SwitchCompat[] switchCompatArray);
    void alarmSpecificSetup(TextView fromDate, TextView toDate, TimePicker timePicker);
    void alarmSpecificSetup(TextView fromDate, TimePicker timePicker);
    void alarmSpecificHandle(SwitchCompat[] switchCompatArray, TextView fromDate, TextView toDate,TimePicker timePicker);
    void alarmSpecificHandle(SwitchCompat[] switchCompatArray, TextView fromDate,TimePicker timePicker, boolean isAllDate);

    void alarmButtonShowDateTimePicker(TextView textView);



}
