package com.minhvu.proandroid.sqlite.database.compoments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.main.view.Activity.view.SortView;
import com.minhvu.proandroid.sqlite.database.main.view.Adapter.SortPagerAdapter;

/**
 * Created by vomin on 11/3/2017.
 */

public class PagerDialog extends DialogFragment {
    SortView sortView;
    public PagerDialog(SortView sortView){
        this.sortView = sortView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.alert_sort, container, false);
        TabLayout tabLayout = (TabLayout) layout.findViewById(R.id.tabLayout);
        ViewPager viewPager = (ViewPager) layout.findViewById(R.id.viewPager);
        tabLayout.setupWithViewPager(viewPager);
        SortPagerAdapter adapter = new SortPagerAdapter(getChildFragmentManager(), sortView);
        viewPager.setAdapter(adapter);
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        window.setLayout(650, 800);
        window.setGravity(Gravity.CENTER);
    }


}
