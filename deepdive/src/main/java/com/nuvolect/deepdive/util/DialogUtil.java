package com.nuvolect.deepdive.util;//

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;

public class DialogUtil {

    public static interface DialogCallback {

        public void confirmed();
        public void canceled();
    }

    public static void confirmDialog(
            Activity act,
            String title,
            String message,
            String cancelButtonText,
            String confirmButtonText,
            final DialogCallback dialogCallback) {

        final AlertDialog.Builder alert = new AlertDialog.Builder(act);
        alert.setTitle(title);

//        alert.setIcon(R.drawable.mga_icon_96_outline);
        alert.setMessage(message);

        alert.setPositiveButton( confirmButtonText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                dialogCallback.confirmed();
            }
        });

        alert.setNegativeButton( cancelButtonText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                alert.setOnCancelListener( null);
                dialogCallback.canceled();
            }
        });

        alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                dialogCallback.canceled();
            }
        });

        alert.show();
    }
    public static void confirmDialog(
            Activity act,
            String title, String message, String confirmButtonText,
            final DialogCallback dialogCallback) {

        final AlertDialog.Builder alert = new AlertDialog.Builder(act);
        alert.setTitle(title);

//        alert.setIcon(R.drawable.mga_icon_96_outline);
        alert.setMessage(message);

        alert.setPositiveButton(confirmButtonText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                dialogCallback.confirmed();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                alert.setOnCancelListener( null);
                dialogCallback.canceled();
            }
        });

        alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                dialogCallback.canceled();
            }
        });

        alert.show();
    }

    public static void dismissDialog(Activity act, String title, String message) {

        AlertDialog.Builder alert = new AlertDialog.Builder(act);
        alert.setTitle(title);

//        alert.setIcon(R.drawable.mga_icon_96_outline);
        alert.setMessage(message);

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    public interface InputDialogCallbacks{

        public void done(String result);
        public void cancel();
    }

    public static void inputDialog(
        Activity act, String title, String message, String hint,
            boolean enableOkButton, final InputDialogCallbacks callbacks){

        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        builder.setTitle(title);
        builder.setMessage(message);

        final EditText edittext = new EditText(act);
        edittext.setHint(hint);

        if( ! enableOkButton){
            edittext.setEnabled( enableOkButton);
            edittext.setFocusable(false);
        }
        builder.setView(edittext);

        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                String result = edittext.getText().toString();
                callbacks.done(result);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                callbacks.cancel();
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                callbacks.cancel();
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();

        //enable/ disable buttons of dialog
        if(! enableOkButton )
            dialog.getButton(AlertDialog.BUTTON1).setEnabled(false); //BUTTON1 is positive button
    }
}
