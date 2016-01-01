package com.guokrspace.duducar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.guokrspace.duducar.adapter.NewsAdapter;
import com.guokrspace.duducar.communication.http.model.News;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * Created by hyman on 15/12/22.
 */
public class NewsActivity extends AppCompatActivity implements Handler.Callback{

    private static final int HANDLE_REFRESH_OVER = 1;

    private Context context;
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;

    private NewsAdapter mAdapter;
    private List<News> newsRecord = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private SwipeRefreshLayout mRefreshLayout;

    private boolean isRefreshing = false;

    private SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (isRefreshing) {
                Toast.makeText(context, "正在刷新，请稍后...", Toast.LENGTH_SHORT).show();
                return;
            }
            /*MobclickAgent.reportError(context, "信息中心，界面刷新报错");*/
            isRefreshing = true;
            mHandler.sendEmptyMessageDelayed(HANDLE_REFRESH_OVER, 500);
        }
    };

    private Handler mHandler = new Handler(Looper.getMainLooper(), this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        context = NewsActivity.this;
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        //进入页面自动刷新
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setRefreshing(mRefreshLayout, true, true);
            }
        }, 500);

    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void initView() {
        initToolBar();

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        Resources resources = context.getResources();
        mRefreshLayout.setColorSchemeColors(resources.getColor(R.color.materialRed),
                resources.getColor(R.color.materialBlue),
                resources.getColor(R.color.materialYellow),
                resources.getColor(R.color.materialGreen));
        //下拉刷新
        mRefreshLayout.setOnRefreshListener(refreshListener);

        mRecyclerView = (RecyclerView) findViewById(R.id.news_recyclerview);
        newsRecord.addAll(getNews());
        mAdapter = new NewsAdapter(context, newsRecord);
        mRecyclerView.setAdapter(mAdapter);
        layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(40));

    }

    private void initToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("消息中心");
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewsActivity.this.finish();
            }
        });
    }

    public List<News> getNews() {
        List<News> newsList = new ArrayList<>();
        News news = new News(1, "嘟嘟迎元旦 打车送妹纸", "2015-12-24", "", "史上最大力度促销, 打车送妹子, 元旦不孤单", "");
        newsList.add(news);
        return newsList;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case HANDLE_REFRESH_OVER:
                isRefreshing = false;
                mRefreshLayout.setRefreshing(false);
                break;
        }
        return false;
    }

    private class SpaceItemDecoration extends RecyclerView.ItemDecoration{

        private int space;

        public SpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

            if(parent.getChildPosition(view) != 0)
                outRect.top = space;
        }
    }

    /**
     * 著作权归作者所有。
     * 商业转载请联系作者获得授权，非商业转载请注明出处。
     * 作者：RxRead
     * 链接：http://www.zhihu.com/question/35422150/answer/62708927
     * 来源：知乎
     */

    public static void setRefreshing(SwipeRefreshLayout refreshLayout,boolean refreshing, boolean notify){
        Class<? extends SwipeRefreshLayout> refreshLayoutClass = refreshLayout.getClass();
        if (refreshLayoutClass != null) {

            try {
                Method setRefreshing = refreshLayoutClass.getDeclaredMethod("setRefreshing", boolean.class, boolean.class);
                setRefreshing.setAccessible(true);
                setRefreshing.invoke(refreshLayout, refreshing, notify);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
