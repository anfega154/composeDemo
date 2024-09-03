package com.mantum.cmms.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.mantum.R;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.core.Mantum;

public class TransaccionHelper {

    public static class Dialog extends Mantum.Activity {
        private ProgressDialog progressDialog;
        Context context;

        public Dialog() {
        }

        public Dialog(Context context) {
            this.context = context;
        }

        public void showProgressDialog() {
            if (context == null) {
                context = this;
            }

            boolean directTransaction = UserPermission.check(context, UserPermission.REALIZAR_TRANSACCIONES_DIRECTAS, false);
            if (directTransaction) {
                progressDialog = new ProgressDialog(context);
                progressDialog.setTitle(context.getString(R.string.title_direct_transaction));
                progressDialog.setMessage(context.getString(R.string.send_direct_transaction));
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        }

        public void showProgressDialogMovement() {
            if (context == null) context = this;
            boolean directTransactionMovements = UserPermission.check(context, UserPermission.TRANSACCIONES_DIRECTAS_MOVIMIENTOS, false);
            if (directTransactionMovements) {
                progressDialog = new ProgressDialog(context);
                progressDialog.setTitle(context.getString(R.string.title_direct_transaction));
                progressDialog.setMessage(context.getString(R.string.send_direct_transaction));
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        }

        public void dismissProgressDialog() {
            if (!isFinishing() && progressDialog != null) {
                progressDialog.dismiss();
            }
        }
    }
}
