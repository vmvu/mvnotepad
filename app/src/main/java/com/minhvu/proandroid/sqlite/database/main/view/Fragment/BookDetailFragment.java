package com.minhvu.proandroid.sqlite.database.main.view.Fragment;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.main.model.DetailModel;
import com.minhvu.proandroid.sqlite.database.main.model.IDetailModel;
import com.minhvu.proandroid.sqlite.database.main.model.IImageModel;
import com.minhvu.proandroid.sqlite.database.main.model.ImageModel;
import com.minhvu.proandroid.sqlite.database.main.presenter.DetailPresenter;
import com.minhvu.proandroid.sqlite.database.main.presenter.IDetailPresenter;
import com.minhvu.proandroid.sqlite.database.main.presenter.IImagePresenter;
import com.minhvu.proandroid.sqlite.database.main.presenter.ImagePresenter;
import com.minhvu.proandroid.sqlite.database.main.view.Adapter.ColorAdapter;
import com.minhvu.proandroid.sqlite.database.main.view.Adapter.ImageAdapter;
import com.minhvu.proandroid.sqlite.database.models.data.NoteContract;
import com.minhvu.proandroid.sqlite.database.models.entity.Color;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Created by vomin on 8/5/2017.
 */

public class BookDetailFragment extends Fragment implements IDetailShow, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOGTAG = BookDetailFragment.class.getSimpleName();

    private IDetailPresenter mMainPresenter;
    private IImagePresenter mImagePresenter;

    ScrollView scrollView;

    private EditText etTitle;
    private EditText etContent;
    private ImageButton btnColor;
    ImageButton btnSetting;
    private ViewGroup viewGroup;
    RecyclerView ImageRecyclerView;
    ImageAdapter imageAdapter;

    String currentUri;

    private static final int ID_LOADER = 99;
    private static final int TAKE_PHOTO_CODE = 55;
    private static final int PICK_IMAGE_CODE = 54;


    private boolean takePhoto_check = true;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mMainPresenter.onViewHasChanged();
            return false;
        }
    };
    private View.OnTouchListener mTouchOnDisplayKeyboard = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            //imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
            etContent.requestFocus();
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            return false;
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Uri uri = null;
        Bundle bundle = getArguments();
        if (bundle != null) {
            String uriString = bundle.getString(getString(R.string.note_uri));
            uri = Uri.parse(uriString);
        }
        SharedPreferences preferences = getActivity()
                .getSharedPreferences(getString(R.string.PREFS_ALARM_FILE), Context.MODE_PRIVATE);
        IDetailModel mModel = new DetailModel(preferences);
        mMainPresenter = new DetailPresenter();
        mMainPresenter.setModel(mModel);
        mMainPresenter.bindView(this);
        mMainPresenter.setCurrentUri(uri);
        mModel.setPresenter(mMainPresenter);
        //
        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 0);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        //set background default
        viewGroup = container;
        View layout = inflater.inflate(R.layout.fragment_detail, container, false);
        scrollView = (ScrollView) layout.findViewById(R.id.sv_content_place);
        etTitle = (EditText) layout.findViewById(R.id.etxtTitle);
        etContent = (EditText) layout.findViewById(R.id.etContent);
        btnSetting = (ImageButton) layout.findViewById(R.id.btnSetting);
        btnColor = (ImageButton) layout.findViewById(R.id.btnColor);
        btnColor.setTag(0);
        viewGroup.setBackgroundColor(getResources().getColor(R.color.backgroundColor_default));
        setup(layout);
        scrollView.setOnTouchListener(mTouchOnDisplayKeyboard);
        //restore
        if (savedInstanceState != null) {
            etTitle.setText(savedInstanceState.getString("title"));
            etContent.setText(savedInstanceState.getString("content"));
            btnColor.setTag(savedInstanceState.getInt("Color_tag"));
        }

        btnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupColorTable(v);
                mMainPresenter.onViewHasChanged();
            }
        });

        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupSettingTable(v);
            }
        });

        etTitle.setOnTouchListener(mTouchListener);
        etContent.setOnTouchListener(mTouchListener);

        etContent.addTextChangedListener(new TextWatcher() {
            String charBefore;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                charBefore = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String titleTemp = etTitle.getText().toString();
                if (charBefore != null) {
                    if (titleTemp.equals(charBefore)) {
                        etTitle.setText(s);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return layout;
    }

    private void setup(View layout) {
        StaggeredGridLayoutManager staggeredGL = new StaggeredGridLayoutManager(3, 1);
        ImageRecyclerView = (RecyclerView) layout.findViewById(R.id.recycler_place_image);
        ImageRecyclerView.setLayoutManager(staggeredGL);

        mImagePresenter = new ImagePresenter();
        IImageModel model = new ImageModel();
        mImagePresenter.setModel(model);
        model.setPresenter(mImagePresenter);
        mImagePresenter.onLoadImages(getActivityContext(), mMainPresenter.getCurrentUri());
        imageAdapter = new ImageAdapter(getActivityContext(), mImagePresenter);
        ImageRecyclerView.setAdapter(imageAdapter);
    }

    private void popupColorTable(View view) {
        int popupWidth = 600;
        int popupHeight = 620;
        int[] local = new int[2];
        view.getLocationInWindow(local);

        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getBaseContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.popup_color_table, null);

        final PopupWindow popup = popupConfiguration(layout, popupWidth, popupHeight, local[0], local[1] + 250, Gravity.NO_GRAVITY);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        //LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.gridColorTable);
        recyclerView.setLayoutManager(layoutManager);
        ColorAdapter adapter = new ColorAdapter(getActivity(), new ColorAdapter.IColorAdapter() {
            @Override
            public void onClick(int colorPos) {
                setColor(colorPos);
                popup.dismiss();
            }
        });
        recyclerView.setAdapter(adapter);

    }

    private PopupWindow popupConfiguration(View layout, int width, int height, int x, int y, int gravity) {
        PopupWindow popup = new PopupWindow(getActivityContext());
        popup.setContentView(layout);
        popup.setWidth(width);
        popup.setHeight(height);
        popup.setFocusable(true);
        popup.setBackgroundDrawable(new BitmapDrawable());
        popup.showAtLocation(layout, gravity, x, y);
        return popup;
    }


    private void setColor(int colorPos) {
        Color color = Color.getColor(getActivityContext(), colorPos);
        btnColor.setColorFilter(color.getHeaderColor());
        btnColor.setTag(colorPos);
        viewGroup.setBackgroundColor(color.getBackgroundColor());
    }

    private void popupSettingTable(View view) {
        final LayoutInflater inflater = (LayoutInflater)
                getActivityContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.popup_setting_table, null);


        final ImageView ivAlarm = (ImageView) layout.findViewById(R.id.ivAlarm);
        ivAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainPresenter.setAlarmOnClick(etTitle, etContent, btnColor, ivAlarm);
                chooseAlarmMode();
            }
        });
        final ImageView ivDelete = (ImageView) layout.findViewById(R.id.ivDelete);
        ivDelete.setTag("ivDelete");
        ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainPresenter.deleteOnClick(etTitle, etContent, btnColor);
            }
        });
        final ImageView ivLockNote = (ImageView) layout.findViewById(R.id.ivLock);
        ivLockNote.setTag("ivLockNote");
        ivLockNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupViewForPasswordFeature(inflater, layout);
            }
        });

        final ImageView ivUnLockNote = (ImageView) layout.findViewById(R.id.ivUnLock);
        ivUnLockNote.setTag("ivUnLockNote");
        ivUnLockNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainPresenter.unLockOnClick(etTitle, etContent, btnColor, layout);

            }
        });

        Button btnImage = (Button) layout.findViewById(R.id.btnImageAdd);
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    takePhotoFromCamera(TAKE_PHOTO_CODE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Button btnImageStorage = (Button) layout.findViewById(R.id.btnImageLocal);
        btnImageStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageFromStorage();
            }
        });
        mMainPresenter.showTableSetting(layout, view);
    }

    private void takePhotoFromCamera(int requestCode) throws IOException {
        Log.d("takePhoto", "takePhotoFromCamera");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getActivityContext().getPackageManager()) != null) {
            File file = getOutputMediaFile();
            if (file == null) {
                Log.d("takePhoto", "file is null");
                return;
            }
            Log.d("takePhoto", "file_path: " + currentUri);
            Uri photoUri = Uri.fromFile(file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            takePhoto_check = false;
            startActivityForResult(intent, requestCode);
        }
    }

    private void pickImageFromStorage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        else{
            intent.setAction(Intent.ACTION_GET_CONTENT);
        }
        takePhoto_check = false;
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == FragmentActivity.RESULT_OK){
            if (requestCode == TAKE_PHOTO_CODE  && !TextUtils.isEmpty(currentUri)) {
                mImagePresenter.addImage(currentUri, mMainPresenter.getCurrentUri());
                currentUri = "";
            }
            if(requestCode == PICK_IMAGE_CODE && data.getData() != null){
                Uri uri = data.getData();
                try{
                    savePickImage(getBitmapFromUri(uri));
                    if(!TextUtils.isEmpty(currentUri)){
                        mImagePresenter.addImage(currentUri, mMainPresenter.getCurrentUri());
                        currentUri = "";
                    }
                }catch (IOException e){
                    Log.d("pick_image_storage","miss");
                }

            }
        }

        takePhoto_check = true;
    }

    // this section is for camera features
    private File getOutputMediaFile() throws IOException {
        File mediaStorageDir = new File(getActivityContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "mvnote");
        boolean success = true;
        if (!mediaStorageDir.exists()) {
            success = mediaStorageDir.mkdirs();
        }
        if (!success) {
            return null;
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File image = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        this.currentUri = "file:" + image.getAbsolutePath();
        Log.d("takePhoto", "absolutePath:" + currentUri);
        return image;
    }
    private Bitmap getBitmapFromUri(Uri uri) throws IOException{
        ParcelFileDescriptor parcelFileDescriptor = getActivity().getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    private void savePickImage(final Bitmap bitmap) throws IOException {
        File file = getOutputMediaFile();
        FileOutputStream fos = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.flush();
        fos.close();
    }




    private void setupViewForPasswordFeature(LayoutInflater inflater, final View layoutParent) {
        Log.d("Pin", "vao day");
        final View layout = inflater.inflate(R.layout.popup_password_set, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivityContext());
        builder.setView(layout);
        final AlertDialog dialog = builder.create();

        final EditText etPassWord = (EditText) layout.findViewById(R.id.etPassWord);
        etPassWord.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                etPassWord.setText("");
                return false;
            }
        });
        ImageButton iBtnYes = (ImageButton) layout.findViewById(R.id.btnYes);
        AppCompatImageButton iBtnNo = (AppCompatImageButton) layout.findViewById(R.id.btnNo);

        iBtnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        iBtnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainPresenter.lockOnClick(etTitle, etContent, btnColor, etPassWord, layoutParent);
                dialog.dismiss();
            }
        });

        dialog.show();


    }

    private void chooseAlarmMode() {
        LayoutInflater inflater = (LayoutInflater) getActivityContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.popup_alarm_choose, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivityContext());
        builder.setView(layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        final SwitchCompat scPin = (SwitchCompat) layout.findViewById(R.id.scPin);
        final SwitchCompat sc15Min = (SwitchCompat) layout.findViewById(R.id.sc15Minute);
        final SwitchCompat sc30Min = (SwitchCompat) layout.findViewById(R.id.sc30Minute);
        final SwitchCompat scAtTime = (SwitchCompat) layout.findViewById(R.id.scAtTime);
        final SwitchCompat scRepeat = (SwitchCompat) layout.findViewById(R.id.scRepeat);
        final SwitchCompat scReset = (SwitchCompat) layout.findViewById(R.id.scReset);

        scPin.setTag(getResources().getString(R.string.type_of_switch_pin));
        sc15Min.setTag(getResources().getString(R.string.type_of_switch_15min));
        sc30Min.setTag(getResources().getString(R.string.type_of_switch_30min));
        scAtTime.setTag(getResources().getString(R.string.type_of_switch_at_time));
        scRepeat.setTag(getResources().getString(R.string.type_of_switch_repeater));
        scReset.setTag(getResources().getString(R.string.type_of_switch_reset));

        final SwitchCompat[] sc = new SwitchCompat[]{scPin, sc15Min, sc30Min, scAtTime, scRepeat, scReset};

        mMainPresenter.handleForAlarms(sc, layout);

        View.OnClickListener scOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainPresenter.switchCompatOnClick(v, sc);
            }
        };
        Log.d("Pin", "after scOnClickListener");
        scPin.setOnClickListener(scOnClickListener);
        sc15Min.setOnClickListener(scOnClickListener);
        sc30Min.setOnClickListener(scOnClickListener);
        scAtTime.setOnClickListener(scOnClickListener);
        scRepeat.setOnClickListener(scOnClickListener);
        scReset.setOnClickListener(scOnClickListener);
    }


    @Override
    public void showAlarmSpecial(final boolean isAllDayType, final SwitchCompat[] sc, final String switchType) {

        LayoutInflater layoutInflater = (LayoutInflater)
                getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View layout = layoutInflater.inflate(R.layout.alarm_special_dialog, null);
        final TextView tvFromDate = (TextView) layout.findViewById(R.id.tvFromDate);
        final TextView tvToDate = (TextView) layout.findViewById(R.id.tvToDate);
        ImageButton btnFromDate = (ImageButton) layout.findViewById(R.id.btnFromDate);
        ImageButton btnToDate = (ImageButton) layout.findViewById(R.id.btnToDate);
        final ImageButton btnYes = (ImageButton) layout.findViewById(R.id.btnYes);
        ImageButton btnNo = (ImageButton) layout.findViewById(R.id.btnNo);
        LinearLayout linearLayout = (LinearLayout) layout.findViewById(R.id.layoutToDate);
        View blackLine1dp = layout.findViewById(R.id.blackLine1dp);
        final TimePicker tpWhen = (TimePicker) layout.findViewById(R.id.tpWhen);
        tpWhen.setIs24HourView(true);
        btnYes.setTag(false);

        btnFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainPresenter.alarmButtonShowDateTimePicker(tvFromDate);
            }
        });


        if (isAllDayType) {
            btnToDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMainPresenter.alarmButtonShowDateTimePicker(tvToDate);
                }
            });
            mMainPresenter.alarmSpecificSetup(tvFromDate, tvToDate, tpWhen);

        } else {
            mMainPresenter.alarmSpecificSetup(tvFromDate, tpWhen);
            linearLayout.setVisibility(View.GONE);
            blackLine1dp.setVisibility(View.GONE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivityContext());
        builder.setView(layout);
        final AlertDialog dialog = builder.create();
        dialog.show();

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (!isAllDayType) {
                    mMainPresenter.alarmSpecificHandle(sc, tvFromDate, tpWhen, false);
                } else {
                    mMainPresenter.alarmSpecificHandle(sc, tvFromDate, tvToDate, tpWhen);
                }
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        // layout khong bi thu nho
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        if (mMainPresenter.getCurrentUri() != null) {
            getLoaderManager().initLoader(ID_LOADER, null, this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mMainPresenter.onPause(etTitle, etContent, btnColor, 1, takePhoto_check);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMainPresenter.onDestroy(getActivity().isChangingConfigurations());
        mImagePresenter.onDestroy(getActivity().isChangingConfigurations());
        mMainPresenter = null;
        mImagePresenter = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        String title = etTitle.getText().toString();
        String content = etContent.getText().toString();
        outState.putString("title", title);
        outState.putString("content", content);
        outState.putInt("Color_tag", (int) btnColor.getTag());
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] colNames_Book = NoteContract.NoteEntry.getColumnNamesForNote();
        return new CursorLoader(getContext(), mMainPresenter.getCurrentUri(), colNames_Book, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1)
            return;
        data.moveToFirst();
        etTitle.setText(data.getString(data.getColumnIndex(NoteContract.NoteEntry.COL_TITLE)));
        etContent.setText(data.getString(data.getColumnIndex(NoteContract.NoteEntry.COL_CONTENT)));
        int posColor = data.getInt(data.getColumnIndex(NoteContract.NoteEntry.COL_COLOR));
        setColor(posColor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        etTitle.setText("");
        etContent.setText("");
        btnColor.setTag(null);
    }


    @Override
    public Context getAppContext() {
        return getActivityContext().getApplicationContext();
    }

    @Override
    public Context getActivityContext() {
        return getActivity();
    }

    @Override
    public void showToast(Toast toast) {
        toast.show();
    }

    @Override
    public void showAlert(AlertDialog dialog) {
        dialog.show();
    }

    @Override
    public void showDateTimePicker(DatePickerDialog dialog) {
        dialog.show();
    }

    @Override
    public void finishIfSelf() {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.remove(this).commit();
        getActivity().finish();
    }

}
