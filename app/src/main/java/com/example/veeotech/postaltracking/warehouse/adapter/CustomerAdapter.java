package com.example.veeotech.postaltracking.warehouse.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.veeotech.postaltracking.R;
import com.example.veeotech.postaltracking.warehouse.bean.CustomerIdBean;

import java.util.List;

/**
 * Created by VeeoTech on 2018/5/4.
 */

public class CustomerAdapter extends BaseQuickAdapter<CustomerIdBean.DataBean,BaseViewHolder> {
    public CustomerAdapter(List<CustomerIdBean.DataBean> data) {
        super(R.layout.item_select_customer, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, CustomerIdBean.DataBean item) {
        helper.setText(R.id.tv_warehouse_customer_id_show,item.getCustomer_id());
        Log.d("item",""+item.getCustomer_id());
    }

}
