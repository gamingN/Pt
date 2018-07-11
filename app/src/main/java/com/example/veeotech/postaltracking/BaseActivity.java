package com.example.veeotech.postaltracking;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.veeotech.postaltracking.utils.ActivityUtil;
import com.example.veeotech.postaltracking.utils.IntentKeyUtils;
import com.example.veeotech.postaltracking.utils.SPUtils;

import butterknife.ButterKnife;

/**
 * Created by VeeoTech on 17/4/2018.
 * Activity基類,
 * 封裝toolbar,統一bind的Butterknife
 */

public abstract class BaseActivity extends AppCompatActivity{
    private LinearLayout parentLinearLayout;
    private TextView mTvTitle;
    private TextView mTvRight;
    private Toolbar mToolbar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContentView(R.layout.activity_base);
        ActivityUtil.getInstance().addActivity(this);
        setContentView(getLayoutId());
        ButterKnife.bind(this);
        initToolBar();
        setBackIcon();
        setRightText();
        init();
    }

    /**
     * 設置toolbar右部按鈕的標題
     */
    public void setRightText() {
        if (isShowRightText()) {
            mTvRight.setVisibility(View.VISIBLE);
            mTvRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rightBtnClick();
                }
            });
        }
    }

    /**
     * 設置toolbar右部按鈕的點擊事件
     */
    protected void rightBtnClick() {
        ActivityUtil.getInstance().OutSign();
        SPUtils.put(getApplicationContext(), IntentKeyUtils.SP_LOGIN_USER,"");
        SPUtils.put(getApplicationContext(),IntentKeyUtils.SP_LOGIN_PASSWORD,"");
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(intent);

    }

    /**
     * 初始化toolbar,此方法不需要更改
     */
    private void initToolBar() {
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mTvRight = (TextView) findViewById(R.id.tv_right);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    /**
     * 子類用於初始化操作,此方法運行在onCreate方法中
     */
    protected abstract void init();


    //  overwrite the function in sub-activity and return the layout id of sub-activity


    protected abstract int getLayoutId();

    /**
     * 不需要改動
     * @param layoutResID
     */
    private void initContentView(@LayoutRes int layoutResID) {
        ViewGroup viewGroup = (ViewGroup) findViewById(android.R.id.content);
        viewGroup.removeAllViews();
        parentLinearLayout = new LinearLayout(this);
        parentLinearLayout.setOrientation(LinearLayout.VERTICAL);
        viewGroup.addView(parentLinearLayout);
        //  add the layout of BaseActivity in parentLinearLayout
        LayoutInflater.from(this).inflate(layoutResID, parentLinearLayout, true);
    }

    /**
     * 不需要改動
     * @param layoutResID the layout id of sub Activity
     */
    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        //  added the sub-activity layout id in parentLinearLayout
        LayoutInflater.from(this).inflate(layoutResID, parentLinearLayout, true);

    }

    /**
     * 返回按钮事件
     */
    private void setBackIcon() {
        if (null != getToolbar() && isShowBacking()) {
            getToolbar().setNavigationIcon(R.drawable.ic_action_backbtn);
            getToolbar().setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    /**
     * @return TextView in center
     */
    public TextView getToolbarTitle() {
        return mTvTitle;
    }

    /**
     * @return TextView on the right
     */
//    public TextView getSubTitle() {
//        return mTvRight;
//    }

    /**
     *設置子類toolbar標題
     * @param title
     */
    public void setToolBarTitle(CharSequence title) {
        if (mTvTitle != null) {
            mTvTitle.setText(title);
        } else {
            getToolbar().setTitle(title);
            setSupportActionBar(getToolbar());
        }
    }

    public void setRightTvTitle(CharSequence title) {
        mTvRight.setText(title);
    }

    public Toolbar getMyToolbar() {
        return mToolbar;
    }

    /**
     * the toolbar of this Activity
     *
     * @return support.v7.widget.Toolbar.
     */
    public Toolbar getToolbar() {
        return (Toolbar) findViewById(R.id.toolbar);
    }

    /**
     *設置為true顯示返回按鈕,默認為false
     * @return
     */
    protected abstract boolean isShowBacking();

    /**
     * 設置為true顯示右部按鈕,默認為false
     * @return
     */
    protected abstract boolean isShowRightText();
}
