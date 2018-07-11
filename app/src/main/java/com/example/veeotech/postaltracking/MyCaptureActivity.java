package com.example.veeotech.postaltracking;

import android.os.Bundle;
import android.os.Handler;

import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.CaptureManager;

/**
 * Created by VeeoTech on 18/4/2018.
 * 設置掃碼只能豎屏操作的介面
 */

public class MyCaptureActivity extends CaptureActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
