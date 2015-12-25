package com.guokrspace.duducar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.guokrspace.duducar.adapter.NewsAdapter;
import com.guokrspace.duducar.communication.http.model.News;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hyman on 15/12/22.
 */
public class NewsActivity extends AppCompatActivity {

    private Context context;
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;

    private NewsAdapter mAdapter;
    private List<News> newsRecord = new ArrayList<>();
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        context = NewsActivity.this;
        initView();
    }

    private void initView() {
        initToolBar();
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
}
