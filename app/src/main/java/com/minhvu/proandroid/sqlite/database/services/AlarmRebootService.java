package com.minhvu.proandroid.sqlite.database.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.models.DAO.NoteDAO;
import com.minhvu.proandroid.sqlite.database.models.data.NoteContract;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;

import java.util.Calendar;
import java.util.List;

/**
 * Created by vomin on 9/16/2017.
 */

public class AlarmRebootService extends ALongRunningNonStickyBroadcastService {
    private SharedPreferences preferences = null;

    public AlarmRebootService() {
        super("AlarmRebootService");
    }

    @Override
    public void handIntentBroadcast(Intent intentBroadcast) {
        if (intentBroadcast.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            List<Note> noteList = getNoteList();

            if (noteList.size() > 0) {
                preferences = getSharedPreferences(getString(R.string.PREFS_ALARM_FILE), MODE_PRIVATE);
                checkedAlarmNote(noteList);
            }
        }
    }

    private void TestNotification() {
        Notification.Builder builder = new Notification.Builder(this)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_notes)
                .setContentText("ALARM-REBOOT")
                .setOngoing(true)
                .setContentTitle("title2");

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(0, builder.build());
    }


    private List<Note> getNoteList() {
        NoteDAO dao = new NoteDAO(this);
        return dao.loadDataByDELETE(0);
    }

    private void checkedAlarmNote(List<Note> list) {
        for (Note note : list) {
            String noteAlarm = preferences.getString(getString(R.string.PREFS_ALARM_SWITCH_KEY) + note.getId(), "");
            if (TextUtils.isEmpty(noteAlarm) || noteAlarm.equals(getString(R.string.type_of_switch_reset))) {
                continue;
            }
            restoreAlarm(note, noteAlarm);
        }
    }

    private void restoreAlarm(Note note, String type) {
        Uri uri = ContentUris.withAppendedId(NoteContract.NoteEntry.CONTENT_URI, note.getId());
        Intent intent = new Intent(getString(R.string.broadcast_receiver_pin));
        intent.putExtra(getString(R.string.notify_note_uri), uri.toString());
        intent.putExtra(getString(R.string.notify_note_title), note.getTitle());
        if (!TextUtils.isEmpty(note.getPassword())) {
            intent.putExtra(getString(R.string.notify_note_content), note.getContent());
        }
        intent.putExtra(getString(R.string.notify_note_color), note.getIdColor());
        intent.putExtra(getString(R.string.notify_note_pin), false);


        final String scPin = getString(R.string.type_of_switch_pin);
        final String sc15Min = getString(R.string.type_of_switch_15min);
        final String sc30Min = getString(R.string.type_of_switch_30min);
        final String scAtTime = getString(R.string.type_of_switch_at_time);
        final String scRepeater = getString(R.string.type_of_switch_repeater);
        if (type.equals(scPin)) {
            intent.putExtra(getString(R.string.notify_note_pin), true);
            sendBroadcast(intent);
        } else if (type.equals(sc15Min)) {

            long time15Min = Long.parseLong(preferences.getString(getString(R.string.PREFS_ALARM_WHEN) + note.getId(), "0"));
            alarm(intent, time15Min, (int) note.getId(), false);
        } else if (type.equals(sc30Min)) {
            long time30Min = Long.parseLong(preferences.getString(getString(R.string.PREFS_ALARM_WHEN) + note.getId(), "0"));
            alarm(intent, time30Min, (int) note.getId(), false);
        } else if (type.equals(scAtTime)) {
            alarmSpecial(note, intent, false);
        } else if (type.equals(scRepeater)) {
            intent.putExtra(getString(R.string.PREFS_ALARM_TO_DATE),
                    preferences.getString(getString(R.string.PREFS_ALARM_TO_DATE) + note.getId(), "0"));
            alarmSpecial(note, intent, true);
        }
    }

    private void alarmSpecial(Note note, Intent intent, boolean isRepeater) {

        String date = preferences.getString(getString(R.string.PREFS_ALARM_FROM_DATE) + note.getId(), "0");
        String time = preferences.getString(getString(R.string.PREFS_ALARM_WHEN) + note.getId(), "0");
        long timeLongType = Long.parseLong(time);
        int minute = (int) ((timeLongType / 60000) % 60);
        int hour = (int) ((timeLongType / (60 * 60000)) % 24);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(date));
        calendar.add(Calendar.MINUTE, minute);
        calendar.add(Calendar.HOUR_OF_DAY, hour);

        alarm(intent, calendar.getTime().getTime(), (int) note.getId(), isRepeater);
    }

    private void alarm(Intent intent, long setTime, int requestCode, boolean isRepeater) {
        PendingIntent pi = PendingIntent.getBroadcast(this, requestCode, intent, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(setTime);

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
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

}
