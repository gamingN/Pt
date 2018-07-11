package com.example.veeotech.postaltracking.pickup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.tu.loadingdialog.LoadingDailog;
import com.example.veeotech.postaltracking.BaseActivity;
import com.example.veeotech.postaltracking.R;
import com.example.veeotech.postaltracking.pickup.adapter.PackageListAdapter;
import com.example.veeotech.postaltracking.pickup.bean.ContentNumBean;
import com.example.veeotech.postaltracking.pickup.bean.PackageInfoBean;
import com.example.veeotech.postaltracking.pickup.bean.PackageListBean;
import com.example.veeotech.postaltracking.pickup.bean.PackageNumBean;
import com.example.veeotech.postaltracking.utils.AlertDialogUtil;
import com.example.veeotech.postaltracking.utils.HttpUtils;
import com.example.veeotech.postaltracking.utils.IntentKeyUtils;
import com.example.veeotech.postaltracking.utils.NetUtils;
import com.example.veeotech.postaltracking.utils.SPUtils;
import com.example.veeotech.postaltracking.utils.ToastUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by VeeoTech on 27/4/2018.
 * 訂單列表按鈕
 */

public class PackageListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener{
    @BindView(R.id.rv_package_list)
    RecyclerView rvPackageList;
    @BindView(R.id.tv_package_list_pn)
    TextView tvPackageListPn;
    @BindView(R.id.tv_package_list_an)
    TextView tvPackageListAn;
    @BindView(R.id.bt_package_list_commit)
    Button btPackageListCommit;
    @BindView(R.id.tv_package_list_orderId)
    TextView tvPackageListOrderId;
    @BindView(R.id.srfl_pack_list)
    SwipeRefreshLayout swipeRefreshLayout;

    private String str_order_id;

    /**
     * 訂單列表的bean
     */
    private List<PackageListBean> lists;

    /**
     * 訂單列表的適配器
     */
    private PackageListAdapter packageListAdapter;

    private BroadcastReceiver bordcastReceiver;
    private LocalBroadcastManager broadcastManager;

    /**
     * 不同介面進入的flag
     */
    private String packageFlag;

    private LoadingDailog.Builder loadBuilder;

    private LoadingDailog loadingDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadBuilder = new LoadingDailog.Builder(PackageListActivity.this)
                .setCancelable(false)
                .setCancelOutside(false)
                .setMessage("加載中...")
                .setShowMessage(true);
        loadingDialog = loadBuilder.create();


        packageFlag=getIntent().getStringExtra(IntentKeyUtils.INTENT_PACKAGE_FLAG);
        if(packageFlag.equals("1")){
            btPackageListCommit.setVisibility(View.INVISIBLE);
        }else if(packageFlag.equals("2")){
            btPackageListCommit.setVisibility(View.VISIBLE);
        }

        lists=new ArrayList<>();
        str_order_id = getIntent().getStringExtra(IntentKeyUtils.INTENT_ORDER_RESULT);
        tvPackageListOrderId.setText("訂單號:"+str_order_id);
        SPUtils.put(getApplicationContext(),IntentKeyUtils.SP_ORDER_ID,str_order_id);
        require(str_order_id);

