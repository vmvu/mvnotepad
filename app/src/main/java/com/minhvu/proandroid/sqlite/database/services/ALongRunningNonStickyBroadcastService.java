package com.minhvu.proandroid.sqlite.database.services;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by vomin on 8/7/2017.
 */

public abstract class ALongRunningNonStickyBroadcastService extends IntentService {
    private static final String LOGTAG = ALongRunningNonStickyBroadcastService.class.getSimpleName();
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */

    public ALongRunningNonStickyBroadcastService(String name) {
        super(name);
    }
    public abstract void handIntentBroadcast(Intent intentBroadcast);

    @Override
    public void onCreate() {
        super.onCreate();

        LightedGreenRoom.setup(getApplicationContext());
        LightedGreenRoom.s_registerClient();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        LightedGreenRoom.s_enter();
        return Service.START_NOT_STICKY;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try{
            Intent broadcastIntent = intent.getParcelableExtra("original_intent");
            handIntentBroadcast(broadcastIntent);
        }finally {
            LightedGreenRoom.s_leave();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LightedGreenRoom.s_unregisterClient();
    }
}
