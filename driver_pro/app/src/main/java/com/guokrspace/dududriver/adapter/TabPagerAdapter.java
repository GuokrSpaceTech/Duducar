package com.guokrspace.dududriver.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.ui.GrabOrderFragment;
import com.guokrspace.dududriver.ui.MeFragment;
import com.guokrspace.dududriver.util.LogUtil;
import com.viewpagerindicator.IconPageIndicator;
import com.viewpagerindicator.IconPagerAdapter;

/**
 * Created by hyman on 15/10/22.
 */
public class TabPagerAdapter extends FragmentPagerAdapter implements IconPagerAdapter {

    protected static final String[] TITLE = new String[] { "我", "抢单", "更多" };

    protected static final int[] ICONS = new int[] {
            R.drawable.indicator_me_selector,
            R.drawable.indicator_graborder_selector,
            R.drawable.indicator_more_selector
    };

    public TabPagerAdapter(FragmentManager fm){
        super(fm);
    }
    @Override
    public Fragment getItem(int position) {

        LogUtil.i("getItem", position + "");
        switch (position) {
            case 0:
                return MeFragment.newInstance();

            case 1:
            case 2:
                return GrabOrderFragment.newInstance();

            default:
                break;
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLE[position % TITLE.length].toUpperCase();
    }


    @Override
    public int getIconResId(int index) {
        return ICONS[index % ICONS.length];
    }

    @Override
    public int getCount() {//获取数据集大小
        return TITLE.length;
    }
}
