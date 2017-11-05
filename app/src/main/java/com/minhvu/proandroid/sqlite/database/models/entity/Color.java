package com.minhvu.proandroid.sqlite.database.models.entity;

import android.content.Context;
import android.util.Log;

import com.minhvu.proandroid.sqlite.database.R;

import java.util.ArrayList;
import java.util.List;

public class Color {
    public int getHeaderColor() {
        return headerColor;
    }

    private void setHeaderColor(int headerColor) {
        this.headerColor = headerColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    private void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    private int headerColor;
    private int backgroundColor;

    public static List<Color> getColors(Context context){
        ArrayList<Color> colors = new ArrayList<>();
        int[] headerColors = context.getResources().getIntArray(R.array.header_color);
        int[] backgroundColors = context.getResources().getIntArray(R.array.background_color);
        for(int i = 0 ; i < headerColors.length; i++){
            Log.d("Color_list:", i + " - " + headerColors[i]);
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
        color.setHeaderColor(headerColors[pos]);
        color.setBackgroundColor(backgroundColors[pos]);
        return color;
    }

    public static int getColorPos(Context ctx, int idColor){
        int[] headerColors = ctx.getResources().getIntArray(R.array.header_color);
        for(int i = 0; i < headerColors.length; i++){
            if(headerColors[i] == idColor)
                return i;
        }
        return -1;
    }

}
