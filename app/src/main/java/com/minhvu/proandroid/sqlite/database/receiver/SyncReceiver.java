package com.minhvu.proandroid.sqlite.database.receiver;

import com.minhvu.proandroid.sqlite.database.services.SyncService;

/**
 * Created by vomin on 11/28/2017.
 */

public class SyncReceiver extends ALongRunningReceiver {
    @Override
    public Class getLRSClass() {
        return SyncService.class;
    }
}
