package com.minhvu.proandroid.sqlite.database.main.view.Activity;

import android.Manifest;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.Utils.DesEncrypter;
import com.minhvu.proandroid.sqlite.database.main.view.Adapter.NoteAdapter;
import com.minhvu.proandroid.sqlite.database.models.data.NoteContract;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity
        implements NoteAdapter.IBookAdapterOnClickHandler, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOGTAG = "MainActivity";

    private NoteAdapter bookNoteAdapter;
    private final int ID_BOOK_LOADER = 101;

    FloatingActionButton fab;
    RecyclerView recyclerView;
    GridLayoutManager gridLayoutManager;
    private int mPosition = RecyclerView.NO_POSITION;
    private Point point = new Point();

    private Cipher cipher;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private KeyguardManager keyguardManager;
    private FingerprintManager fingerprintManager;
    private final String FINGERPRINT_KEY = "fingerprint_k";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupInitialize();

        getSupportLoaderManager().initLoader(ID_BOOK_LOADER, null, this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        requestPermission();
    }

    void requestPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.USE_FINGERPRINT}, 2);
        }
    }

    private void setupInitialize() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Note pad");
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        // the third parameter reverse a UI list
        //LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        gridLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);

        //recyclerView.setAdapter(bookAdapter);
        bookNoteAdapter = new NoteAdapter(this, this);
        bookNoteAdapter.onRecyclerViewAttached(recyclerView);
        recyclerView.setAdapter(bookNoteAdapter);


        fab = (FloatingActionButton) findViewById(R.id.fabInsert);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BookDetailActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public void onClick(final Note note, final int itemPosition) {
        if (!TextUtils.isEmpty(note.getPassword())) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            if (!unlockFingerprint(inflater, note, itemPosition)) {
                unlockText(inflater, note, itemPosition);
            }

        } else {
            Uri uri = ContentUris.withAppendedId(NoteContract.NoteEntry.CONTENT_URI, note.getId());
            openDetailActivity(uri, itemPosition);
        }
    }

    private void unlockText(LayoutInflater inflater, final Note note, final int itemPosition) {
        LinearLayout viewGroup = (LinearLayout) this.findViewById(R.id.popupViewGroup);
        View dialogLayout = inflater.inflate(R.layout.popup_password_set, viewGroup);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogLayout);
        final AlertDialog dialog = builder.create();
        final EditText editText = (EditText) dialogLayout.findViewById(R.id.etPassWord);
        editText.setFocusable(true);
        ImageButton imgBtnNo = (ImageButton) dialogLayout.findViewById(R.id.btnNo);
        ImageButton imgBtnYes = (ImageButton) dialogLayout.findViewById(R.id.btnYes);

        imgBtnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = editText.getText().toString();
                if (TextUtils.isEmpty(password)) {
                    return;
                }
                DesEncrypter decrypt = new DesEncrypter();
                String pas = decrypt.decrypt(note.getPassword(), note.getPassSalt());
                if (pas.equals(password)) {
                    dialog.dismiss();
                    Uri uri = ContentUris.withAppendedId(NoteContract.NoteEntry.CONTENT_URI, note.getId());
                    openDetailActivity(uri, itemPosition);
                } else {
                    editText.setText("");
                }

            }
        });
        imgBtnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        showDialog(dialog);
    }

    private boolean unlockFingerprint(final LayoutInflater inflater, final Note note, final int itemPosition) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View layout = inflater.inflate(R.layout.fingerprint_layout, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(layout);
            final AlertDialog dialog = builder.create();
            Button btnCancel = (Button) layout.findViewById(R.id.btnCancel);
            Button btnUsePassword = (Button) layout.findViewById(R.id.btnUsePassword);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            btnUsePassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    unlockText(inflater, note, itemPosition);
                }
            });

            keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
            //check whether the device has a fingerprint sensor
            if (!fingerprintManager.isHardwareDetected()) {
                // device don't support fingerprint
                return false;
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.USE_FINGERPRINT}, 2);
            }
            //check that the user has registered at least one fingerprint
            if (!fingerprintManager.hasEnrolledFingerprints()) {
                return false;
            }
            //check that the lock-screen is secured
            if (!keyguardManager.isKeyguardSecure()) {
                return false;
            } else {
                try {
                    generateKey();
                } catch (FingerprintException e) {
                    e.printStackTrace();
                }
                if (initCipher()) {
                    FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                    Uri uri = ContentUris.withAppendedId(NoteContract.NoteEntry.CONTENT_URI, note.getId());
                    FingerprintHandler handler = new FingerprintHandler(uri, itemPosition, dialog);
                    handler.startAuth(fingerprintManager, cryptoObject);
                }
            }
            dialog.show();
            return true;
        } else {
            return false;
        }
    }

    private void generateKey() throws FingerprintException {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");

            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyStore.load(null);
            KeyGenParameterSpec keyGenParameterSpec =
                    new KeyGenParameterSpec.Builder(FINGERPRINT_KEY, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                            //Configure this key so that the user has to confirm their identity
                            //with a fingerprint each time they want to use it
                            .setUserAuthenticationRequired(true)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7).build();
            keyGenerator.init(keyGenParameterSpec);
            keyGenerator.generateKey();

        } catch (KeyStoreException |
                NoSuchAlgorithmException |
                NoSuchProviderException |
                CertificateException |
                IOException |
                InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            throw new FingerprintException(e);
        }
    }

    private boolean initCipher() {
        String algorithm = KeyProperties.KEY_ALGORITHM_AES + "/" +
                KeyProperties.BLOCK_MODE_CBC + "/" +
                KeyProperties.ENCRYPTION_PADDING_PKCS7;
        try {
            cipher = Cipher.getInstance(algorithm);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get Cipher", e);
        }
        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(FINGERPRINT_KEY, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return false;
        } catch (CertificateException | NoSuchAlgorithmException | IOException | UnrecoverableKeyException |
                KeyStoreException e) {
            e.printStackTrace();
        }
        return true;
    }


    public void openDetailActivity(Uri uri, int itemPosition) {
        Intent intent = new Intent(this, BookDetailActivity.class);
        intent.setData(uri);
        startActivityForResult(intent, itemPosition);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onLongClick(View view, Note note) {
        Toast.makeText(this, "onLongClick:", Toast.LENGTH_SHORT).show();
        showPopup(note);
    }

    private void showDialog(AlertDialog dialog) {
        dialog.show();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        point.set((int) ev.getX(), (int) ev.getY());
        return super.dispatchTouchEvent(ev);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showPopup(final Note note) {
        final Uri uri = ContentUris.withAppendedId(NoteContract.NoteEntry.CONTENT_URI, note.getId());
        int popupWidth = 600;
        int popupHeight = 400;

        LinearLayout viewGroup = (LinearLayout) this.findViewById(R.id.popupViewGroup);

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.popup_activity_main, viewGroup);

        final PopupWindow popupWindow = new PopupWindow(this);
        popupWindow.setContentView(layout);
        popupWindow.setWidth(popupWidth);
        popupWindow.setHeight(popupHeight);
        popupWindow.setFocusable(true);
        // clear the default translucent background
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        getPositionPopupDisplay(popupWidth, popupHeight);
        Log.d(LOGTAG, "width2 = " + point.x + " height2 = " + point.y);
        popupWindow.showAtLocation(layout, Gravity.NO_GRAVITY, point.x, point.y);
        TextView tvTitlePopup = (TextView) layout.findViewById(R.id.tvTitle_Popup);
        tvTitlePopup.setText(note.getTitle());
        TextView tvDeletePopup = (TextView) layout.findViewById(R.id.tvDelete_popup);
        tvDeletePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteBook(uri);
                cancelNotification((int) note.getId());
                popupWindow.dismiss();
            }
        });
    }

    private void getPositionPopupDisplay(int widthPopup, int heightPopup) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightDevice = displayMetrics.heightPixels;
        int widthDevice = displayMetrics.widthPixels;
        Log.d(LOGTAG, "width = " + widthDevice + " height = " + heightDevice);
        if (point.y > heightDevice - heightPopup) {
            point.y = heightDevice - heightPopup / 2;
        }
    }

    private void deleteBook(Uri uri) {
        if (uri == null)
            return;
        ContentResolver resolver = getContentResolver();
        int deleted = resolver.delete(uri, null, null);
        if (deleted > 0) {
            Toast.makeText(this, "Delete successful", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelNotification(int noteID) {
        if (noteID == -1)
            return;
        String action_broadcast = getString(R.string.broadcast_receiver_pin);
        Intent intent = new Intent(action_broadcast);
        PendingIntent pi = PendingIntent.getBroadcast(this, noteID, intent, 0);
        if (pi != null) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.cancel(noteID);
        }

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id != ID_BOOK_LOADER)
            return null;
        Uri uri = NoteContract.NoteEntry.CONTENT_URI;
        String selection = NoteContract.NoteEntry.COL_DELETE + "=?";
        String[] selectionArgs = new String[]{"0"};
        return new CursorLoader(this, uri, NoteContract.NoteEntry.getColumnNames(), selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        bookNoteAdapter.swapData(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bookNoteAdapter.swapData(null);
    }


    private class FingerprintHandler extends FingerprintManager.AuthenticationCallback {
        private Uri uri;
        private int itemPosition;
        private  AlertDialog dialog;
        FingerprintHandler(Uri uri, int itemPosition, AlertDialog dialog){
            this.uri = uri;
            this.itemPosition = itemPosition;
            this.dialog = dialog;
        }


        void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject){
            CancellationSignal cancellationSignal = new CancellationSignal();
            if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.USE_FINGERPRINT) != PermissionChecker.PERMISSION_GRANTED){
                return;
            }
            manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
        }


        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            Toast.makeText(MainActivity.this, "Finger don't match", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            dialog.dismiss();
            openDetailActivity(uri, itemPosition);
        }
    }


    private class FingerprintException extends Exception {
        public FingerprintException(Exception e) {
            super(e);
        }
    }
}
