package com.guokrspace.dududriver.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.guokrspace.dududriver.DuduDriverApplication;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.adapter.RecordListAdapter;
import com.guokrspace.dududriver.common.Constants;
import com.guokrspace.dududriver.database.OrderRecord;
import com.guokrspace.dududriver.model.HistoryOrderResponseModel;
import com.guokrspace.dududriver.net.ResponseHandler;
import com.guokrspace.dududriver.net.SocketClient;
import com.guokrspace.dududriver.util.SharedPreferencesUtils;
import com.guokrspace.dududriver.view.DividerItemDecoration;
import com.squareup.picasso.Picasso;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by hyman on 15/10/23.
 */
public class MeFragment extends BaseFragment implements Handler.Callback{

    @Bind(R.id.avatar_civ)
    ImageView civAvatar;
    @Bind(R.id.name_tv)
    TextView tvName;
    @Bind(R.id.me_ratingbar)
    RatingBar mRatingbar;
    @Bind(R.id.rating_tv)
    TextView tvRating;
    @Bind(R.id.oder_num)
    TextView tvOderNum;
    @Bind(R.id.height_praise_rate)
    TextView tvPraiseRate;
    @Bind(R.id.balance_icon)
    ImageView ivBalance;
    @Bind(R.id.balance_tv)
    TextView tvBanlance;
    @Bind(R.id.balance_rl)
    RelativeLayout balanceLayout;
    @Bind(R.id.search_rl)
    RelativeLayout searchLayout;
    @Bind(R.id.order_records)
    RecyclerView mRecyclerView;
    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout refreshLayout;

