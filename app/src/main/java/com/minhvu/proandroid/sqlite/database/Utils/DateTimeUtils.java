package com.minhvu.proandroid.sqlite.database.Utils;

import android.icu.text.SimpleDateFormat;

import java.text.ParseException;

/**
 * Created by vomin on 8/25/2017.
 */

public class DateTimeUtils {
    public static String longToStringDate(long timeMillis){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(timeMillis);
    }

    public static long stringToLongDate(String timeString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try{
            return dateFormat.parse(timeString).getTime();
        }catch (ParseException e){
            return 0;
        }
    }


}
