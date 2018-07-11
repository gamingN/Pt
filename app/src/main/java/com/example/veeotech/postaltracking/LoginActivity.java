package com.example.veeotech.postaltracking;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.android.tu.loadingdialog.LoadingDailog;
import com.example.veeotech.postaltracking.pickup.PickUpActivity;
import com.example.veeotech.postaltracking.utils.HttpUtils;
import com.example.veeotech.postaltracking.utils.IntentKeyUtils;
import com.example.veeotech.postaltracking.utils.SPUtils;
import com.example.veeotech.postaltracking.utils.ScreenUtils;
import com.example.veeotech.postaltracking.utils.ToastUtil;
import com.example.veeotech.postaltracking.warehouse.SelectWayActivity;
import com.example.veeotech.postaltracking.warehouse.bean.Loginbean;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by VeeoTech on 17/4/2018.
 * 登陸介面
 * 登錄請求,根據請求權限進入不同主介面
 */

public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.et_login_user)
    EditText et_user;

    @BindView(R.id.et_login_password)
    EditText et_password;

    @BindView(R.id.bt_login)
    Button bt_login;

    @BindView(R.id.bt_delete_user)
    Button bt_delete_user;

    @BindView(R.id.bt_delete_password)
    Button bt_delete_password;

    private String uid;
    private String pw;

    private LoadingDailog.Builder loadBuilder;

    private LoadingDailog loadingDialog;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        /**
         * loading控件,在需要打開的地方調用.show(),關閉時調用.dismiss()即可
         */
        loadBuilder = new LoadingDailog.Builder(LoginActivity.this)
                .setCancelable(false)
                .setCancelOutside(false)
                .setMessage("加載中...")
                .setShowMessage(true);
        loadingDialog = loadBuilder.create();

        Window window = getWindow();
        //取消状态栏透明
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //添加Flag把状态栏设为可绘制模式
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.status_color));

        uid = (String) SPUtils.get(getApplicationContext(), IntentKeyUtils.SP_LOGIN_USER, "");
        pw = (String) SPUtils.get(getApplicationContext(), IntentKeyUtils.SP_LOGIN_PASSWORD, "");

        if (!TextUtils.isEmpty(uid) && !TextUtils.isEmpty(pw)) {
            loadingDialog.show();
            loginRequest();
        }

        Log.e("KIKI", "height:" + ScreenUtils.getScreenHeight(getApplicationContext()));
        Log.e("KIKI", "width:" + ScreenUtils.getScreenWidth(getApplicationContext()));

        /**
         * 輸入欄中刪除按鈕事件
         */
        bt_delete_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_user.setText("");
            }
        });

        bt_delete_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_password.setText("");
            }
        });

    }

    /**
     * 登錄按鈕事件
     *
     * @param view
     */
    @OnClick(R.id.bt_login)
    public void onclick(View view) {
        uid = et_user.getText().toString();
        pw = et_password.getText().toString();
        if (TextUtils.isEmpty(uid) && TextUtils.isEmpty(pw)) {
            ToastUtil.showToastShort(getApplicationContext(), "請填寫用戶名和密碼!");
        } else if (TextUtils.isEmpty(uid) || TextUtils.isEmpty(pw)) {
            ToastUtil.showToastShort(getApplicationContext(), "用戶名或密碼不能為空!");
        } else {
            loadingDialog.show();
            loginRequest();
        }
    }


    /**
     * 登錄按鈕的請求方法
     */
    private void loginRequest() {
        Call<Loginbean> call = HttpUtils.getIserver().getloginInfo(uid, pw);
        call.enqueue(new Callback<Loginbean>() {
            @Override
            public void onResponse(Call<Loginbean> call, Response<Loginbean> response) {
                Loginbean mLoginbean = response.body();
                if (mLoginbean.getFlag() == 1) {
                    String role = mLoginbean.getData().get(0).getRole();
                    SPUtils.put(getApplicationContext(), IntentKeyUtils.KEY_LOGIN_UID, uid);
                    SPUtils.put(getApplicationContext(), IntentKeyUtils.KEY_ROLE, role);
                    if (role.equals("pickup")) {
                        Intent intent = new Intent(LoginActivity.this, PickUpActivity.class);
                        intent.putExtra(IntentKeyUtils.INTENT_UID, uid);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(LoginActivity.this, SelectWayActivity.class);
                        intent.putExtra(IntentKeyUtils.INTENT_UID, uid);
                        startActivity(intent);
                    }
                    SPUtils.put(getApplicationContext(), IntentKeyUtils.SP_LOGIN_USER, uid);
                    SPUtils.put(getApplicationContext(), IntentKeyUtils.SP_LOGIN_PASSWORD, pw);
                    loadingDialog.dismiss();
                    finish();

                } else {
                    et_user.setText("");
                    et_password.setText("");
                    loadingDialog.dismiss();
                    ToastUtil.showToastShort(getApplicationContext(), "登錄失敗");
                }
            }

            @Override
            public void onFailure(Call<Loginbean> call, Throwable t) {
                loadingDialog.dismiss();
            }
        });
    }

//    @Override
//    protected void init() {
//        setToolBarTitle("員工請登錄");
//    }
//
//    @Override
//    protected int getLayoutId() {
//        return R.layout.activity_login;
//    }
//
//    @Override
//    protected boolean isShowBacking() {
//        return false;
//    }
//
//    @Override
//    protected boolean isShowRightText() {
//        return false;
//    }
}
