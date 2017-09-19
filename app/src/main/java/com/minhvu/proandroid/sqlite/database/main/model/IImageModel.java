package com.minhvu.proandroid.sqlite.database.main.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.minhvu.proandroid.sqlite.database.main.presenter.IImagePresenter;

import java.util.List;

/**
 * Created by vomin on 9/6/2017.
 */

public interface IImageModel {
    void onDestroy(boolean isChangingConfiguration);
    void setPresenter(IImagePresenter presenter);
    void loadImages(Context context, int noteId);
    List<String> getImageList();
    String getImage(int position);

    void insertImage(Context context,String imagePath, int noteId);
    void deleteImage(Context context, String imagePath, int position);
    boolean deleteAllImages(Context context, int noteId);

    int getCount();
}
