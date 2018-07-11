package com.example.veeotech.postaltracking.pickup.adapter;

import android.content.Intent;
import android.view.View;

import com.android.tu.loadingdialog.LoadingDailog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.veeotech.postaltracking.R;
import com.example.veeotech.postaltracking.pickup.OrderMixActivity;
import com.example.veeotech.postaltracking.pickup.bean.CheckOrderBean;
import com.example.veeotech.postaltracking.pickup.bean.CustomerBean;
import com.example.veeotech.postaltracking.pickup.bean.OrderCreateBean;
import com.example.veeotech.postaltracking.utils.HttpUtils;
import com.example.veeotech.postaltracking.utils.IntentKeyUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by VeeoTech on 28/4/2018.
 * 查看未完成訂單的適配器
 */

public class CheckOrderAdapter extends BaseQuickAdapter<CheckOrderBean,BaseViewHolder>{

    private LoadingDailog.Builder loadBuilder;

    private LoadingDailog loadingDialog;

    /**
     * 佈局
     * @param data 數據bean
     */
    public CheckOrderAdapter(List<CheckOrderBean> data) {
        super(R.layout.item_check_order,data);
    }

    /**
     * 設置item條目中的數據和點擊事件
     * @param helper
     * @param item
     */
    @Override
    protected void convert(BaseViewHolder helper, final CheckOrderBean item) {
        helper.setText(R.id.tv_item_checkorder,item.getOrder_id());
        helper.setText(R.id.tv_item_checkout_customer,item.getCustomer_id());
        helper.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadBuilder = new LoadingDailog.Builder(mContext)
                        .setCancelable(false)
                        .setCancelOutside(false)
                        .setMessage("加載中...")
                        .setShowMessage(true);
                loadingDialog = loadBuilder.create();
                loadingDialog.show();
                requireCustomer(item.getOrder_id());
            }
        });
    }

    /**
     * 客戶信息請求
     * @param order_id
     */
    private void requireCustomer(final String order_id) {
        Call<CustomerBean> call = HttpUtils.getIserver().customerRequire(order_id);
        call.enqueue(new Callback<CustomerBean>() {
            @Override
            public void onResponse(Call<CustomerBean> call, Response<CustomerBean> response) {
                if(response.body().getFlag()==1){
                    loadingDialog.dismiss();
                    String customer_id=response.body().getData().get(0).getCustomer_id();
                    Intent intent=new Intent(mContext, OrderMixActivity.class);
                    intent.putExtra(IntentKeyUtils.INTENT_CUSTOMER_ID,customer_id);
                    intent.putExtra(IntentKeyUtils.INTENT_ORDERMIX,order_id);
                    mContext.startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<CustomerBean> call, Throwable t) {
                loadingDialog.dismiss();
            }
        });


    }
}
