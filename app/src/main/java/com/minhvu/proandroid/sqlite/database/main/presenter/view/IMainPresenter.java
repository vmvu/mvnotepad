package com.minhvu.proandroid.sqlite.database.main.presenter.view;

import android.view.View;
import android.widget.ImageButton;

import com.minhvu.proandroid.sqlite.database.main.model.view.IMainModel;
import com.minhvu.proandroid.sqlite.database.main.view.Adapter.NoteAdapter2;
import com.minhvu.proandroid.sqlite.database.main.view.Fragment.view.IMainView;

/**
 * Created by vomin on 10/7/2017.
 */

public interface IMainPresenter {
    void onDestroy(boolean isChangingConfiguration);
    void loadData();
    void onBindViewHolder(NoteAdapter2.NoteViewHolder viewHolder, int position);
    void setModel(IMainModel model);
    void bindView(IMainView.View view);

    void AdapterOnClick(int position);
    void AdapterLongClick(View view, int position);

    int getDataCount();

    void updateView(int requestCode);
    void updateAdapter();

    void colorSort(int colorPos);

    void alphaSort();
    void colorOrderSort();
    void sortByModifiedTime();
    void sortByImportant();

}
