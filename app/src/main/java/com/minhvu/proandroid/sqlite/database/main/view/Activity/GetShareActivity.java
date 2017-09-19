package com.minhvu.proandroid.sqlite.database.main.view.Activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.main.model.GetShareModel;
import com.minhvu.proandroid.sqlite.database.main.model.IGetShareModel;
import com.minhvu.proandroid.sqlite.database.main.presenter.GetSharePresenter;
import com.minhvu.proandroid.sqlite.database.main.presenter.IGetSharePresenter;

/**
 * Created by vomin on 9/11/2017.
 */

public class GetShareActivity extends AppCompatActivity implements View.OnClickListener, GetShareView{
    TextView tvImageCount;
    EditText etTitle;
    EditText etContent;
    ImageButton btnSave;
    Button btnDetail;
    Button btnRemoveTitle;

    private IGetSharePresenter presenter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_share_activity);
        setupView();
        setup();

        Intent intent = getIntent();
        if(intent.hasExtra(Intent.EXTRA_TEXT)){
            String stringShares = intent.getStringExtra(Intent.EXTRA_TEXT);
            processText(stringShares);
        }

        if(intent.getData() != null){
            Uri noteUri = intent.getData();
            loadNote(noteUri);
        }

        if(intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT) != null){
            String processText = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                processText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT).toString();
                processText(processText);
            }
        }
    }

    void setupView(){
        tvImageCount = (TextView) findViewById(R.id.tvImageCount);
        etTitle = (EditText) findViewById(R.id.etTitle);
        etContent = (EditText) findViewById(R.id.etContent);
        btnSave = (ImageButton) findViewById(R.id.btnInsert);
        btnSave.setOnClickListener(this);
        btnRemoveTitle = (Button) findViewById(R.id.btnRemoveTitle);
        btnDetail = (Button) findViewById(R.id.btnDetail);
        etTitle.setEnabled(false);

        btnRemoveTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etTitle.requestFocus();
                etTitle.setText("");
                etTitle.setEnabled(true);
            }
        });

        invisibleView();
    }
    void setup(){
        IGetShareModel model = new GetShareModel();
        presenter = new GetSharePresenter();
        presenter.bindView(this);
        presenter.setModel(model);
        model.setPresenter(presenter);
    }


    @Override
    public void onClick(View v) {
        presenter.saveNote(etTitle, etContent);
        finish();
    }

    private void processText(String stringsShare){
        if(stringsShare == null){
            return;
        }
        etTitle.setText(stringsShare);
        etContent.setText(stringsShare);
    }
    private void handleShare(String[] stringsShare){
        if(stringsShare == null){
            return;
        }
        if(stringsShare.length == 1){
            etTitle.setText(stringsShare[0]);
            etContent.setText(stringsShare[0]);
        }
        if(stringsShare.length == 2){
            etTitle.setText(stringsShare[0]);
            etContent.setText(stringsShare[1]);
        }


    }

    private void loadNote(Uri uri){
        if(uri == null){
            return;
        }
        presenter.setCurrentUri(uri);
        presenter.loadNote();
    }

    @Override
    public Context getActivityContext() {
        return this;
    }

    @Override
    public Context getAppContext() {
        return getAppContext();
    }

    @Override
    public void visibleView() {
        tvImageCount.setVisibility(View.VISIBLE);
        btnDetail.setVisibility(View.VISIBLE);
    }

    @Override
    public void invisibleView() {
        tvImageCount.setVisibility(View.GONE);
        btnDetail.setVisibility(View.GONE);
    }

    @Override
    public void updateView(String title, String content) {
        etTitle.setText(title);
        etContent.setText(content);
    }

    @Override
    public void finishThis() {
        finish();
    }

    @Override
    public void showToast(Toast toast) {
        toast.show();
    }

    @Override
    public void showDialog(Dialog dialog) {
        dialog.show();
    }

    @Override
    public void updateImageCount(int count) {
        String data = count + " Images";
        tvImageCount.setText(data);
    }

    @Override
    public void lockContent() {
        etContent.setFocusable(false);
        btnSave.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy(isChangingConfigurations());
        presenter = null;
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
