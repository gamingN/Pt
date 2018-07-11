package com.example.veeotech.postaltracking.posHand;

import android.content.Context;
import android.content.Intent;
import android.posapi.PosApi;
import android.widget.Toast;

import com.example.veeotech.postaltracking.R;
import com.example.veeotech.postaltracking.app.App;
import com.example.veeotech.postaltracking.utils.IntentKeyUtils;
import com.example.veeotech.postaltracking.utils.SPUtils;

/**
 * Created by VeeoTech on 23/4/2018.
 * 集成掃描槍sdk初始化,無需改動
 */

public class PosScanner {

    private PosApi mPosSDK;
    public static int initScanner_flag;
    private Context context;

    public PosScanner(Context context){
        this.context=context;
    }

    public void initPos(){
        initScanner_flag= (int) SPUtils.get(App.getInstance(), IntentKeyUtils.SP_INITSCANNER,0);
        if(initScanner_flag==1) {
            //获取PosApi的实例类
            mPosSDK = App.getInstance().getPosApi();
            //初始化接口时回调
            mPosSDK.setOnComEventListener(mCommEventListener);
            //获取状态时回调
            mPosSDK.setOnDeviceStateListener(onDeviceStateListener);

            Intent newIntent = new Intent(context, ScanService.class);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(newIntent);
        }
    }

    PosApi.OnCommEventListener mCommEventListener = new PosApi.OnCommEventListener() {
        @Override
        public void onCommState(int cmdFlag, int state, byte[] resp, int respLen) {
            // TODO Auto-generated method stub
            switch (cmdFlag) {
                case PosApi.POS_INIT:
                    if (state == PosApi.COMM_STATUS_SUCCESS) {
                        Toast.makeText(context, "設備初始化成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "設備初始化失敗", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    private PosApi.OnDeviceStateListener onDeviceStateListener = new PosApi.OnDeviceStateListener() {
        /**
         * @param state 0-获取状态成功  1-获取状态失败
         * @param version 设备固件版本
         * @param serialNo 设备序列号
         * @param psam1 psam1 状态   0-正常   1-无卡   2-卡错误
         * @param psam2 psam2 状态   0-正常   1-无卡   2-卡错误
         * @param ic IC卡 状态   0-正常   1-无卡   2-卡错误
         * @param swipcard 磁卡状态 o-正常  1-故障
         * @param printer 打印机状态 0-正常  1-缺纸
         */
        public void OnGetState(int state, String version, String serialNo, int psam1, int psam2, int ic, int swipcard, int printer) {
            ProgressDialogUtils.dismissProgressDialog();
            if (state == PosApi.COMM_STATUS_SUCCESS) {

                StringBuilder sb = new StringBuilder();
                String mPsam1 = null;
                switch (psam1) {
                    case 0:
                        mPsam1 = context.getString(R.string.state_normal);
                        break;
                    case 1:
                        mPsam1 = context.getString(R.string.state_no_card);
                        break;
                    case 2:
                        mPsam1 = context.getString(R.string.state_card_error);
                        break;
                }

                String mPsam2 = null;
                switch (psam2) {
                    case 0:
                        mPsam2 = context.getString(R.string.state_normal);
                        break;
                    case 1:
                        mPsam2 = context.getString(R.string.state_no_card);
                        break;
                    case 2:
                        mPsam2 = context.getString(R.string.state_card_error);
                        break;
                }

                String mIc = null;
                switch (ic) {
                    case 0:
                        mIc = context.getString(R.string.state_normal);
                        break;
                    case 1:
                        mIc = context.getString(R.string.state_no_card);
                        break;
                    case 2:
                        mIc = context.getString(R.string.state_card_error);
                        break;
                }

                String magnetic_card = null;
                switch (swipcard) {
                    case 0:
                        magnetic_card = context.getString(R.string.state_normal);
                        break;
                    case 1:
                        magnetic_card = context.getString(R.string.state_fault);
                        break;

                }

                String mPrinter = null;
                switch (printer) {
                    case 0:
                        mPrinter = context.getString(R.string.state_normal);
                        break;
                    case 1:
                        mPrinter = context.getString(R.string.state_no_paper);
                        break;

                }

                sb.append(/*getString(R.string.pos_status)+"\n "
                            +*/
                        context.getString(R.string.psam1_) + mPsam1 + "\n" //pasm1
                                + context.getString(R.string.psam2) + mPsam2 + "\n" //pasm2
                                + context.getString(R.string.ic_card) + mIc + "\n" //card
                                + context.getString(R.string.magnetic_card) + magnetic_card + "\n" //磁条卡
                                + context.getString(R.string.printer) + mPrinter + "\n" //打印机
                );

                sb.append(context.getString(R.string.pos_serial_no) + serialNo + "\n");
                sb.append(context.getString(R.string.pos_firmware_version) + version);
                DialogUtils.showTipDialog(context, sb.toString());

            } else {
                // 获取状态失败
                DialogUtils.showTipDialog(context, context.getString(R.string.get_pos_status_failed));
            }
        }
    };

}
