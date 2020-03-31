package com.example.filepicklibrary.utility;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.filepicklibrary.R;


public class DialogBuilder {

    private static AlertDialog dialog;

    public interface DialogCallback {
        void onPositiveButtonClick(View view);
        void onNegativeButtonClick(View view);
    }


    public static void showGenericDialog(final Activity context, boolean isCancellable, String title, String message,
                                         String positiveBtnText, String negativeBtnText, final DialogCallback dialogCallback) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(isCancellable);
            builder.setTitle(Html.fromHtml("<font color='#000000'>" + title + "</font>"));
            builder.setMessage(Html.fromHtml("<font color='#000000'>" + message + "</font>"));
            builder.setPositiveButton(positiveBtnText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (dialogCallback != null) {
                        dialogCallback.onPositiveButtonClick(new View(context));
                    }
                }
            });
            builder.setNegativeButton(negativeBtnText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (dialogCallback != null) {
                        dialogCallback.onNegativeButtonClick(new View(context));
                    }
                }
            });
            dialog = builder.create();

            dialog.setOnShowListener( new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                }
            });

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(context, R.color.white)));
            }

            if (!context.isFinishing()) {
                dialog.show();
            }
        } catch (Exception ignore) {
        }
    }

    public static void showOkDialog(Activity context, boolean isCancellable, String title, String message,
                                    final DialogCallback dialogCallback) {
        showGenericDialog(context, isCancellable, title, message, context.getString(R.string.dialog_btn_ok_text), null, dialogCallback);
    }

    public static void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

}
