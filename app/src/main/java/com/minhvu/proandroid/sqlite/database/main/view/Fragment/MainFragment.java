package com.minhvu.proandroid.sqlite.database.main.view.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.minhvu.proandroid.sqlite.database.main.view.Adapter.NoteAdapter;
import com.minhvu.proandroid.sqlite.database.main.view.Fragment.view.IMainView;

/**
 * Created by vomin on 10/7/2017.
 */

public class MainFragment extends AFragment implements IMainView.View, NoteAdapter.INoteAdapter {

    RecyclerView mRecyclerView;
    NoteAdapter mNoteAdapter;
    IMainPresenter mPresenter;

    BroadcastReceiver mSignInOutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!TextUtils.isEmpty(intent.getStringExtra(getString(R.string.sign_out_flag))))
                mPresenter.userSignOutUpdate();
            else {
                mPresenter.userSignInUpdate();
            }
        }
    };


    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivityContext()).unregisterReceiver(mSignInOutReceiver);
        mPresenter.onDestroy(getActivity().isChangingConfigurations());
        mPresenter = null;
        super.onDestroy();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mPresenter = new MainPresenter();
        mPresenter.bindView(this);
        MainModel model = new MainModel(getActivityContext());
        mPresenter.setModel(model);
        model.setPresenter(mPresenter);
        Log.d("Life", "onCreate");
        registerReceiverForSignOutFromUser();
    }

    private void registerReceiverForSignOutFromUser() {
        LocalBroadcastManager.getInstance(getActivityContext()).
                registerReceiver(mSignInOutReceiver, new IntentFilter(getString(R.string.broadcast_sign_out)));
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("Life", "onCreateView");
        View layout = inflater.inflate(R.layout.fragment_main, container, false);

        mRecyclerView = layout.findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mNoteAdapter = new NoteAdapter(this);
        mNoteAdapter.onAttachedToRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(mNoteAdapter);
        return layout;
    }

    @Override
    public void updateAdapter() {
        mNoteAdapter.notifyDataSetChanged();
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
        mPresenter.AdapterOnClick(position);
    }

    @Override
    public void onLongClick(View view, int position) {
        mPresenter.AdapterLongClick(view, position);
    }

    @Override
    public void onBindViewHolder(NoteAdapter.NoteViewHolder holder, int position) {
        mPresenter.onBindViewHolder(holder, position);
    }

    @Override
    public void startActivityResult(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPresenter.updateView(requestCode);
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
        return mPresenter.getDataCount();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.updateView(-1);
        mPresenter.colorSort(-1);
    }

    @Override
    public void colorSort(int position) {
        mPresenter.colorSort(position);
    }

    @Override
    public void alphaSort() {
        mPresenter.alphaSort();
    }

    @Override
    public void colorOrderSort() {
        mPresenter.colorOrderSort();
    }

    @Override
    public void sortByModifiedTime() {
        mPresenter.sortByModifiedTime();
    }

    @Override
    public void sortByImportant() {
        mPresenter.sortByImportant();
    }

}
