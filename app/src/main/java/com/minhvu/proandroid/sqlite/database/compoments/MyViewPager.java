package com.minhvu.proandroid.sqlite.database.compoments;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

/**
 * Created by vomin on 11/3/2017.
 */

public class MyViewPager extends ViewPager {
    PagerAdapter mPagerAdapter;

    @Override
    public void setAdapter(PagerAdapter adapter) {

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(mPagerAdapter != null){
            super.setAdapter(mPagerAdapter);
        }
    }

    public void storeAdapter(PagerAdapter adapter){
        this.mPagerAdapter = adapter;
        int a =5;
    }

    public MyViewPager(Context context) {
        super(context);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

}
