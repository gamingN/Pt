package com.example.veeotech.postaltracking.pickup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.posapi.PosApi;
import android.posapi.PrintQueue;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.tu.loadingdialog.LoadingDailog;
import com.example.veeotech.postaltracking.BaseActivity;
import com.example.veeotech.postaltracking.R;
import com.example.veeotech.postaltracking.pickup.bean.OrderCreateBean;
import com.example.veeotech.postaltracking.pickup.bean.UnitBean;
import com.example.veeotech.postaltracking.posHand.BarcodeCreater;
import com.example.veeotech.postaltracking.posHand.BitmapTools;
import com.example.veeotech.postaltracking.posHand.PosScanner;
import com.example.veeotech.postaltracking.posHand.ScanService;
import com.example.veeotech.postaltracking.utils.HttpUtils;
import com.example.veeotech.postaltracking.utils.IntentKeyUtils;
import com.example.veeotech.postaltracking.utils.NetUtils;
import com.example.veeotech.postaltracking.utils.SPUtils;
import com.example.veeotech.postaltracking.utils.ToastUtil;

import org.angmarch.views.NiceSpinner;

import java.io.UnsupportedEncodingException;
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
 * 修改袋號的介面
 */

public class PackageAlterActivity extends BaseActivity {
    @BindView(R.id.ed_package_num)
    EditText edPackageNum;

    //單位下拉的控件
    @BindView(R.id.np_unit)
    NiceSpinner npUnit;

    @BindView(R.id.ed_package_weight)
    EditText edPackageWeight;
    @BindView(R.id.tv_package_alter_id)
    TextView tvPackageAlterId;
    @BindView(R.id.bt_package_alter_commit)
    Button btPackageAlterCommit;
    @BindView(R.id.bt_alter_print)
    Button btAlterPrint;
    @BindView(R.id.ll_alter_print)
    LinearLayout llAlterPrint;


    private List<String> lists;

    private String intentPid;

    private String package_qty;
    private String package_unit;
    private String package_weight;

    private LoadingDailog.Builder loadBuilder;

    private LoadingDailog loadingDialog;

    //不同頁面進入此介面的flag
    private String provider_flag="test";

    private Bitmap mBitmap;

    private PrintQueue mPrintQueue = null;

    boolean isCanPrint=true;

    private int exit=0;

    private String orderId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadBuilder = new LoadingDailog.Builder(PackageAlterActivity.this)
                .setCancelable(false)
                .setCancelOutside(false)
                .setMessage("加載中...")
                .setShowMessage(true);
        loadingDialog = loadBuilder.create();

