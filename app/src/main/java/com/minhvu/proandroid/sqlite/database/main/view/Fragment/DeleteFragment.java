package com.minhvu.proandroid.sqlite.database.main.view.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.main.model.DeleteModel;
import com.minhvu.proandroid.sqlite.database.main.model.view.IDeleteModel;
import com.minhvu.proandroid.sqlite.database.main.presenter.DeletePresenter;
import com.minhvu.proandroid.sqlite.database.main.presenter.view.IDeletePresenter;
import com.minhvu.proandroid.sqlite.database.main.view.Adapter.NoteAdapter2;
import com.minhvu.proandroid.sqlite.database.main.view.Fragment.view.IDeleteView;

/**
 * Created by vomin on 10/16/2017.
 */

public class DeleteFragment extends AFragment implements IDeleteView, NoteAdapter2.INoteAdapter {

    RecyclerView mRecyclerView;
    NoteAdapter2 mNoteAdapter;
    IDeletePresenter mPresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new DeletePresenter();
        mPresenter.bindView(this);
        IDeleteModel model = new DeleteModel(getActivityContext());
        mPresenter.setModel(model);
        model.setPresenter(mPresenter);
        mPresenter.loadData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_main, container, false);

        mRecyclerView = (RecyclerView) layout.findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mNoteAdapter = new NoteAdapter2(this);
        mNoteAdapter.onAttachedToRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(mNoteAdapter);
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.isThereANewData();
    }

    @Override
    public Context getActivityContext() {
        return getActivity();
    }

    @Override
    public Context getBaseContext() {
        return getActivity().getBaseContext();
    }

    @Override
    public Context getAppContext() {
        return getActivity().getApplicationContext();
    }

    @Override
    public void startActivityResult(Intent intent, int requestCode) {

    }

    @Override
    public void showToast(Toast toast) {
        toast.show();
    }

    @Override
    public void showDialog(AlertDialog dialog) {
        dialog.show();
    }

    @Override
    public void updateAdapter() {
        mNoteAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateViewAtPosition(int position) {
        mNoteAdapter.notifyItemChanged(position);
    }

    @Override
    public DisplayMetrics getDimensionOnScreen() {
        return null;
    }

    @Override
    public void onClick(View view, int position) {
        mPresenter.AdapterOnClick(position);
    }

    @Override
    public void onLongClick(View view, int position) {
        mPresenter.AdapterLongClick(view, position);
    }

    @Override
    public void onBindViewHolder(NoteAdapter2.NoteViewHolder holder, int position) {
        mPresenter.onBindViewHolder(holder, position);
    }

    @Override
    public int getDataCount() {
        return mPresenter.getDataCount();
    }



    @Override
    public void onDestroy() {
        mPresenter.onDestroy(getActivity().isChangingConfigurations());
        mPresenter = null;
        super.onDestroy();
    }

    @Override
    public void colorSort(int position) {

    }

    @Override
    public void alphaSort() {

    }

    @Override
    public void colorOrderSort() {

    }

    @Override
    public void sortByModifiedTime() {

    }

    @Override
    public void sortByImportant() {

    }
}
