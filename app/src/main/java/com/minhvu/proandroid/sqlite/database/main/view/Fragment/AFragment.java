package com.minhvu.proandroid.sqlite.database.main.view.Fragment;

import android.support.v4.app.Fragment;

/**
 * Created by vomin on 11/1/2017.
 */

public abstract class AFragment extends Fragment {
    public abstract void colorSort(int position);

    public abstract void alphaSort();
    public abstract void colorOrderSort();
    public abstract void sortByModifiedTime();
    public abstract void sortByImportant();
}
