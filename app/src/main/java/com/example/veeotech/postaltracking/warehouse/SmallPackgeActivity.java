package com.example.veeotech.postaltracking.warehouse;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.tu.loadingdialog.LoadingDailog;
import com.example.veeotech.postaltracking.BaseActivity;
import com.example.veeotech.postaltracking.R;
import com.example.veeotech.postaltracking.pickup.PickUpActivity;
import com.example.veeotech.postaltracking.posHand.PosScanner;
import com.example.veeotech.postaltracking.posHand.ScanService;
import com.example.veeotech.postaltracking.utils.AlertDialogUtil;
import com.example.veeotech.postaltracking.utils.HttpUtils;
import com.example.veeotech.postaltracking.utils.IntentKeyUtils;
import com.example.veeotech.postaltracking.utils.NetUtils;
import com.example.veeotech.postaltracking.utils.SPUtils;
import com.example.veeotech.postaltracking.utils.ScannerUtils;
import com.example.veeotech.postaltracking.utils.ToastUtil;
import com.example.veeotech.postaltracking.warehouse.adapter.SmallPackgeAdapter;
import com.example.veeotech.postaltracking.warehouse.bean.FlagBean;
import com.example.veeotech.postaltracking.warehouse.bean.SmallPackgeBean;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.functions.Consumer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by VeeoTech on 2018/4/20.
 */

public class SmallPackgeActivity extends BaseActivity  {
    @BindView(R.id.tv_warehouse_country_id)
    TextView tvWarehouseCountryId;
    @BindView(R.id.lv_warehouse_packge_info)
    ListView lvWarehousePackgeInfo;
    @BindView(R.id.btn_warehouse_barcode)
    Button btnWarehouseBarcode;
    @BindView(R.id.btn_warehouse_smallpackge_finish)
    Button btnWarehouseSmallpackgeFinish;
    private long firstTime = 0;
    private String big_id;
    private IntentIntegrator integrator;
    private String result_scanner;
    public SmallPackgeAdapter adapter;
    boolean isOneShow = false;
    CallBackListener callBackListener;
    boolean isDeleteAble = true;

    private byte mGpioPower = 0x1E ;//PB14
    private byte mGpioTrig = 0x29 ;//PC9
    private AlertDialogUtil alertDialogUtil ;
    private BroadcastReceiver bordcastReceiver;
    private LocalBroadcastManager broadcastManager;

    private LoadingDailog.Builder loadBuilder;

    private LoadingDailog loadingDialog;

    int position;

    private String str_pos;

