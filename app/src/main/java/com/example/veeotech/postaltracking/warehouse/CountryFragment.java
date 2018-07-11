package com.example.veeotech.postaltracking.warehouse;

import android.Manifest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.tu.loadingdialog.LoadingDailog;
import com.example.veeotech.postaltracking.R;
import com.example.veeotech.postaltracking.app.App;
import com.example.veeotech.postaltracking.pickup.PackageAlterActivity;
import com.example.veeotech.postaltracking.pickup.bean.UnitBean;
import com.example.veeotech.postaltracking.posHand.PosScanner;
import com.example.veeotech.postaltracking.posHand.ScanService;
import com.example.veeotech.postaltracking.utils.HttpUtils;
import com.example.veeotech.postaltracking.utils.IntentKeyUtils;
import com.example.veeotech.postaltracking.utils.NetUtils;
import com.example.veeotech.postaltracking.utils.SPUtils;
import com.example.veeotech.postaltracking.utils.ScannerUtils;
import com.example.veeotech.postaltracking.utils.ToastUtil;
import com.example.veeotech.postaltracking.warehouse.bean.ListslugBean;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.angmarch.views.NiceSpinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.functions.Consumer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by VeeoTech on 2018/4/20.
 */

public class CountryFragment extends android.support.v4.app.Fragment {

    @BindView(R.id.btn_warehouse_barcode)
    Button btnWarehouseBarcode;

    @BindView(R.id.np_country)
    NiceSpinner npUnit;

    private String result_scanner;

    IntentResult result;
    private IntentIntegrator integrator;
    Unbinder unbinder;

    private byte mGpioPower = 0x1E;//PB14
    private byte mGpioTrig = 0x29;//PC9
    private BroadcastReceiver bordcastReceiver;
    private LocalBroadcastManager broadcastManager;

    private LoadingDailog.Builder loadBuilder;

    private LoadingDailog loadingDialog;

    private String package_country = "請選擇郵政";

    private List<String> lists;

    private Map<String, String> maps;

    private String str_pos;

    public static CountryFragment getInstance() {
        CountryFragment fragment = new CountryFragment();
        return fragment;
    }

    public CountryFragment() {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null || result.getContents().equals("")) {
                // ToastUtil.showToastShort(getActivity(), "掃碼取消");
            } else {
                ToastUtil.showToastShort(getActivity(), "掃描成功,請開始掃描貨件碼.");

                for (String getKey : maps.keySet()) {
                    if (maps.get(getKey).equals(npUnit.getText().toString())) {
                        str_pos = getKey;
                    }
                }

                result_scanner = result.getContents();
                Intent intent = new Intent(getActivity(), SmallPackgeActivity.class);
                intent.putExtra(IntentKeyUtils.INTENT_POS_FLAG, str_pos);
                intent.putExtra(IntentKeyUtils.INTENT_ORDER_RESULT, result_scanner);
                startActivity(intent);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_country, container, false);
        lists = new ArrayList<>();
        maps = new HashMap<>();
        loadBuilder = new LoadingDailog.Builder(getActivity())
                .setCancelable(false)
                .setCancelOutside(false)
                .setMessage("加載中...")
                .setShowMessage(true);
        loadingDialog = loadBuilder.create();
        new PosScanner(getActivity()).initPos();
        unbinder = ButterKnife.bind(this, view);

        btnWarehouseBarcode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (NetUtils.isConnected(getActivity())) {
                    if (!npUnit.getText().toString().equals(package_country)) {
                        SPUtils.put(getActivity(), IntentKeyUtils.SP_RESULT_BACK, "3");
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
                            RxPermissions rxPermissions_barcode = new RxPermissions(getActivity());
                            rxPermissions_barcode.requestEach(Manifest.permission.CAMERA)
                                    .subscribe(new Consumer<Permission>() {
                                        @Override
                                        public void accept(Permission permission) throws Exception {
                                            if (permission.granted) {
                                                new ScannerUtils(CountryFragment.this, integrator).selectFragment();
                                            } else {
                                                Toast.makeText(getActivity(), "沒有權限", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }else{
                        ToastUtil.showToastShort(getActivity(),"請選擇郵政方式");
                    }
                } else {
                    ToastUtil.showToastShort(getActivity(), "當前網絡不可用,請檢查網絡!");
                }
                return false;
            }
        });

        broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.MEDICAL3_BROADCAST");
        bordcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(npUnit.getText().toString().equals(package_country)) {
                    ToastUtil.showToastShort(getActivity(),"請選擇郵政方式");
                }else {
                    for (String getKey : maps.keySet()) {
                        if (maps.get(getKey).equals(npUnit.getText().toString())) {
                            str_pos = getKey;
                        }
                    }
                    result_scanner = intent.getStringExtra(IntentKeyUtils.RESULT_BROADCAST);
                    Intent intent2 = new Intent(getActivity(), SmallPackgeActivity.class);
                    intent2.putExtra(IntentKeyUtils.INTENT_POS_FLAG, str_pos);
                    intent2.putExtra(IntentKeyUtils.INTENT_ORDER_RESULT, result_scanner);
                    startActivity(intent2);
                }
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
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            SPUtils.put(App.getInstance(), IntentKeyUtils.SP_RESULT_BACK, "3");
        }
        Log.d("fragment", "CountryFragment setUserVisibleHint" + isVisibleToUser);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        integrator = null;
        broadcastManager.unregisterReceiver(bordcastReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadingDialog.show();

//        lists.add(package_country);
//        lists.add("kiki1");
//        lists.add("kiki2");
//        lists.add("kiki3");
//        lists.add("kiki4");
//        npUnit.attachDataSource(lists);

        requireUnit();
    }

    /**
     * 單位的請求
     */
    private void requireUnit() {
        Call<ListslugBean> call = HttpUtils.getIserver().getSlug();
        call.enqueue(new Callback<ListslugBean>() {
            @Override
            public void onResponse(Call<ListslugBean> call, Response<ListslugBean> response) {
                if (response.body().getFlag() == 1) {
                    lists.clear();
                    maps.clear();
                    List<ListslugBean.DataEntity> data = response.body().getData();
                    for (int i = 0; i < data.size(); i++) {
                        lists.add(data.get(i).getName());
                        maps.put(data.get(i).getSlug_id(), data.get(i).getName());
                    }

                    if (!TextUtils.isEmpty(package_country)) {
                        maps.put("kiki", package_country);
                        lists.add(0, package_country);
                    }
                    npUnit.attachDataSource(lists);
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<ListslugBean> call, Throwable t) {
                loadingDialog.dismiss();
            }
        });
    }
}
