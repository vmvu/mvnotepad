package com.minhvu.proandroid.sqlite.database.main.presenter.view;

import android.view.View;

import com.minhvu.proandroid.sqlite.database.main.model.view.IDeleteModel;
import com.minhvu.proandroid.sqlite.database.main.view.Adapter.NoteAdapter2;
import com.minhvu.proandroid.sqlite.database.main.view.Fragment.view.IDeleteView;

/**
 * Created by vomin on 10/16/2017.
 */

public interface IDeletePresenter {
    void onDestroy(boolean isChangingConfiguration);
    void loadData();
    void onBindViewHolder(NoteAdapter2.NoteViewHolder viewHolder, int position);
    void setModel(IDeleteModel model);
    void bindView(IDeleteView view);

    void AdapterOnClick(int position);
    void AdapterLongClick(View view, int position);

    int getDataCount();

    void updateView(int requestCode);
    void updateAdapter();
}
