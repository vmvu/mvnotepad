package com.minhvu.proandroid.sqlite.database.receiver;

import com.minhvu.proandroid.sqlite.database.services.SignInService;

/**
 * Created by vomin on 1/9/2018.
 */

public class SignInReceiver extends ALongRunningReceiver {
    @Override
    public Class getLRSClass() {
        return SignInService.class;
    }
}
