package com.guokrspace.dududriver.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gc.materialdesign.views.ButtonFlat;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.adapter.BillListAdapter;
import com.guokrspace.dududriver.common.Constants;
import com.guokrspace.dududriver.common.NewOrderReceiver;
import com.guokrspace.dududriver.database.BillRecord;
import com.guokrspace.dududriver.model.BillItem;
import com.guokrspace.dududriver.model.GetBalanceResponse;
import com.guokrspace.dududriver.model.GetBillsResponse;
import com.guokrspace.dududriver.model.WithdrawResponse;
import com.guokrspace.dududriver.net.ResponseHandler;
import com.guokrspace.dududriver.net.SocketClient;
import com.guokrspace.dududriver.util.CommonUtil;
import com.guokrspace.dududriver.util.FastJsonTools;
import com.guokrspace.dududriver.view.HymanScrollView;
import com.guokrspace.dududriver.view.NoScrollListView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.drakeet.materialdialog.MaterialDialog;

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
    @Bind(R.id.scrollview)
    HymanScrollView mScrollView;

    TextView availableTextView;
    EditText inputCash;
    ButtonFlat totalButton;
    RelativeLayout loadMoreLayout = null;
    TextView footViewText;

    boolean isWithdrawDate = true;

    @OnClick(R.id.substitute_pay)
    void doWithdraw() {
        if (!isNetworkAvailable()) {
            showToast("网络不可用");
            return;
        }

        if (!isWithdrawDate && confirmBalanceDialog != null) {
            confirmBalanceDialog.show();
            return;
        }

        progressDialog = new ProgressDialog(context, ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        //进行体现请求
        SocketClient.getInstance().sendGetBalanceRequest("1", new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                GetBalanceResponse response = null;
                if (!TextUtils.isEmpty(messageBody)) {
                    Log.e("hyman_balance", "加载成功：" + messageBody);
                    response = JSON.parseObject(messageBody, GetBalanceResponse.class);
                }
                if (response != null && !TextUtils.isEmpty(response.getAvailable())) {
                    availableBalance = Double.parseDouble(response.getAvailable());
                }
                if (confirmBalanceDialog != null) {
                    availableTextView.setText(availableBalance + "");
                    confirmBalanceDialog.show();
                }
            }

            @Override
            public void onFailure(String error) {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                if (!TextUtils.isEmpty(error)) {
//                    Log.e("hyman_balance", "加载失败：" + error);
                    showToast("加载失败: " + error);
                }
            }

            @Override
            public void onTimeout() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                showToast("请求超时...");
            }
        });
    }

    private static final int HANDLE_REFRESH_SUCCESS = 1;
    private static final int HANLDE_LOADMORE_OVER = 2;


    private Context context;

    private MaterialDialog confirmBalanceDialog = null;
    private ProgressDialog progressDialog = null;

    private BillListAdapter mAdapter;
    private List<BillItem> mDatas = new ArrayList<>();

    private double availableBalance = 0;
    private double withdrawBalance = 0.0;
    private Long currentBillId = Long.MAX_VALUE;
    private boolean isRefreshing = false;
    private boolean isLoading = false;
    private boolean hasMore = false;
    private boolean hasLoadLocalRecord = false;


    private NewOrderReceiver receiver;
    private MainOrderDialog dialog;


    private Handler mHandler = new Handler(Looper.getMainLooper(), this);
    private Runnable autoRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            setRefreshing(mRefreshLayout, true, true);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    //监听service传来的消息
    private void registerBroadcastReceiver(){
        receiver = new NewOrderReceiver(mHandler);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_NEW_ORDER);
        filter.setPriority(1000);
        registerReceiver(receiver, filter);
    }

    //刷新事件监听
    private SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            hasMore = true;
            hasLoadLocalRecord = true;
            if (isRefreshing) {
                showToast("正在刷新，请稍后...");
                return;
            }
            isRefreshing = true;
            if (isNetworkAvailable()) {
                currentBillId = Long.MAX_VALUE;
//                mHandler.sendEmptyMessageDelayed(HANDLE_REFRESH_OVER, 1000l);
                // TODO 刷新、获取账单流水数据
                SocketClient.getInstance().getBillsRequest("old", Constants.ORDER_PAGE_NUM, currentBillId, new ResponseHandler(Looper.myLooper()) {
                    @Override
                    public void onSuccess(String messageBody) {
                        mRefreshLayout.setRefreshing(false);
                        isRefreshing = false;
                        GetBillsResponse billsResponse = null;
                        if (!TextUtils.isEmpty(messageBody)) {
//                            Log.e("hyman_bill_log", "加载成功" + messageBody);
                                billsResponse = FastJsonTools.getObject(messageBody, GetBillsResponse.class);
//                            Log.e("hyman_bill_log", billsResponse.modelToString());
                        }
                        mHandler.sendMessage(mHandler.obtainMessage(HANDLE_REFRESH_SUCCESS, billsResponse.getBalance() == null ? "0.0" : billsResponse.getBalance()));
                        List<BillItem> bills = null;
                        if (billsResponse != null) {
                            bills = billsResponse.getBill_list();
                            loadRefreshRecord(bills);
                            List<BillRecord> billRecords = new ArrayList<>();
                            for (BillItem billItem : bills) {
                                billRecords.add(transform2BillRecord(billItem));
                            }
                            mApplication.mDaoSession.getBillRecordDao().deleteAll();
                            mApplication.mDaoSession.getBillRecordDao().insertInTx(billRecords);
                        }


                    }

                    @Override
                    public void onFailure(String error) {
                        mRefreshLayout.setRefreshing(false);
                        isRefreshing = false;
                        //失败加载本地数据
                        if (!hasLoadLocalRecord) {
                            List<BillRecord> localRecords = mApplication.mDaoSession.getBillRecordDao().loadAll();
                            List<BillItem> bills = new ArrayList<BillItem>();
                            for (BillRecord localRecord : localRecords) {
                                BillItem bill = transform2BillItem(localRecord);
                                bills.add(bill);
                            }
                            loadRefreshRecord(bills);
                            hasLoadLocalRecord = true;
                        }
//                        Log.e("hyman_bill_log", "加载成功" + error);
                        showToast("刷新失败...");
                    }

                    @Override
                    public void onTimeout() {
                        mRefreshLayout.setRefreshing(false);
                        isRefreshing = false;
                        if (!hasLoadLocalRecord) {
                            List<BillRecord> localRecords = mApplication.mDaoSession.getBillRecordDao().loadAll();
                            List<BillItem> bills = new ArrayList<BillItem>();
                            for (BillRecord localRecord : localRecords) {
                                BillItem bill = transform2BillItem(localRecord);
                                bills.add(bill);
                            }
                            loadRefreshRecord(bills);
                            hasLoadLocalRecord = true;
                        }
                        showToast("请求超时...");
                    }
                });
//                    mHandler.sendMessageDelayed(mHandler.obtainMessage(HANDLE_REFRESH_OVER), 1000);
            } else {
                //无网络加载本地数据
                mRefreshLayout.setRefreshing(false);
                isRefreshing = false;
                if (!hasLoadLocalRecord) {
                    List<BillRecord> localRecords = mApplication.mDaoSession.getBillRecordDao().loadAll();
                    List<BillItem> bills = new ArrayList<BillItem>();
                    for (BillRecord localRecord : localRecords) {
                        BillItem bill = transform2BillItem(localRecord);
                        bills.add(bill);
                    }
                    loadRefreshRecord(bills);
                    hasLoadLocalRecord = true;
                }
            }
        }

    };

    private BillItem transform2BillItem(BillRecord localRecord) {
        BillItem billItem = new BillItem();
        billItem.setId(localRecord.getId());
        billItem.setTime(localRecord.getTime());
        billItem.setOpposite(localRecord.getOpposite());
        billItem.setMoney(localRecord.getMoney());
        billItem.setDescription(localRecord.getDescription());
        billItem.setType(localRecord.getType());
        return billItem;
    }

    private BillRecord transform2BillRecord(BillItem billItem) {
        BillRecord billRecord = new BillRecord();
        billRecord.setId(billItem.getId());
        billRecord.setType(billItem.getType());
        billRecord.setDescription(billItem.getDescription());
        billRecord.setMoney(billItem.getMoney());
        billRecord.setOpposite(billItem.getOpposite());
        billRecord.setTime(billItem.getTime());
        return billRecord;
    }

    private void loadRefreshRecord(List<BillItem> bills) {
        Collections.sort(bills, new Comparator<BillItem>() {
            @Override
            public int compare(BillItem lhs, BillItem rhs) {
//                Long lhsTime = Long.parseLong(lhs.getTime());
//                Long rhsTime = Long.parseLong(rhs.getTime());
                Long lhsId = lhs.getId();
                Long rhsId = rhs.getId();
                if (lhsId < rhsId) return 1;
                return -1;
            }
        });
        if (bills.size() != 0) {
            mDatas.clear();
            mDatas.addAll(bills);
            mAdapter.notifyDataSetChanged();
        }
        currentBillId = bills.get(bills.size() - 1).getId();
        /*for (BillItem billItem : bills) {
            Log.e("hyman_refresh", billItem.modelToString());
        }*/
    }

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

        loadMoreLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.get_more_footview, null, false);
        footViewText = (TextView) loadMoreLayout.findViewById(R.id.tv_foot_title);
        footViewText.setText("正在加载...");
        loadMoreLayout.setVisibility(View.GONE);

        mListview.setNoScroll(true);
        mAdapter = new BillListAdapter(context, mDatas);
        mListview.addFooterView(loadMoreLayout);
        mListview.setAdapter(mAdapter);

        mScrollView.setOnGetMoreListener(new HymanScrollView.OnGetMoreListener() {
            @Override
            public void onGetMore() {
                if (hasMore) {
                    if (isRefreshing || isLoading) {
                        mScrollView.getMoreComplete();
                        if (loadMoreLayout != null) {
                            loadMoreLayout.setVisibility(View.GONE);
                        }
                        Log.d("hyman_log", "cannot load more");
                    } else {
                        if (loadMoreLayout != null) {
                            loadMoreLayout.setVisibility(View.VISIBLE);
                        }
                        loadMoreData();
                    }
                } else {
                    mScrollView.getMoreComplete();
                    if (loadMoreLayout != null) {
                        loadMoreLayout.setVisibility(View.GONE);
                    }
                }
            }
        });

        //初始化SwipeRefreshLayout
        Resources resources = context.getResources();
        mRefreshLayout.setColorSchemeColors(resources.getColor(R.color.materialRed),
                resources.getColor(R.color.materialBlue),
                resources.getColor(R.color.materialYellow),
                resources.getColor(R.color.materialGreen));
        //下拉刷新
        mRefreshLayout.setOnRefreshListener(refreshListener);
        int todayInWeek = CommonUtil.getDayOfWeek(new Date(System.currentTimeMillis()));
        if (todayInWeek == 2) {
            initConfirmBalanceDialog();
        } else {

            isWithdrawDate = false;
            withdrawButton.setBackgroundColor(Color.GRAY);
            confirmBalanceDialog = new MaterialDialog(context);
            confirmBalanceDialog.setTitle("提现提醒");
            confirmBalanceDialog.setMessage("下一个提现日为:" + CommonUtil.getNextWithdrawDate(todayInWeek));
            confirmBalanceDialog.setPositiveButton("确定", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmBalanceDialog.dismiss();
                }
            });
            confirmBalanceDialog.setCanceledOnTouchOutside(false);
        }

    }

    private void initConfirmBalanceDialog() {
        confirmBalanceDialog = new MaterialDialog(context);
        confirmBalanceDialog.setTitle("确认提现金额");
        LayoutInflater _inflater = LayoutInflater.from(context);
        RelativeLayout dialogView = (RelativeLayout) _inflater.inflate(R.layout.confirm_balance_dialog, null, false);
        availableTextView = (TextView) dialogView.findViewById(R.id.available_balance);
        inputCash = (EditText) dialogView.findViewById(R.id.input_cash);
        totalButton = (ButtonFlat) dialogView.findViewById(R.id.total_btn);
        totalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputCash != null) {
                    inputCash.setText(availableBalance + "");
                }
            }
        });
        confirmBalanceDialog.setContentView(dialogView);
        confirmBalanceDialog.setCanceledOnTouchOutside(false);
        confirmBalanceDialog.setNegativeButton("放弃", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputCash.setText("");
                confirmBalanceDialog.dismiss();
            }
        });
        confirmBalanceDialog.setPositiveButton("确认", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputBalance = inputCash.getText().toString();
                if (TextUtils.isEmpty(inputBalance)) {
                    showToast("请输入提现金额！");
                    return;
                }
                if (!CommonUtil.checkBalanceFormat(inputBalance)) {
                    showToast("输入格式不合法，请重新输入");
                    inputCash.setText("");
                    return;
                }
                if (Double.parseDouble(inputBalance) > availableBalance) {
                    showToast("余额不足");
                    inputCash.setText("");
                    return;
                }
                if (!isNetworkAvailable()) {
                    showToast("网络无法访问...");
                    return;
                }
                inputCash.setText("");
                confirmBalanceDialog.dismiss();
                if (progressDialog != null) {
                    progressDialog.show();
                }
                SocketClient.getInstance().sendWithdrawRequest("1", Double.parseDouble(inputBalance), new ResponseHandler(Looper.myLooper()) {
                    @Override
                    public void onSuccess(String messageBody) {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                        WithdrawResponse response = null;
                        if (!TextUtils.isEmpty(messageBody)) {
                            response = JSON.parseObject(messageBody, WithdrawResponse.class);
//                            Log.e("hyman_widthdraw", "请求成功：" + messageBody);
                        }
                        if (response != null) {
                            showToast("您成功提现 " + response.getCash() + " 元。");
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                        if (!TextUtils.isEmpty(error)) {
                            JSONObject jsonObject = JSON.parseObject(error);
                            if (jsonObject == null) {
                                showToast("请求失败");
                                return;
                            }
                            int status = jsonObject.getIntValue("status");
                            switch (status) {
                                case -1:
                                    showToast("非提现日");
                                    break;
                                case -2:
                                    showToast("余额不够");
                                    break;
                                default:
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onTimeout() {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                        showToast("请求超时，请稍后再试...");
                    }
                });
            }
        });


    }

    private void loadMoreData() {
        Log.e("hyman_loadmore", currentBillId + "");
        isLoading = true;
        // TODO: 加载数据
        SocketClient.getInstance().getBillsRequest("old", Constants.ORDER_PAGE_NUM, currentBillId, new ResponseHandler(Looper.getMainLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                mHandler.sendEmptyMessage(HANLDE_LOADMORE_OVER);
                isLoading = false;
                GetBillsResponse responseModel = null;
                if (!TextUtils.isEmpty(messageBody)) {
//                    responseModel = FastJsonTools.getObject(messageBody, HistoryOrderResponseModel.class);
                    responseModel = JSON.parseObject(messageBody, GetBillsResponse.class);
                }
                List<BillItem> bills = null;
                if (responseModel != null) {
                    bills = responseModel.getBill_list();
                    Collections.sort(bills, new Comparator<BillItem>() {
                        @Override
                        public int compare(BillItem lhs, BillItem rhs) {
                            Long lhsId = lhs.getId();
                            Long rhsId = rhs.getId();
                            if (lhsId < rhsId) return 1;
                            return -1;
                        }
                    });
                    if (bills.size() != 0) {
                        mDatas.addAll(bills);
                        mAdapter.notifyDataSetChanged();
                    }
                    if (bills.size() > 0) {
                        currentBillId = bills.get(bills.size() - 1).getId();
                    }
                    if (bills.size() < Constants.ORDER_PAGE_NUM) {
                        hasMore = false;
                    }
                }

            }

            @Override
            public void onFailure(String error) {
                mHandler.sendEmptyMessage(HANLDE_LOADMORE_OVER);
                isLoading = false;
                showToast("加载更多失败...");
            }

            @Override
            public void onTimeout() {
                mHandler.sendEmptyMessage(HANLDE_LOADMORE_OVER);
                isLoading = false;
                showToast("加载更多超时...");
            }
        });
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
        registerBroadcastReceiver();
        //进入页面自动刷新
        mHandler.postDelayed(autoRefreshRunnable, 200);
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
            case HANDLE_REFRESH_SUCCESS:
                balanceTextView.setText((CharSequence) msg.obj);
                break;
            case HANLDE_LOADMORE_OVER:
                mScrollView.getMoreComplete();
                if (loadMoreLayout != null) {
                    loadMoreLayout.setVisibility(View.GONE);
                }
                break;
            case Constants.MESSAGE_NEW_ORDER:
                if(CommonUtil.getCurOrderItem() == null){
                    return false;
                }
                if(dialog == null || !dialog.isVisible()){
                    dialog = new MainOrderDialog(context, CommonUtil.getCurOrderItem());
                    dialog.show(getSupportFragmentManager(), "mainorderdialog");
                }
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


    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        return;
    }
}
