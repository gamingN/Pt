package com.example.veeotech.postaltracking.pickup;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.veeotech.postaltracking.BaseActivity;
import com.example.veeotech.postaltracking.R;
import com.example.veeotech.postaltracking.pickup.bean.PackageInfoBean;
import com.example.veeotech.postaltracking.utils.AlertDialogUtil;
import com.example.veeotech.postaltracking.utils.HttpUtils;
import com.example.veeotech.postaltracking.utils.IntentKeyUtils;
import com.example.veeotech.postaltracking.utils.ScannerUtils;
import com.example.veeotech.postaltracking.utils.ToastUtil;
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
 * Created by VeeoTech on 18/4/2018.
 * 已廢用
 */

public class PackageActivity extends BaseActivity {


    @BindView(R.id.bt_package_create)
    Button btPackageCreate;
    @BindView(R.id.tv_package_id)
    TextView tvPackageId;
    @BindView(R.id.bt_package_commit)
    Button btPackageCommit;
    @BindView(R.id.tv_package_order)
    TextView tvPackageOrder;
    @BindView(R.id.bt_package_scanner_two)
    Button btPackageScannerTwo;


    private String order_id;

    private IntentIntegrator integrator;

    private RxPermissions rxPermissions;
    private String result_scanner;

    @Override
    protected void init() {
        setToolBarTitle("生成袋條碼");
        order_id = getIntent().getStringExtra(IntentKeyUtils.INTENT_ORDER_ID);
        tvPackageOrder.setText("訂單號:" + order_id);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_package;
    }

    @Override
    protected boolean isShowBacking() {
        return true;
    }

    @Override
    protected boolean isShowRightText() {
        return false;
    }


    @OnClick({R.id.bt_package_create,R.id.bt_package_commit,R.id.bt_package_scanner_two})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bt_package_create:
                AlertDialogUtil dialogUtil = new AlertDialogUtil(PackageActivity.this);
                dialogUtil.showDialog("是否生成一個袋子條碼？");
                dialogUtil.setDialogPositiveButtonListener(new AlertDialogUtil.DialogPositiveButtonListener() {
                    @Override
                    public void setDialogPositiveButtonListener() {
                        packageCreate(order_id);
                    }
                });
                break;
            case R.id.bt_package_commit:
                finish();
                break;
            case R.id.bt_package_scanner_two:
                rxPermissions=new RxPermissions(PackageActivity.this);
                rxPermissions.requestEach(Manifest.permission.CAMERA)
                        .subscribe(new Consumer<Permission>() {
                            @Override
                            public void accept(Permission permission) throws Exception {
                                if (permission.granted) {
                                    new ScannerUtils(PackageActivity.this,integrator).selectActivity();
                                }else {
                                    ToastUtil.showToastShort(getApplicationContext(), "沒有權限");
                                }
                            }
                        });
                break;
        }
    }

    private void packageCreate(String order_id) {
        Call<PackageInfoBean> call = HttpUtils.getIserver().getPackageInfo(order_id);
        call.enqueue(new Callback<PackageInfoBean>() {
            @Override
            public void onResponse(Call<PackageInfoBean> call, Response<PackageInfoBean> response) {
                if (response.body().getFlag() == 1) {
                    String str_packageid = response.body().getData().get(0).getPackage_id();
                    tvPackageId.setText("袋子條碼:" + str_packageid);
                    ToastUtil.showToastShort(getApplicationContext(),"已成功生成袋子條碼");
                } else {
//                    ToastUtil.showToastShort(getApplicationContext(), "訂單生成錯誤");
                }
            }

            @Override
            public void onFailure(Call<PackageInfoBean> call, Throwable t) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                ToastUtil.showToastShort(this, "掃碼取消！");
            } else {
                result_scanner =result.getContents();
                tvPackageId.setText("袋子條碼:"+result_scanner);
                ToastUtil.showToastShort(getApplicationContext(),"已成功生成袋子條碼");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        integrator=null;
        super.onDestroy();
    }

}
