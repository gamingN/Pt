package com.example.veeotech.postaltracking.utils;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.example.veeotech.postaltracking.MyCaptureActivity;
import com.example.veeotech.postaltracking.warehouse.CountryFragment;
import com.google.zxing.integration.android.IntentIntegrator;


/**
 * Created by VeeoTech on 18/4/2018.
 */

public class ScannerUtils {

    IntentIntegrator integrator;
    Activity mActivity;
    public ScannerUtils(Activity activity,IntentIntegrator integrator){
        this.mActivity=activity;
        this.integrator=integrator;
    }

    Fragment mFragment;

    public ScannerUtils(Fragment fragment, IntentIntegrator integrator) {
        this.mFragment = fragment;
        this.integrator = integrator;
    }

    public void selectFragment() {
        integrator = IntentIntegrator.forSupportFragment(mFragment);
// 设置要扫描的条码类型，ONE_D_CODE_TYPES：一维码，QR_CODE_TYPES-二维码
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("掃碼");
        integrator.setCameraId(0);  // 使用默认的相机
        integrator.setBeepEnabled(false); // 扫到码后播放提示音
        integrator.setCaptureActivity(MyCaptureActivity.class);
        integrator.initiateScan();
    }

    public void selectActivity(){
        integrator = new IntentIntegrator(mActivity);
// 设置要扫描的条码类型，ONE_D_CODE_TYPES：一维码，QR_CODE_TYPES-二维码
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("掃碼");
        integrator.setCameraId(0);  // 使用默认的相机
        integrator.setBeepEnabled(false); // 扫到码后播放提示音
        integrator.setCaptureActivity(MyCaptureActivity.class);
        integrator.initiateScan();
    }



}
