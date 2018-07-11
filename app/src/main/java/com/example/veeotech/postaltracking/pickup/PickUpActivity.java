package com.example.veeotech.postaltracking.pickup;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.tu.loadingdialog.LoadingDailog;
import com.example.veeotech.postaltracking.BaseActivity;
import com.example.veeotech.postaltracking.R;
import com.example.veeotech.postaltracking.pickup.bean.OrderCreateBean;
import com.example.veeotech.postaltracking.posHand.PosScanner;
import com.example.veeotech.postaltracking.posHand.ScanService;
import com.example.veeotech.postaltracking.utils.HttpUtils;
import com.example.veeotech.postaltracking.utils.IntentKeyUtils;
import com.example.veeotech.postaltracking.utils.NetUtils;
import com.example.veeotech.postaltracking.utils.SPUtils;
import com.example.veeotech.postaltracking.utils.ScannerUtils;
import com.example.veeotech.postaltracking.utils.ToastUtil;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.functions.Consumer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by VeeoTech on 17/4/2018.
 * 掃描用戶生成訂單的介面
 */

public class PickUpActivity extends BaseActivity {


    @BindView(R.id.bt_scanner_two)
    Button bt_scanner_two;

    @BindView(R.id.bt_check_old)
    Button bt_check_old;

    private IntentIntegrator integrator;

    private long firstTime = 0;

    //掃描後得到的結果
    private String result_scanner;

    private byte mGpioPower = 0x1E;//PB14
    private byte mGpioTrig = 0x29;//PC9

    private BroadcastReceiver bordcastReceiver;
    private LocalBroadcastManager broadcastManager;

    //請求回來的order_id
    private String result_orderid;

    private LoadingDailog.Builder loadBuilder;

    private LoadingDailog loadingDialog;

    //登錄進系統的員工id
    private String str_uid;


    @Override
    protected void onResume() {
        super.onResume();
        new PosScanner(this).initPos();
        SPUtils.put(getApplicationContext(), IntentKeyUtils.SP_RESULT_BACK, "1");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SPUtils.put(getApplicationContext(), IntentKeyUtils.SP_RESULT_BACK, "1");

        loadBuilder = new LoadingDailog.Builder(PickUpActivity.this)
                .setCancelable(false)
                .setCancelOutside(false)
                .setMessage("加載中...")
                .setShowMessage(true);
        loadingDialog = loadBuilder.create();

        new PosScanner(this).initPos();

        /**
         * 掃描按鈕監聽時間
         */
        bt_scanner_two.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (NetUtils.isConnected(getApplicationContext())) {

                    /**
                     * 判斷設備是否為手持機
                     */
                    if (PosScanner.initScanner_flag == 1) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN: {
                                ScanService.mApi.gpioControl(mGpioTrig, 0, 0);
                                break;
                            }
                            case MotionEvent.ACTION_UP: {
                                ScanService.mApi.gpioControl(mGpioTrig, 0, 1);
                                break;
                            }
                            default:
                                break;
                        }
                    } else {
                        RxPermissions rxPermissions = new RxPermissions(PickUpActivity.this);
                        rxPermissions.requestEach(Manifest.permission.CAMERA)
                                .subscribe(new Consumer<Permission>() {
                                    @Override
                                    public void accept(Permission permission) throws Exception {
                                        if (permission.granted) {
                                            new ScannerUtils(PickUpActivity.this, integrator).selectActivity();
                                        } else {
                                            ToastUtil.showToastShort(getApplicationContext(), "沒有權限");
                                        }
                                    }
                                });
                    }
                } else {
                    ToastUtil.showToastShort(getApplicationContext(),"當前網絡不可用,請檢查網絡!");
                }

                return false;
            }
        });

        /**
         * 接收掃描槍發送的廣播
         */
        broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.MEDICAL_BROADCAST");
        bordcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                loadingDialog.show();
                result_scanner = intent.getStringExtra(IntentKeyUtils.RESULT_BROADCAST);
                Log.e("KIKI", "onReceive: " + result_scanner);
                orderprovider(result_scanner,str_uid);
            }
        };
        broadcastManager.registerReceiver(bordcastReceiver, intentFilter);

        /**
         * 查看未完成訂單按钮事件
         */
        bt_check_old.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(PickUpActivity.this,CheckOrderActivity.class);
                intent.putExtra(IntentKeyUtils.INTENT_UID,str_uid);
                startActivity(intent);
            }
        });

    }

    /**
     * 用攝像頭掃碼的回調
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Log.e("KIKI", "onActivityResult: 進入1");
            } else if (result.getContents() != null) {
                loadingDialog.show();
                Log.e("KIKI", "onActivityResult: 進入2");
                result_scanner = result.getContents();
                orderprovider(result_scanner,str_uid);
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * 生成訂單號請求
     * @param customer_id
     * @param str_uid
     */
    private void orderprovider(final String customer_id,String str_uid) {
        Call<OrderCreateBean> call = HttpUtils.getIserver().getOrderProvider(customer_id,str_uid);
        call.enqueue(new Callback<OrderCreateBean>() {
            @Override
            public void onResponse(Call<OrderCreateBean> call, Response<OrderCreateBean> response) {
                if (response.body().getFlag() == 1) {
                    result_orderid = response.body().getData().get(0).getOrder_id();
                    Intent intent = new Intent(PickUpActivity.this, OrderMixActivity.class);
                    intent.putExtra(IntentKeyUtils.INTENT_ORDERMIX, result_orderid);
                    intent.putExtra(IntentKeyUtils.INTENT_CUSTOMER_ID,customer_id);
                    startActivity(intent);
                    ToastUtil.showToastShort(getApplicationContext(), "訂單生成成功");
                    loadingDialog.dismiss();
                } else {
                    ToastUtil.showToastShort(getApplicationContext(), "訂單生成錯誤");
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<OrderCreateBean> call, Throwable t) {
                    loadingDialog.dismiss();
                    ToastUtil.showToastShort(getApplicationContext(),"網絡請求失敗,請檢查網絡狀態");
            }
        });
    }


    @Override
    protected void init() {
        str_uid= (String) SPUtils.get(getApplicationContext(),IntentKeyUtils.SP_LOGIN_USER,"");
        setToolBarTitle("收貨員:"+str_uid);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_pickup;
    }

    @Override
    protected boolean isShowBacking() {
        return false;
    }

    @Override
    protected boolean isShowRightText() {
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 2000) {
                ToastUtil.showToastShort(getApplicationContext(), "再按一次退出程序");
                firstTime = secondTime;
                return true;
            } else {
                System.exit(0);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        integrator = null;
        broadcastManager.unregisterReceiver(bordcastReceiver);
    }


}
