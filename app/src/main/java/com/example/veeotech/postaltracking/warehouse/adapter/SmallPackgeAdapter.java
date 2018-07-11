package com.example.veeotech.postaltracking.warehouse.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.veeotech.postaltracking.R;
import com.example.veeotech.postaltracking.utils.AlertDialogUtil;
import com.example.veeotech.postaltracking.warehouse.CallBackListener;
import com.example.veeotech.postaltracking.warehouse.bean.SmallPackgeBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by VeeoTech on 2018/4/20.
 */

public class SmallPackgeAdapter extends BaseAdapter implements View.OnClickListener {
    private List<SmallPackgeBean.SmallIdBean> smallIdList;
    public Context context;
    private LayoutInflater inflater ;
    private CallBackListener callBackListener;


    public SmallPackgeAdapter(Context context,CallBackListener callBackListener){
        smallIdList = new ArrayList<>();
        this.context = context;
        inflater = (LayoutInflater)context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        this.callBackListener = callBackListener;
    }
    @Override
    public int getCount() {
        return smallIdList.size();
    }

    @Override
    public Object getItem(int i) {
        return smallIdList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;

        if(view == null){
            view = inflater.inflate(R.layout.item_smallid,null);
            viewHolder = new ViewHolder();
            viewHolder.tv_warehouse_small_id = (TextView)view.findViewById(R.id.tv_warehouse_small_id);
            viewHolder.ib_warehouse_delete = (ImageButton)view.findViewById(R.id.ib_warehouse_delete);
            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.tv_warehouse_small_id.setText("貨品編號:"+smallIdList.get(i).getGoods_code());
        viewHolder.ib_warehouse_delete.setOnClickListener(this);
        viewHolder.ib_warehouse_delete.setTag(i);
        return view;
    }

    public void add(SmallPackgeBean.SmallIdBean Bean){
        smallIdList.add(Bean);
        notifyDataSetChanged();
    }
    public void clear(){
        smallIdList.clear();
    }

    @Override
    public void onClick(View view) {

        callBackListener.onClickCallBack(view);
    }


    private static class ViewHolder{
        ImageButton ib_warehouse_delete;
        TextView tv_warehouse_small_id;
    }

    public void remove(int i){
        smallIdList.remove(i);
        notifyDataSetChanged();
    }

    public String getSmallId(int position){
        return smallIdList.get(position).getGoods_code();
    }
}
