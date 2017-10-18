package com.minhvu.proandroid.sqlite.database.main.presenter;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.Utils.DateTimeUtils;
import com.minhvu.proandroid.sqlite.database.Utils.DesEncrypter;
import com.minhvu.proandroid.sqlite.database.main.model.view.IDetailModel;
import com.minhvu.proandroid.sqlite.database.main.presenter.view.IDetailPresenter;
import com.minhvu.proandroid.sqlite.database.main.view.Fragment.view.IDetailFragment;
import com.minhvu.proandroid.sqlite.database.models.data.NoteContract;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;

import java.util.Calendar;

/**
 * Created by vomin on 8/24/2017.
 */

public class DetailPresenter extends MvpPresenter<IDetailModel, IDetailFragment.View> implements IDetailPresenter {
    private static final String LOGTAG = "DetailPresenter";
    private Uri mCurrentUri = null;
    private boolean mHasChange = false;


    @Override
    public void onDestroy(boolean isChangeConfiguration) {
        unbindView();
        model.onDestroy(isChangeConfiguration);
        if (!isChangeConfiguration) {
            model = null;
            mCurrentUri = null;
        }
    }

    @Override
    public Context getAppContext() {
        return getView().getAppContext();
    }

    @Override
    public Context getActivityContext() {
        return getView().getActivityContext();
    }

    @Override
    public void setCurrentUri(Uri currentUri) {
        this.mCurrentUri = currentUri;
    }

    @Override
    public Uri getCurrentUri() {
        return mCurrentUri;
    }

    @Override
    public void onViewHasChanged() {
        mHasChange = true;
    }

    private PopupWindow popupConfiguration(View layout, int width, int height, int x, int y, int gravity) {
        PopupWindow popup = new PopupWindow(getActivityContext());
        popup.setContentView(layout);
        popup.setWidth(width);
        popup.setHeight(height);
        popup.setFocusable(true);
        popup.setBackgroundDrawable(new BitmapDrawable());
        popup.showAtLocation(layout, gravity, x, y);
        return popup;
    }