        /**
         * 打印按鈕點擊事件
         */
        btAlterPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printOne();
            }
        });
    }

    @Override
    protected void init() {
        lists = new ArrayList<>();
        setToolBarTitle("修改袋信息");
        intentPid=getIntent().getStringExtra(IntentKeyUtils.INTENT_PACKAGE_ID);

        provider_flag=getIntent().getStringExtra(IntentKeyUtils.INTENT_PROVIDER_FLAG);
        Log.e("KIKI", "init: "+provider_flag);

        tvPackageAlterId.setText(intentPid);

        package_qty=getIntent().getStringExtra(IntentKeyUtils.INTENT_LIST_QTY);
        package_unit=getIntent().getStringExtra(IntentKeyUtils.INTENT_LIST_UNIT);
        package_weight=getIntent().getStringExtra(IntentKeyUtils.INTENT_LIST_WEIGHT);

        edPackageNum.setText(package_qty);
        edPackageWeight.setText(package_weight);
        npUnit.setText(package_unit);

        if(!TextUtils.isEmpty(edPackageNum.getText().toString())) {
            edPackageNum.setSelection(edPackageNum.getText().toString().length());
        }

        orderId= (String) SPUtils.get(getApplicationContext(),IntentKeyUtils.SP_ORDER_ID,"");
        Log.e("KIKI", "init: "+orderId);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_package_alter;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(provider_flag.equals("provider_flag_l")){
            llAlterPrint.setVisibility(View.VISIBLE);
        }
        loadingDialog.show();
        requireUnit();
    }

    /**
     *單位的請求
     */
    private void requireUnit() {
        Call<UnitBean> call = HttpUtils.getIserver().getUnit();
        call.enqueue(new Callback<UnitBean>() {
            @Override
            public void onResponse(Call<UnitBean> call, Response<UnitBean> response) {
                if (response.body().getFlag() == 1) {
                    lists.clear();
                    List<UnitBean.DataEntity> data = response.body().getData();
                    for (int i = 0; i < data.size(); i++) {
                        lists.add(data.get(i).getName());
                    }

                    if(!TextUtils.isEmpty(package_unit)) {
                        lists.add(0, package_unit);
                    }
                    npUnit.attachDataSource(lists);
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<UnitBean> call, Throwable t) {
                    loadingDialog.dismiss();
            }
        });
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
     * 提交點擊事件
     */
    @OnClick(R.id.bt_package_alter_commit)
    public void onViewClicked() {
        if(NetUtils.isConnected(getApplicationContext())){
            loadingDialog.show();

            String str_qty=edPackageNum.getText().toString();
            String str_unit=npUnit.getText().toString();
            String str_weight=edPackageWeight.getText().toString();

            if(!TextUtils.isEmpty(str_qty)){
                packageAlter(intentPid,str_qty,str_unit,str_weight);
            }else{
                ToastUtil.showToastShort(getApplicationContext(),"件數不可為空!");
                loadingDialog.dismiss();
            }

        }else {
            ToastUtil.showToastShort(getApplicationContext(),"當前網絡不可用,請檢查網絡!");
        }
    }

    /**
     * 修改袋號的請求
     * @param intentPid
     * @param str_qty
     * @param str_unit
     * @param str_weight
     */
    private void packageAlter(String intentPid, String str_qty, String str_unit, String str_weight) {
        Call<OrderCreateBean> call = HttpUtils.getIserver().alterPackage(orderId,intentPid,str_qty,str_unit,str_weight);
        call.enqueue(new Callback<OrderCreateBean>() {
            @Override
            public void onResponse(Call<OrderCreateBean> call, Response<OrderCreateBean> response) {
                if(response.body().getFlag()==1){
                    if(provider_flag.equals("provider_flag_c")){
                        printOne();
                        Log.e("KIKI", "create:printone" );
                    }else{
                        loadingDialog.dismiss();
                        finish();
                    }
                    ToastUtil.showToastShort(getApplicationContext(),"成功更新袋子信息!");
                }else{
                    loadingDialog.dismiss();
                    ToastUtil.showToastShort(getApplicationContext(),"更新袋子信息失敗!");
                    finish();
                }
            }

            @Override
            public void onFailure(Call<OrderCreateBean> call, Throwable t) {
                    loadingDialog.dismiss();
            }
        });
    }

    /**
     * 重寫返回鍵的事件
     */
    @Override
    public void onBackPressed() {
        if(!TextUtils.isEmpty(provider_flag)) {
            if (provider_flag.equals("provider_flag_c") || provider_flag.equals("provider_flag_b")) {
                deletePackage(intentPid);
            }
            super.onBackPressed();
        }else{
            super.onBackPressed();
        }
    }

    /**
     * 刪除小包事件(進入後直接返回)
     * @param packageId
     */
    private void deletePackage(String packageId) {
        Call<String> call = HttpUtils.getIserver().deletePackage(packageId, (String) SPUtils.get(getApplicationContext(),IntentKeyUtils.SP_ORDER_ID,""));
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.body().equals("-1")){

                }else{

                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                loadingDialog.dismiss();
            }
        });
    }

    /**
     * 打印方法
     */
    private void printOne() {
        if (PosScanner.initScanner_flag == 1) {
            loadingDialog.show();
            mPrintQueue = new PrintQueue(this, ScanService.mApi);
            //打印队列初始化
            mPrintQueue.init();
            //打印队列设置监听
            mPrintQueue.setOnPrintListener(new PrintQueue.OnPrintListener() {
                //打印完成

                /**
                 * 打印成功回調
                 */
                @Override
                public void onFinish() {
                    // TODO Auto-generated method stub
                    //打印完成
//                    ToastUtil.showToastShort(getApplicationContext(),
//                            getString(R.string.print_complete));
                    //当前可打印
                    isCanPrint = true;
                    loadingDialog.dismiss();
                    if(provider_flag.equals("provider_flag_c")) {
                        finish();
                    }
                }

                //打印失败
                @Override
                public void onFailed(int state) {
                    // TODO Auto-generated method stub
                    isCanPrint = true;
                    switch (state) {
                        case PosApi.ERR_POS_PRINT_NO_PAPER:
                            // 打印缺纸
                            showTip(getString(R.string.print_no_paper));
                            break;
                        case PosApi.ERR_POS_PRINT_FAILED:
                            // 打印失败
                            showTip(getString(R.string.print_failed));
                            break;
                        case PosApi.ERR_POS_PRINT_VOLTAGE_LOW:
                            // 电压过低
                            showTip(getString(R.string.print_voltate_low));
                            break;
                        case PosApi.ERR_POS_PRINT_VOLTAGE_HIGH:
                            // 电压过高
                            showTip(getString(R.string.print_voltate_high));
                            break;
                    }
                }

                @Override
                public void onGetState(int arg0) {
                    // TODO Auto-generated method stub

                }

                //打印设置
                @Override
                public void onPrinterSetting(int state) {
                    // TODO Auto-generated method stub
                    isCanPrint = true;
                    switch (state) {
                        case 0:
                            Toast.makeText(getApplicationContext(), "持續有紙", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            //缺纸
                            Toast.makeText(getApplicationContext(), getString(R.string.no_paper), Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            //检测到黑标
                            Toast.makeText(getApplicationContext(), getString(R.string.label), Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });


            if (!isCanPrint) return;
            mBitmap = BarcodeCreater.creatBarcode(getApplicationContext(),
                    tvPackageAlterId.getText().toString(), 300, 200, true,
                    1);
            byte[] printData = BitmapTools.bitmap2PrinterBytes(mBitmap);
            mPrintQueue.addBmp(25, 50, mBitmap.getWidth(),
                    mBitmap.getHeight(), printData);
            try {
                mPrintQueue.addText(25, "\n\n\n\n\n".toString()
                        .getBytes("GBK"));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //设为不可打印
            isCanPrint = false;
            //打印队列开始执行
            mPrintQueue.printStart();
        }else{
            loadingDialog.dismiss();
            if(provider_flag.equals("provider_flag_c")) {
                finish();
            }
            ToastUtil.showToastShort(getApplicationContext(), "手機設備,沒有生成條碼功能");
            }
    }

    private void showTip(String msg) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.tips))
                .setMessage(msg)
                .setNegativeButton(getString(R.string.close),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // TODO Auto-generated method stub
                                dialog.dismiss();
                            }
                        }).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBitmap != null) {
            mBitmap.recycle();
        }

        if (mPrintQueue != null) {
            mPrintQueue.close();
        }
    }
}
