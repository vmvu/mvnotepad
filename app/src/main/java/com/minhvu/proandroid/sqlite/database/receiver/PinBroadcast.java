package com.minhvu.proandroid.sqlite.database.receiver;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.text.TextUtils;

import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.main.view.Activity.GetShareActivity;

import java.text.ParseException;

/**
 * Created by vomin on 8/11/2017.
 */

public class PinBroadcast extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        Uri uri = Uri.parse(intent.getStringExtra(context.getResources().getString(R.string.notify_note_uri)));
        if (uri == null)
            return;
        String id = uri.getPathSegments().get(1);

        SharedPreferences pref = context.getSharedPreferences(
                context.getString(R.string.PREFS_ALARM_FILE), Context.MODE_PRIVATE);
        String switchType = pref.getString(context.getString(R.string.PREFS_ALARM_SWITCH_KEY) + id, "");
        if (switchType.equals("scAllDay")) {

            String toDate = pref.getString(context.getString(R.string.PREFS_ALARM_TO_DATE) + id, "0");
            long toDateLongType = Long.parseLong(toDate.trim());

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            long currentTime = 0;
            try {
                currentTime = dateFormat.parse(dateFormat.format(System.currentTimeMillis())).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (toDateLongType < currentTime) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(context.getString(R.string.PREFS_ALARM_SWITCH_KEY) + id, "scReset");
                PendingIntent pi = PendingIntent.getBroadcast(context, Integer.parseInt(id), intent, 0);
                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                am.cancel(pi);
                return;
            }

        }
        changeToSCResetState(context, uri.getPathSegments().get(1).trim());
        sendNotification(context, intent, uri);

    }

    private void changeToSCResetState(Context context, String id) {
        SharedPreferences pref = context
                .getSharedPreferences(context.getString(R.string.PREFS_ALARM_FILE), Context.MODE_PRIVATE);
        String switchType = pref.getString(context.getString(R.string.PREFS_ALARM_SWITCH_KEY) + id, "");
        if (TextUtils.isEmpty(switchType)) {
            return;
        }
        if (switchType.equals(context.getString(R.string.type_of_switch_15min)) ||
                switchType.equals(context.getString(R.string.type_of_switch_30min)) ||
                switchType.equals(context.getString(R.string.type_of_switch_at_time))) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(
                    context.getString(R.string.PREFS_ALARM_SWITCH_KEY) + id, context.getString(R.string.type_of_switch_reset));
            editor.apply();
        }
    }

    private void sendNotification(Context ctx, Intent intent, Uri uri) {
        String title = intent.getStringExtra(ctx.getString(R.string.notify_note_title));
        boolean onGoing = intent.getBooleanExtra(ctx.getString(R.string.notify_note_pin), false);
        Bitmap icon = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_search_black_24dp);
        int id = Integer.parseInt(uri.getPathSegments().get(1).trim());
        Intent i = new Intent(ctx, GetShareActivity.class);
        i.setData(uri);
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, id, i, 0);

        Notification.Builder builder = new Notification.Builder(ctx)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_notes)
                .setTicker(title)
                .setLargeIcon(icon)
                .setOngoing(onGoing)
                .setAutoCancel(!onGoing)
                .setContentIntent(pendingIntent)
                .setContentTitle(title);

        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(id, builder.build());
    }


}
