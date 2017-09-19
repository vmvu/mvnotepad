package com.minhvu.proandroid.sqlite.database.main.presenter;

import java.lang.ref.WeakReference;

/**
 * Created by vomin on 8/28/2017.
 */

public abstract class MvpPresenter<M, V> {
    protected M model;
    private WeakReference<V> view;

    public void setModel(M model){
        this.resetState();
        this.model = model;
        if(setupDone()){
            updateView();
        }
    }

    protected void resetState(){
    }

    public void bindView(V view){
        this.view = new WeakReference<V>(view);
        if(setupDone()){
            updateView();
        }
    }

    public void unbindView(){
        this.view = null;
    }

    protected V getView(){
        return this.view != null ? view.get() : null;
    }

    public boolean setupDone(){
        return model != null && view != null;
    }

    protected abstract void updateView();
}
