package com.minhvu.proandroid.sqlite.database.main.presenter.view;

import android.content.Context;
import android.net.Uri;

import com.minhvu.proandroid.sqlite.database.main.model.view.IImageModel;
import com.minhvu.proandroid.sqlite.database.main.view.Adapter.ImageAdapter;
import com.minhvu.proandroid.sqlite.database.main.view.Fragment.view.IDetailFragment;

/**
 * Created by vomin on 9/6/2017.
 */

public interface IImagePresenter {
    int getImagesCount();
    void setModel(IImageModel model);
    void bindView(IDetailFragment.ImageView view);
    void onBindViewHolder(ImageAdapter.ImageViewHolder holder, int position);
    void onImageClick(int position);

    void onLoadImages(Context context, Uri noteUri);
    void addImage(String path, Uri noteUri);
    void onDestroy(boolean isChangingConfiguration);
    void notifyView();

    void deleteAllImage(Context context, int position);
}
