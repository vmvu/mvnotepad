package com.minhvu.proandroid.sqlite.database.main.view.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.main.view.Activity.view.SortView;

/**
 * Created by vomin on 11/25/2017.
 */

public class SortWindow extends Fragment {
    private SortView sortView;
    public SortWindow(SortView sortView){
        this.sortView = sortView;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.sort_pager, container, false);
        ImageButton btnAlphaSort = (ImageButton) layout.findViewById(R.id.btn_Alpha_Sort);
        btnAlphaSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortView.alphaSort();
            }
        });

        ImageButton btnModifiedTimeSort = (ImageButton) layout.findViewById(R.id.btn_Modified_Time_Sort);
        btnModifiedTimeSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortView.modifiedTimeSort();
            }
        });

        ImageButton btnSortByColorOrder = (ImageButton) layout.findViewById(R.id.btn_sort_by_color_order);
        btnSortByColorOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortView.colorOrderSort();
            }
        });

        ImageButton btnSortByImportant = (ImageButton) layout.findViewById(R.id.btn_sort_by_important);
        btnSortByImportant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortView.sortByImportant();
            }
        });



        return layout;
    }
}
