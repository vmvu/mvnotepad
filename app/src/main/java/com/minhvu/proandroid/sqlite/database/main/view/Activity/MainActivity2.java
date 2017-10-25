package com.minhvu.proandroid.sqlite.database.main.view.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.main.view.Fragment.DeleteFragment;
import com.minhvu.proandroid.sqlite.database.main.view.Fragment.MainFragment;

/**
 * Created by vomin on 10/10/2017.
 */

public class MainActivity2 extends AppCompatActivity {
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private ImageButton btnDeletePage;

    private static final int CODE_FOR_NEW_NOTE = -1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        setupInit();

        toolbar.setTitleTextColor(getResources().getColor(R.color.black));
        openMainPage(btnDeletePage);

        requestPermission();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }
    void openMainPage(ImageButton button){
        toolbar.setTitle(getString(R.string.main_page));
        fab.setVisibility(View.VISIBLE);
        button.setTag(true);
        MainFragment mainFragment;
        FragmentManager fManager = getSupportFragmentManager();
        mainFragment = (MainFragment) fManager.findFragmentByTag(MainFragment.class.getSimpleName());
        if(mainFragment == null){
            mainFragment = new MainFragment();
        }
        FragmentTransaction transaction = fManager.beginTransaction();
        transaction.replace(R.id.place, mainFragment, MainFragment.class.getSimpleName());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    void openDeletePage(ImageButton button){
        toolbar.setTitle(getString(R.string.delete_page));
        fab.setVisibility(View.GONE);
        button.setTag(false);
        DeleteFragment deleteFragment;
        FragmentManager fManager = getSupportFragmentManager();
        deleteFragment = (DeleteFragment) fManager.findFragmentByTag(DeleteFragment.class.getSimpleName());
        if(deleteFragment == null){
            deleteFragment = new DeleteFragment();
        }

        FragmentTransaction transaction = fManager.beginTransaction();
        transaction.replace(R.id.place, deleteFragment, DeleteFragment.class.getSimpleName());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    void setupInit(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        btnDeletePage = (ImageButton) findViewById(R.id.btnDeletePage);
        btnDeletePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!((boolean) btnDeletePage.getTag())){
                    openMainPage(btnDeletePage);
                }else{
                    openDeletePage(btnDeletePage);
                }
            }
        });
        fab = (FloatingActionButton) findViewById(R.id.fabInsert);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity();
            }
        });
    }



    void openActivity(){
        Intent intent = new Intent(MainActivity2.this, BookDetailActivity.class);
        startActivity(intent);
    }

    void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.USE_FINGERPRINT}, 2);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
