package com.example.veeotech.postaltracking.warehouse;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.android.tu.loadingdialog.LoadingDailog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.veeotech.postaltracking.BaseActivity;
import com.example.veeotech.postaltracking.R;
import com.example.veeotech.postaltracking.utils.HttpUtils;
import com.example.veeotech.postaltracking.utils.IntentKeyUtils;
import com.example.veeotech.postaltracking.utils.ToastUtil;
import com.example.veeotech.postaltracking.warehouse.adapter.CustomerAdapter;
import com.example.veeotech.postaltracking.warehouse.bean.CustomerIdBean;
import com.example.veeotech.postaltracking.warehouse.bean.OrderBean;
import com.example.veeotech.postaltracking.warehouse.bean.TypeInfoBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by VeeoTech on 2018/5/4.
 */

public class CustomerActivity extends BaseActivity {
    @BindView(R.id.rv_warehouse_select_customer)
    RecyclerView rvWarehouseSelectCustomer;
    private List<CustomerIdBean.DataBean> OrderId_List;
    private CustomerAdapter customerAdapter;
    private LoadingDailog loadingDialog;
    private LoadingDailog.Builder loadBuilder;
    private String package_id;
    private String order_id;
    private Context context = this;
    private List<TypeInfoBean.CargoBean> cargoBeanList;
    @Override
    protected void init() {

        setToolBarTitle("倉庫員");
        if (null != getIntent()) {
            package_id = getIntent().getStringExtra(IntentKeyUtils.INTENT_ORDER_RESULT);
            cargoBeanList = new ArrayList<>();
            cargoBeanList = (List<TypeInfoBean.CargoBean>) getIntent().getSerializableExtra(IntentKeyUtils.INTENT_ORDER_ID);
        } else {
            finish();
            ToastUtil.showToastShort(this, "網絡鏈接錯誤,請檢查網絡設置後重試");
        }
        //加載loading對話框
        loadBuilder=new LoadingDailog.Builder(this)
                .setCancelable(false)
                .setCancelOutside(false)
                .setMessage("加載中...")
                .setShowMessage(true);
        loadingDialog=loadBuilder.create();

        //接受intent跳轉傳遞信息
        //獲取customer id
        getCustomer_id(package_id);

        //創建布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvWarehouseSelectCustomer.setLayoutManager(layoutManager);

        //創建適配器
        OrderId_List = new ArrayList<>();
        customerAdapter = new CustomerAdapter(OrderId_List);

        //給RecyclerView設置適配器
        rvWarehouseSelectCustomer.setAdapter(customerAdapter);

        customerAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Intent intent = new Intent(context,CargoSortActivity.class);
                intent.putExtra("customer_id",OrderId_List.get(position).getCustomer_id());
                intent.putExtra("order_id",OrderId_List.get(position).getOrder_id());
                intent.putExtra("package_id",package_id);
                intent.putExtra(IntentKeyUtils.INTENT_ORDER_ID,(Serializable) cargoBeanList);
                context.startActivity(intent);
                Log.d("zwx","OrderInfo"+OrderId_List.get(position).getOrder_id());
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_select_customer;
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
        ButterKnife.bind(this);
    }

    public void getCustomer_id(final String package_id) {
        loadingDialog.show();
        Call<CustomerIdBean> orderBeanCall = HttpUtils.getIserver().getCustomerId(package_id);
        orderBeanCall.enqueue(new Callback<CustomerIdBean>() {
            @Override
            public void onResponse(Call<CustomerIdBean> call, Response<CustomerIdBean> response) {
                CustomerIdBean dataBean = response.body();
                if (dataBean.getFlag() == 1) {
                    for(CustomerIdBean.DataBean dataBean1 : dataBean.getData()) {
                        OrderId_List.add(dataBean1);
                    }
                    customerAdapter.notifyDataSetChanged();
                } else if (dataBean.getFlag() == -1) {
                }
                if(OrderId_List.size()==1){
                    Intent intent = new Intent(context,CargoSortActivity.class);
                    intent.putExtra("customer_id",OrderId_List.get(0).getCustomer_id());
                    intent.putExtra("order_id",OrderId_List.get(0).getOrder_id());
                    intent.putExtra("package_id",package_id);
                    intent.putExtra(IntentKeyUtils.INTENT_ORDER_ID, (Serializable) cargoBeanList);
                    finish();
                    startActivity(intent);
                }
                loadingDialog.dismiss();
            }

            @Override
            public void onFailure(Call<CustomerIdBean> call, Throwable t) {
                loadingDialog.dismiss();
                finish();
                ToastUtil.showToastShort(getApplicationContext(),"網絡繁忙請稍後再試");
            }
        });
    }
}

