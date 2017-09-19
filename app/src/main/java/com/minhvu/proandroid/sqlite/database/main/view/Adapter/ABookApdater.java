package com.minhvu.proandroid.sqlite.database.main.view.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;

/**
 * Created by vomin on 8/6/2017.
 */

public abstract class ABookApdater<VH extends ABookApdater.ViewHolder> extends RecyclerView.Adapter<VH> {
    private int selectItem = 0;
    protected RecyclerView mRecyclerView;

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        recyclerView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();

                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        return tryMoveSelection(lm, 1);
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        return tryMoveSelection(lm, -1);
                    }
                }
                return false;
            }
        });
    }

    private boolean tryMoveSelection(RecyclerView.LayoutManager lm, int direction) {
        int nextSelectItem = selectItem + direction;
        if (nextSelectItem >= 0 && nextSelectItem < getItemCount()) {
            notifyItemChanged(selectItem);
            selectItem = nextSelectItem;
            notifyItemChanged(selectItem);
            lm.scrollToPosition(selectItem);
            return true;
        }
        return false;
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        holder.itemView.setSelected(selectItem == position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notifyItemChanged(selectItem);
                    selectItem = mRecyclerView.getChildPosition(v);
                    notifyItemChanged(selectItem);
                }
            });
        }
    }
}
