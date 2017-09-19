package com.minhvu.proandroid.sqlite.database.main.view.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.models.entity.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vomin on 8/10/2017.
 */

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ViewHolder> {
    private Context context;
    private IColorAdapter iColorAdapter;
    private ArrayList<Color> mColorData = new ArrayList<>();

    public ColorAdapter(Context context, IColorAdapter iColorAdapter) {
        this.context = context;
        this.iColorAdapter = iColorAdapter;
        mColorData.addAll(Color.getColors(context));
    }

    public interface IColorAdapter {
        void onClick(int colorPos);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.color_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Color color = mColorData.get(position);
        holder.tv.setBackgroundColor(color.getHeaderColor());
    }


    @Override
    public int getItemCount() {
        return mColorData.size();
    }
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv;

        ViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.tvColorItem);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            iColorAdapter.onClick(getAdapterPosition());
        }
    }
}
