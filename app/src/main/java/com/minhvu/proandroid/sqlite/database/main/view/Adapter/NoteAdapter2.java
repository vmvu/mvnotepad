package com.minhvu.proandroid.sqlite.database.main.view.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.minhvu.proandroid.sqlite.database.R;

/**
 * Created by vomin on 10/7/2017.
 */

public class NoteAdapter2 extends RecyclerView.Adapter<NoteAdapter2.NoteViewHolder>  {

    private INoteAdapter adapterEvent;

    public interface INoteAdapter{
        void onClick(View view, int position);
        void onLongClick(View view, int position);
        void onBindViewHolder(NoteViewHolder holder, int position);
        int getDataCount();
    }

    public NoteAdapter2(INoteAdapter adapterEvent){
        this.adapterEvent = adapterEvent;
    }

    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.note_item, parent, false);
        return new NoteViewHolder(layout);
    }


    @Override
    public void onBindViewHolder(NoteViewHolder holder, int position) {
        adapterEvent.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return adapterEvent.getDataCount();
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        public TextView tvTitle;
        public TextView tvContent;
        public TextView tvDateCreated;
        public TextView tvLastOn;
        public View background;
        public View lineHeader;
        public ImageView ivPinIcon;
        public ImageView ivLockIcon;


        NoteViewHolder(View itemView) {
            super(itemView);
            setup(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }
        private void setup(View view) {
            tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            tvContent = (TextView) view.findViewById(R.id.tvContent);
            tvDateCreated = (TextView) view.findViewById(R.id.tvDateCreated);
            tvLastOn = (TextView) view.findViewById(R.id.tvLastUpdate);
            background = view.findViewById(R.id.bgNote);
            lineHeader = view.findViewById(R.id.lineNote);
            ivPinIcon = (ImageView) view.findViewById(R.id.ivPinStarIcon);
            ivLockIcon = (ImageView) view.findViewById(R.id.ivLockIcon);

            ivPinIcon.setVisibility(View.GONE);
            ivLockIcon.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View v) {
            adapterEvent.onClick(v, getAdapterPosition());
        }


        @Override
        public boolean onLongClick(View v) {
            adapterEvent.onLongClick(v, getAdapterPosition());
            return true;
        }
    }
}