    @Override
    protected void init() {
        setToolBarTitle("倉庫員");

        loadBuilder=new LoadingDailog.Builder(SmallPackgeActivity.this)
                .setCancelable(false)
                .setCancelOutside(false)
                .setMessage("加載中...")
                .setShowMessage(true);
        loadingDialog=loadBuilder.create();

        broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.MEDICAL5_BROADCAST");
        bordcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                loadingDialog.show();
                Log.e("zwx","收到广播");
                result_scanner=intent.getStringExtra(IntentKeyUtils.RESULT_BROADCAST);
                updateSmallId(result_scanner);
            }
        };
        broadcastManager.registerReceiver(bordcastReceiver, intentFilter);

        new PosScanner(this).initPos();
        alertDialogUtil = new AlertDialogUtil(this);
        big_id = getIntent().getStringExtra(IntentKeyUtils.INTENT_ORDER_RESULT);
        str_pos = getIntent().getStringExtra(IntentKeyUtils.INTENT_POS_FLAG);

        updateSmallPackage(big_id);
        tvWarehouseCountryId.setText("國家編號 :"+big_id);
        callBackListener = new CallBackListener() {
            @Override
            public void onClickCallBack(View v) {
                position = Integer.parseInt(String.valueOf(v.getTag()));
                alertDialogUtil.showDialog("請確認是否刪除貨品信息");
                alertDialogUtil.setDialogPositiveButtonListener(new AlertDialogUtil.DialogPositiveButtonListener() {
                    @Override
                    public void setDialogPositiveButtonListener() {
                        loadingDialog.show();
                        deleteSmallPackge(adapter.getSmallId(position), position);
                    }
                });

            }
        };
        adapter = new SmallPackgeAdapter(this,callBackListener);

        lvWarehousePackgeInfo.setAdapter(adapter);

        SPUtils.put(getApplicationContext(),IntentKeyUtils.SP_RESULT_BACK,"5");
        if (NetUtils.isConnected(getApplicationContext())) {
            if (PosScanner.initScanner_flag == 1) {

            } else {
//                RxPermissions rxPermissions_barcode = new RxPermissions(SmallPackgeActivity.this);
//                rxPermissions_barcode.requestEach(Manifest.permission.CAMERA)
//                        .subscribe(new Consumer<Permission>() {
//                            @Override
//                            public void accept(Permission permission) throws Exception {
//                                if (permission.granted) {
//                                    new ScannerUtils(SmallPackgeActivity.this, integrator).selectActivity();
//                                    isOneShow = true;
//                                } else {
//                                    ToastUtil.showToastShort(getApplicationContext(), "沒有權限");
//                                }
//                            }
//                        });
            }
        }else{
            ToastUtil.showToastShort(getApplicationContext(),"當前網絡不可用,請檢查網絡!");
        }

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_samllpackge;
    }

    @Override
    protected boolean isShowBacking() {
        return true;
    }

    @Override
    protected boolean isShowRightText() {
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (TextUtils.isEmpty(result.getContents())) {
             //   ToastUtil.showToastShort(this,"掃碼取消");
            } else {
                loadingDialog.show();
                result_scanner = result. getContents();
                updateSmallId(result_scanner);
                if(isOneShow){
                    new ScannerUtils(SmallPackgeActivity.this, integrator).selectActivity();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @OnClick({R.id.btn_warehouse_barcode,R.id.btn_warehouse_smallpackge_finish})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_warehouse_barcode:
                RxPermissions rxPermissions_barcode = new RxPermissions(SmallPackgeActivity.this);
                rxPermissions_barcode.requestEach(Manifest.permission.CAMERA)
                        .subscribe(new Consumer<Permission>() {
                            @Override
                            public void accept(Permission permission) throws Exception {
                                if (permission.granted) {
                                    new ScannerUtils(SmallPackgeActivity.this, integrator).selectActivity();
                                    isOneShow = true;
                                } else {
                                    ToastUtil.showToastShort(getApplicationContext(),"沒有權限");
                                }
                            }
                        });
                break;
            case R.id.btn_warehouse_smallpackge_finish:
                finish();
                Intent intent = new Intent(SmallPackgeActivity.this,SelectWayActivity.class);
                startActivity(intent);
                break;
        }
    }

    public void updateSmallId(String small_id){
//        Log.e("zwx","发送请求"+String.valueOf(SPUtils.get(getApplicationContext(),IntentKeyUtils.SP_LOGIN_USER,"")));
        String aid= (String) SPUtils.get(getApplicationContext(),IntentKeyUtils.SP_LOGIN_USER,"");
        Call<SmallPackgeBean> call= HttpUtils.getIserver().getSmallPackgeInfo(big_id,small_id,aid);
        call.enqueue(new Callback<SmallPackgeBean>() {
            @Override
            public void onResponse(Call<SmallPackgeBean> call, Response<SmallPackgeBean> response) {
                SmallPackgeBean smallPackgeBean = response.body();
                if(smallPackgeBean.getFlag()==2){
                    adapter.clear();
                    for(SmallPackgeBean.SmallIdBean smallIdBean : smallPackgeBean.getData()){
                        adapter.add(smallIdBean);
                    }
                    loadingDialog.dismiss();
                    Log.e("zwx","获取数据");
                    ToastUtil.showToastShort(getApplicationContext(),"掃碼成功");
                }else if(smallPackgeBean.getFlag()==-1){
                    if(null!=smallPackgeBean.getData()){
                        adapter.clear();
                        for(SmallPackgeBean.SmallIdBean smallIdBean : smallPackgeBean.getData()){
                            adapter.add(smallIdBean);
                        }
                        loadingDialog.dismiss();
                        ToastUtil.showToastShort(getApplicationContext(),"貨品碼已存在");
                    }
                    loadingDialog.dismiss();
                }else if(smallPackgeBean.getFlag()==-2){
                    ToastUtil.showToastShort(getApplicationContext(),"貨品碼不能與箱子碼一樣");
                    loadingDialog.dismiss();
                }else if(smallPackgeBean.getFlag()==-4){
                    ToastUtil.showToastShort(getApplicationContext(),"貨品碼不存在或已被使用");
                    loadingDialog.dismiss();
                }else {
                    ToastUtil.showToastShort(getApplicationContext(),"掃碼失敗");
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<SmallPackgeBean> call, Throwable t) {
                loadingDialog.dismiss();
                ToastUtil.showToastShort(getApplicationContext(),"掃碼失敗,請檢查網絡是否連接.");
            }
        });
    }

    public void deleteSmallPackge(String small_id, final int position){
        Call<FlagBean> call = HttpUtils.getIserver().deleteSmallPackge(small_id);
        call.enqueue(new Callback<FlagBean>() {
            @Override
            public void onResponse(Call<FlagBean> call, Response<FlagBean> response) {
                FlagBean flagBean = response.body();
                if(flagBean.getFlag()==1){
                    adapter.remove(position);
                    ToastUtil.showToastShort(getApplicationContext(),"刪除貨物成功");
                    loadingDialog.dismiss();
                }else {
                    adapter.remove(position);
                    ToastUtil.showToastShort(getApplicationContext(),"刪除貨品失敗");
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<FlagBean> call, Throwable t) {
                Log.d("删除货品",t.toString()+"");
                loadingDialog.dismiss();
                ToastUtil.showToastShort(getApplicationContext(),"網絡鏈接錯誤,請檢查網絡設置後重試");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        new PosScanner(this).initPos();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        SPUtils.put(getApplicationContext(),IntentKeyUtils.SP_RESULT_BACK,"3");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        integrator=null;
        broadcastManager.unregisterReceiver(bordcastReceiver);
        SPUtils.put(getApplicationContext(),IntentKeyUtils.SP_RESULT_BACK,"3");
    }


    public void updateSmallPackage(String country_code){
        Log.e("zwx","发送请求");
        Call<SmallPackgeBean> call= HttpUtils.getIserver().getBigPackgeInfo(country_code,str_pos);
        call.enqueue(new Callback<SmallPackgeBean>() {
            @Override
            public void onResponse(Call<SmallPackgeBean> call, Response<SmallPackgeBean> response) {
                SmallPackgeBean smallPackgeBean = response.body();
                if(smallPackgeBean.getFlag()==1){
                    adapter.clear();
                    for(SmallPackgeBean.SmallIdBean smallIdBean : smallPackgeBean.getData()){
                        adapter.add(smallIdBean);
                    }
                    loadingDialog.dismiss();
                    Log.e("zwx","获取数据");
                    ToastUtil.showToastShort(getApplicationContext(),"掃碼成功");
                }else if(smallPackgeBean.getFlag()==-1 || smallPackgeBean.getFlag()==2){
                    if(null!=smallPackgeBean.getData()){
                        adapter.clear();
                        for(SmallPackgeBean.SmallIdBean smallIdBean : smallPackgeBean.getData()){
                            adapter.add(smallIdBean);
                        }
                    }
                    loadingDialog.dismiss();

                }else if(smallPackgeBean.getFlag()==-2){
                    loadingDialog.dismiss();
                }else if(smallPackgeBean.getFlag()==-3){
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<SmallPackgeBean> call, Throwable t) {
                loadingDialog.dismiss();
                ToastUtil.showToastShort(getApplicationContext(),"掃碼失敗,請檢查網絡是否連接.");
            }
        });
    }

}
