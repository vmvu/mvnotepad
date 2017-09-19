package com.minhvu.proandroid.sqlite.database.main.view.Activity;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.Utils.DesEncrypter;
import com.minhvu.proandroid.sqlite.database.main.view.Adapter.NoteAdapter;
import com.minhvu.proandroid.sqlite.database.models.data.NoteContract;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;

public class MainActivity extends AppCompatActivity
        implements NoteAdapter.IBookAdapterOnClickHandler, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOGTAG = "MainActivity";

    private NoteAdapter bookNoteAdapter;
    private final int ID_BOOK_LOADER = 101;

    FloatingActionButton fab;
    RecyclerView recyclerView;
    private int mPosition = RecyclerView.NO_POSITION;


    private Point point = new Point();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupInitialize();

        getSupportLoaderManager().initLoader(ID_BOOK_LOADER, null, this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public void onClick(final Note note, final int itemPosition) {
        if (!TextUtils.isEmpty(note.getPassword())) {

            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
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
        } else {
            Uri uri = ContentUris.withAppendedId(NoteContract.NoteEntry.CONTENT_URI, note.getId());
            openDetailActivity(uri, itemPosition);
        }
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


    private void setupInitialize() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Tieu de toolbar");
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        // the third parameter reverse a UI list
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id != ID_BOOK_LOADER)
            return null;
        Log.d("Loaf", "onCreateLoader");
        Uri uri = NoteContract.NoteEntry.CONTENT_URI;
        String selection = NoteContract.NoteEntry.COL_DELETE + "=?";
        String[] selectionArgs = new String[]{"0"};
        return new CursorLoader(this, uri, NoteContract.NoteEntry.getColumnNames(), selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d("Loaf", "onLoadFinished: " + data.getCount());
        bookNoteAdapter.swapData(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bookNoteAdapter.swapData(null);
    }
}
