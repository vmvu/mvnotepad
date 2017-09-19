package com.minhvu.proandroid.sqlite.database.main.model;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;

import com.minhvu.proandroid.sqlite.database.main.presenter.IImagePresenter;
import com.minhvu.proandroid.sqlite.database.models.data.NoteContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vomin on 9/6/2017.
 */

public class ImageModel implements IImageModel {
    private IImagePresenter presenter;
    private ArrayList<String> mImageList;

    public ImageModel() {
        mImageList = new ArrayList<>();
    }


    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        if(!isChangingConfiguration){
            presenter = null;
            mImageList.clear();
        }
    }

    @Override
    public void setPresenter(IImagePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void loadImages(Context context, int noteId) {
        ContentResolver contentResolver = context.getContentResolver();
        String selection = NoteContract.ImageEntry.COL_NOTE_ID + "=" + noteId;
        Cursor c = contentResolver.query(NoteContract.ImageEntry.CONTENT_URI,
                NoteContract.ImageEntry.getColumnNames(), selection, null, null);
        if (c != null && c.moveToFirst()) {
            mImageList.clear();
            int pathPos = c.getColumnIndex(NoteContract.ImageEntry.COL_NAME_PATH);
            do {
                String path = c.getString(pathPos);
                mImageList.add(path);
                //mBitmapList.add(rotateImage(path, 90));
            } while (c.moveToNext());
        }
        c.close();
    }
    private Bitmap rotateImage(String path, float angle)  {
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
    public void insertImage(Context context, String imagePath, int noteId) {
        Log.d("addImage", "model: vao day");
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put(NoteContract.ImageEntry.COL_NAME_PATH, imagePath);
        cv.put(NoteContract.ImageEntry.COL_NOTE_ID, noteId);
        Uri success = contentResolver.insert(NoteContract.ImageEntry.CONTENT_URI, cv);
        if (success != null) {
            if(mImageList == null){
                mImageList = new ArrayList<>();
            }
            mImageList.add(imagePath);
            Log.d("addImage", "size_list:" + mImageList.size());
            if(mImageList.size() > 0){
                presenter.notifyView();
            }
        }
    }

    @Override
    public void deleteImage(Context context, String imagePath, int position) {
        String[] temp = imagePath.split("/");
        Uri uri = Uri.withAppendedPath(NoteContract.ImageEntry.CONTENT_URI, temp[temp.length - 1]);
        String selection = NoteContract.ImageEntry.COL_NAME_PATH + "='" + imagePath + "'";
        ContentResolver contentResolver = context.getContentResolver();
        int success = contentResolver.delete(uri, selection, null);
        if (success > 0) {
            mImageList.remove(position);
            presenter.notifyView();
        }
    }



    @Override
    public boolean deleteAllImages(Context context, int noteId) {
        ContentResolver contentResolver = context.getContentResolver();
        String selection = NoteContract.ImageEntry.COL_NOTE_ID + "=" + noteId;
        int success = contentResolver.delete(NoteContract.ImageEntry.CONTENT_URI, selection, null);
        if (success > 0) {
            mImageList.clear();
            return true;
        }
        return false;
    }

    @Override
    public int getCount() {
        if(mImageList == null){
            return 0;
        }
        return mImageList.size();
    }


}
