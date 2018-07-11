package com.example.veeotech.postaltracking.warehouse.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by VeeoTech on 2018/4/23.
 */

public class FragmentAdapter extends FragmentPagerAdapter {
    List<Fragment> fragmentList;
    public FragmentAdapter(FragmentManager fm, List<Fragment> fragmentList) {
        super(fm);
        this.fragmentList = fragmentList;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position) ;
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }
}