        packageListAdapter = new PackageListAdapter(lists);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorTheme));
        rvPackageList.setLayoutManager(new LinearLayoutManager(this));
        rvPackageList.setAdapter(packageListAdapter);
        swipeRefreshLayout.setOnRefreshListener(this);

        broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.LIST_BROADCAST");
        bordcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                require(str_order_id);
            }
        };
        broadcastManager.registerReceiver(bordcastReceiver, intentFilter);
    }

    /**
     * 請求訂單的所有袋號
     * @param orderId
     */
    private void require(String orderId) {
        loadingDialog.show();
        Call<String> call = HttpUtils.getIserver().getPackageAll(orderId);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.body().equals("-1")) {
                    lists.clear();
                    refresh(lists);
                    requireContentNum(str_order_id);
                    loadingDialog.dismiss();
//                    Toast.makeText(getActivity(), "沒有數據", Toast.LENGTH_SHORT).show();
                } else {
                    lists.clear();
                    Gson gson = new Gson();
                    List<PackageListBean> packlistlists = gson.fromJson(response.body().toString(), new TypeToken<List<PackageListBean>>() {
                    }.getType());
                    for (PackageListBean packlist : packlistlists) {
                        lists.add(packlist);
                    }
                    refresh(lists);
                    requireContentNum(str_order_id);
                    loadingDialog.dismiss();
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                ToastUtil.showToastShort(getApplicationContext(),"獲取數據失敗,請檢查網絡!");
                loadingDialog.dismiss();
            }
        });
    }

    @Override
    protected void init() {
        setToolBarTitle("訂單中所有袋");

    }

    @Override
    protected int getLayoutId() {
        return R.layout.package_list_activity;
    }

    @Override
    protected boolean isShowBacking() {
        return true;
    }

    @Override
    protected boolean isShowRightText() {
        return false;
    }

    /**
     * 提交按鈕點擊事件
     */
    @OnClick(R.id.bt_package_list_commit)
    public void onViewClicked() {
        if (NetUtils.isConnected(getApplicationContext())) {
            AlertDialogUtil dialogUtil = new AlertDialogUtil(PackageListActivity.this);
            dialogUtil.showDialog("是否確認提交此訂單？");
            dialogUtil.setDialogPositiveButtonListener(new AlertDialogUtil.DialogPositiveButtonListener() {
                @Override
                public void setDialogPositiveButtonListener() {
                    loadingDialog.show();
                    orderComfirm(str_order_id);
                }
            });
        }else{
            ToastUtil.showToastShort(getApplicationContext(),"當前網絡不可用,請檢查網絡!");
        }
    }

    /**
     * 訂單提交請求
     * @param str_order_id
     */
    private void orderComfirm(String str_order_id) {
        Call<String> call = HttpUtils.getIserver().orderComfirm(str_order_id);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.body().equals("1")){
                    loadingDialog.dismiss();
                    ToastUtil.showToastShort(getApplicationContext(),"此訂單已完成!");
                    Intent intent=new Intent(PackageListActivity.this,PickUpActivity.class);
                    startActivity(intent);
                }else{
                    loadingDialog.dismiss();
                    ToastUtil.showToastShort(getApplicationContext(),"提交失敗!");
                    finish();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                loadingDialog.dismiss();
                ToastUtil.showToastShort(getApplicationContext(),"網絡請求失敗,請檢查網絡狀態");
            }
        });

    }

    /**
     * 刷新數據
     * @param packageBeanList
     */
    public void refresh(List<PackageListBean> packageBeanList) {
        this.lists = packageBeanList;
        packageListAdapter.notifyDataSetChanged();

    }

    /**
     * 重寫SwipeRefreshLayout的刷新事件
     */
    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                lists.clear();
//                packageListAdapter.notifyDataSetChanged();
                require(str_order_id);
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 700);
    }

    @Override
    public void onResume() {
        super.onResume();
        swipeRefreshLayout.setRefreshing(true);
        onRefresh();
        requireContentNum(str_order_id);
    }

    /**
     * 請求內容數量
     * @param str_order_id
     */
    private void requireContentNum(String str_order_id) {
        Call<ContentNumBean> call = HttpUtils.getIserver().requireContentNum(str_order_id);
        call.enqueue(new Callback<ContentNumBean>() {
            @Override
            public void onResponse(Call<ContentNumBean> call, Response<ContentNumBean> response) {
                if(response.body().getFlag()==1){
                    tvPackageListPn.setText("總袋數:"+response.body().getData().get(0).getTotal_package());
                    if(TextUtils.isEmpty(response.body().getData().get(0).getTotal_content())){
                        tvPackageListAn.setText("總件數:0");
                    }else {
                        tvPackageListAn.setText("總件數:" + response.body().getData().get(0).getTotal_content());
                    }
                }
            }

            @Override
            public void onFailure(Call<ContentNumBean> call, Throwable t) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        broadcastManager.unregisterReceiver(bordcastReceiver);
    }
}
