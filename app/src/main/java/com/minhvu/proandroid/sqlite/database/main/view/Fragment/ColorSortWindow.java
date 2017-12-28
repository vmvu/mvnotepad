package com.minhvu.proandroid.sqlite.database.main.view.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.main.view.Adapter.ColorAdapter;
import com.minhvu.proandroid.sqlite.database.main.view.Activity.view.SortView;

/**
 * Created by vomin on 10/31/2017.
 */

public class ColorSortWindow extends Fragment {
    RecyclerView mRecyclerView;
    ColorAdapter mColorAdapter;
    SortView mSortView = null;

    private final int RESET_SORT = -1;

    public ColorSortWindow(SortView sortView){
        this.mSortView = sortView;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.color_sort_pager, container, false);
        Button btnResetSort = (Button) layout.findViewById(R.id.btnResetSort);
        btnResetSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSortView.colorSort(RESET_SORT);
            }
        });
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        mRecyclerView = (RecyclerView) layout.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(layoutManager);

        mColorAdapter = new ColorAdapter(getActivity(), new ColorAdapter.IColorAdapter() {
            @Override
            public void onClick(int colorPos) {
                mSortView.colorSort(colorPos);
            }
        });
        mRecyclerView.setAdapter(mColorAdapter);
        return layout;
    }


}
