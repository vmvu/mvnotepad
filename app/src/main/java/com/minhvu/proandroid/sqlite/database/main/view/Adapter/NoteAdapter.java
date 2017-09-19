package com.minhvu.proandroid.sqlite.database.main.view.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.Utils.DateTimeUtils;
import com.minhvu.proandroid.sqlite.database.models.data.NoteContract;
import com.minhvu.proandroid.sqlite.database.models.entity.Color;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;

import java.util.ArrayList;

/**
 * Created by vomin on 8/6/2017.
 */

public class NoteAdapter extends ABookApdater<NoteAdapter.viewHolder> {


    private Context mContext;
    private NoteAdapter.IBookAdapterOnClickHandler mClickHandler;
    private ArrayList<Note> mNoteList = new ArrayList<>();

    public interface IBookAdapterOnClickHandler {
        void onClick(Note note, int itemPosition);

        void onLongClick(View view, Note note);
    }

    public NoteAdapter(Context context, NoteAdapter.IBookAdapterOnClickHandler onClickHandler) {
        this.mClickHandler = onClickHandler;
        this.mContext = context;
    }

    @Override
    public viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // cau hinh
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImediately = false;
        View view = inflater.inflate(R.layout.book_item, parent, shouldAttachToParentImediately);
        return new NoteAdapter.viewHolder(view);
    }

    @Override
    public void onBindViewHolder(viewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Note note = mNoteList.get(position);
        if (note.isDelete()) {
            holder.itemView.setVisibility(View.GONE);
            return;
        }

        holder.tvTitle.setText(note.getTitle());
        holder.tvDateCreated.setText(DateTimeUtils.longToStringDate(note.getDateCreated()));
        holder.tvLastOn.setText(DateTimeUtils.longToStringDate(note.getLastOn()));

        if (TextUtils.isEmpty(note.getPassword())) {
            holder.ivLockIcon.setVisibility(View.GONE);
            holder.tvContent.setText(note.getContent());
        } else {
            holder.ivLockIcon.setVisibility(View.VISIBLE);
            holder.tvContent.setVisibility(View.GONE);
        }
        Color color = Color.getColor(mContext, note.getIdColor());
        holder.background.setBackgroundColor(color.getBackgroundColor());
        holder.lineHeader.setBackgroundColor(color.getHeaderColor());

        if (isNotePined((int) note.getId())) {
            holder.ivPinIcon.setColorFilter(color.getHeaderColor());
            holder.ivPinIcon.setVisibility(View.VISIBLE);
        }else{
            holder.ivPinIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mNoteList.size();
    }


    public void swapData(final Cursor newData) {
        if(newData == null){
            mNoteList.clear();
            notifyDataSetChanged();
            return;
        }
        mNoteList.clear();
        Note note;
        if (newData.moveToFirst()) {
            int idPos = newData.getColumnIndex(NoteContract.NoteEntry._ID);
            int titlePos = newData.getColumnIndex(NoteContract.NoteEntry.COL_TITLE);
            int contentPos = newData.getColumnIndex(NoteContract.NoteEntry.COL_CONTENT);
            int colorPos = newData.getColumnIndex(NoteContract.NoteEntry.COL_COLOR);
            int passwordPos = newData.getColumnIndex(NoteContract.NoteEntry.COL_PASSWORD);
            int keyPos = newData.getColumnIndex(NoteContract.NoteEntry.COL_PASSWORD_SALT);
            int dateCreatedPos = newData.getColumnIndex(NoteContract.NoteEntry.COL_DATE_CREATED);
            int lastUpdatePos = newData.getColumnIndex(NoteContract.NoteEntry.COL_LAST_ON);
            int typePos = newData.getColumnIndex(NoteContract.NoteEntry.COL_TYPE_OF_TEXT);
            int deletedPos = newData.getColumnIndex(NoteContract.NoteEntry.COL_DELETE);
            do {
                note = new Note();
                note.setId(newData.getLong(idPos));
                note.setTitle(newData.getString(titlePos));
                note.setContent(newData.getString(contentPos));
                note.setIdColor(newData.getInt(colorPos));
                note.setPassword(newData.getString(passwordPos));
                note.setPassSalt(newData.getString(keyPos));
                note.setDateCreated(Long.parseLong( newData.getString(dateCreatedPos)));
                note.setLastOn(Long.parseLong(newData.getString(lastUpdatePos)));
                note.setIdTypeOfText(newData.getInt(typePos));
                if (newData.getInt(deletedPos) == 1) {
                    note.setDelete(true);
                }
                mNoteList.add(note);
            } while (newData.moveToNext());
        }
        notifyDataSetChanged();
    }


    public void onRecyclerViewAttached(RecyclerView recyclerView) {
        this.mRecyclerView = recyclerView;
    }


    private boolean isNotePined(int idNote) {
        SharedPreferences preferences =
                mContext.getSharedPreferences(mContext.getString(R.string.PREFS_ALARM_FILE), Context.MODE_PRIVATE);
        String key = mContext.getString(R.string.PREFS_ALARM_SWITCH_KEY) + idNote;
        String switchType = preferences.getString(key, "");
        if (switchType.trim().equals("scPin")) {
            return true;
        }
        return false;
    }


    class viewHolder extends ABookApdater.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private TextView tvTitle;
        private TextView tvContent;
        private TextView tvDateCreated;
        private TextView tvLastOn;
        private View background;
        private View lineHeader;
        private ImageView ivPinIcon;
        private ImageView ivLockIcon;


        viewHolder(View itemView) {
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
            Note note = mNoteList.get(getAdapterPosition());
            mClickHandler.onClick(note, getAdapterPosition());
        }


        @Override
        public boolean onLongClick(View v) {
            Note note = mNoteList.get(getAdapterPosition());
            mClickHandler.onLongClick(itemView, note);
            return true;
        }
    }
}
