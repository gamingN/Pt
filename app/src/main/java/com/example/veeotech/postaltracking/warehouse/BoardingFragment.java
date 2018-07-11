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
import com.example.veeotech.postaltracking.warehouse.bean.FlagBean;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.functions.Consumer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by VeeoTech on 2018/4/27.
 */

public class BoardingFragment extends Fragment {

    Unbinder unbinder;
    @BindView(R.id.btn_warehouse_boarding_barcode)
    Button btnWarehouseBoardingBarcode;

    private String result_scanner;
    private String order_id;
    IntentResult result;
    private IntentIntegrator integrator;

    private String brocast_result_scanner;
    private byte mGpioPower = 0x1E;//PB14
    private byte mGpioTrig = 0x29;//PC9
    public String countryid;

    private BroadcastReceiver bordcastReceiver;
    private LocalBroadcastManager broadcastManager;

    private LoadingDailog.Builder loadBuilder;

    private LoadingDailog loadingDialog;

    public static BoardingFragment getInstance() {
        BoardingFragment fragment;
        fragment = new BoardingFragment();
        return fragment;
    }

    public BoardingFragment() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null || result.getContents().equals("")) {
                // ToastUtil.showToastShort(getActivity(), "掃碼取消");
            } else {
                loadingDialog.show();
                countryid = result.getContents();
                setBoardingStatus(countryid);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_boarding, container, false);
        unbinder = ButterKnife.bind(this, view);
        loadBuilder = new LoadingDailog.Builder(getActivity())
                .setCancelable(false)
                .setCancelOutside(false)
                .setMessage("加載中...")
                .setShowMessage(true);
        loadingDialog = loadBuilder.create();

        new PosScanner(getActivity()).initPos();

        btnWarehouseBoardingBarcode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (NetUtils.isConnected(getActivity())) {
                    SPUtils.put(getActivity(), IntentKeyUtils.SP_RESULT_BACK, "7");
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
                                            new ScannerUtils(BoardingFragment.this, integrator).selectFragment();
                                        } else {
                                            ToastUtil.showToastShort(getActivity(), "沒有權限");
                                        }
                                    }
                                });
                    }
                } else {
                    ToastUtil.showToastShort(getActivity(), "當前網絡不可用,請檢查網絡!");
                }
                return false;
            }
        });

        broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.MEDICAL7_BROADCAST");
        bordcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadingDialog.show();
                brocast_result_scanner = intent.getStringExtra(IntentKeyUtils.RESULT_BROADCAST);
                countryid = brocast_result_scanner;
                setBoardingStatus(countryid);
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


    @Override
    public void onDestroy() {
        super.onDestroy();
        integrator = null;
        broadcastManager.unregisterReceiver(bordcastReceiver);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            SPUtils.put(App.getInstance(), IntentKeyUtils.SP_RESULT_BACK, "7");
        }
        Log.d("fragment", "WareHouse setUserVisibleHint" + isVisibleToUser);
    }

    public void setBoardingStatus(String country_id) {
        Call<FlagBean> flagBeanCall = HttpUtils.getIserver().updateBoardingInfo(country_id);
        flagBeanCall.enqueue(new Callback<FlagBean>() {
            @Override
            public void onResponse(Call<FlagBean> call, Response<FlagBean> response) {
                FlagBean flagBean = response.body();
                if (flagBean.getFlag() != 1) {
                    ToastUtil.showToastShort(getActivity(), "掃描貨品登機失敗");
                } else {
                    ToastUtil.showToastShort(getActivity(), "掃描貨品登機成功");
                }
                loadingDialog.dismiss();
            }

            @Override
            public void onFailure(Call<FlagBean> call, Throwable t) {
                loadingDialog.dismiss();
                ToastUtil.showToastShort(getActivity(), "掃描貨品登機失敗");
            }
        });
    }

}
