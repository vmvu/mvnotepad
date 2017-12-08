package com.minhvu.proandroid.sqlite.database.main.presenter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;

import com.facebook.drawee.view.SimpleDraweeView;
import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.main.model.view.IImageModel;
import com.minhvu.proandroid.sqlite.database.main.presenter.view.IImagePresenter;
import com.minhvu.proandroid.sqlite.database.main.view.Adapter.ImageAdapter;
import com.minhvu.proandroid.sqlite.database.main.view.Fragment.view.IDetailFragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class ImagePresenter extends MvpPresenter<IImageModel, IDetailFragment.ImageView> implements IImagePresenter {

    @Override
    public int getImagesCount() {
        return model.getCount();
    }

    @Override
    public void onBindViewHolder(final ImageAdapter.ImageViewHolder holder, final int position) {
        final String path = model.getImage(position);
     /*   Bitmap smallImage = null;
        smallImage  = resizeImage(uri);*/

        new AsyncTask<String, Bitmap, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                Bitmap bitmap = model.getSmallBitmapImage(params[0]);
                publishProgress(bitmap);
                return null;
            }

            @Override
            protected void onProgressUpdate(Bitmap... values) {
                super.onProgressUpdate(values);
                holder.imageView.setImageBitmap(values[0]);
            }
        }.execute(path);


    }




    @Override
    public void onImageClick(final int position) {
        final String path = model.getImage(position);
        LayoutInflater inflater = LayoutInflater.from(getView().getActivityContext());
        View layout = inflater.inflate(R.layout.image_item, null);
        final SimpleDraweeView imageView = (SimpleDraweeView) layout.findViewById(R.id.img);
        final ImageButton btnRotate = (ImageButton) layout.findViewById(R.id.btnRotate);
        final Bitmap bitmap = model.getBitmapImage(path);
        imageView.setImageBitmap(bitmap);
        btnRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap temp = model.getBitmapImage(path);
                temp = rotateImage(temp, 90);
                imageView.setImageBitmap(temp);
                model.updateBitmapImage(temp, path);
                model.updateSmallBitmap(path);
                getView().notifyUpdateItemChang(position);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getView().getActivityContext());
        builder.setView(layout);
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    model.deleteImage(getView().getActivityContext(), path, position);
                } finally {
                    dialog.dismiss();
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private Bitmap rotateImage(Bitmap bitmap, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    @Override
    public void deleteAllImage(Context context, int noteID) {
        model.deleteAllImages(context, noteID);
    }

    @Override
    public void onLoadImages(Context context, Uri noteUri) {
        if (noteUri == null) {
            return;
        }
        int noteId = Integer.parseInt(noteUri.getPathSegments().get(1));
        model.loadImages(context, noteId);
        /*if(model.getCount() > 0){
            getView().notificationUpdate();
        }*/
    }

    @Override
    public void addImage(String path, Uri noteUri) {
        if (noteUri == null) {
            return;
        }
        int noteId = Integer.parseInt(noteUri.getPathSegments().get(1));
        model.insertImage(getView().getActivityContext(), path, noteId);
    }

    @Override
    public void notifyView() {
        getView().notifyUpdate();
    }

    @Override
    protected void updateView() {

    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        unbindView();
        model.onDestroy(isChangingConfiguration);
        if (!isChangingConfiguration) {
            model = null;
        }
    }
}
