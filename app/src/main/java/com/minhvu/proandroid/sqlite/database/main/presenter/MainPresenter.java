package com.minhvu.proandroid.sqlite.database.main.presenter;

import android.Manifest;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.Utils.DateTimeUtils;
import com.minhvu.proandroid.sqlite.database.Utils.DeEncrypter;
import com.minhvu.proandroid.sqlite.database.Utils.Sort;
import com.minhvu.proandroid.sqlite.database.main.model.view.IMainModel;
import com.minhvu.proandroid.sqlite.database.main.presenter.view.IMainPresenter;
import com.minhvu.proandroid.sqlite.database.main.view.Activity.DetailNoteActivity;
import com.minhvu.proandroid.sqlite.database.main.view.Adapter.NoteAdapter;
import com.minhvu.proandroid.sqlite.database.main.view.Fragment.view.IMainView;
import com.minhvu.proandroid.sqlite.database.models.data.NoteContract;
import com.minhvu.proandroid.sqlite.database.models.entity.Color;
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

/**
 * Created by vomin on 10/7/2017.
 */

public class MainPresenter extends MvpPresenter<IMainModel, IMainView.View> implements IMainPresenter {

    private Cipher mCipher;
    private KeyStore mKeyStore;
    private final String FINGERPRINT_KEY = "fingerprint_k";

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        unbindView();
        model.onDestroy(isChangingConfiguration);
        if(!isChangingConfiguration){
            model = null;
        }
    }

    @Override
    public void loadData() {
        Thread thread = new Thread(){
            @Override
            public void run() {
                model.loadData(getView().getActivityContext());
            }
        };
        thread.start();
    }

    @Override
    public void onBindViewHolder(NoteAdapter.NoteViewHolder viewHolder, int position) {
        Note note = model.getNote(position);
        if (note.isDelete()) {
            viewHolder.itemView.setVisibility(View.GONE);
            return;
        }
        viewHolder.itemView.setVisibility(View.VISIBLE);

        viewHolder.tvTitle.setText(note.getTitle());
        viewHolder.tvDateCreated.setText(DateTimeUtils.longToStringDate(note.getDateCreated()));
        viewHolder.tvLastOn.setText(DateTimeUtils.longToStringDate(note.getLastOn()));

        if (TextUtils.isEmpty(note.getPassword())) {
            viewHolder.ivLockIcon.setVisibility(View.GONE);
            viewHolder.tvContent.setVisibility(View.VISIBLE);
            viewHolder.tvContent.setText(note.getContent());
        } else {
            viewHolder.ivLockIcon.setVisibility(View.VISIBLE);
            viewHolder.tvContent.setVisibility(View.GONE);
        }

        Color color = Color.getColor(getView().getActivityContext(), note.getIdColor());
        viewHolder.background.setBackgroundColor(getView().getActivityContext().getColor(android.R.color.white));
        //viewHolder.lineHeader.setBackgroundColor(color.getHeaderColor());
        viewHolder.tvTitle.setTextColor(color.getHeaderColor());
        viewHolder.lineHeader.setVisibility(View.GONE);

        if (isImportantNote((int) note.getId())) {
            viewHolder.ivPinIcon.setColorFilter(color.getHeaderColor());
            viewHolder.ivPinIcon.setVisibility(View.VISIBLE);
        }else{
            viewHolder.ivPinIcon.setVisibility(View.GONE);
        }
    }
    private boolean isImportantNote(int noteID) {
        SharedPreferences preferences = getView().getActivityContext().getSharedPreferences(
                getView().getActivityContext().getString(R.string.PREFS_ALARM_FILE), Context.MODE_PRIVATE);

        String key = getView().getActivityContext().getString(R.string.PREFS_ALARM_SWITCH_KEY) + noteID;
        String switchType = preferences.getString(key, "");
        return switchType.trim().equals("scPin");
    }

    @Override
    public void AdapterOnClick(int position) {
        Note note = model.getNote(position);
        if (!TextUtils.isEmpty(note.getPassword())) {
            LayoutInflater inflater = (LayoutInflater)  getView().getActivityContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (!unlockFingerprint(inflater, note, position)) {
                unlockText(inflater, note, position);
            }
        } else {
            Uri uri = ContentUris.withAppendedId(NoteContract.NoteEntry.CONTENT_URI, note.getId());
            openDetailActivity(uri, position);
        }
    }

    @Override
    public void AdapterLongClick(View view, int position) {
        popupChooseTable(view, position);
    }

    private void popupChooseTable(View view, int position){

        final Note note = model.getNote(position);
        //final Uri uri = ContentUris.withAppendedId(NoteContract.NoteEntry.CONTENT_URI, note.getId());

        LayoutInflater inflater = LayoutInflater.from(getView().getActivityContext());
        View layout = inflater.inflate(R.layout.popup_activity_main, null);

        int []local = new int[2];
        view.getLocationInWindow(local);
        int popupWidth = 600;
        int popupHeight = 400;

        final PopupWindow popupWindow = new PopupWindow(getView().getActivityContext());
        popupWindow.setContentView(layout);
        popupWindow.setWidth(popupWidth);
        popupWindow.setHeight(popupHeight);
        popupWindow.setFocusable(true);
        // clear the default translucent background
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        getPositionPopupDisplay(popupWidth, popupHeight, local);
        popupWindow.showAtLocation(layout, Gravity.NO_GRAVITY, local[0], local[1]);
        TextView tvTitlePopup = layout.findViewById(R.id.tvTitle_Popup);
        tvTitlePopup.setText(note.getTitle());
        TextView tvDeletePopup = layout.findViewById(R.id.tvDelete_popup);
        tvDeletePopup.setOnClickListener(v -> {
            deleteBook(note.getId());
            cancelNotification((int) note.getId());
            popupWindow.dismiss();
        });
    }
    private void getPositionPopupDisplay(int widthPopup, int heightPopup, int[] local) {
        DisplayMetrics displayMetrics = getView().getDimensionOnScreen();
        int heightDevice = displayMetrics.heightPixels;
        int widthDevice = displayMetrics.widthPixels;
        if (local[1] > heightDevice - heightPopup) {
            local[1] = heightDevice - heightPopup / 2;
        }
    }

    private void deleteBook(long noteID) {
        boolean isDeleted = model.deleteNote(getView().getActivityContext(), noteID);
        if(isDeleted){
            getView().updateAdapter();
        }

    }

    private void cancelNotification(int noteID) {
        if (noteID == -1)
            return;
        String action_broadcast = getView().getActivityContext().getString(R.string.broadcast_receiver_pin);
        Intent intent = new Intent(action_broadcast);
        PendingIntent pi = PendingIntent.getBroadcast(getView().getActivityContext(), noteID, intent, 0);
        if (pi != null) {
            NotificationManager nm = (NotificationManager) getView().getActivityContext().getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(noteID);
        }

    }

    @Override
    public int getDataCount() {
        return model.getCount();
    }



    private void openDetailActivity(Uri uri, int position) {
        Intent intent = new Intent(getView().getActivityContext(), DetailNoteActivity.class);
        intent.setData(uri);
        getView().startActivityResult(intent,position );
    }

    private boolean unlockFingerprint(final LayoutInflater inflater, final Note note, final int itemPosition) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View layout = inflater.inflate(R.layout.fingerprint_layout, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(getView().getActivityContext());
            builder.setView(layout);
            final AlertDialog dialog = builder.create();
            Button btnCancel = layout.findViewById(R.id.btnCancel);
            Button btnUsePassword = layout.findViewById(R.id.btnUsePassword);
            btnCancel.setOnClickListener(v -> dialog.dismiss());

            btnUsePassword.setOnClickListener(v -> {
                dialog.dismiss();
                unlockText(inflater, note, itemPosition);
            });

            KeyguardManager mKeyguardManager = (KeyguardManager) getView().getActivityContext().getSystemService(Context.KEYGUARD_SERVICE);
            FingerprintManager mFingerprintManager = (FingerprintManager) getView().getActivityContext().getSystemService(Context.FINGERPRINT_SERVICE);
            //check whether the device has a fingerprint sensor
            if (!mFingerprintManager.isHardwareDetected()) {
                // device don't support fingerprint
                return false;
            }

            if (ContextCompat.checkSelfPermission(getView().getActivityContext(), Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
               return false;
            }
            //check that the user has registered at least one fingerprint
            if (!mFingerprintManager.hasEnrolledFingerprints()) {
                return false;
            }
            //check that the lock-screen is secured
            if (!mKeyguardManager.isKeyguardSecure()) {
                return false;
            } else {
                try {
                    generateKey();
                } catch (FingerprintException e) {
                    e.printStackTrace();
                }
                if (initCipher()) {
                    FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(mCipher);
                    Uri uri = ContentUris.withAppendedId(NoteContract.NoteEntry.CONTENT_URI, note.getId());
                    FingerprintHandler handler = new FingerprintHandler(uri, itemPosition, dialog);
                    handler.startAuth(mFingerprintManager, cryptoObject);
                }
            }
            dialog.show();
            return true;
        } else {
            return false;
        }
    }
    private void unlockText(LayoutInflater inflater, final Note note, final int itemPosition) {
        View dialogLayout = inflater.inflate(R.layout.popup_password_set, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getView().getActivityContext());
        builder.setView(dialogLayout);
        final AlertDialog dialog = builder.create();
        final EditText editText =  dialogLayout.findViewById(R.id.etPassWord);
        editText.setFocusable(true);
        ImageButton imgBtnNo =  dialogLayout.findViewById(R.id.btnNo);
        ImageButton imgBtnYes =  dialogLayout.findViewById(R.id.btnYes);

        imgBtnYes.setOnClickListener(v -> {
            String password = editText.getText().toString();
            if (TextUtils.isEmpty(password)) {
                return;
            }
            String pas = DeEncrypter.decryptString(note.getPassword(), note.getPassSalt());
            if (pas.equals(password)) {
                dialog.dismiss();
                Uri uri = ContentUris.withAppendedId(NoteContract.NoteEntry.CONTENT_URI, note.getId());
                openDetailActivity(uri, itemPosition);
            } else {
                editText.setText("");
            }

        });
        imgBtnNo.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
    private void generateKey() throws FingerprintException {
        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");

            KeyGenerator mKeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            mKeyStore.load(null);
            KeyGenParameterSpec keyGenParameterSpec =
                    new KeyGenParameterSpec.Builder(FINGERPRINT_KEY, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                            //Configure this key so that the user has to confirm their identity
                            //with a fingerprint each time they want to use it
                            .setUserAuthenticationRequired(true)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7).build();
            mKeyGenerator.init(keyGenParameterSpec);
            mKeyGenerator.generateKey();

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
            mCipher = Cipher.getInstance(algorithm);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get Cipher", e);
        }
        try {
            mKeyStore.load(null);
            SecretKey key = (SecretKey) mKeyStore.getKey(FINGERPRINT_KEY, null);
            mCipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return false;
        } catch (CertificateException | NoSuchAlgorithmException | IOException | UnrecoverableKeyException |
                KeyStoreException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected void updateView() {

    }

    @Override
    public void updateAdapter() {
        getView().updateAdapter();
    }

    @Override
    public void colorSort(int colorPos) {

        Sort.colorSort(model.getNoteList(), colorPos);
        getView().updateAdapter();
    }

    @Override
    public void alphaSort() {
        Sort.alphaSort(model.getNoteList());
        getView().updateAdapter();
    }

    @Override
    public void colorOrderSort() {
        Sort.colorOrderSort(model.getNoteList());
        getView().updateAdapter();
    }

    @Override
    public void sortByModifiedTime() {
        Sort.modifiedTimeSort(model.getNoteList());
        getView().updateAdapter();
    }

    @Override
    public void sortByImportant() {
        Sort.sortByImportant(getView().getActivityContext(), model.getNoteList());
        getView().updateAdapter();
    }

    @Override
    public void userSignOutUpdate() {
        model.getNoteList().clear();
        getView().updateAdapter();
    }

    @Override
    public void userSignInUpdate() {
        model.loadData(getView().getActivityContext());
        getView().updateAdapter();
    }

    @Override
    public void updateView(final int requestCode) {
        if (requestCode == -1 ) {
            if(model.isCheckCount(getView().getActivityContext())){
                model.getNewNote(getView().getActivityContext());
                getView().updateAdapter();
            }
        }
        else{
            model.updateNote(getView().getActivityContext(), requestCode);
            getView().updateAdapter();
        }
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


        private void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject){
            CancellationSignal cancellationSignal = new CancellationSignal();
            if(ActivityCompat.checkSelfPermission(getView().getActivityContext(), Manifest.permission.USE_FINGERPRINT) != PermissionChecker.PERMISSION_GRANTED){
                return;
            }
            manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
        }


        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            Toast toast = Toast.makeText(getView().getActivityContext(), "Finger don't match", Toast.LENGTH_SHORT);
            getView().showToast(toast);
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
