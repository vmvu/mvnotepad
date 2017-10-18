package com.minhvu.proandroid.sqlite.database.main.view.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.minhvu.proandroid.sqlite.database.R;

/**
 * Created by vomin on 9/6/2017.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
   private IImageAdapter adapterEvent;

    public interface IImageAdapter{
        void onClick(View view, int position);
        void onBindViewHolder(ImageViewHolder holder, int position);
        int getDataCount();
        View onCreateViewHolder(ViewGroup parent);
    }

    public ImageAdapter(ImageAdapter.IImageAdapter imageAdapter){
        this.adapterEvent = imageAdapter;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageViewHolder(adapterEvent.onCreateViewHolder(parent));
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        adapterEvent.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return adapterEvent.getDataCount();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public SimpleDraweeView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = (SimpleDraweeView) itemView.findViewById(R.id.img);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            adapterEvent.onClick(v, getAdapterPosition());
        }
    }
}
