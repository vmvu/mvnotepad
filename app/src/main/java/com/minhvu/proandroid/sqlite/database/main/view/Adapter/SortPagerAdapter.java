package com.minhvu.proandroid.sqlite.database.main.view.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.minhvu.proandroid.sqlite.database.main.view.Activity.view.SortView;
import com.minhvu.proandroid.sqlite.database.main.view.Fragment.ColorSortWindow;

/**
 * Created by vomin on 11/1/2017.
 */

public class SortPagerAdapter extends FragmentStatePagerAdapter {

    private int mNumOfPages;
    private SortView sortView;

    public SortPagerAdapter(FragmentManager fm, SortView sortView) {
        super(fm);
        this.sortView = sortView;
    }

    public SortPagerAdapter(FragmentManager fm, int mNumOfPages, SortView sortView) {
        super(fm);
        this.sortView = sortView;
        this.mNumOfPages = mNumOfPages;

    }

    @Override
    public Fragment getItem(int position) {
        return new ColorSortWindow(sortView);
    }

    @Override
    public int getCount() {
        return 1;
    }


    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Color";
        }
        return null;
    }
}
