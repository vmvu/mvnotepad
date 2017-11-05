package com.minhvu.proandroid.sqlite.database.main.view.Activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AlertDialogLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.compoments.MyViewPager;
import com.minhvu.proandroid.sqlite.database.compoments.PagerDialog;
import com.minhvu.proandroid.sqlite.database.main.view.Activity.view.SortView;
import com.minhvu.proandroid.sqlite.database.main.view.Adapter.SortPagerAdapter;
import com.minhvu.proandroid.sqlite.database.main.view.Fragment.AFragment;
import com.minhvu.proandroid.sqlite.database.main.view.Fragment.DeleteFragment;
import com.minhvu.proandroid.sqlite.database.main.view.Fragment.MainFragment;

import java.util.zip.Inflater;

/**
 * Created by vomin on 10/10/2017.
 */

public class MainActivity2 extends AppCompatActivity implements SortView {
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private ImageButton btnDeletePage;
    private ImageButton btnSort;

    private PagerDialog pagerDialog;
    private AFragment fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        setupInit();

        toolbar.setTitleTextColor(getResources().getColor(R.color.black));
        openMainPage();

        requestPermission();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }
    void openMainPage(){
        toolbar.setTitle(getString(R.string.main_page));
        fab.setVisibility(View.VISIBLE);
        btnDeletePage.setTag(true);
        btnDeletePage.setImageResource(R.drawable.ic_delete_black_40dp);

        FragmentManager fManager = getSupportFragmentManager();
        fragment = (MainFragment) fManager.findFragmentByTag(MainFragment.class.getSimpleName());
        if(fragment == null){
            fragment = new MainFragment();
        }

        FragmentTransaction transaction = fManager.beginTransaction();
        transaction.replace(R.id.place, fragment, MainFragment.class.getSimpleName());
        transaction.commit();
    }

    void openDeletePage(){
        toolbar.setTitle(getString(R.string.delete_page));
        fab.setVisibility(View.GONE);
        btnDeletePage.setImageResource(R.drawable.ic_home_black_24dp);
        btnDeletePage.setTag(false);

        FragmentManager fManager = getSupportFragmentManager();
        fragment = (DeleteFragment) fManager.findFragmentByTag(DeleteFragment.class.getSimpleName());
        if(fragment == null){
            fragment = new DeleteFragment();
        }

        FragmentTransaction transaction = fManager.beginTransaction();
        transaction.replace(R.id.place, fragment, DeleteFragment.class.getSimpleName());
        transaction.commit();
    }

    void setupInit(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        btnSort = (ImageButton) findViewById(R.id.btnSort);
        btnDeletePage = (ImageButton) findViewById(R.id.btnDeletePage);

        btnSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortVew();
            }
        });

        btnDeletePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!((boolean) btnDeletePage.getTag())){
                    openMainPage();
                }else{
                    openDeletePage();
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


    void sortVew(){

            pagerDialog = new PagerDialog(this);
            pagerDialog.setCancelable(true);

        pagerDialog.show(getSupportFragmentManager(), "dialog");

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


    @Override
    public void colorSort(int position) {
        fragment.colorSort(position);
        pagerDialog.dismiss();
    }
}
