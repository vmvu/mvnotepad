package com.minhvu.proandroid.sqlite.database.main.presenter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.minhvu.proandroid.sqlite.database.R;
import com.minhvu.proandroid.sqlite.database.Utils.DeEncrypter;
import com.minhvu.proandroid.sqlite.database.main.model.view.IGetShareModel;
import com.minhvu.proandroid.sqlite.database.main.presenter.view.IGetSharePresenter;
import com.minhvu.proandroid.sqlite.database.main.view.Activity.BookDetailActivity;
import com.minhvu.proandroid.sqlite.database.main.view.Activity.view.IGetShareActivity;
import com.minhvu.proandroid.sqlite.database.models.entity.Note;

/**
 * Created by vomin on 9/12/2017.
 */

public class GetSharePresenter extends MvpPresenter<IGetShareModel, IGetShareActivity.View> implements IGetSharePresenter {
    private Uri currentUri = null;

    @Override
    public Context getActivityContext() {
        return getView().getActivityContext();
    }

    @Override
    public Context getAppContext() {
        return getView().getAppContext();
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        unbindView();
        if (!isChangingConfiguration) {
            model = null;
            currentUri = null;
        }
    }

    @Override
    public void setCurrentUri(Uri uri) {
        this.currentUri = uri;
    }

    @Override
    public Uri getCurrentUri() {
        return currentUri;
    }

    @Override
    public void loadNote() {
        if (currentUri == null) {
            return;
        }
        Object v = getView();
        Note note = model.loadNote(currentUri.getPathSegments().get(1));
        if (note != null) {
            if (!TextUtils.isEmpty(model.getmNote().getPassword())) {
                getView().lockContent();
                note.setContent("LOCK CONTENT");
            }
            long imageCount = model.getCountImages(Long.parseLong(currentUri.getPathSegments().get(1)));
            getView().visibleView();
            updateView(note);
            getView().updateImageCount((int)imageCount);
        }
    }

    @Override
    public void updateView(Note note) {
        getView().updateView(note.getTitle(), note.getContent());
    }

    @Override
    public void onDetailOnClick() {
        final Note note = model.getmNote();
        if (!TextUtils.isEmpty(note.getPassword())) {

            LayoutInflater inflater = (LayoutInflater) getActivityContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialogLayout = inflater.inflate(R.layout.popup_password_set, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivityContext());
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
                    String pas = DeEncrypter.decryptString(note.getPassword(), note.getPassSalt());
                    if (pas.equals(password)) {
                        dialog.dismiss();
                        startActivity();
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


            getView().showDialog(dialog);

        } else {
            startActivity();
        }

    }

    private void startActivity() {
        Intent intent = new Intent(getActivityContext(), BookDetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setData(currentUri);
        getView().getActivityContext().startActivity(intent);
    }

    @Override
    public void saveNote(EditText title, EditText content) {
        if (currentUri == null) {
            boolean success = model.insertNote(title.getText().toString(), content.getText().toString());
            if (success) {
                Toast toast = Toast.makeText(getActivityContext(), "saved Data", Toast.LENGTH_SHORT);
                getView().showToast(toast);
            }

        } else {
            boolean success = model.updateNote(currentUri.getPathSegments().get(1),
                    title.getText().toString(), content.getText().toString());
            if (success) {
                Toast toast = Toast.makeText(getActivityContext(), "saved Data", Toast.LENGTH_SHORT);
                getView().showToast(toast);
            }
        }
    }

    @Override
    public void updateView() {

    }
}
