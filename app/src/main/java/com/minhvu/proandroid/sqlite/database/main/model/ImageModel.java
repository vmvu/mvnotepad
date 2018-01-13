package com.minhvu.proandroid.sqlite.database.main.model;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;

import com.minhvu.proandroid.sqlite.database.main.model.view.IImageModel;
import com.minhvu.proandroid.sqlite.database.main.presenter.view.IImagePresenter;
import com.minhvu.proandroid.sqlite.database.models.DAO.ImageDAO;
import com.minhvu.proandroid.sqlite.database.models.data.ImageContract;
import com.minhvu.proandroid.sqlite.database.models.entity.Image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by vomin on 9/6/2017.
 */

public class ImageModel implements IImageModel {
    private IImagePresenter presenter;
    private List<String> mImageList;
    private HashMap<String, Bitmap> mBitmap;
    private HashMap<String, Bitmap> mSmallBitmap;

    private ImageDAO mImageDAO;

    public ImageModel(Context context) {
        mImageList = new ArrayList<>();
        mImageDAO = new ImageDAO(context);
    }

    public ImageModel(Context context, int noteID) {
        mImageList = new ArrayList<>();
        mImageDAO = new ImageDAO(context);
        loadImages(noteID);
    }


    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        if (!isChangingConfiguration) {
            presenter = null;
            mImageList.clear();
        }
    }

    @Override
    public void setPresenter(IImagePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void loadImages(long noteId) {
        mImageList = mImageDAO.loadImagePathListOfNote(noteId);
    }

    private Bitmap rotateImage(String path, float angle) {
        Uri uri = Uri.parse(path);
       /* File file = new File(uri.getPath());
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        }catch (FileNotFoundException e){
            e.getStackTrace();
        }*/
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 3;
        Bitmap source = BitmapFactory.decodeFile(uri.getPath(), options);
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }


    @Override
    public List<String> getImageList() {
        return mImageList;
    }

    @Override
    public String getImage(int position) {
        return mImageList.get(position);
    }


    @Override
    public void insertImage(String imagePath, long noteId) {
        Image image = new Image(imagePath, 0, noteId);
        if (mImageDAO.insertItem(image)) {
            mImageList.add(imagePath);
            presenter.notifyView();
        }
    }

    @Override
    public void deleteImage(String imagePath, int position) {
        Image image = new Image(imagePath, -1);
        if (mImageDAO.updateByPath(image)) {
            mImageList.remove(position);
            presenter.notifyView();
            removeImageLocal(imagePath);
        }
    }


    private void removeImageLocal(final String imagePath) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Uri uri = Uri.parse(imagePath);
                File file = new File((uri.getPath()));
                if (file.exists()) {
                    if (file.delete()) {
                        Log.d("delete-image", "success");
                    }
                }
            }
        };
        thread.start();
    }

    @Override
    public boolean deleteAllImages(long noteId) {
        Image image = new Image(null, -1);
        if (mImageDAO.updateByNoteID(image)) {
            for (String path : mImageList) {
                removeImageLocal(path);
            }
            mImageList.clear();
            return true;
        }
        return false;
    }

    @Override
    public int getCount() {
        if (mImageList == null) {
            return 0;
        }
        return mImageList.size();
    }

    @Override
    public Bitmap getBitmapImage(String path) {
        if (mBitmap == null) {
            mBitmap = new HashMap<>();
        }
        Bitmap bitmap = mBitmap.get(path);
        if (bitmap == null) {
            try {
                bitmap = loadFile(path);
            } catch (FileNotFoundException e) {
                e.getStackTrace();
            }
            mBitmap.put(path, bitmap);
        }
        return bitmap;
    }

    @Override
    public void updateBitmapImage(Bitmap bitmap, String path) {
        mBitmap.put(path, bitmap);
    }

    private Bitmap loadFile(String path) throws FileNotFoundException {
        Uri uri = Uri.parse(path);
        File file = new File(uri.getPath());
        FileInputStream fis = new FileInputStream(file);
        return BitmapFactory.decodeStream(fis);
    }

    @Override
    public Bitmap getSmallBitmapImage(String path) {
        if (mSmallBitmap == null) {
            mSmallBitmap = new HashMap<>();
        }
        Bitmap bitmap = mSmallBitmap.get(path);
        if (bitmap == null) {
            bitmap = getBitmapImage(path);
            bitmap = resizeImage(bitmap);
            mSmallBitmap.put(path, bitmap);
        }
        return bitmap;
    }

    private Bitmap resizeImage(Bitmap bitmap) {
        /*ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        return BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size());*/
        return bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 10, (int) bitmap.getHeight() / 10, true);
    }

    @Override
    public void updateSmallBitmap(String path) {
        Bitmap bitmap = getBitmapImage(path);
        mSmallBitmap.put(path, resizeImage(bitmap));
    }
}
