package com.guokrspace.dududriver.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.adapter.TabPagerAdapter;
import com.viewpagerindicator.TabPageIndicator;

/**
 * Created by hyman on 15/10/22.
 */
public class MainActivity extends BaseActivity {

    private Context context;

    private ViewPager pager;
    private TabPagerAdapter mAdapter;
    private TabPageIndicator mIndicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        initView();
    }

    private void initView() {
        mIndicator = (TabPageIndicator) findViewById(R.id.indicator);

        pager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new TabPagerAdapter(getSupportFragmentManager());
        pager.setOffscreenPageLimit(1);//设置预加载页数,第一次进入程序起作用，因为都是从网络上获取数据，因此预加载意义不大
        //当然就缓存一页，因为每次的加载都需要保证列表是最新的
        pager.setAdapter(mAdapter);
        mIndicator.setViewPager(pager);
        mIndicator.setCurrentItem(1);//设置启动首先显示的抢单界面
    }
}