    @Override
    public void showTableSetting(View layout, View parent) {
        View lock = layout.findViewWithTag("ivLockNote");
        View unlock = layout.findViewWithTag("ivUnLockNote");
        View delete = layout.findViewWithTag("ivDelete");
        if (mCurrentUri == null) {
            lock.setVisibility(View.GONE);
            unlock.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);

        } else {
            delete.setVisibility(View.VISIBLE);
            ContentResolver resolver = getActivityContext().getContentResolver();
            Cursor c = resolver.query(mCurrentUri, new String[]{NoteContract.NoteEntry.COL_PASSWORD}, null, null, null);
            if (c.moveToNext()) {
                String password = c.getString(c.getColumnIndex(NoteContract.NoteEntry.COL_PASSWORD));
                if (TextUtils.isEmpty(password)) {
                    lock.setVisibility(View.VISIBLE);
                    unlock.setVisibility(View.GONE);
                } else {
                    lock.setVisibility(View.GONE);
                    unlock.setVisibility(View.VISIBLE);
                }
                c.close();
            }
        }
        int popupWith = 130;
        int popupHeight = 630;
        if(mCurrentUri != null){
            popupHeight = 920;
        }
        int[] local = new int[2];
        parent.getLocationInWindow(local);
        popupConfiguration(layout, popupWith, popupHeight, local[0] + 10, local[1] + 167, Gravity.NO_GRAVITY);
    }

    @Override
    public void deleteOnClick(final EditText title, final EditText content, final ImageButton color) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivityContext());
        builder.setTitle("Delete");
        builder.setMessage("Are you delete");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ContentValues cv = noteBase(title, content, color);
                cv.put(NoteContract.NoteEntry.COL_DELETE, "1");
                boolean isDeleted = model.update(mCurrentUri, cv, null, null);
                if (isDeleted) {
                    Intent intent = new Intent(getActivityContext().getString(R.string.broadcast_receiver_pin));
                    int noteId = Integer.parseInt(mCurrentUri.getPathSegments().get(1));
                    cancelAlarmAndNotification(intent, noteId);
                    cancelNotification(noteId);
                    getView().finishIfSelf();
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        getView().showAlert(dialog);
    }

    @Override
    public void setAlarmOnClick(EditText title, EditText content, ImageButton color, View alarm) {
        saveNoteInternal(title, content, color, 1);
    }


    private ContentValues noteBase(EditText etTitle, EditText etContent, View btColor) {
        Log.d("Pin", "index:" + btColor.getTag());
        String title = etTitle.getText().toString();
        String content = etContent.getText().toString();
        int color = (int) btColor.getTag();

        ContentValues cv = new ContentValues();
        cv.put(NoteContract.NoteEntry.COL_TITLE, title);
        cv.put(NoteContract.NoteEntry.COL_CONTENT, content);
        cv.put(NoteContract.NoteEntry.COL_COLOR, color);
        cv.put(NoteContract.NoteEntry.COL_LAST_ON, System.currentTimeMillis() + "");
        return cv;
    }

    @Override
    public void lockOnClick(EditText etTitle, EditText etContent, ImageButton btColor, EditText etPassword, View layout) {
        if (mCurrentUri == null)
            return;
        String password = etPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            Toast toast = Toast.makeText(getActivityContext(), "Text is empty", Toast.LENGTH_SHORT);
            getView().showToast(toast);
            return;
        }
        DesEncrypter encrypt = new DesEncrypter();
        String pasEncrypt = encrypt.encrypt(password);
        String key = encrypt.getKey();
        ContentValues cv = noteBase(etTitle, etContent, btColor);
        if (!TextUtils.isEmpty(pasEncrypt) && !TextUtils.isEmpty(key)) {
            cv.put(NoteContract.NoteEntry.COL_PASSWORD, pasEncrypt);
            cv.put(NoteContract.NoteEntry.COL_PASSWORD_SALT, key);
        }
        Log.d("Password", mCurrentUri.toString());

        boolean setPassword = model.update(mCurrentUri, cv, null, null);
        if (setPassword) {
            getView().showToast(
                    Toast.makeText(getActivityContext(), "Password is saved", Toast.LENGTH_SHORT)
            );
            layout.findViewWithTag("ivUnLockNote").setVisibility(View.VISIBLE);
            layout.findViewWithTag("ivLockNote").setVisibility(View.GONE);
        }

    }

    @Override
    public void unLockOnClick(final EditText title, final EditText content, final ImageButton color, final View layout) {
        if (mCurrentUri == null)
            return;
        final AlertDialog.Builder builder = new AlertDialog.Builder(getView().getActivityContext());
        builder.setTitle("UnLock");
        builder.setMessage("Are you sure you want to unlock?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ContentValues cv = noteBase(title, content, color);
                cv.put(NoteContract.NoteEntry.COL_PASSWORD, "");
                cv.put(NoteContract.NoteEntry.COL_PASSWORD_SALT, "");
                boolean unLock = model.update(mCurrentUri, cv, null, null);
                if (unLock) {
                    Toast toast = Toast.makeText(getActivityContext(), "Note is unlock", Toast.LENGTH_SHORT);
                    getView().showToast(toast);
                    layout.findViewWithTag("ivUnLockNote").setVisibility(View.GONE);
                    layout.findViewWithTag("ivLockNote").setVisibility(View.VISIBLE);
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        getView().showAlert(dialog);

    }

    private int getColor(int color) {


        int[] headerColors = getView().getActivityContext().getResources().getIntArray(R.array.header_color);
        int c = 0;
        for (int i = 0; i < headerColors.length; i++) {
            if (headerColors[i] == color)
                return i;
        }
        return c;
    }


    private void saveNoteInternal(EditText title, EditText content, ImageButton color, int typeOfText) {
        Note note = new Note();
        note.setTitle(title.getText().toString() + "");
        note.setContent(content.getText().toString() + "");
        note.setIdColor((int) color.getTag());
        note.setIdTypeOfText(typeOfText);
        note.setLastOn(System.currentTimeMillis());
        if (mCurrentUri == null) {
            note.setDateCreated(System.currentTimeMillis());
            insertNote(note);
        } else {
            updateNote(note);
        }
    }

    @Override
    public void onPause(EditText title, EditText content, ImageButton color, int typeOfText, boolean isCheck) {
        //printLog(title, content, color, typeOfText);
        if (isCheck && !this.mHasChange) {
            return;
        }
        if (isCheck && TextUtils.isEmpty(title.getText().toString()) && TextUtils.isEmpty(content.getText().toString())) {
            return;
        }
        saveNoteInternal(title, content, color, typeOfText);
        mHasChange = false;

    }

    private void insertNote(final Note note) {
        Uri uri = model.insertData(NoteContract.NoteEntry.CONTENT_URI, note.getValues());
        if (uri != null) {
            setCurrentUri(uri);
            getView().showToast(
                    Toast.makeText(getView().getActivityContext(), "save data", Toast.LENGTH_SHORT)
            );
        }
    }

    private void updateNote(final Note note) {
        if (mCurrentUri == null) {
            return;
        }
        boolean success = model.update(mCurrentUri, note.getValues(), null, null);
        if (success) {
            getView().showToast(
                    Toast.makeText(getView().getActivityContext(), "save data", Toast.LENGTH_SHORT)
            );
        }
    }




    //===============================================
    @Override
    public  void activePrompt(EditText title, EditText content) {
        if(mCurrentUri == null){
            return;
        }

        String typeOfSwitch = model.getDataSharePreference(
                getActivityContext().getString(R.string.PREFS_ALARM_SWITCH_KEY) + getNoteID()).trim();
        if (TextUtils.isEmpty(typeOfSwitch)) {
            return;
        }

        Context ctx = getView().getActivityContext();

        String action_broadcast = ctx.getString(R.string.broadcast_receiver_pin);
        Intent intent = new Intent(action_broadcast);
        intent.putExtra(ctx.getString(R.string.notify_note_uri), mCurrentUri.toString());
        intent.putExtra(ctx.getString(R.string.notify_note_title), title.getText().toString());

        intent.putExtra(ctx.getString(R.string.notify_note_pin), false);

        int nodeID_int = Integer.parseInt(getNoteID());

        switch (typeOfSwitch) {
            case "scPin":
                intent.putExtra(ctx.getString(R.string.notify_note_pin), true);
                getView().getActivityContext().sendBroadcast(intent);
                return;
            case "sc15Min":
                long time15Min = System.currentTimeMillis() + 15 * 60000;
                model.setDataSharePreference(ctx.getString(R.string.PREFS_ALARM_WHEN) + getNoteID(), time15Min + "");
                cancelNotification(nodeID_int);
                alarm(intent, time15Min, nodeID_int, false);
                break;
            case "sc30Min":
                long time30Min = System.currentTimeMillis() + 30 * 60000;
                model.setDataSharePreference(ctx.getString(R.string.PREFS_ALARM_WHEN) + getNoteID(), time30Min + "");
                cancelNotification(nodeID_int);
                alarm(intent, time30Min, nodeID_int, false);
                break;
            case "scAtTime":
                alarmSpecial(intent, nodeID_int, false);
                cancelNotification(nodeID_int);
                break;
            case "scRepeater":
                intent.putExtra(ctx.getString(R.string.PREFS_ALARM_TO_DATE),
                        model.getDataSharePreference(ctx.getString(R.string.PREFS_ALARM_TO_DATE) + getNoteID()));
                cancelNotification(nodeID_int);
                alarmSpecial(intent, nodeID_int, true);
                break;
            default:
                cancelAlarmAndNotification(intent, nodeID_int);
                cancelNotification(nodeID_int);
                break;
        }
    }

    private void alarmSpecial(Intent intent, int requestCode, boolean isRepeater) {

        String date = model.getDataSharePreference(
                getView().getActivityContext().getString(R.string.PREFS_ALARM_FROM_DATE) + getNoteID());
        String time = model.getDataSharePreference(
                getView().getActivityContext().getString(R.string.PREFS_ALARM_WHEN) + getNoteID());
        long timeLongType = Long.parseLong(time);
        int minute = (int) ((timeLongType / 60000) % 60);
        int hour = (int) ((timeLongType / (60 * 60000)) % 24);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(date));
        calendar.add(Calendar.MINUTE, minute);
        calendar.add(Calendar.HOUR_OF_DAY, hour);

        alarm(intent, calendar.getTime().getTime(), requestCode, isRepeater);
    }

    private void alarm(Intent intent, long setTime, int requestCode, boolean isRepeater) {
        PendingIntent pi = PendingIntent.getBroadcast(getView().getActivityContext(), requestCode, intent, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(setTime);

        AlarmManager am = (AlarmManager) getView().getActivityContext().getSystemService(Context.ALARM_SERVICE);
        if (isRepeater) {
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
            } else am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
        }
    }
    private void cancelNotification(int requestCode){
        NotificationManager nm = (NotificationManager) getActivityContext().getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(requestCode);
    }

    private void cancelAlarmAndNotification(Intent intent, int requestCode) {
        PendingIntent pi = PendingIntent.getBroadcast(getActivityContext(), requestCode, intent, PendingIntent.FLAG_NO_CREATE);
        if (pi != null) {
            AlarmManager am = (AlarmManager) getActivityContext().getSystemService(Context.ALARM_SERVICE);
            am.cancel(pi);
        }
        //clear
        saveStateSwitch(getActivityContext().getString(R.string.PREFS_ALARM_FROM_DATE), "");
        saveStateSwitch(getActivityContext().getString(R.string.PREFS_ALARM_TO_DATE), "");
        saveStateSwitch(getActivityContext().getString(R.string.PREFS_ALARM_WHEN), "");
    }
    private void setCheckForSwitch(SwitchCompat[] sc, String switchState) {
        for (SwitchCompat s : sc) {
            boolean checked = s.isChecked();
            if (s.getTag().toString().equals(switchState)) {
                s.setChecked(true);
                saveStateSwitch(getView().getActivityContext().getString(R.string.PREFS_ALARM_SWITCH_KEY), switchState);
            } else {
                s.setChecked(false);
            }
        }
    }

    private void saveStateSwitch(String key, String data) {
        key += mCurrentUri.getPathSegments().get(1);
        model.setDataSharePreference(key, data);
    }

    @Override
    public void handleForAlarms(SwitchCompat[] switchCompatArray, View layout) {
        //[0]pin - [1]sc15Min - [2]sc30Min - [3]scWhen - [4]scAllDay - [5]scReset
        String stateSwitch = model.getDataSharePreference(
               getActivityContext().getString(R.string.PREFS_ALARM_SWITCH_KEY) + getNoteID());
        if (TextUtils.isEmpty(stateSwitch) || stateSwitch.equals(getActivityContext().getString(R.string.type_of_switch_reset))) {
            switchCompatArray[5].setChecked(true);//scReset
        } else {
            setCheckForSwitch(switchCompatArray, stateSwitch);
        }
    }
    @Override
    public void switchCompatReset(View view, SwitchCompat[] switchCompatArray){
        String state = model.getDataSharePreference(getActivityContext().getString(R.string.PREFS_ALARM_SWITCH_KEY) + getNoteID());
        setCheckForSwitch(switchCompatArray, state);
    }

    @Override
    public void switchCompatOnClick(View view, SwitchCompat[] switchCompatArray) {
        SwitchCompat sc = (SwitchCompat) view;
        if (!sc.isChecked()) {
            if (sc.getTag().toString().equals(getActivityContext().getString(R.string.type_of_switch_repeater))) {
                saveStateSwitch(getActivityContext().getString(R.string.PREFS_ALARM_SWITCH_KEY),
                        getActivityContext().getString(R.string.type_of_switch_reset));
                getView().showAlarmSpecial(true, switchCompatArray, sc.getTag().toString());
            } else if (sc.getTag().toString().equals(getActivityContext().getString(R.string.type_of_switch_at_time))) {
                saveStateSwitch(getActivityContext().getString(R.string.PREFS_ALARM_SWITCH_KEY),
                        getActivityContext().getString(R.string.type_of_switch_reset));
                getView().showAlarmSpecial(false, switchCompatArray, sc.getTag().toString());
            } else {
                saveStateSwitch(getActivityContext().getString(R.string.PREFS_ALARM_SWITCH_KEY),
                       sc.getTag().toString());
            }
        } else {
            saveStateSwitch(getActivityContext().getString(R.string.PREFS_ALARM_SWITCH_KEY),
                    getActivityContext().getString(R.string.type_of_switch_reset));

        }
    }

    @Override
    public void alarmSpecificSetup(TextView fromDate, TextView toDate, TimePicker timePicker) {
        alarmSpecificSetup(fromDate, timePicker);
        String key = getView().getActivityContext().getString(R.string.PREFS_ALARM_TO_DATE) + getNoteID();
        String dateString = model.getDataSharePreference(key);
        long date;
        if (!TextUtils.isEmpty(dateString)) {
            date = Long.parseLong(dateString);
        } else {
            final Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            date = calendar.getTimeInMillis();
        }
        toDate.setText(DateTimeUtils.longToStringDate(date));
    }

    @Override
    public void alarmSpecificSetup(TextView fromDate, TimePicker timePicker) {

        String key = getView().getActivityContext().getString(R.string.PREFS_ALARM_FROM_DATE) + getNoteID();
        String date = model.getDataSharePreference(key);

        if (TextUtils.isEmpty(date)) {
            fromDate.setText(DateTimeUtils.longToStringDate(System.currentTimeMillis()));
        } else {
            fromDate.setText(DateTimeUtils.longToStringDate(Long.parseLong(date.trim())));
        }
        String timeKey = getView().getActivityContext().getString(R.string.PREFS_ALARM_WHEN) + getNoteID();
        String timeString = model.getDataSharePreference(timeKey);
        int minute, hour;
        if (TextUtils.isEmpty(timeString)) {
            Calendar calendar = Calendar.getInstance();
            minute = calendar.get(Calendar.MINUTE);
            hour = calendar.get(Calendar.HOUR_OF_DAY);

        } else {
            long timeMillis = Long.parseLong(timeString.trim());
            minute = (int) ((timeMillis / (60 * 1000)) % 60);
            hour = (int) ((timeMillis / (60 * 60 * 1000)) % 24);
        }
        timePicker.setMinute(minute);
        timePicker.setHour(hour);

    }

    @Override
    public void alarmButtonShowDateTimePicker(TextView textView) {
        long timeMillis = DateTimeUtils.stringToLongDate(textView.getText().toString());
        DatePickerDialog dialog = settingDatePicker(timeMillis, textView);
        getView().showDateTimePicker(dialog);
    }

    @Override
    public void alarmSpecificHandle(SwitchCompat[] switchCompatArray, TextView fromDate, TextView toDate, TimePicker timePicker) {
        alarmSpecificHandle(switchCompatArray, fromDate, timePicker, true);
        long toDateLongType = DateTimeUtils.stringToLongDate(toDate.getText().toString());
        saveStateSwitch(getView().getActivityContext().getString(R.string.PREFS_ALARM_TO_DATE), toDateLongType + "");

        setCheckForSwitch(switchCompatArray, switchCompatArray[4].getTag().toString());
        //activePrompt();
    }

    @Override
    public void alarmSpecificHandle(SwitchCompat[] switchCompatArray, TextView fromDate, TimePicker timePicker, boolean isAllDate) {
        long date = DateTimeUtils.stringToLongDate(fromDate.getText().toString());
        saveStateSwitch(getView().getActivityContext().getString(R.string.PREFS_ALARM_FROM_DATE), date + "");

        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        long time = (hour * 60 + minute) * 60 * 1000;
        saveStateSwitch(getView().getActivityContext().getString(R.string.PREFS_ALARM_WHEN), time + "");

        if (!isAllDate) {
            setCheckForSwitch(switchCompatArray, switchCompatArray[3].getTag().toString());
            //activePrompt();
        }

    }


    private DatePickerDialog settingDatePicker(long timeMillis, final TextView textView) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        int nYear = calendar.get(Calendar.YEAR);
        int nMonth = calendar.get(Calendar.MONTH);
        int nDay = calendar.get(Calendar.DAY_OF_MONTH);
        Log.d(LOGTAG, nDay + "-" + nMonth + "-" + nYear);
        DatePickerDialog dialog = new DatePickerDialog(getView().getActivityContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                textView.setText(dayOfMonth + "//" + (month + 1) + "//" + year);
            }
        }, nYear, nMonth, nDay);
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        return dialog;
    }

    private String getNoteID() {
        if (getCurrentUri() == null) {
            return null;
        }
        return getCurrentUri().getPathSegments().get(1);
    }


    @Override
    protected void updateView() {

    }
}
