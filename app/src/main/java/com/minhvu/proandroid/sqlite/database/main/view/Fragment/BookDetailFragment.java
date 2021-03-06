package com.minhvu.proandroid.sqlite.database.main.view.Fragment;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
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
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
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
import com.minhvu.proandroid.sqlite.database.Utils.KMPSearch;
import com.minhvu.proandroid.sqlite.database.main.model.DetailModel;
import com.minhvu.proandroid.sqlite.database.main.model.view.IDetailModel;
import com.minhvu.proandroid.sqlite.database.main.model.view.IImageModel;
import com.minhvu.proandroid.sqlite.database.main.model.ImageModel;
import com.minhvu.proandroid.sqlite.database.main.presenter.DetailPresenter;
import com.minhvu.proandroid.sqlite.database.main.presenter.view.IDetailPresenter;
import com.minhvu.proandroid.sqlite.database.main.presenter.view.IImagePresenter;
import com.minhvu.proandroid.sqlite.database.main.presenter.ImagePresenter;
import com.minhvu.proandroid.sqlite.database.main.view.Adapter.ColorAdapter;
import com.minhvu.proandroid.sqlite.database.main.view.Adapter.ImageAdapter;
import com.minhvu.proandroid.sqlite.database.main.view.Fragment.view.IDeleteView;
import com.minhvu.proandroid.sqlite.database.main.view.Fragment.view.IDetailFragment;
import com.minhvu.proandroid.sqlite.database.models.data.NoteContract;
import com.minhvu.proandroid.sqlite.database.models.entity.Color;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * Created by vomin on 8/5/2017.
 */

