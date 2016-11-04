package com.guokrspace.dududriver.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.adapter.QueryGridAdapter;
import com.guokrspace.dududriver.view.DividerGridItemDecoration;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by hyman on 15/12/29.
 */
public class QueryActivity extends BaseActivity {

    private static final String URL_DIVIDE = "http://120.24.237.15/index.php/Webview/Index/index/type/fenchen";
    private static final String URL_BILL = "http://www.duducab.com/index.php/Weixin/Driver/account";
    private static final String URL_ACHIEVEMENT = "http://120.24.237.15/index.php/Webview/Index/index/type/cj_view";

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.query_items)
    RecyclerView mRecyclerView;
    private Context context;

    private QueryGridAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);
        ButterKnife.bind(this);
        context = QueryActivity.this;
        initView();
    }

    private void initView() {
        initToolBar();

        mAdapter = new QueryGridAdapter(context);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerGridItemDecoration(context));
        mRecyclerView.setAdapter(mAdapter);
        final int spanCount = 3;
        final GridLayoutManager layoutManager = new GridLayoutManager(context, spanCount);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter.setmOnItemClickListener(new QueryGridAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                switch (position) {
                    case 0:
                        //进入分成
                        enterWebView(WebViewActivity.WEBVIEW_DIVIDE, URL_DIVIDE);
                        break;
                    case 1:
                        //进入成绩查询
                        enterWebView(WebViewActivity.WEBVIEW_ACHIEVEMENT, URL_ACHIEVEMENT);
                        break;
                    case 2:
                        //进入账单
                        enterWebView(WebViewActivity.WEBVIEW_BILL, URL_BILL);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void enterWebView(int type, String url) {
        Intent _intent = new Intent(QueryActivity.this, WebViewActivity.class);
        _intent.putExtra(WebViewActivity.WEBVIEW_TYPE, type);
//        _intent.putExtra("url", url);
        startActivity(_intent);
    }

    private void initToolBar() {
        mToolbar.setTitle("");
        mToolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.return_black));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
