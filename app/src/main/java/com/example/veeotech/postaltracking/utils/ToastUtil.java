package com.example.veeotech.postaltracking.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by VeeoTech on 21/4/2018.
 */

public class ToastUtil {

    private static Toast mToast = null;

    public static void showToastDIY(Context context, String text, int duration) {
            if (mToast == null) {
                mToast = Toast.makeText(context, text, duration);
            } else {
                mToast.setText(text);
                mToast.setDuration(duration);
            }
            mToast.show();
        }

    public static void showToastShort(Context context, String text) {
        if (mToast == null) {
            mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    public static void showToastLong(Context context, String text) {
        if (mToast == null) {
            mToast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        } else {
            mToast.setText(text);
            mToast.setDuration(Toast.LENGTH_LONG);
        }
        mToast.show();
    }


}
