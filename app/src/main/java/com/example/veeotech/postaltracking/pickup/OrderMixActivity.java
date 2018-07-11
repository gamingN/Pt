package com.example.veeotech.postaltracking.pickup;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.posapi.PosApi;
import android.posapi.PrintQueue;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.tu.loadingdialog.LoadingDailog;
import com.example.veeotech.postaltracking.BaseActivity;
import com.example.veeotech.postaltracking.R;
import com.example.veeotech.postaltracking.app.App;
import com.example.veeotech.postaltracking.pickup.bean.OrderCreateBean;
import com.example.veeotech.postaltracking.pickup.bean.PackageInfoBean;
import com.example.veeotech.postaltracking.pickup.bean.PackageListBean;
import com.example.veeotech.postaltracking.posHand.BarcodeCreater;
import com.example.veeotech.postaltracking.posHand.BitmapTools;
import com.example.veeotech.postaltracking.posHand.PosScanner;
import com.example.veeotech.postaltracking.posHand.PrintActivity;
import com.example.veeotech.postaltracking.posHand.ScanService;
import com.example.veeotech.postaltracking.utils.AlertDialogUtil;
import com.example.veeotech.postaltracking.utils.HttpUtils;
import com.example.veeotech.postaltracking.utils.IntentKeyUtils;
import com.example.veeotech.postaltracking.utils.NetUtils;
import com.example.veeotech.postaltracking.utils.SPUtils;
import com.example.veeotech.postaltracking.utils.ScannerUtils;
import com.example.veeotech.postaltracking.utils.ToastUtil;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.UnsupportedEncodingException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.functions.Consumer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by VeeoTech on 23/4/2018.
 * 生成袋號介面
 */

public class OrderMixActivity extends BaseActivity {


    @BindView(R.id.tv_ordermix_ordernum)
    TextView tvOrdermixOrdernum;
    @BindView(R.id.tv_ordermix_packagenum)
    TextView tvOrdermixPackagenum;
    @BindView(R.id.bt_ordermix_print)
    Button btOrdermixPrint;
    @BindView(R.id.ll_ordermix)
    LinearLayout llOrdermix;
    @BindView(R.id.bt_package_provide)
    Button btPackageProvide;
    @BindView(R.id.bt_package_scanner)
    Button btPackageScanner;
    @BindView(R.id.bt_package_alter)
    Button btPackageAlter;
    @BindView(R.id.bt_package_null)
    Button btPackageNull;
    @BindView(R.id.bt_ordermix_commit)
    Button btOrdermixCommit;
    @BindView(R.id.bt_ordermix_delete)
    Button btOrdermisDelete;
    @BindView(R.id.ll_invisible)
    LinearLayout llInvisble;


    private String order_id;

    private byte mGpioPower = 0x1E;//PB14
    private byte mGpioTrig = 0x29;//PC9

    private BroadcastReceiver bordcastReceiver;
    private LocalBroadcastManager broadcastManager;

    private IntentIntegrator integrator;

    private String result_scanner;

    private LoadingDailog.Builder loadBuilder;

    private LoadingDailog loadingDialog;

    //不同方式進入修改袋子信息介面的flag
    private String provider_flag="provider_flag";

    //接收二維碼
    private Bitmap mBitmap;

    //打印隊列
    private PrintQueue mPrintQueue = null;

    //是否正在打印
    boolean isCanPrint=true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadBuilder = new LoadingDailog.Builder(OrderMixActivity.this)
                .setCancelable(false)
                .setCancelOutside(false)
                .setMessage("加載中...")
                .setShowMessage(true);
        loadingDialog = loadBuilder.create();

        SPUtils.put(getApplicationContext(), IntentKeyUtils.SP_RESULT_BACK, "2");

        new PosScanner(this).initPos();

