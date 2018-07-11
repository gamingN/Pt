package com.example.veeotech.postaltracking.warehouse;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.android.tu.loadingdialog.LoadingDailog;
import com.example.veeotech.postaltracking.BaseActivity;
import com.example.veeotech.postaltracking.R;
import com.example.veeotech.postaltracking.utils.HttpUtils;
import com.example.veeotech.postaltracking.utils.IntentKeyUtils;
import com.example.veeotech.postaltracking.utils.ToastUtil;
import com.example.veeotech.postaltracking.warehouse.adapter.CargoAdapter;
import com.example.veeotech.postaltracking.warehouse.bean.FlagBean;
import com.example.veeotech.postaltracking.warehouse.bean.OrderBean;
import com.example.veeotech.postaltracking.warehouse.bean.TypeInfoBean;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by VeeoTech on 2018/4/19.
 */

public class CargoSortActivity extends BaseActivity implements AbsListView.OnScrollListener {
    @BindView(R.id.tv_order_by_id)
    TextView tvCustomerById;
    @BindView(R.id.btn_warehouse_cargo_commit)
    Button btnWarehouseCargoCommit;
    @BindView(R.id.lv_warehouse_cargo)
    ListView lvWarehouseCargo;
    @BindView(R.id.ib_warehouse_editable)
    ImageButton ibWarehouseEditable;
    private long firstTime = 0;
    private String customerid;
    private String packge_id;
    public String order_id;
    private CargoAdapter cargoAdapter;
    private ImageButton iv_warehouse_editable;
    private List<TypeInfoBean.CargoBean> cargoBeanList;
    private List<TypeInfoBean.CargoBean> TypeCargoBeanList;
    OnFinishListener onFinishListener;
    private LoadingDailog.Builder loadBuilder;

    private LoadingDailog loadingDialog;

    private boolean isEditable = false;
    @Override
    protected void init() {
        setToolBarTitle("倉庫員");
        loadBuilder = new LoadingDailog.Builder(this)
                .setCancelable(false)
                .setCancelOutside(false)
                .setMessage("加載中...")
                .setShowMessage(true);
        loadingDialog = loadBuilder.create();

        TypeCargoBeanList = new ArrayList<>();
        lvWarehouseCargo.setOnScrollListener(this);
        if (null != getIntent()) {
            packge_id = getIntent().getStringExtra(IntentKeyUtils.INTENT_ORDER_RESULT);
            Log.d("zwx", "qwer" + packge_id);
            customerid = getIntent().getStringExtra("customer_id");
            order_id = getIntent().getStringExtra("order_id");
            cargoBeanList = new ArrayList<>();
            cargoBeanList = (List<TypeInfoBean.CargoBean>) getIntent().getSerializableExtra(IntentKeyUtils.INTENT_ORDER_ID);
            tvCustomerById.setText("Customer ID :" + customerid);
        } else {
            finish();
            ToastUtil.showToastShort(this, "網絡鏈接錯誤,請檢查網絡設置後重試");
        }
        Map<String, String> cargoInfo = new TreeMap<String, String>();
        for (int i = 0; i < cargoBeanList.size(); i++) {
            cargoInfo.put(String.valueOf(cargoBeanList.get(i).getType()), cargoBeanList.get(i).getWeight());
        }
        Gson gson = new Gson();
        String typeJson = gson.toJson(cargoInfo);
        Log.d("zwx", typeJson + "");
        getTypeWeight(order_id, typeJson);
        onFinishListener = new OnFinishListener() {
            @Override
            public void OnFinish() {
                loadingDialog.show();
                cargoAdapter = new CargoAdapter(getApplicationContext(), TypeCargoBeanList);
                lvWarehouseCargo.setAdapter(cargoAdapter);
//                loadingDialog.dismiss();
            }
        };
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_cargo;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_warehouse_cargo_commit)
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_warehouse_cargo_commit:

                Map<String, String> cargoInfo = new HashMap<String, String>();
                for (int i = 0; i < cargoAdapter.getCount(); i++) {
                    Log.d("zwx","map123"+cargoAdapter.getList(i).getWeight());
                    if(cargoAdapter.getList(i).getWeight().equals("")){
                        cargoInfo.put(cargoAdapter.getList(i).getType(), "0");
                    }else {
                        cargoInfo.put(cargoAdapter.getList(i).getType(), cargoAdapter.getList(i).getWeight());
                    }
                }
                Gson gson = new Gson();
                String JsonStr = gson.toJson(cargoInfo);
                Log.d("tag", JsonStr + "");
                SendCargo(this, order_id, JsonStr);
                break;
        }
    }

    public void SendCargo(final Context context, String order_id, final String Json) {
        loadingDialog.show();
        Call<FlagBean> flagBeanCall = HttpUtils.getIserver().sendCargoInfo(customerid,order_id, Json);
        flagBeanCall.enqueue(new Callback<FlagBean>() {
            @Override
            public void onResponse(Call<FlagBean> call, Response<FlagBean> response) {
                FlagBean flagBean = response.body();
                if (flagBean.getFlag() == 1) {
                    Intent intent = new Intent(CargoSortActivity.this,SelectWayActivity.class);
                    startActivity(intent);
                    finish();
                    ToastUtil.showToastShort(context, "提交成功");
                } else if (flagBean.getFlag() == -1) {
                    ToastUtil.showToastShort(context, "提交失敗,請重新提交");
                }
                loadingDialog.dismiss();
            }

            @Override
            public void onFailure(Call<FlagBean> call, Throwable t) {
                Log.d("tag", "" + t.toString());
                loadingDialog.dismiss();
                ToastUtil.showToastShort(context, "網絡鏈接錯誤,請檢查網絡設置後重試.");
            }
        });
    }


    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        switch (i) {
            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:

                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(CargoSortActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                break;
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {

    }

    public void getTypeWeight(String order_id, final String Json) {
        loadingDialog.show();
        Call<TypeInfoBean> typeInfoBeanCall = HttpUtils.getIserver().getTypeWeight(order_id, Json);
        typeInfoBeanCall.enqueue(new Callback<TypeInfoBean>() {
            @Override
            public void onResponse(Call<TypeInfoBean> call, Response<TypeInfoBean> response) {
                TypeInfoBean typeInfoBean = response.body();
                if (typeInfoBean.getFlag() == 1) {
                    if (typeInfoBean.getData().size() == 0) {
                        ToastUtil.showToastShort(getApplicationContext(), "網絡鏈接不穩定,請稍後再試");
                        finish();
                        return;
                    }
                    for (TypeInfoBean.CargoBean cargoBean : typeInfoBean.getData()) {
                        TypeCargoBeanList.add(cargoBean);
                    }
                    onFinishListener.OnFinish();
                    loadingDialog.dismiss();
                } else if (typeInfoBean.getFlag() == -1) {
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<TypeInfoBean> call, Throwable t) {
                finish();
                ToastUtil.showToastShort(getApplicationContext(), "網絡鏈接錯誤,請檢查網絡設置後重試.");
                loadingDialog.dismiss();
            }
        });
    }


    @OnClick(R.id.ib_warehouse_editable)
    public void onViewClicked() {
        if(!isEditable) {
            isEditable = true;
            cargoAdapter.setEditable(true);
        }else if(isEditable){
            isEditable = false;
            cargoAdapter.setEditable(false);
        }
    }
}

