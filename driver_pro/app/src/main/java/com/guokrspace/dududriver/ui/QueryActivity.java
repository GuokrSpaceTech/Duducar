package com.guokrspace.dududriver.ui;

import android.content.Context;
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
public class QueryActivity extends AppCompatActivity {

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
                        showToast("分成");
                        break;
                    case 1:
                        //进入成绩查询
                        showToast("成绩查询");
                        break;
                    case 2:
                        //进入账单
                        showToast("账单");
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void showToast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
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
