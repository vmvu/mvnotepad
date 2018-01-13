package com.minhvu.proandroid.sqlite.database.models.data;

import android.net.Uri;

/**
 * Created by vomin on 1/12/2018.
 */

interface Contract {
    String AUTHORITY = "com.minhvu.proandroid.sqlite.database";
    Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
}
