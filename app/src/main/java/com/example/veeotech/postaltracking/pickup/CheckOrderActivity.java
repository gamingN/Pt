package com.example.veeotech.postaltracking.pickup;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.android.tu.loadingdialog.LoadingDailog;
import com.example.veeotech.postaltracking.BaseActivity;
import com.example.veeotech.postaltracking.R;
import com.example.veeotech.postaltracking.pickup.adapter.CheckOrderAdapter;
import com.example.veeotech.postaltracking.pickup.adapter.PackageListAdapter;
import com.example.veeotech.postaltracking.pickup.bean.CheckOrderBean;
import com.example.veeotech.postaltracking.pickup.bean.PackageListBean;
import com.example.veeotech.postaltracking.utils.HttpUtils;
import com.example.veeotech.postaltracking.utils.IntentKeyUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by VeeoTech on 28/4/2018.
 * 未完成訂單介面
 */

public class CheckOrderActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener{
    @BindView(R.id.rv_check_order)
    RecyclerView rvCheckOrder;
    @BindView(R.id.srfl_check_order)
    SwipeRefreshLayout srflCheckOrder;

    private String str_uid;

    /**
     * 未完成訂單的bean
     */
    private List<CheckOrderBean> lists;

    /**
     * 未完成訂單列表的適配器
     */
    private CheckOrderAdapter checkOrderAdapter;

    private LoadingDailog.Builder loadBuilder;

    private LoadingDailog loadingDialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadBuilder = new LoadingDailog.Builder(CheckOrderActivity.this)
                .setCancelable(false)
                .setCancelOutside(false)
                .setMessage("加載中...")
                .setShowMessage(true);
        loadingDialog = loadBuilder.create();

        str_uid=getIntent().getStringExtra(IntentKeyUtils.INTENT_UID);
        lists=new ArrayList<>();

        loadingDialog.show();
        checkOrder(str_uid);

        //以下幾行為初始化控件
        checkOrderAdapter = new CheckOrderAdapter(lists);
        srflCheckOrder.setColorSchemeColors(getResources().getColor(R.color.colorTheme));
        rvCheckOrder.setLayoutManager(new LinearLayoutManager(this));
        rvCheckOrder.setAdapter(checkOrderAdapter);
        srflCheckOrder.setOnRefreshListener(this);

    }

    /**
     * 獲取未完成訂單列表的請求
     * @param str_uid
     */
    private void checkOrder(String str_uid) {
        Call<String> call = HttpUtils.getIserver().checkOrder(str_uid);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.body().equals("-1")){
                    lists.clear();
                    refresh(lists);
                    loadingDialog.dismiss();
                }else {
                    lists.clear();
                    Gson gson = new Gson();
                    List<CheckOrderBean> checkorderlists = gson.fromJson(response.body().toString(), new TypeToken<List<CheckOrderBean>>() {
                    }.getType());
                    for (CheckOrderBean checklist : checkorderlists) {
                        lists.add(checklist);
                    }
                    refresh(lists);
                }
                loadingDialog.dismiss();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                loadingDialog.dismiss();
            }
        });

    }

    /**
     * 刷新數據
     * @param lists
     */
    private void refresh(List<CheckOrderBean> lists) {
        this.lists = lists;
        checkOrderAdapter.notifyDataSetChanged();
    }


    @Override
    protected void init() {
        setToolBarTitle("未完成訂單");
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_check_order;
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
     *重寫SwipeRefreshLayout的刷新事件
     */
    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                lists.clear();
//                checkOrderAdapter.notifyDataSetChanged();
                checkOrder(str_uid);
                srflCheckOrder.setRefreshing(false);
            }
        }, 700);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        finish();
    }
}