    @OnClick(R.id.balance_rl) void enterBalancePage() {
        startActivity(new Intent(getActivity(), BalanceActivity.class));
        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @OnClick(R.id.search_rl) void enterSearchPage() {
        startActivity(new Intent(getActivity(), QueryActivity.class));
        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private DuduDriverApplication mApplication;
    private LinearLayoutManager mLayoutManager;

    public static final int LOAD_BASEINFO = 0x100;
    private static final int HANDLE_REFRESH_OVER = 0x111;

    private RecordListAdapter mAdapter;
    private List<OrderRecord> listItems = new ArrayList<>();

    private Long currentOrderId = Long.MAX_VALUE;

    private boolean isRefreshing = false;
    private boolean isLoading = false;
    private boolean hasMore = true;
    private boolean hasLoadLocalRecord = false;

    private Handler mHandler = new Handler(Looper.getMainLooper(), this);

    //刷新事件监听
    private SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {

        @Override
        public void onRefresh() {
            hasMore = true;
            if (isRefreshing) {
                showToast("正在刷新，请稍后...");
                return;
            }
            if (isNetworkAvailable()) {
                currentOrderId = Long.MAX_VALUE;

                SocketClient.getInstance().getHistoryOrders("old", Constants.ORDER_PAGE_NUM, currentOrderId, new ResponseHandler(Looper.myLooper()) {
                    @Override
                    public void onSuccess(String messageBody) {
                        refreshLayout.setRefreshing(false);
                        isRefreshing = false;
                        HistoryOrderResponseModel responseModel = null;
                        if (!TextUtils.isEmpty(messageBody)) {
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
                        refreshLayout.setRefreshing(false);
                        isRefreshing = false;
                        //失败加载本地数据
                        if (!hasLoadLocalRecord) {
                            List<OrderRecord> localRecords = mApplication.mDaoSession.getOrderRecordDao().loadAll();
                            loadRefreshRecord(localRecords);
                            hasLoadLocalRecord = true;
                        }
                        showToast("刷新失败...");
                    }

                    @Override
                    public void onTimeout() {
                        refreshLayout.setRefreshing(false);
                        isRefreshing = false;
                        if (!hasLoadLocalRecord) {
                            List<OrderRecord> localRecords = mApplication.mDaoSession.getOrderRecordDao().loadAll();
                            loadRefreshRecord(localRecords);
                            hasLoadLocalRecord = true;
                        }
                        showToast("请求超时...");
                    }
                });
//                    mHandler.sendMessageDelayed(mHandler.obtainMessage(HANDLE_REFRESH_OVER), 1000);
            } else {
                //无网络加载本地数据
                refreshLayout.setRefreshing(false);
                isRefreshing = false;
                if (!hasLoadLocalRecord) {
                    List<OrderRecord> localRecords = mApplication.mDaoSession.getOrderRecordDao().loadAll();
                    loadRefreshRecord(localRecords);
                    hasLoadLocalRecord = true;
                }
            }
        }

    };

    private void loadRefreshRecord(List<OrderRecord> orders) {
        if(orders.size() == 0){
            return;
        }
        Collections.sort(orders, new Comparator<OrderRecord>() {
            @Override
            public int compare(OrderRecord lhs, OrderRecord rhs) {
                Long lhsId = lhs.getId();
                Long rhsId = rhs.getId();
                if (lhsId < rhsId) return 1;
                return -1;
            }
        });
        if (orders.size() != 0) {
            listItems.clear();
            listItems.addAll(orders);
            mAdapter.notifyDataSetChanged();
        }
        currentOrderId = orders.get(orders.size() - 1).getId();
    }

    public static MeFragment newInstance() {
        final MeFragment meFragment = new MeFragment();
        return meFragment;
    }

    public Handler getHanlder(){
        mHandler = new Handler(this);
        return mHandler;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = DuduDriverApplication.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, container, false);
        ButterKnife.bind(this, view);
        Log.e("mefragment ", "createView init");
        initView();Log.e("mefragment ", "afterinit");
        return view;
    }

    @Override
    public void onResume() {
        Log.e("mefragment ", "super onresume");
        super.onResume();
        //更新司机个人信息
        Log.e("mefragment ", "onresume");
        updateDriverInfo();
        Log.e("mefragment ", "updateDriverInfo");
        //进入页面自动刷新
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setRefreshing(refreshLayout, true, true);
            }
        }, 2000);
        Log.e("mefragment ", "fresh delay");
    }

    private void initView() {
        //初始化历史订单列表
        mAdapter = new RecordListAdapter(getActivity(), listItems);
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        //添加onScrollListener实现加载更多
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
                int totalItemCount = mAdapter.getItemCount();
                //lastVisibleItem >= totalItemCount - 3 表示剩下3个item自动加载
                if (hasMore && lastVisibleItem >= totalItemCount - 3 && dy > 0) {
                    if (isRefreshing || isLoading) {
                        Log.d("hyman_log", "cannot load more");
                    } else {
                        loadMoreData();
                    }
                }
            }
        });

        //初始化SwipeRefreshLayout
        Resources resources = getActivity().getResources();
        refreshLayout.setColorSchemeColors(resources.getColor(R.color.materialRed),
                resources.getColor(R.color.materialBlue),
                resources.getColor(R.color.materialYellow),
                resources.getColor(R.color.materialGreen));
        //下拉刷新
        refreshLayout.setOnRefreshListener(refreshListener);

    }

    private void loadMoreData() {
        isLoading = true;

        // TODO: 加载数据
        SocketClient.getInstance().getHistoryOrders("old", Constants.ORDER_PAGE_NUM, currentOrderId, new ResponseHandler(Looper.getMainLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                isLoading = false;
                HistoryOrderResponseModel responseModel = null;
                if (!TextUtils.isEmpty(messageBody)) {
//                    responseModel = FastJsonTools.getObject(messageBody, HistoryOrderResponseModel.class);
                    responseModel = new Gson().fromJson(messageBody, HistoryOrderResponseModel.class);
                }
                List<OrderRecord> orders = null;
                if (responseModel != null) {
                    orders = responseModel.getOrder_list();
                    Collections.sort(orders, new Comparator<OrderRecord>() {
                        @Override
                        public int compare(OrderRecord lhs, OrderRecord rhs) {
                            Long lhsId = lhs.getId();
                            Long rhsId = rhs.getId();
                            if (lhsId < rhsId) return 1;
                            return -1;
                        }
                    });
                    if (orders.size() != 0) {
                        List<OrderRecord> orderRecords = new ArrayList<OrderRecord>();
                        orderRecords.addAll(orders);
                        listItems.addAll(orderRecords);
                        mAdapter.notifyDataSetChanged();
                    }
                    currentOrderId = orders.get(orders.size() - 1).getId();
                    if (orders.size() < Constants.ORDER_PAGE_NUM) {
                        hasMore = false;
                    }
                }

            }

            @Override
            public void onFailure(String error) {
                isLoading = false;
                showToast("加载更多失败...");
            }

            @Override
            public void onTimeout() {
                isLoading = false;
                showToast("加载更多超时...");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case LOAD_BASEINFO:
                updateDriverInfo();
            break;
            case HANDLE_REFRESH_OVER:
                refreshLayout.setRefreshing(false);
                showToast("加载完成");
                isRefreshing = false;
                break;
            default:
                break;
        }
        return false;
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

    private void updateDriverInfo(){
        String name = (String) SharedPreferencesUtils.getParam(getActivity(), Constants.PREFERENCE_KEY_DRIVER_NAME, "司机师傅");
        float stars = Float.parseFloat((String) SharedPreferencesUtils.getParam(getActivity(), Constants.PREFERNECE_KEY_DRIVER_RATING, "5"));
        int total_order = Integer.parseInt((String) SharedPreferencesUtils.getParam(getActivity(), Constants.PREFERENCE_KEY_DRIVER_TOTAL_ORDER, "0"));
        float favorable_rate = Float.parseFloat((String) SharedPreferencesUtils.getParam(getActivity(), Constants.PREFERENCE_KEY_DRIVER_FAVORABLE_RATE, "0.0"));
        String balance = (String) SharedPreferencesUtils.getParam(getActivity(), Constants.PREFERENCE_KEY_DRIVER_BALANCE, "0.00");
        String avatar = (String)SharedPreferencesUtils.getParam(getActivity(), Constants.PREFERENCE_KEY_DRIVER_AVATAR, "");

        tvName.setText(name);
        mRatingbar.setRating(stars);
        tvRating.setText(stars + " 星");
        tvOderNum.setText(String.format(getResources().getString(R.string.my_order_num), total_order));
        tvPraiseRate.setText("好评率 " + favorable_rate + "%");
        tvBanlance.setText(" " + balance);
        if(avatar.length() > 5){
            Picasso.with(getContext())
                    .load(avatar).centerCrop()
                    .fit().into(civAvatar);
        }

    }
}
