package com.minhvu.proandroid.sqlite.database.main.view.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

public class DeleteFragment extends Fragment implements IDeleteView, NoteAdapter2.INoteAdapter {

    RecyclerView recyclerView;
    NoteAdapter2 noteAdapter;
    IDeletePresenter presenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new DeletePresenter();
        presenter.bindView(this);
        IDeleteModel model = new DeleteModel();
        presenter.setModel(model);
        model.setPresenter(presenter);
        presenter.loadData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_main, container, false);

        recyclerView = (RecyclerView) layout.findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);
        noteAdapter = new NoteAdapter2(this);
        noteAdapter.onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(noteAdapter);
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.updateCountList();
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
        noteAdapter.notifyDataSetChanged();
    }

    @Override
    public DisplayMetrics getDimensionOnScreen() {
        return null;
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
    public int getDataCount() {
        return presenter.getDataCount();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy(getActivity().isChangingConfigurations());
        presenter = null;
    }
}
