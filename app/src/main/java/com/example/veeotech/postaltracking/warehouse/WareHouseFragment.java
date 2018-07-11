package com.example.veeotech.postaltracking.warehouse;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.tu.loadingdialog.LoadingDailog;
import com.example.veeotech.postaltracking.R;
import com.example.veeotech.postaltracking.app.App;
import com.example.veeotech.postaltracking.posHand.PosScanner;
import com.example.veeotech.postaltracking.posHand.ScanService;
import com.example.veeotech.postaltracking.utils.HttpUtils;
import com.example.veeotech.postaltracking.utils.IntentKeyUtils;
import com.example.veeotech.postaltracking.utils.NetUtils;
import com.example.veeotech.postaltracking.utils.SPUtils;
import com.example.veeotech.postaltracking.utils.ScannerUtils;
import com.example.veeotech.postaltracking.utils.ToastUtil;
import com.example.veeotech.postaltracking.warehouse.bean.CargoBean;
import com.example.veeotech.postaltracking.warehouse.bean.CustomerIdBean;
import com.example.veeotech.postaltracking.warehouse.bean.TypeBean;
import com.example.veeotech.postaltracking.warehouse.bean.TypeInfoBean;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.functions.Consumer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.veeotech.postaltracking.R.id.btn_warehouse_barcode;


/**
 * Created by VeeoTech on 17/4/2018.
 */

public class WareHouseFragment extends Fragment  {

    private String result_scanner;
    private String order_id;
    private List<TypeInfoBean.CargoBean> cargoBeanList;
    OnFinishListener onFinishListener;
    IntentResult result;
    private IntentIntegrator integrator;
    @BindView(btn_warehouse_barcode)
    Button btnWarehouseBarcode;
    Unbinder unbinder;
    private String brocast_result_scanner;
    private byte mGpioPower = 0x1E ;//PB14
    private byte mGpioTrig = 0x29 ;//PC9
    private String packageid;

    private BroadcastReceiver bordcastReceiver;
    private LocalBroadcastManager broadcastManager;

    private LoadingDailog.Builder loadBuilder;

    private LoadingDailog loadingDialog;

    private boolean isSend = false;

    public static WareHouseFragment getInstance() {
        WareHouseFragment fragment;
        fragment = new WareHouseFragment();
        return fragment;
    }

