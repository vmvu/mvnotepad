package com.minhvu.proandroid.sqlite.database.main.view.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.main.model.MainModel;
import com.minhvu.proandroid.sqlite.database.main.presenter.view.IMainPresenter;
import com.minhvu.proandroid.sqlite.database.main.presenter.MainPresenter;
import com.minhvu.proandroid.sqlite.database.main.view.Adapter.NoteAdapter2;
import com.minhvu.proandroid.sqlite.database.main.view.Fragment.view.IMainView;

/**
 * Created by vomin on 10/7/2017.
 */

public class MainFragment extends AFragment implements IMainView.View, NoteAdapter2.INoteAdapter {

    RecyclerView recyclerView;
    NoteAdapter2 noteAdapter;
    IMainPresenter presenter;


    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy(getActivity().isChangingConfigurations());
        presenter = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        presenter = new MainPresenter();
        presenter.bindView(this);
        MainModel model = new MainModel(getActivityContext());
        presenter.setModel(model);
        model.setPresenter(presenter);
        Log.d("Life", "onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("Life", "onCreateView");
        View layout = inflater.inflate(R.layout.fragment_main, container, false);

        recyclerView = (RecyclerView) layout.findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);
        noteAdapter = new NoteAdapter2(this);
        noteAdapter.onAttachedToRecyclerView(recyclerView);
        int count = presenter.getDataCount();
        recyclerView.setAdapter(noteAdapter);
        return layout;
    }

    @Override
    public void updateAdapter() {
        noteAdapter.notifyDataSetChanged();
    }

    @Override
    public Context getActivityContext() {
        return getActivity();
    }

    @Override
    public Context getAppContext() {
        return getActivity().getApplicationContext();
    }

    @Override
    public void onClick(View view, int position) {
        presenter.AdapterOnClick(position);
    }

    @Override
    public void onLongClick(View view, int position) {
        presenter.AdapterLongClick(view, position);
    }

    @Override
    public void onBindViewHolder(NoteAdapter2.NoteViewHolder holder, int position) {
        presenter.onBindViewHolder(holder, position);
    }

    @Override
    public void startActivityResult(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        presenter.updateView(requestCode);
    }

    @Override
    public void showToast(Toast toast) {
        toast.show();
    }

    @Override
    public DisplayMetrics getDimensionOnScreen() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    @Override
    public int getDataCount() {
        return presenter.getDataCount();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.updateView(-1);
        presenter.colorSort(-1);
    }

    @Override
    public void colorSort(int position) {
        presenter.colorSort(position);
    }
}
