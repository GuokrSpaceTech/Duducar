package com.guokrspace.dududriver.ui;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonFlat;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.adapter.BillListAdapter;
import com.guokrspace.dududriver.model.BillInfo;
import com.guokrspace.dududriver.view.NoScrollListView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by hyman on 15/12/9.
 */
public class BalanceActivity extends BaseActivity implements Handler.Callback{


    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.my_balance)
    TextView balanceTextView;
    @Bind(R.id.substitute_pay)
    ButtonFlat withdrawButton;
    @Bind(R.id.noscrolllistview)
    NoScrollListView mListview;
    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mRefreshLayout;

    @OnClick(R.id.substitute_pay)
    void doWithdraw() {
        //进行体现请求
    }

    private static final int HANDLE_REFRESH_OVER = 0x111;

    private Context context;

    private BillListAdapter mAdapter;
    private List<BillInfo> mDatas;

    private Long currentBillId = Long.MAX_VALUE;
    private boolean isRefreshing = false;
    private boolean isLoading = false;

    private Handler mHandler = new Handler(Looper.getMainLooper(), this);
    private Runnable autoRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            setRefreshing(mRefreshLayout, true, true);
        }
    };

    //刷新事件监听
    private SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {

        @Override
        public void onRefresh() {
            if (isRefreshing) {
                showToast("正在刷新，请稍后...");
                return;
            }
            if (isNetworkAvailable()) {
                currentBillId = Long.MAX_VALUE;
                mHandler.sendEmptyMessageDelayed(HANDLE_REFRESH_OVER, 1000l);
                // TODO 刷新、获取账单流水数据
                /*SocketClient.getInstance().getHistoryOrders("old", Constants.ORDER_PAGE_NUM, currentOrderId, new ResponseHandler(Looper.myLooper()) {
                    @Override
                    public void onSuccess(String messageBody) {
                        mRefreshLayout.setRefreshing(false);
                        isRefreshing = false;
                        HistoryOrderResponseModel responseModel = null;
                        if (!TextUtils.isEmpty(messageBody)) {
                            Log.e("hyman_log", "加载成功" + messageBody);
//                                responseModel = FastJsonTools.getObject(messageBody, HistoryOrderResponseModel.class);
                            responseModel = new Gson().fromJson(messageBody, HistoryOrderResponseModel.class);
                        }
                        List<OrderRecord> orders = null;
                        if (responseModel != null) {
                            Log.e("hyman_log", "hahaha");
                            orders = responseModel.getOrder_list();
                            loadRefreshRecord(orders);
                            mApplication.mDaoSession.getOrderRecordDao().deleteAll();
                            mApplication.mDaoSession.getOrderRecordDao().insertInTx(orders);
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        mRefreshLayout.setRefreshing(false);
                        isRefreshing = false;
                        //失败加载本地数据
                        *//*if (!hasLoadLocalRecord) {
                            List<OrderRecord> localRecords = mApplication.mDaoSession.getOrderRecordDao().loadAll();
                            loadRefreshRecord(localRecords);
                            hasLoadLocalRecord = true;
                        }*//*
                        showToast("刷新失败...");
                    }

                    @Override
                    public void onTimeout() {
                        mRefreshLayout.setRefreshing(false);
                        isRefreshing = false;
                        if (!hasLoadLocalRecord) {
                            List<OrderRecord> localRecords = mApplication.mDaoSession.getOrderRecordDao().loadAll();
                            loadRefreshRecord(localRecords);
                            hasLoadLocalRecord = true;
                        }
                        showToast("请求超时...");
                    }
                });*/
//                    mHandler.sendMessageDelayed(mHandler.obtainMessage(HANDLE_REFRESH_OVER), 1000);
            } else {
                //无网络加载本地数据
                mRefreshLayout.setRefreshing(false);
                isRefreshing = false;
                /*if (!hasLoadLocalRecord) {
                    List<OrderRecord> localRecords = mApplication.mDaoSession.getOrderRecordDao().loadAll();
                    loadRefreshRecord(localRecords);
                    hasLoadLocalRecord = true;
                }*/
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);
        ButterKnife.bind(this);
        context = BalanceActivity.this;

        initView();

    }

    private void initView() {
        //init toolbar
        initToolBar();

        mDatas = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            mDatas.add(new BillInfo("156.0", "13600000008", "1", "1456783290", "收到车费"));
        }
        mListview.setNoScroll(true);
        mAdapter = new BillListAdapter(context, mDatas);
        mListview.setAdapter(mAdapter);

        //初始化SwipeRefreshLayout
        Resources resources = context.getResources();
        mRefreshLayout.setColorSchemeColors(resources.getColor(R.color.materialRed),
                resources.getColor(R.color.materialBlue),
                resources.getColor(R.color.materialYellow),
                resources.getColor(R.color.materialGreen));
        //下拉刷新
        mRefreshLayout.setOnRefreshListener(refreshListener);
    }

    private void initToolBar() {
        mToolbar.setTitle("");
        mToolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.return_white));
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

    @Override
    protected void onResume() {
        super.onResume();
        //进入页面自动刷新
        mHandler.postDelayed(autoRefreshRunnable, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(autoRefreshRunnable);
        ButterKnife.unbind(this);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case HANDLE_REFRESH_OVER:
                if (mRefreshLayout != null) {
                    mRefreshLayout.setRefreshing(false);
                }
                showToast("加载完成");
                isRefreshing = false;
                break;
            default:
                break;
        }
        return false;
    }


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
