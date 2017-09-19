package com.minhvu.proandroid.sqlite.database.receiver;

import com.minhvu.proandroid.sqlite.database.services.AlarmRebootService;

/**
 * Created by vomin on 9/16/2017.
 */

public class HandlerRebootReceiver extends ALongRunningReceiver {
    @Override
    public Class getLRSClass() {
        return AlarmRebootService.class;
    }
}
