package com.example.veeotech.postaltracking.pickup.adapter;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.tu.loadingdialog.LoadingDailog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.veeotech.postaltracking.R;
import com.example.veeotech.postaltracking.app.App;
import com.example.veeotech.postaltracking.pickup.OrderMixActivity;
import com.example.veeotech.postaltracking.pickup.PackageAlterActivity;
import com.example.veeotech.postaltracking.pickup.PickUpActivity;
import com.example.veeotech.postaltracking.pickup.bean.PackageListBean;
import com.example.veeotech.postaltracking.pickup.bean.WeightBean;
import com.example.veeotech.postaltracking.utils.AlertDialogUtil;
import com.example.veeotech.postaltracking.utils.HttpUtils;
import com.example.veeotech.postaltracking.utils.IntentKeyUtils;
import com.example.veeotech.postaltracking.utils.NetUtils;
import com.example.veeotech.postaltracking.utils.SPUtils;
import com.example.veeotech.postaltracking.utils.ToastUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by VeeoTech on 28/4/2018.
 * 訂單列表適配器
 */

public class PackageListAdapter extends BaseQuickAdapter<PackageListBean,BaseViewHolder>{

    private LoadingDailog.Builder loadBuilder;

    private LoadingDailog loadingDialog;

    /**
     * 佈局
     * @param data 數據bean
     */
    public PackageListAdapter(List<PackageListBean> data) {
        super(R.layout.item_package_list, data);
    }

    /**
     * 設置item條目的中的數據和點擊事件
     * @param helper
     * @param item
     */
    @Override
    protected void convert(BaseViewHolder helper, final PackageListBean item) {
        helper.setText(R.id.tv_item_package_id,item.getPackage_id());
        helper.setText(R.id.tv_item_package_qty,item.getQty()+item.getUnit());
        helper.setText(R.id.tv_item_package_weight,item.getWeight()+"KG");

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
                requireWeight(item.getPackage_id());
            }
        });

        helper.getView(R.id.bt_item_package_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetUtils.isConnected(mContext)) {
                    AlertDialogUtil dialogUtil = new AlertDialogUtil(mContext);
                    dialogUtil.showDialog("是否刪除此袋子條碼？");
                    dialogUtil.setDialogPositiveButtonListener(new AlertDialogUtil.DialogPositiveButtonListener() {
                        @Override
                        public void setDialogPositiveButtonListener() {
                            loadBuilder = new LoadingDailog.Builder(mContext)
                                    .setCancelable(false)
                                    .setCancelOutside(false)
                                    .setMessage("加載中...")
                                    .setShowMessage(true);
                            loadingDialog = loadBuilder.create();
                            loadingDialog.show();
                            deletePackage(item.getPackage_id());
                        }
                    });
                }else{
                    ToastUtil.showToastShort(mContext,"當前網絡不可用,請檢查網絡!");
                }
            }
        });
    }

    /**
     * 袋子件數和重量的請求
     * @param package_id
     */
    private void requireWeight(final String package_id) {
        Call<WeightBean> call = HttpUtils.getIserver().getWeight((String) SPUtils.get(mContext,IntentKeyUtils.SP_ORDER_ID,""),package_id);
        call.enqueue(new Callback<WeightBean>() {
            @Override
            public void onResponse(Call<WeightBean> call, Response<WeightBean> response) {
                if(response.body().getFlag()==1){
                    loadingDialog.dismiss();
                    Intent intent=new Intent(mContext,PackageAlterActivity.class);
                    intent.putExtra(IntentKeyUtils.INTENT_PACKAGE_ID,package_id);
                    intent.putExtra(IntentKeyUtils.INTENT_LIST_QTY,response.body().getData().get(0).getQty());
                    intent.putExtra(IntentKeyUtils.INTENT_LIST_UNIT,response.body().getData().get(0).getUnit());
                    intent.putExtra(IntentKeyUtils.INTENT_LIST_WEIGHT,response.body().getData().get(0).getWeight());
                    intent.putExtra(IntentKeyUtils.INTENT_PROVIDER_FLAG,"provider_flag_l");
                    mContext.startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<WeightBean> call, Throwable t) {
                    loadingDialog.dismiss();
            }
        });

    }

    /**
     * 刪除袋子請求
     * @param packageId
     */
    private void deletePackage(String packageId) {
        Call<String> call = HttpUtils.getIserver().deletePackage(packageId, (String) SPUtils.get(mContext,IntentKeyUtils.SP_ORDER_ID,""));
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.body().equals("-1")){
                    loadingDialog.dismiss();
                    ToastUtil.showToastShort(mContext,"刪除失敗!");
                }else{
                    Intent broadcast = new Intent("android.intent.action.LIST_BROADCAST");
                    LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(broadcast);
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                loadingDialog.dismiss();
                ToastUtil.showToastShort(mContext,"網絡請求失敗,請檢查網絡狀態");
            }
        });
    }
}
