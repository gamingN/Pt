package com.example.veeotech.postaltracking.posHand;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;


public class DialogUtils {

    public static void showTipDialog(Context context, CharSequence message){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(""+message);
        builder.setTitle(""+"提示");
        builder.setPositiveButton(""+"確定", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }
}