public class BookDetailFragment extends Fragment implements IDetailFragment.View, LoaderManager.LoaderCallbacks<Cursor>,
        ImageAdapter.IImageAdapter, IDetailFragment.ImageView {


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
    private static final int PICK_PDF_FILE_CODE = 70;


    private boolean takePhoto_check = true;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mMainPresenter.onViewHasChanged();
            return false;
        }
    };

    final GestureDetector gestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            InputMethodManager manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.showSoftInput(etContent, InputMethodManager.SHOW_IMPLICIT);
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            etContent.requestFocus();
            int selected = etContent.getSelectionStart();
            String str = etContent.getText().toString();
            etContent.setText(str);
            etContent.setSelection(selected);
            mMainPresenter.onViewHasChanged();
            return true;
        }
    });

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
        ImageButton btnRemoveTitle = (ImageButton) layout.findViewById(R.id.btnRemoveTitle);
        etTitle = (EditText) layout.findViewById(R.id.etTitle);
        etContent = (EditText) layout.findViewById(R.id.etContent);
        btnSetting = (ImageButton) layout.findViewById(R.id.btnSetting);
        btnColor = (ImageButton) layout.findViewById(R.id.btnColor);
        btnColor.setTag(0);
        viewGroup.setBackgroundColor(getResources().getColor(R.color.backgroundColor_default));
        setup(layout);
        //
        btnRemoveTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etTitle.setText("");
                etTitle.requestFocus();
            }
        });

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

        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
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
        GridLayoutManager staggeredGL = new GridLayoutManager(getActivity(), 3);
        ImageRecyclerView = (RecyclerView) layout.findViewById(R.id.recycler_place_image);
        ImageRecyclerView.setLayoutManager(staggeredGL);

        mImagePresenter = new ImagePresenter();
        IImageModel model = new ImageModel(getActivityContext());
        mImagePresenter.setModel(model);
        model.setPresenter(mImagePresenter);
        mImagePresenter.onLoadImages(getActivityContext(), mMainPresenter.getCurrentUri());
        mImagePresenter.bindView(this);
        imageAdapter = new ImageAdapter(this);
        ImageRecyclerView.setAdapter(imageAdapter);
    }

    private void popupColorTable(View view) {
        int popupWidth = 530;
        int popupHeight = 490;
        int[] local = new int[2];
        view.getLocationInWindow(local);

        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getBaseContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.popup_color_table, null);

        final PopupWindow popup = popupConfiguration(layout, popupWidth, popupHeight, local[0] - 210, local[1] + 170, Gravity.NO_GRAVITY);

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

        final ImageView ivSearch = (ImageView) layout.findViewById(R.id.ivSearch);
        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.search_view_layout, null);
                EditText editText = (EditText) layout.findViewById(R.id.etPatternSearch);
                searchPopup(layout);
                editText.requestFocus();
                searchFunction(editText);
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

        ImageButton btnImage = (ImageButton) layout.findViewById(R.id.btnImageAdd);
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhotoFromCamera();
            }
        });
        ImageButton btnPickImage = (ImageButton) layout.findViewById(R.id.btnPickImage);
        btnPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageFromStorage();
            }
        });

        ImageButton btnPickPdf = (ImageButton) layout.findViewById(R.id.btnPickPdf);
        btnPickPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickPdfFile();
            }
        });
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            btnPickImage.setVisibility(View.GONE);
        }

        mMainPresenter.showTableSetting(layout, view);

        //file:/storage/emulated/0/DCIM/mvnote/PDF_20171006_231300.pdf
    }


    private void searchPopup(View layout) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        PopupWindow popup = new PopupWindow(getActivity());
        popup.setContentView(layout);
        popup.setWidth(width);
        popup.setHeight(150);
        popup.setFocusable(true);
        popup.setBackgroundDrawable(new BitmapDrawable());
        popup.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popup.showAtLocation(layout, Gravity.CENTER_HORIZONTAL, 0, height);
    }

    private void searchFunction(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString())) {
                    String content = etContent.getText().toString();
                    int searchPos[] = KMPSearch.KMP(content, s.toString());
                    if (searchPos != null) {
                        Spannable myTextColor = new SpannableString(content);
                        for (int i = 0; i < searchPos.length; i++) {
                            myTextColor.setSpan(new BackgroundColorSpan(android.graphics.Color.YELLOW),
                                    searchPos[i], searchPos[i] + s.toString().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        etContent.setText(myTextColor);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getActivityContext().getPackageManager()) != null) {
            File file = getOutputMediaFile();
            if (file == null) {
                return;
            }
            Uri photoUri = Uri.fromFile(file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            takePhoto_check = false;
            startActivityForResult(intent, TAKE_PHOTO_CODE);
        }
    }

    private void pickImageFromStorage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        } else {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        }
        takePhoto_check = false;
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_CODE);
    }

    private void pickPdfFile() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivityForResult(Intent.createChooser(intent, "Select file"), PICK_PDF_FILE_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == FragmentActivity.RESULT_OK) {
            if (requestCode == TAKE_PHOTO_CODE && !TextUtils.isEmpty(currentUri)) {
                mImagePresenter.addImage(currentUri, mMainPresenter.getCurrentUri());
                currentUri = "";
            }

            if (requestCode == PICK_IMAGE_CODE && data.getData() != null) {
                Uri uri = data.getData();
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            savePickImage(getBitmapFromUri(uri));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (!TextUtils.isEmpty(currentUri)) {
                            mImagePresenter.addImage(currentUri, mMainPresenter.getCurrentUri());
                            currentUri = "";
                        }
                    }
                }.start();
            }
            if (requestCode == PICK_PDF_FILE_CODE && data.getData() != null) {
                Uri uri = data.getData();
                String pdfFilePath = loadPDF(uri);
                if (!TextUtils.isEmpty(pdfFilePath)) {

                }
                currentUri = "";
            }
        }

        takePhoto_check = true;
    }

    // this section is for camera features
    private File getOutputMediaFile() {
        File mediaStorageDir = new File(
                getActivityContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), getString(R.string.my_storage));
        boolean success = true;
        if (!mediaStorageDir.exists()) {
            success = mediaStorageDir.mkdirs();
        }
        if (!success) {
            return null;
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File imageFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        this.currentUri = "file:" + imageFile.getAbsolutePath();
        return imageFile;
    }

    private File getOutputDocumentFile() {
        //File documentStorageDir = new File(getActivityContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "mvnote");
        File documentStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "mvnote");
        boolean success = true;
        if (!documentStorageDir.exists()) {
            success = documentStorageDir.mkdirs();
        }
        if (!success) {
            return null;
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File documentFile = new File(documentStorageDir.getPath() + File.separator + "PDF_" + timeStamp + ".pdf");
        this.currentUri = "file:" + documentFile.getAbsolutePath();
        return documentFile;
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = getActivity().getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    private void savePickImage(final Bitmap bitmap) throws IOException {
        File file = getOutputMediaFile();
        FileOutputStream fos = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, fos);
        fos.flush();
        fos.close();
    }

    private String loadPDF(Uri uri) {
        boolean isDocument = DocumentsContract.isDocumentUri(getActivityContext(), uri);
        if (!isDocument) {
            return "";
        }
        try {
            ParcelFileDescriptor parcelFileDescriptor = getActivity().getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            FileInputStream fis = new FileInputStream(fileDescriptor);
            FileOutputStream fos = new FileOutputStream(getOutputDocumentFile());
            copyFile(fis, fos);
            fis.close();
            fis = null;
            fos.flush();
            fos.close();
            fos = null;
            return currentUri;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void copyFile(InputStream is, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = is.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }


    private void setupViewForPasswordFeature(LayoutInflater inflater, final View layoutParent) {
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

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mMainPresenter.activePrompt(etTitle, etContent);
            }
        });

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


        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final View view = v;
                GestureDetector gd = new GestureDetector(getActivityContext(), new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public void onShowPress(MotionEvent e) {
                        mMainPresenter.switchCompatOnClick(view, sc);
                    }
                });
                return gd.onTouchEvent(event);
            }
        };
        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mMainPresenter.switchCompatReset((View) buttonView, sc);
            }
        };

        scPin.setOnTouchListener(onTouchListener);
        sc15Min.setOnTouchListener(onTouchListener);
        sc30Min.setOnTouchListener(onTouchListener);
        scAtTime.setOnTouchListener(onTouchListener);
        scRepeat.setOnTouchListener(onTouchListener);
        scReset.setOnTouchListener(onTouchListener);

        scPin.setOnCheckedChangeListener(onCheckedChangeListener);
        sc15Min.setOnCheckedChangeListener(onCheckedChangeListener);
        sc30Min.setOnCheckedChangeListener(onCheckedChangeListener);
        scAtTime.setOnCheckedChangeListener(onCheckedChangeListener);
        scRepeat.setOnCheckedChangeListener(onCheckedChangeListener);
        scReset.setOnCheckedChangeListener(onCheckedChangeListener);
    }


    @Override
    public void showAlarmSpecial(final boolean isAllDateType, final SwitchCompat[] sc, final String switchType) {

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
                String fromDate = tvFromDate.getText().toString();
                Log.d("Prompt", "textView: " + tvFromDate.getText().toString());
                mMainPresenter.alarmButtonShowDateTimePicker(tvFromDate);
            }
        });


        if (isAllDateType) {
            btnToDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMainPresenter.alarmButtonShowDateTimePicker(tvToDate);
                }
            });
            mMainPresenter.setupAlarmSpecial(tvFromDate, tvToDate, tpWhen);

        } else {
            mMainPresenter.setupAlarmSpecial(tvFromDate, tpWhen);
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
                if (!isAllDateType) {
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
        Intent intent = new Intent();
        if (mMainPresenter.getCurrentUri() != null) {
            intent.putExtra("newnote", true);
        }
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
    public void notifyUpdate() {
        imageAdapter.notifyDataSetChanged();

    }

    @Override
    public void notifyUpdateItemChang(int position) {
        imageAdapter.notifyItemChanged(position);
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

    @Override
    public void onClick(View view, int position) {
        mImagePresenter.onImageClick(position);
    }

    @Override
    public View onCreateViewHolder(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View layout = inflater.inflate(R.layout.image_small_item, parent, false);
        return layout;
    }

    @Override
    public void onBindViewHolder(ImageAdapter.ImageViewHolder holder, int position) {
        mImagePresenter.onBindViewHolder(holder, position);
    }

    @Override
    public int getDataCount() {
        return mImagePresenter.getImagesCount();
    }
}
