package com.example.veeotech.postaltracking;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.allenliu.versionchecklib.v2.AllenVersionChecker;
import com.allenliu.versionchecklib.v2.builder.DownloadBuilder;
import com.allenliu.versionchecklib.v2.builder.UIData;
import com.allenliu.versionchecklib.v2.callback.ForceUpdateListener;
import com.allenliu.versionchecklib.v2.callback.RequestVersionListener;
import com.example.veeotech.postaltracking.utils.ToastUtil;
import com.example.veeotech.postaltracking.utils.VersionUtil;
import com.example.veeotech.postaltracking.warehouse.bean.VersionBean;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by VeeoTech on 2018/5/4.
 * 閃屏頁,用於請求版本更新操作
 */

public class SplashActivity extends Activity {

    //版本更新請求地址
    private static final String CHECK_VERSION_URL = "http://easy-logistics.com.hk/postal/api/checkVersion.php";
    @BindView(R.id.btn_splash_enter_activity)
    Button btnSplashEnterActivity;

    //版本更新api
    private DownloadBuilder builder;

    private Handler handler = new Handler();
    private int ENTER_MESSAGE = 1;
    private boolean InLoginActivity = false;
    private Runnable runnable;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Window window = getWindow();
        //取消状态栏透明
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //添加Flag把状态栏设为可绘制模式
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.status_color));
        ButterKnife.bind(this);
        request();
    }

    /**
     * 版本更新請求
     */
    private void request() {
       builder = AllenVersionChecker
                .getInstance()
                .requestVersion()
                .setRequestUrl(CHECK_VERSION_URL)
                .request(new RequestVersionListener() {
                    @Nullable
                    @Override
                    public UIData onRequestVersionSuccess(String result) {
                        Gson gson = new Gson();
                        VersionBean versionBean = gson.fromJson(result, VersionBean.class);
                        if (Integer.valueOf(versionBean.getVersion()) > VersionUtil.getVersionCode(getApplicationContext())) {
                            Log.d("zwx","download url:"+versionBean.getDownload_url());
                            return UIData.create().setDownloadUrl("http://192.168.0.59:8080/postal_tracking/postaltracking.apk")
                                    .setTitle("版本更新").setContent(versionBean.getContent());
                        } else {
//                            runnable = new Runnable() {
//                                @Override
//                                public void run() {
//                                    InLoginActivity = true;
                                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                                    finish();
//                                }
//                            };
//                            handler.postDelayed(runnable,3000);
                            return null;
                        }
                    }

                    @Override
                    public void onRequestVersionFailure(String message) {
                        finish();
                        ToastUtil.showToastShort(getApplicationContext(), "版本更新錯誤,請檢查網絡后再試");
                        Log.d("zwx","down"+message);
                    }
                });
        builder.excuteMission(this);
//        builder.setForceUpdateListener(new ForceUpdateListener() {
//            @Override
//            public void onShouldForceUpdate() {
//                 finish();
//            }
//        });


    }

    /**
     * 閃屏頁上按鈕點擊事件
     */
    @OnClick(R.id.btn_splash_enter_activity)
    public void onViewClicked() {
        InLoginActivity = true;
        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        finish();
        handler.removeCallbacks(runnable);
    }
}
