package com.example.clientsellingmedicine.utils;

import android.app.Dialog;
import android.content.Context;

import com.example.clientsellingmedicine.R;

public class LoadingManager {

    private static Dialog loadingDialog;

    // Show loading dialog
    public static void showLoading(Context context) {
        if (loadingDialog == null) {
            loadingDialog = new Dialog(context);
            loadingDialog.setContentView(R.layout.loading_dialog);
            loadingDialog.setCancelable(false); // Prevent dismissal
        }
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    // Hide loading dialog
    public static void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}
