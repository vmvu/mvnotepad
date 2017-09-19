package com.minhvu.proandroid.sqlite.database.main.presenter;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.facebook.drawee.view.SimpleDraweeView;
import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.main.model.IImageModel;
import com.minhvu.proandroid.sqlite.database.main.view.Fragment.IImageView;

/**
 * Created by vomin on 9/6/2017.
 */

public class ImagePresenter extends MvpPresenter<IImageModel, IImageView> implements IImagePresenter {

    @Override
    public int getImagesCount() {
        return model.getCount();
    }


    @Override
    public void onBindViewHolder(final SimpleDraweeView imageView, final int position) {
        String path = model.getImage(position);
        Uri uri  = Uri.parse(path);
        imageView.setImageURI(uri);
    }


    @Override
    public void onImageClick(final int position) {
        final String path = model.getImage(position);
        LayoutInflater inflater = LayoutInflater.from(getView().getActivityContext());
        View layout = inflater.inflate(R.layout.image_item, null);
        SimpleDraweeView imageView = (SimpleDraweeView) layout.findViewById(R.id.img);
        Uri uri  = Uri.parse(path);
        imageView.setImageURI(uri);

        AlertDialog.Builder builder = new AlertDialog.Builder(getView().getActivityContext());
        builder.setView(layout);
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                model.deleteImage(getView().getActivityContext(), path, position);
                dialog.dismiss();
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
    private Bitmap resizeImage(Bitmap source){
       return Bitmap.createScaledBitmap(source,(int)(source.getWidth() * 0.4), (int)(source.getHeight()*0.4), true);
    }



}
