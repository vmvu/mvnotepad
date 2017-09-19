package com.minhvu.proandroid.sqlite.database.main.view.Component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by vomin on 8/7/2017.
 */

public class LinedEditText extends android.widget.EditText {
    private Rect mRect;
    private Paint mPaint;

    public LinedEditText(Context context) {
        super(context);
        mRect = new Rect();
        mPaint = new Paint();

        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(Color.BLACK);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int height = getHeight();
        int lHeight = getLineHeight();

        int count = height/lHeight;
        if(getLineCount() > count){
            count = getLineCount();
        }
        Rect r = mRect;
        Paint paint = mPaint;

        int baseline = getLineBounds(0, r);

        for(int i = 0; i < count; i++){
            canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
            baseline += getLineHeight();
        }
        super.onDraw(canvas);
    }
}
