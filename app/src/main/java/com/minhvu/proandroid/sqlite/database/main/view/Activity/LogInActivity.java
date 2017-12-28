package com.minhvu.proandroid.sqlite.database.main.view.Activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.Utils.DateTimeUtils;
import com.minhvu.proandroid.sqlite.database.services.SyncService;

/**
 * Created by Minh Vu on 14/12/2017.
 */

public class LogInActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = LogInActivity.class.getSimpleName();

    private static final int RC_SIGN_IN = 551;

    private CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private LoginManager mLoginManager;
    private GoogleSignInClient mGoogleSignInClient;

    private LoginButton mLoginBtnFB;
    private ImageButton mCustomBtnFB;
    private ImageButton mCustomBtnGG;

    private View mSignInPlaceView;
    private View mSyncInfoPlaceView;

    private TextView mTxtNameUser;
    private TextView mTxtNumberOfNote;
    private TextView mTxtLastSync;

    private ImageView mImgLogo;

    private ProgressBar mProgressBar;

    private int mSignInState = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setup();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        changeSignInView(mUser != null);

        setupFacebookSignIn();
        setupGoogleSignIn();
    }

    private void setupFacebookSignIn() {
        AppEventsLogger.activateApp(this);
        mCallbackManager = CallbackManager.Factory.create();
        mLoginManager = LoginManager.getInstance();
        mLoginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
                Toast.makeText(LogInActivity.this, "Success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(LogInActivity.this, "Login attempt cancel", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LogInActivity.this, "Login is wrong", Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.default_web_client_id)).build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setup() {

        mLoginBtnFB = (LoginButton) findViewById(R.id.btnLoginFB);
        mCustomBtnFB = (ImageButton) findViewById(R.id.customBtnFB);
        mCustomBtnGG = (ImageButton) findViewById(R.id.customBtnGG);

        mLoginBtnFB.setReadPermissions("email", "public_profile");

        mCustomBtnFB.setOnClickListener(this);
        mCustomBtnGG.setOnClickListener(this);

        mSignInPlaceView = findViewById(R.id.sign_in_place);
        mSyncInfoPlaceView = findViewById(R.id.sync_info_place);

        mImgLogo = (ImageView) findViewById(R.id.logo);

        mTxtNameUser = (TextView) findViewById(R.id.txt_user_name);
        mTxtNumberOfNote = (TextView) findViewById(R.id.txt_number_of_text);
        mTxtLastSync = (TextView) findViewById(R.id.txt_last_sync);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        ((ImageButton) findViewById(R.id.btn_sync)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.btn_disconnect)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.btn_sign_out)).setOnClickListener(this);

    }

    private void progressBarOnOff(boolean ON) {
        if (ON) {
            mCustomBtnFB.setEnabled(false);
            mCustomBtnGG.setEnabled(false);
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mCustomBtnFB.setEnabled(true);
            mCustomBtnGG.setEnabled(true);
            mSignInPlaceView.setEnabled(true);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private void changeSignInView(boolean isFlag) {
        if (!isFlag) {
            mSignInPlaceView.setVisibility(View.VISIBLE);
            mSyncInfoPlaceView.setVisibility(View.GONE);
        } else {
            UpdateLogo();
            mSignInPlaceView.setVisibility(View.GONE);
            mSyncInfoPlaceView.setVisibility(View.VISIBLE);
        }
    }

    private void UpdateLogo(){
        switch (mSignInState){
            case 1:
                mImgLogo.setImageDrawable(getDrawable(R.drawable.ic_facebook));
                break;
            case 2:
                mImgLogo.setImageDrawable(getDrawable(R.drawable.ic_google));
                break;
            default:
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserInfo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                handleAuthWithGoogle(account);
            } catch (ApiException e) {
                changeSignInView(false);
            }
        }
        progressBarOnOff(false);
    }


    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mUser = mAuth.getCurrentUser();
                progressBarOnOff(false);
                changeSignInView(true);
                updateUserInfo();
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBarOnOff(false);
                mUser = null;
                mLoginManager.logOut();
            }
        });


    }

    private void handleAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBarOnOff(false);
                if (task.isSuccessful()) {
                    mUser = mAuth.getCurrentUser();
                    changeSignInView(true);
                    updateUserInfo();
                } else {
                    mUser = null;
                    changeSignInView(false);
                }
            }
        });
    }

    private void updateUserInfo() {
        if (mUser == null)
            return;
        mTxtNameUser.setText(mUser.getDisplayName());
        //((TextView)findViewById(R.id.txt_count_note)).setText();
        String currentStringTime = DateTimeUtils.longToStringDate(System.currentTimeMillis());
        mTxtLastSync.setText(currentStringTime);
    }

    @Override
    public void onClick(View view) {
        final int viewID = view.getId();
        switch (viewID) {
            case R.id.customBtnFB:
                progressBarOnOff(true);
                mSignInState = 1;
                mLoginBtnFB.performClick();
                break;
            case R.id.customBtnGG:
                progressBarOnOff(true);
                mSignInState = 2;
                googleSignInClick();
                break;
            case R.id.btn_sync:
                activeSyncCloud(0);
                break;
            case R.id.btn_disconnect:
                signOut();
                break;
            case R.id.btn_sign_out:
                activeSyncCloud(1);
                signOut();
                break;
            default:
        }
    }

    private void googleSignInClick() {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);

    }

    private void googleSignOutClick() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                changeSignInView(false);
            }
        });
    }

    private void googleRevokeAccess() {
        mAuth.signOut();
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                changeSignInView(false);
            }
        });
    }

    private void facebookSignOutClick() {
        mLoginManager.logOut();
        changeSignInView(false);
    }

    private void activeSyncCloud(int mode) {
        String userID = mUser.getUid();
        String broadcastAction = getString(R.string.broadcast_sync);
        Intent intent = new Intent(broadcastAction);
        intent.putExtra(getString(R.string.user_token), mUser.getUid());
        intent.putExtra(getString(R.string.user_sign_out_delete_data), mode);
        sendBroadcast(intent);
    }

    private void signOut() {
        mUser = null;
        facebookSignOutClick();
        if (mSignInState == 2) {
            googleRevokeAccess();
        }
        mAuth.signOut();
        mSignInState = -1;
        changeSignInView(false);
        // report to MainActivity, update data
        sendReportUpdateDataForSignOutFromUser();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUser = null;
    }


    private void sendReportUpdateDataForSignOutFromUser(){
        Intent intent = new Intent(getString(R.string.broadcast_sign_out));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

    private SyncService mService;
    private boolean mBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            SyncService.LocalBinder binder = (SyncService.LocalBinder)iBinder;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };
}