    public WareHouseFragment() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null || result.getContents().equals("")) {
               // ToastUtil.showToastShort(getActivity(), "掃碼取消");
            } else {
                loadingDialog.show();
                packageid = result.getContents();
                getType();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_warehouse, container, false);

        //加載loading對話框
        loadBuilder=new LoadingDailog.Builder(getActivity())
                .setCancelable(false)
                .setCancelOutside(false)
                .setMessage("加載中...")
                .setShowMessage(true);
        loadingDialog=loadBuilder.create();

        unbinder = ButterKnife.bind(this, view);
        cargoBeanList = new ArrayList<>();
        new PosScanner(getActivity()).initPos();

        //網絡請求拿到type值回調接口
        onFinishListener = new OnFinishListener() {
            @Override
            public void OnFinish() {
                if(cargoBeanList.size()!=0) {
                    getCustomerId(getActivity(), packageid);
                }else {
                    loadingDialog.dismiss();
                    ToastUtil.showToastShort(getActivity(),"網絡連接異常請稍後再試.");
                    return;
                }
            }
        };

        //設置掃碼觸碰按鈕監聽
        btnWarehouseBarcode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (NetUtils.isConnected(getActivity())) {
                    SPUtils.put(getActivity(), IntentKeyUtils.SP_RESULT_BACK, "4");
                    if (PosScanner.initScanner_flag == 1) {
                        switch (motionEvent.getAction()) {
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
                        RxPermissions rxPermissions_barcode = new RxPermissions(getActivity());
                        rxPermissions_barcode.requestEach(Manifest.permission.CAMERA)
                                .subscribe(new Consumer<Permission>() {
                                    @Override
                                    public void accept(Permission permission) throws Exception {
                                        if (permission.granted) {
                                            new ScannerUtils(WareHouseFragment.this, integrator).selectFragment();
                                        } else {
                                            ToastUtil.showToastShort(getActivity(), "沒有權限");
                                        }
                                    }
                                });
                    }
                }else {
                    ToastUtil.showToastShort(getActivity(),"當前網絡不可用,請檢查網絡!");
                }
                return false;
            }
        });

        broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.MEDICAL4_BROADCAST");
        bordcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadingDialog.show();
                brocast_result_scanner=intent.getStringExtra(IntentKeyUtils.RESULT_BROADCAST);
                packageid = brocast_result_scanner;
                getType();
            }
        };
        broadcastManager.registerReceiver(bordcastReceiver, intentFilter);
        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        integrator = null;
        unbinder.unbind();
    }


    public void getCustomerId(final Context context, final String packgeId) {
        Call<CustomerIdBean> orderIdBeanCall = HttpUtils.getIserver().getCustomerId(packgeId);
        orderIdBeanCall.enqueue(new Callback<CustomerIdBean>() {
            @Override
            public void onResponse(Call<CustomerIdBean> call, Response<CustomerIdBean> response) {
                CustomerIdBean customerIdBean = response.body();
                if (customerIdBean.getFlag() == 1) {
                    String customerID = customerIdBean.getData().get(0).getCustomer_id();
                    if(customerID !=null) {
                            Intent intent = new Intent(getActivity(), CustomerActivity.class);
                            intent.putExtra(IntentKeyUtils.INTENT_ORDER_RESULT, packgeId);
                            intent.putExtra(IntentKeyUtils.INTENT_ORDER_ID, (Serializable) cargoBeanList);
                            startActivity(intent);
                    }
                    loadingDialog.dismiss();
                } else if (customerIdBean.getFlag() == -1) {
                    loadingDialog.dismiss();
                    ToastUtil.showToastShort(context,"查詢不到袋子信息,請確認袋子條碼是否正確.");
                }
            }

            @Override
            public void onFailure(Call<CustomerIdBean> call, Throwable t) {
                loadingDialog.dismiss();
                ToastUtil.showToastShort(context,"網絡鏈接錯誤,請檢查網絡設置後重試");
            }
        });
    }

    //獲取分區信息
    public void getType(){
            cargoBeanList.clear();
            final Call<TypeBean> typeBeanCall = HttpUtils.getIserver().getType();
            typeBeanCall.enqueue(new Callback<TypeBean>() {
                @Override
                public void onResponse(Call<TypeBean> call, Response<TypeBean> response) {
                    TypeBean typeBean = response.body();
                    if (typeBean.getFlag() == 1) {

                        for (TypeBean.DataBean dataBean1 : typeBean.getData()) {
                            TypeInfoBean.CargoBean cargoBean = new TypeInfoBean.CargoBean();
                            cargoBean.setType(dataBean1.getType());
                            cargoBean.setWeight("0");
                            cargoBeanList.add(cargoBean);
                            Log.d("zwx","cargotype"+cargoBean.getWeight());
                        }
                      //  SPUtils.setDataList(getActivity(), IntentKeyUtils.SP_COUNTRY_INFO, cargoBeanList);
                        onFinishListener.OnFinish();
                    } else {
                        ToastUtil.showToastShort(getActivity(), "網絡鏈接錯誤,請檢查網絡設置後重試.");
                    }
                }

                @Override
                public void onFailure(Call<TypeBean> call, Throwable t) {
                        loadingDialog.dismiss();
                    ToastUtil.showToastShort(getActivity(), "網絡鏈接錯誤,請檢查網絡設置後重試.");
                }
            });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        integrator=null;
        broadcastManager.unregisterReceiver(bordcastReceiver);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            SPUtils.put(App.getInstance(),IntentKeyUtils.SP_RESULT_BACK,"4");
        }
        Log.d("fragment","WareHouse setUserVisibleHint"+isVisibleToUser);
    }


    @Override
    public void onPause() {
        super.onPause();
    }
}


