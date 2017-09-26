package com.minhvu.proandroid.sqlite.database.Utils;

import android.support.v4.content.res.TypedArrayUtils;

import java.util.ArrayList;

/**
 * Created by vomin on 9/26/2017.
 */

public class KMPSearch {
    private static int[] bangNext_MP(String pattern){
        int []MP_map = new int[pattern.length() + 1];
        MP_map[0] = -1;
        int i = 0;
        int j = MP_map[i];
        while(i < pattern.length()){
            while(j >= 0 && (pattern.charAt(i) != pattern.charAt(j))) j=  MP_map[j];
            i++;
            j++;
            MP_map[i] = j;
        }
        return MP_map;
    }

    private static int[] bangNext_KMP(String pattern){
        int m = pattern.length();
        int []KMP_map = new int[m + 1];
        int []MP_map = bangNext_MP(pattern);
        KMP_map[0] =-1;
        KMP_map[m] = MP_map[m];
        for(int i = 1; i < m; i++){
            int j=  MP_map[i];
            if(pattern.charAt(i) != pattern.charAt(j))
                KMP_map[i] = j;
            else
                KMP_map[i] = MP_map[j];
        }
        return KMP_map;
    }

    public static int[] KMP(String str, String pattern){

        int []next = bangNext_KMP(pattern);
        int n = str.length();
        int m = pattern.length();
        int i = 0;
        ArrayList<Integer> list = new ArrayList<>();
        for(int j = 0; j < n; j++){
            while((i >= 0 ) && (pattern.charAt(i) != str.charAt(j)))
                i = next[i];
            i++;
            if(i == m){
                list.add(j - m + 1);
                i = next[i];
            }
        }
        if(list.size() == 0){
            return null;
        }
        int[] result = new int[list.size()];
        for(i = 0; i < list.size(); i++){
            result[i] = list.get(i).intValue();
        }
        return result;
    }
}
