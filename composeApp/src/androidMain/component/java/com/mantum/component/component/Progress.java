package com.mantum.component.component;

import android.app.ProgressDialog;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public class Progress {

    private final Context context;

    private final ProgressDialog progressDialog;

    private boolean prepared;

    public Progress(@NonNull Context context) {
        this.context = context;
        this.prepared = false;
        this.progressDialog = new ProgressDialog(context);
    }

    public void show(@StringRes int title, @StringRes int message) {
        show(context.getString(title), context.getString(message));
    }

    public void show(@NonNull String title, @NonNull String message) {
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        prepared = true;
    }

    public void show() {
        if (prepared && !isShowing()) {
            progressDialog.show();
        }
    }

    public void hidden() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public boolean isShowing() {
        return progressDialog.isShowing();
    }
}