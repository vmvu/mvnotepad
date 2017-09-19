package com.minhvu.proandroid.sqlite.database.main.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.widget.ImageView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.minhvu.proandroid.sqlite.database.main.model.IImageModel;
import com.minhvu.proandroid.sqlite.database.main.view.Adapter.ImageAdapter;
import com.minhvu.proandroid.sqlite.database.main.view.Fragment.IImageView;

/**
 * Created by vomin on 9/6/2017.
 */

public interface IImagePresenter {
    int getImagesCount();
    void setModel(IImageModel model);
    void bindView(IImageView view);
    void onBindViewHolder(SimpleDraweeView imageView, int position);
    void onImageClick(int position);

    void onLoadImages(Context context, Uri noteUri);
    void addImage(String path, Uri noteUri);
    void onDestroy(boolean isChangingConfiguration);
    void notifyView();
}
