package com.example.veeotech.postaltracking.warehouse;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.veeotech.postaltracking.BaseActivity;
import com.example.veeotech.postaltracking.R;
import com.example.veeotech.postaltracking.utils.ToastUtil;
import com.example.veeotech.postaltracking.warehouse.adapter.FragmentAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by VeeoTech on 2018/4/20.
 */

public class SelectWayActivity extends BaseActivity {

    @BindView(R.id.vp_warehouse_select_detail)
    ViewPager vpWarehouseSelectDetail;
    @BindView(R.id.tl_warehouse_select)
    TabLayout tlWarehouseSelect;

    private long firstTime = 0;
    //Tab 文字
    private final int[] TAB_TITLES = new int[]{R.string.warehouse_distinguish, R.string.warehouse_scanner_country, R.string.warehouse_deliver,
    R.string.warehouse_boarding};
    //Tab 图片
    private final int[] TAB_IMGS = new int[]{R.drawable.tl_warehouse_distinguish_image, R.drawable.tl_warehouse_country_image,
    R.drawable.tl_warehouse_deliver_image,R.drawable.tl_warehouse_boarding_image};

    private FragmentAdapter fragmentAdapter;
    private List<Fragment> fragmentList;

    @Override
    protected void init() {
        setToolBarTitle("倉庫員");
        setTabs(tlWarehouseSelect,getLayoutInflater(),TAB_TITLES,TAB_IMGS);
        fragmentList = new ArrayList<>();
        fragmentList.add(WareHouseFragment.getInstance());
        fragmentList.add(CountryFragment.getInstance());
        fragmentList.add(DeliverFragment.getInstance());
        fragmentList.add(BoardingFragment.getInstance());
        fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(),fragmentList);
        vpWarehouseSelectDetail.setAdapter(fragmentAdapter);
        vpWarehouseSelectDetail.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tlWarehouseSelect));
        tlWarehouseSelect.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(vpWarehouseSelectDetail));

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_select_way;
    }

    @Override
    protected boolean isShowBacking() {
        return false;
    }

    @Override
    protected boolean isShowRightText() {
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }


    /**
     * 添加tablayout tab
     */

    private void setTabs(TabLayout tabLayout, LayoutInflater inflater, int[] tabTitles, int[] tabImgs) {
        for (int i = 0; i < tabImgs.length; i++) {
            TabLayout.Tab tab = tabLayout.newTab();
            View view = inflater.inflate(R.layout.item_tablayout, null);
            tab.setCustomView(view);
            TextView tvTitle = (TextView) view.findViewById(R.id.tv_warehouse_tablayout_item);
            tvTitle.setText(tabTitles[i]);
            ImageView imgTab = (ImageView) view.findViewById(R.id.iv_warehouse_tablayout_item);
            imgTab.setImageResource(tabImgs[i]);
            tabLayout.addTab(tab);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 2000) {
                ToastUtil.showToastShort(getApplicationContext(), "再按一次退出程序");
                firstTime = secondTime;
                return true;
            } else {
                System.exit(0);
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}

