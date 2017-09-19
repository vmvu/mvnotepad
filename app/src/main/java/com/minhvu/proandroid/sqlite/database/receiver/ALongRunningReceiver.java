package com.minhvu.proandroid.sqlite.database.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.minhvu.proandroid.sqlite.database.services.LightedGreenRoom;

/**
 * Created by vomin on 8/7/2017.
 */

public abstract class ALongRunningReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LightedGreenRoom.setup(context);
        startService(context, intent);
    }
    private void startService(Context context, Intent intent){
        Intent serviceIntent = new Intent(context, getLRSClass());
        serviceIntent.putExtra("original_intent", intent);
        context.startService(serviceIntent);
    }
    public abstract Class getLRSClass();
}