        /**
         * 掃描綁定袋號的點擊事件
         */
        btPackageScanner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (NetUtils.isConnected(getApplicationContext())) {

                    if (PosScanner.initScanner_flag == 1) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN: {
                                ScanService.mApi.gpioControl(mGpioTrig, 0, 0);
                                break;
                            }
                            case MotionEvent.ACTION_UP: {
                                ScanService.mApi.gpioControl(mGpioTrig, 0, 1);
                                break;
                            }
                            default:
                                break;
                        }
                    } else {
                        RxPermissions rxPermissions = new RxPermissions(OrderMixActivity.this);
                        rxPermissions.requestEach(Manifest.permission.CAMERA)
                                .subscribe(new Consumer<Permission>() {
                                    @Override
                                    public void accept(Permission permission) throws Exception {
                                        if (permission.granted) {
                                            new ScannerUtils(OrderMixActivity.this, integrator).selectActivity();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "沒有權限", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }else{
                    ToastUtil.showToastShort(getApplicationContext(),"當前網絡不可用,請檢查網絡!");
                }
                return false;
            }
        });

        /**
         * 接收掃描槍的廣播
         */
        broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.MEDICAL2_BROADCAST");
        bordcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                loadingDialog.show();
                result_scanner = intent.getStringExtra(IntentKeyUtils.RESULT_BROADCAST);
                tvOrdermixPackagenum.setText(result_scanner);
                bindPackage(order_id, result_scanner);
            }
        };
        broadcastManager.registerReceiver(bordcastReceiver, intentFilter);

        /**
         * 刪除按鈕點擊事件
         */
        btOrdermisDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialogUtil dialogUtil = new AlertDialogUtil(OrderMixActivity.this);
                dialogUtil.showDialog("是否刪除此袋子條碼？");
                dialogUtil.setDialogPositiveButtonListener(new AlertDialogUtil.DialogPositiveButtonListener() {
                    @Override
                    public void setDialogPositiveButtonListener() {
                        if (NetUtils.isConnected(getApplicationContext())) {
                            loadingDialog.show();
                            deletePackage(tvOrdermixPackagenum.getText().toString());
                        }else{
                            ToastUtil.showToastShort(getApplicationContext(),"當前網絡不可用,請檢查網絡!");
                        }
                    }
                });
            }
        });
    }

    /**
     * 刪除袋號請求
     * @param packageId
     */
    private void deletePackage(String packageId) {
        Call<String> call = HttpUtils.getIserver().deletePackage(packageId,order_id);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.body().equals("-1")){
                    ToastUtil.showToastShort(getApplicationContext(),"訂單中不存在此袋號!");
                    loadingDialog.dismiss();
                }else{
                    ToastUtil.showToastShort(getApplicationContext(),"刪除成功!");
                    tvOrdermixPackagenum.setText("");
                    onResume();
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                    loadingDialog.dismiss();
            }
        });
    }

    /**
     * 掃描綁定代號請求
     * @param order_id
     * @param result_scanner
     */
    private void bindPackage(String order_id, final String result_scanner) {
        Call<OrderCreateBean> call = HttpUtils.getIserver().bindPackageNum(order_id, result_scanner);
        call.enqueue(new Callback<OrderCreateBean>() {
            @Override
            public void onResponse(Call<OrderCreateBean> call, Response<OrderCreateBean> response) {
                if (response.body().getFlag() == 1) {
//                    ToastUtil.showToastShort(getApplicationContext(), "袋號綁定成功");
                    loadingDialog.dismiss();
                    Intent intent=new Intent(OrderMixActivity.this,PackageAlterActivity.class);
                    intent.putExtra(IntentKeyUtils.INTENT_PACKAGE_ID,result_scanner);
                    intent.putExtra(IntentKeyUtils.INTENT_PROVIDER_FLAG,"provider_flag_b");
                    startActivity(intent);

                } else if(response.body().getFlag()==-1){
                    ToastUtil.showToastShort(getApplicationContext(),"此袋號已存在!");
                    loadingDialog.dismiss();
                } else {
                    ToastUtil.showToastShort(getApplicationContext(), "袋號綁定失敗");
                    loadingDialog.dismiss();
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
        setToolBarTitle("顧客ID:"+getIntent().getStringExtra(IntentKeyUtils.INTENT_CUSTOMER_ID));
        order_id = getIntent().getStringExtra(IntentKeyUtils.INTENT_ORDERMIX);
        tvOrdermixOrdernum.setText(order_id);
        SPUtils.put(getApplicationContext(),IntentKeyUtils.SP_ORDER_ID,order_id);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_ordermix;
    }

    @Override
    protected boolean isShowBacking() {
        return true;
    }

    @Override
    protected boolean isShowRightText() {
        return false;
    }

    @OnClick({R.id.bt_ordermix_print, R.id.bt_package_provide, R.id.bt_package_alter, R.id.bt_ordermix_commit})
    public void onViewClicked(View view) {
        if (NetUtils.isConnected(getApplicationContext())) {
            switch (view.getId()) {
                /**
                 * 打印按鈕點擊事件
                 */
                case R.id.bt_ordermix_print:
                    if (!TextUtils.isEmpty(tvOrdermixPackagenum.getText().toString())) {
                        if (PosScanner.initScanner_flag == 1) {
//                            Intent intent = new Intent(OrderMixActivity.this, PrintActivity.class);
//                            intent.putExtra(IntentKeyUtils.INTENT_MA_CREATE, tvOrdermixPackagenum.getText().toString());
//                            startActivity(intent);
                            printOne();

                        } else {
                            ToastUtil.showToastShort(getApplicationContext(), "手機設備,沒有生成條碼功能");
                        }
                    } else {
                        ToastUtil.showToastShort(getApplicationContext(), "袋子編號為空!");
                    }

                    break;

                /**
                 * 生成袋號點擊事件
                 */
                case R.id.bt_package_provide:
                    AlertDialogUtil dialogUtil = new AlertDialogUtil(OrderMixActivity.this);
                    dialogUtil.showDialog("是否生成一個袋子條碼？");
                    dialogUtil.setDialogPositiveButtonListener(new AlertDialogUtil.DialogPositiveButtonListener() {
                        @Override
                        public void setDialogPositiveButtonListener() {
                            loadingDialog.show();
                            packageCreate(order_id);
                        }
                    });
                    break;

                /**
                 * 修改袋號點擊事件
                 */
                case R.id.bt_package_alter:
                    Intent intent_alter = new Intent(OrderMixActivity.this, PackageListActivity.class);
                    intent_alter.putExtra(IntentKeyUtils.INTENT_PACKAGE_FLAG, "1");
                    intent_alter.putExtra(IntentKeyUtils.INTENT_ORDER_RESULT, order_id);
                    startActivity(intent_alter);
                    break;

                /**
                 * 提交點擊事件
                 */
                case R.id.bt_ordermix_commit:
                    Intent intent_commit = new Intent(OrderMixActivity.this, PackageListActivity.class);
                    intent_commit.putExtra(IntentKeyUtils.INTENT_PACKAGE_FLAG, "2");
                    intent_commit.putExtra(IntentKeyUtils.INTENT_ORDER_RESULT, order_id);
                    startActivity(intent_commit);
                    break;
            }
        }else{
            ToastUtil.showToastShort(getApplicationContext(),"當前網絡不可用,請檢查網絡!");
        }
    }

    /**
     * 打印條形碼方法
     */
    private void printOne() {
        loadingDialog.show();
        mPrintQueue = new PrintQueue(this, ScanService.mApi);
        //打印队列初始化
        mPrintQueue.init();
        //打印队列设置监听
        mPrintQueue.setOnPrintListener(new PrintQueue.OnPrintListener() {
            //打印完成
            @Override
            public void onFinish() {
                // TODO Auto-generated method stub
                //打印完成
//                ToastUtil.showToastShort(getApplicationContext(),
//                        getString(R.string.print_complete));
                loadingDialog.dismiss();
                //当前可打印
                isCanPrint=true;
            }
            //打印失败
            @Override
            public void onFailed(int state) {
                // TODO Auto-generated method stub
                isCanPrint=true;
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
                isCanPrint=true;
                switch(state){
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
        if(!isCanPrint) return;
        mBitmap = BarcodeCreater.creatBarcode(getApplicationContext(),
                tvOrdermixPackagenum.getText().toString(), 300, 200, true,
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
        isCanPrint=false;
        //打印队列开始执行
        mPrintQueue.printStart();
    }

    /**
     * 生成袋號請求
     * @param order_id
     */
    private void packageCreate(String order_id) {
        Call<PackageInfoBean> call = HttpUtils.getIserver().getPackageInfo(order_id);
        call.enqueue(new Callback<PackageInfoBean>() {
            @Override
            public void onResponse(Call<PackageInfoBean> call, Response<PackageInfoBean> response) {
                if (response.body().getFlag() == 1) {
                    String str_packageid = response.body().getData().get(0).getPackage_id();
                    tvOrdermixPackagenum.setText(str_packageid);
                    ToastUtil.showToastShort(getApplicationContext(), "已成功生成袋子條碼");
                    Intent intent = new Intent(OrderMixActivity.this,PackageAlterActivity.class);
                    intent.putExtra(IntentKeyUtils.INTENT_PACKAGE_ID,str_packageid);
                    intent.putExtra(IntentKeyUtils.INTENT_PROVIDER_FLAG,"provider_flag_c");
                    startActivity(intent);
                    loadingDialog.dismiss();
                } else {
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<PackageInfoBean> call, Throwable t) {
                loadingDialog.dismiss();
                ToastUtil.showToastShort(getApplicationContext(),"生成袋號失敗,請檢查網絡是否連接!");
            }
        });
    }

    /**
     * 攝像頭掃描後回調
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Log.e("KIKI", "onActivityResult: 進入3");
            } else if (result.getContents() != null) {
                Log.e("KIKI", "onActivityResult: 進入4");
                loadingDialog.show();
                result_scanner = result.getContents();
                tvOrdermixPackagenum.setText(result_scanner);
                bindPackage(order_id, result_scanner);
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

//    @Override
//    public void onBackPressed() {
//        ToastUtil.showToastShort(getApplicationContext(), "請確認訂單!");
//    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!TextUtils.isEmpty(tvOrdermixPackagenum.getText().toString())){
            llInvisble.setVisibility(View.VISIBLE);
        }else{
            llInvisble.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        integrator = null;
        broadcastManager.unregisterReceiver(bordcastReceiver);
        if (mBitmap != null) {
            mBitmap.recycle();
        }

        if (mPrintQueue != null) {
            mPrintQueue.close();
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

}
