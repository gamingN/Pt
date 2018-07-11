package com.example.veeotech.postaltracking.pickup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.tu.loadingdialog.LoadingDailog;
import com.example.veeotech.postaltracking.BaseActivity;
import com.example.veeotech.postaltracking.R;
import com.example.veeotech.postaltracking.pickup.bean.OrderCreateBean;
import com.example.veeotech.postaltracking.pickup.bean.PackageNumBean;
import com.example.veeotech.postaltracking.utils.HttpUtils;
import com.example.veeotech.postaltracking.utils.IntentKeyUtils;
import com.example.veeotech.postaltracking.utils.NetUtils;
import com.example.veeotech.postaltracking.utils.ToastUtil;

import butterknife.BindView;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by VeeoTech on 18/4/2018.
 * 已廢用
 */

public class OrderInfoAcitivity extends BaseActivity {
    @BindView(R.id.ed_package_qty)
    EditText ed_package_qty;

    @BindView(R.id.ed_total_qty)
    EditText ed_total_qty;

    @BindView(R.id.bt_orderinfo_commit)
    Button bt_orderinfo_commit;

    @BindView(R.id.tv_orderinfo_orderid)
    TextView tv_orderinfo_orderid;

    private String str_package_qty;
    private String str_total_qty;

    private String str_order_id;

    private String str_package_flag;

    private LoadingDailog.Builder loadBuilder;

    private LoadingDailog loadingDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadBuilder = new LoadingDailog.Builder(OrderInfoAcitivity.this)
                .setCancelable(false)
                .setCancelOutside(false)
                .setMessage("加載中...")
                .setShowMessage(true);
        loadingDialog = loadBuilder.create();

        str_package_flag = getIntent().getStringExtra(IntentKeyUtils.INTENT_PACKAGE_FLAG);

        str_order_id = getIntent().getStringExtra(IntentKeyUtils.INTENT_ORDER_RESULT);

        tv_orderinfo_orderid.setText("訂單號:" + str_order_id);

        packageNumRequire(str_order_id);

    }

    private void packageNumRequire(String str_order_id) {
        Call<PackageNumBean> call = HttpUtils.getIserver().requirePackageNum(str_order_id);
        call.enqueue(new Callback<PackageNumBean>() {
            @Override
            public void onResponse(Call<PackageNumBean> call, Response<PackageNumBean> response) {
                if (response.body().getFlag() == 1) {
                    String str_package = response.body().getData().get(0).getPackage_qty();
                    String str_total = response.body().getData().get(0).getTotal_qty();
                    if (!TextUtils.isEmpty(str_package) && !TextUtils.isEmpty(str_total)) {
                        ed_package_qty.setText(str_package);
                        ed_total_qty.setText(str_total);
                    }
                } else {

                }
            }

            @Override
            public void onFailure(Call<PackageNumBean> call, Throwable t) {

            }
        });
    }

    @OnClick(R.id.bt_orderinfo_commit)
    public void onclik_commit(View view) {

        if (NetUtils.isConnected(getApplicationContext())) {

            loadingDialog.show();

            str_package_qty = ed_package_qty.getText().toString();
            str_total_qty = ed_total_qty.getText().toString();

            if (TextUtils.isEmpty(str_package_qty) && TextUtils.isEmpty(str_total_qty)) {
                if (str_package_flag.equals("1")) {
                    finish();
                } else if (str_package_flag.equals("2")) {
                    Intent intent = new Intent(OrderInfoAcitivity.this, PickUpActivity.class);
                    startActivity(intent);
                }

            } else if (TextUtils.isEmpty(str_package_qty) || TextUtils.isEmpty(str_total_qty)) {
                ToastUtil.showToastShort(getApplicationContext(), "請同時輸入總袋數和總件數");
            } else {
                orderInfoRequest(str_order_id, str_package_qty, str_total_qty);
            }
        } else {
            ToastUtil.showToastShort(getApplicationContext(),"當前網絡不可用,請檢查網絡!");
        }

    }

    private void orderInfoRequest(String order_id, String package_qty, String total_qty) {
        Call<OrderCreateBean> call = HttpUtils.getIserver().alterOrderInfo(order_id, package_qty, total_qty);
        call.enqueue(new Callback<OrderCreateBean>() {
            @Override
            public void onResponse(Call<OrderCreateBean> call, Response<OrderCreateBean> response) {
                if (response.body().getFlag() == 1) {
                    loadingDialog.dismiss();
                    ToastUtil.showToastShort(getApplicationContext(), "已更新訂單數據");

                    if (str_package_flag.equals("1")) {
                        finish();
                    } else if (str_package_flag.equals("2")) {
                        Intent intent = new Intent(OrderInfoAcitivity.this, PickUpActivity.class);
                        startActivity(intent);
                    }

                } else {
                    loadingDialog.dismiss();
                    ToastUtil.showToastShort(getApplicationContext(), "訂單修改錯誤");
                    finish();
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
        setToolBarTitle("記錄袋數/件數");

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_order_info;
    }

    @Override
    protected boolean isShowBacking() {
        return true;
    }

    @Override
    protected boolean isShowRightText() {
        return false;
    }
}
