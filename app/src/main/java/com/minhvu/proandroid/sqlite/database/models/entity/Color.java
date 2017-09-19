package com.minhvu.proandroid.sqlite.database.models.entity;

import android.content.Context;
import android.util.Log;

import com.minhvu.proandroid.sqlite.database.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by vomin on 8/10/2017.
 */

public class Color {
    public int getHeaderColor() {
        return headerColor;
    }

    public void setHeaderColor(int headerColor) {
        this.headerColor = headerColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    int headerColor;
    int backgroundColor;

    public static List<Color> getColors(Context context){
        HashMap<Integer, Integer> pinColor = new HashMap<>();
        ArrayList<Color> colors = new ArrayList<>();
        int[] headerColors = context.getResources().getIntArray(R.array.header_color);
        int[] backgroundColors = context.getResources().getIntArray(R.array.background_color);
        for(int i = 0 ; i < headerColors.length; i++){
            Color color = new Color();
            color.setHeaderColor(headerColors[i]);
            color.setBackgroundColor(backgroundColors[i]);
            colors.add(color);
        }
        return colors;
    }

    public static Color getColor(Context ctx, int pos){
        int[] headerColors = ctx.getResources().getIntArray(R.array.header_color);
        int[] backgroundColors = ctx.getResources().getIntArray(R.array.background_color);
        Color color = new Color();
        Log.d("color pos", pos + "");
        color.setHeaderColor(headerColors[pos]);
        color.setBackgroundColor(backgroundColors[pos]);
        return color;
    }

}
