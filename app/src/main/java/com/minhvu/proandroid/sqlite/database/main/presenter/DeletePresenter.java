package com.minhvu.proandroid.sqlite.database.main.presenter;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.Utils.DateTimeUtils;
import com.minhvu.proandroid.sqlite.database.Utils.DesEncrypter;
import com.minhvu.proandroid.sqlite.database.main.model.view.IDeleteModel;
import com.minhvu.proandroid.sqlite.database.main.model.view.IImageModel;
import com.minhvu.proandroid.sqlite.database.main.model.ImageModel;
import com.minhvu.proandroid.sqlite.database.main.presenter.view.IDeletePresenter;
import com.minhvu.proandroid.sqlite.database.main.presenter.view.IImagePresenter;
import com.minhvu.proandroid.sqlite.database.main.view.Adapter.ImageAdapter;
import com.minhvu.proandroid.sqlite.database.main.view.Adapter.NoteAdapter2;
import com.minhvu.proandroid.sqlite.database.main.view.Fragment.view.IDeleteView;
import com.minhvu.proandroid.sqlite.database.main.view.Fragment.view.IDetailFragment;
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

public class DeletePresenter extends MvpPresenter<IDeleteModel, IDeleteView> implements IDeletePresenter {

    private Cipher cipher;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private KeyguardManager keyguardManager;
    private FingerprintManager fingerprintManager;
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
        try{
            Thread thread = new Thread(){
                @Override
                public void run() {
                    model.loadData(getView().getActivityContext());
                }
            };
            thread.start();
        }finally {

        }
    }

    @Override
    public void onBindViewHolder(NoteAdapter2.NoteViewHolder viewHolder, int position) {
        Note note = model.getNote(position);
        if (note.isDelete()) {
            viewHolder.itemView.setVisibility(View.GONE);
            return;
        }

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
        viewHolder.background.setBackgroundColor(color.getBackgroundColor());
        viewHolder.lineHeader.setBackgroundColor(color.getHeaderColor());


        viewHolder.ivPinIcon.setVisibility(View.GONE);

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
            openDetail(position);
        }
    }


    @Override
    public void AdapterLongClick(View view, int position) {

    }

    private void openDetail(final int position) {
        final Note note = model.getNote(position);

        LayoutInflater inflater = LayoutInflater.from(getView().getActivityContext());
        View layout = inflater.inflate(R.layout.popup_delete_restore, null);

        TextView tvTitle = (TextView) layout.findViewById(R.id.tvTitle);
        TextView tvContent = (TextView) layout.findViewById(R.id.tvContent);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getView().getActivityContext(), 3);
        RecyclerView imageRecycler = (RecyclerView) layout.findViewById(R.id.image_recyclerView);
        imageRecycler.setLayoutManager(gridLayoutManager);
        tvTitle.setText(note.getTitle());
        tvContent.setText(note.getContent());

        //setup imageview

        final IImagePresenter imagePresenter = new ImagePresenter();
        final IImageModel imageModel = new ImageModel(getView().getActivityContext(), (int)note.getId());

        imageModel.setPresenter(imagePresenter);
        imagePresenter.setModel(imageModel);



        final ImageAdapter imageAdapter = new ImageAdapter(new ImageAdapter.IImageAdapter() {
            @Override
            public void onClick(View view, int position) {

            }

            @Override
            public View onCreateViewHolder(ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                return inflater.inflate(R.layout.image_small_item, parent, false);
            }

            @Override
            public void onBindViewHolder(ImageAdapter.ImageViewHolder holder, int position) {
                imagePresenter.onBindViewHolder(holder, position);
            }

            @Override
            public int getDataCount() {
                return imageModel.getCount();
            }
        });
        imageRecycler.setAdapter(imageAdapter);
        final Context ctx = this.getView().getBaseContext();


        imagePresenter.bindView(new IDetailFragment.ImageView() {
            @Override
            public Context getActivityContext() {
                return ctx;
            }

            @Override
            public void notifyUpdate() {
                imageAdapter.notifyDataSetChanged();
            }
        });

        //setup Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getView().getActivityContext());
        builder.setView(layout);
        builder.setPositiveButton(getView().getActivityContext().getString(R.string.popup_restore), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try{
                    if(model.restoreNote(getView().getActivityContext(), note.getId()))
                        updateAdapter();
                }finally {
                    dialog.dismiss();
                }
            }
        });
        builder.setNegativeButton(getView().getActivityContext().getString(R.string.popup_delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try{
                    if(model.deleteNote(getView().getActivityContext(), note.getId())){
                        updateAdapter();
                        imagePresenter.deleteAllImage(getView().getActivityContext(),(int)note.getId());
                        /*Thread thread = new Thread(){
                            @Override
                            public void run() {
                                imagePresenter.deleteAllImage((int)note.getId());
                            }
                        };
                        thread.start();*/
                    }
                }finally {
                    dialog.dismiss();
                }

            }
        });

        builder.setNeutralButton(getView().getActivityContext().getString(R.string.cancel_label), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                imagePresenter.onDestroy(false);
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        getView().showDialog(dialog);
    }




    @Override
    public int getDataCount() {
        return model.getCount();
    }





    private boolean unlockFingerprint(final LayoutInflater inflater, final Note note, final int itemPosition) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View layout = inflater.inflate(R.layout.fingerprint_layout, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(getView().getActivityContext());
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

            keyguardManager = (KeyguardManager) getView().getActivityContext().getSystemService(Context.KEYGUARD_SERVICE);
            fingerprintManager = (FingerprintManager) getView().getActivityContext().getSystemService(Context.FINGERPRINT_SERVICE);
            //check whether the device has a fingerprint sensor
            if (!fingerprintManager.isHardwareDetected()) {
                // device don't support fingerprint
                return false;
            }

            if (ContextCompat.checkSelfPermission(getView().getActivityContext(), Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                return false;
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
                    FingerprintHandler handler = new FingerprintHandler(itemPosition, dialog);
                    handler.startAuth(fingerprintManager, cryptoObject);
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
                    openDetail(itemPosition);
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
        dialog.show();
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

    @Override
    protected void updateView() {

    }

    @Override
    public void updateAdapter() {
        getView().updateAdapter();
    }



    @Override
    public void updateView(final int requestCode) {


        getView().updateAdapter();

    }

    @Override
    public void isThereANewData() {
        if(model.getCount() != model.getCount(getView().getActivityContext())){
            model.loadData(getView().getActivityContext());
            getView().updateAdapter();
        }
    }

    private class FingerprintHandler extends FingerprintManager.AuthenticationCallback {
        private int itemPosition;

        private  AlertDialog dialog;
        FingerprintHandler(int itemPosition, AlertDialog dialog){
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
            openDetail(itemPosition);
        }
    }

    private class FingerprintException extends Exception {
        public FingerprintException(Exception e) {
            super(e);
        }
    }

}
