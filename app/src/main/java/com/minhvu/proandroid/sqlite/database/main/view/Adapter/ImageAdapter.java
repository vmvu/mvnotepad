package com.minhvu.proandroid.sqlite.database.main.view.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.main.presenter.IImagePresenter;
import com.minhvu.proandroid.sqlite.database.main.view.Fragment.IImageView;
/**
 * Created by vomin on 9/6/2017.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageVH> implements IImageView {
    private Context ctx = null;
    private IImagePresenter presenter;

    public ImageAdapter(Context context, IImagePresenter presenter) {
        this.ctx = context;
        this.presenter = presenter;
        presenter.bindView(this);
    }

    @Override
    public ImageVH onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View layout = inflater.inflate(R.layout.image_small_item, parent, false);
        return new ImageVH(layout);
    }

    @Override
    public void onBindViewHolder(ImageVH holder, int position) {
        presenter.onBindViewHolder(holder.imageView, position);
    }

    @Override
    public int getItemCount() {
        return presenter.getImagesCount();
    }

    @Override
    public Context getActivityContext() {
        return this.ctx;
    }

    @Override
    public void notifyUpdate() {
        this.notifyDataSetChanged();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        presenter = null;
        ctx = null;
    }

    class ImageVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        SimpleDraweeView imageView;

        ImageVH(View itemView) {
            super(itemView);
            imageView = (SimpleDraweeView) itemView.findViewById(R.id.img);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            presenter.onImageClick(getAdapterPosition());
        }
    }
}
