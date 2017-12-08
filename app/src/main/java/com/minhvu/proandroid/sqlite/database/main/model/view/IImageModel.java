package com.minhvu.proandroid.sqlite.database.main.model.view;

import android.content.Context;
import android.graphics.Bitmap;

import com.minhvu.proandroid.sqlite.database.main.presenter.view.IImagePresenter;

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

    Bitmap getBitmapImage(String path);
    void updateBitmapImage(Bitmap bitmap, String path);
    Bitmap getSmallBitmapImage(String path);
    void updateSmallBitmap(String path);
    int getCount();
}
