package com.guokrspace.duducar;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.dexafree.materialList.view.MaterialListView;
import com.guokrspace.duducar.adapter.OrdersAdapter;
import com.guokrspace.duducar.common.Constants;
import com.guokrspace.duducar.communication.ResponseHandler;
import com.guokrspace.duducar.communication.SocketClient;
import com.guokrspace.duducar.communication.http.model.Driver;
import com.guokrspace.duducar.communication.http.model.HistoryOrdersResponse;
import com.guokrspace.duducar.communication.http.model.Order;
import com.guokrspace.duducar.database.CommonUtil;
import com.guokrspace.duducar.database.DriverRecord;
import com.guokrspace.duducar.database.DriverRecordDao;
import com.guokrspace.duducar.communication.message.OrderDetail;
import com.guokrspace.duducar.database.OrderRecord;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OrderHistoryActivity.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OrderHistoryActivity} factory method to
 * create an instance of this fragment.
 */
public class OrderHistoryActivity extends AppCompatActivity implements Handler.Callback{


    private DuduApplication mApplication;
    private Context mContext;
    private MaterialListView materialListView;
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;

    private OrdersAdapter mAdapter;
    private List<Order> orderRecords = new ArrayList<>();
    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout mRefreshLayout;

    private static final int HANDLE_REFRESH_OVER = 0x111;

    private Long currentOrderId = Long.MAX_VALUE;

    private boolean isRefreshing = false;
    private boolean isLoading = false;
    private boolean hasMore = true;
    private boolean hasLoadLocalRecord = false;

    private Handler mHandler = new Handler(Looper.getMainLooper(), this);

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
                        mRefreshLayout.setRefreshing(false);
                        isRefreshing = false;
                        HistoryOrdersResponse responseModel = null;
                        if (!TextUtils.isEmpty(messageBody)) {
                            responseModel = JSON.parseObject(messageBody, HistoryOrdersResponse.class);
                        }
                        List<Order> orders = null;
                        if (responseModel != null) {
                            orders = responseModel.getOrder_list();
                            loadRefreshRecord(orders);
                            List<OrderRecord> orderRecords = new ArrayList<>();
                            transform2OrderRecords(orderRecords, orders);
                            mApplication.mDaoSession.getOrderRecordDao().deleteAll();
                            mApplication.mDaoSession.getOrderRecordDao().insertInTx(orderRecords);

                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        mRefreshLayout.setRefreshing(false);
                        isRefreshing = false;
                        //失败加载本地数据
                        if (!hasLoadLocalRecord) {
                            List<OrderRecord> localRecords = mApplication.mDaoSession.getOrderRecordDao().loadAll();
                            List<Order> orders = new ArrayList<Order>();
                            for (OrderRecord localRecord : localRecords) {
                                Order order = transform2Order(localRecord);
                                orders.add(order);
                            }
                            loadRefreshRecord(orders);
                            hasLoadLocalRecord = true;
                        }
                        showToast("刷新失败...");
                    }

                    @Override
                    public void onTimeout() {
                        mRefreshLayout.setRefreshing(false);
                        isRefreshing = false;
                        if (!hasLoadLocalRecord) {
                            List<OrderRecord> localRecords = mApplication.mDaoSession.getOrderRecordDao().loadAll();
                            List<Order> orders = new ArrayList<Order>();
                            for (OrderRecord localRecord : localRecords) {
                                Order order = transform2Order(localRecord);
                                orders.add(order);
                            }
                            loadRefreshRecord(orders);
                            hasLoadLocalRecord = true;
                        }
                        showToast("请求超时...");
                    }
                });
            } else {
                //无网络加载本地数据
                mRefreshLayout.setRefreshing(false);
                isRefreshing = false;
                if (!hasLoadLocalRecord) {
                    List<OrderRecord> localRecords = mApplication.mDaoSession.getOrderRecordDao().loadAll();
                    List<Order> orders = new ArrayList<Order>();
                    for (OrderRecord localRecord : localRecords) {
                        Order order = transform2Order(localRecord);
                        orders.add(order);
                    }
                    loadRefreshRecord(orders);
                    hasLoadLocalRecord = true;
                }
            }

        }
    };

    private Order transform2Order(OrderRecord localRecord) {
        Order order = new Order();
        order.setId(localRecord.getId());//
        order.setDriver_id(localRecord.getDriver_id());//
        order.setAdditional_price(localRecord.getAdditional_price());//
        order.setOrderNum(localRecord.getOrderNum());//
        order.setAdd_price3(localRecord.getAdd_price3());//
        order.setAdd_price1(localRecord.getAdd_price1());//
        order.setAdd_price2(localRecord.getAdd_price2());//
        order.setCar_type(localRecord.getCar_type());//
        order.setIsCancel(localRecord.getIsCancel());//
        order.setIsCityline(localRecord.getIsCityline());//
        order.setLow_speed_time(localRecord.getLow_speed_time());//
        order.setMileage(localRecord.getMileage());//
        order.setEnd_time(localRecord.getEnd_time());//
        order.setOrg_price(localRecord.getOrg_price());//
        order.setPassenger_id(localRecord.getPassenger_id());//
        order.setPassenger_mobile(localRecord.getPassenger_mobile());//
        order.setPay_role(localRecord.getPay_role());//
        order.setPay_time(localRecord.getPay_time());//
        order.setPay_type(localRecord.getPay_type());//
        order.setRating(localRecord.getRating());//
        order.setRent_type(localRecord.getRent_type());//
        order.setStart(localRecord.getStart());//
        order.setStart_lat(localRecord.getStart_lat());//
        order.setStart_lng(localRecord.getStart_lng());//
        order.setStart_time(localRecord.getStart_time());//
        order.setStatus(localRecord.getStatus());//
        order.setSumprice(localRecord.getSumprice());//
        order.setDestination(localRecord.getDestination());//
        order.setDestination_lat(localRecord.getDestination_lat());//
        order.setDestination_lng(localRecord.getDestination_lng());//
        order.setCityline_id(localRecord.getCityline_id());//
        order.setCompany(localRecord.getCompany());//

        DriverRecord driverRecord = mApplication.mDaoSession.getDriverRecordDao().queryBuilder().where(DriverRecordDao.Properties.Id.eq(localRecord.getDriver_id())).list().get(0);
        Driver driver = new Driver();
        driver.setId(driverRecord.getId());
        driver.setAvatar(driverRecord.getAvatar());
        driver.setDescription(driverRecord.getDescription());
        driver.setMobile(driverRecord.getMobile());
        driver.setName(driverRecord.getName());
        driver.setPicture(driverRecord.getPicture());
        driver.setPlate(driverRecord.getPlate());

        order.setDriver(driver);

        return order;
    }

    private void transform2OrderRecords(List<OrderRecord> orderRecords, List<Order> orders) {
        for (Order order : orders) {
            // 如果是司机代付，就不添加到列表中
            if (order.getPay_role() == 1) continue;
            OrderRecord orderRecord = getOrderRecord(order);
            orderRecords.add(orderRecord);
        }
    }

    @NonNull
    private OrderRecord getOrderRecord(Order order) {
        OrderRecord orderRecord = new OrderRecord();

        orderRecord.setId(order.getId());//
        orderRecord.setDriver_id(order.getDriver_id());//
        orderRecord.setAdditional_price(order.getAdditional_price());//
        orderRecord.setOrderNum(order.getOrderNum());//
        orderRecord.setAdd_price3(order.getAdd_price3());//
        orderRecord.setAdd_price1(order.getAdd_price1());//
        orderRecord.setAdd_price2(order.getAdd_price2());//
        orderRecord.setCar_type(order.getCar_type());//
        orderRecord.setIsCancel(order.getIsCancel());//
        orderRecord.setIsCityline(order.getIsCityline());//
        orderRecord.setLow_speed_time(order.getLow_speed_time());//
        orderRecord.setMileage(order.getMileage());//
        orderRecord.setEnd_time(order.getEnd_time());//
        orderRecord.setOrg_price(order.getOrg_price());//
        orderRecord.setPassenger_id(order.getPassenger_id());//
        orderRecord.setPassenger_mobile(order.getPassenger_mobile());//
        orderRecord.setPay_role(order.getPay_role());//
        orderRecord.setPay_time(order.getPay_time());//
        orderRecord.setPay_type(order.getPay_type());//
        orderRecord.setRating(order.getRating());//
        orderRecord.setRent_type(order.getRent_type());//
        orderRecord.setStart(order.getStart());//
        orderRecord.setStart_lat(order.getStart_lat());//
        orderRecord.setStart_lng(order.getStart_lng());//
        orderRecord.setStart_time(order.getStart_time());//
        orderRecord.setStatus(order.getStatus());//
        orderRecord.setSumprice(order.getSumprice());//
        orderRecord.setDestination(order.getDestination());//
        orderRecord.setDestination_lat(order.getDestination_lat());//
        orderRecord.setDestination_lng(order.getDestination_lng());//
        orderRecord.setCityline_id(order.getCityline_id());//
        orderRecord.setCompany(order.getCompany());//
        return orderRecord;
    }

    private void loadRefreshRecord(List<Order> orders) {

        Collections.sort(orders, new Comparator<Order>() {
            @Override
            public int compare(Order lhs, Order rhs) {
                Long lhsId = lhs.getId();
                Long rhsId = rhs.getId();
                if (lhsId < rhsId) return 1;
                return -1;
            }
        });
        if (orders.size() != 0) {
            orderRecords.clear();
            orderRecords.addAll(orders);
            mAdapter.notifyDataSetChanged();
        }
        currentOrderId = orders.get(orders.size() - 1).getId();
    }

    private void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_order_records);
        mApplication = (DuduApplication)getApplicationContext();
        mContext = this;
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //进入页面自动刷新
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setRefreshing(mRefreshLayout, true, true);
            }
        }, 500);
    }

    private void initView() {
        //init toolbar
        initToolBar();


//        orderRecords.addAll(mApplication.mDaoSession.getOrderRecordDao().queryBuilder().list());
        mRecyclerView = (RecyclerView) findViewById(R.id.order_records_recyclerview);
        mAdapter = new OrdersAdapter(mContext, orderRecords);
        mAdapter.setOnItemClickListener(new OrdersAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //点击进入详情界面
                Intent intent = new Intent(mContext, RatingActivity.class);
//                OrderDetail orderDetail = getOrderDetail(orderRecords.get(position));
                intent.putExtra("order", orderRecords.get(position));
                startActivity(intent);

            }

            @Override
            public void onItemLongClick(View view, int position) {
                //长按删除
                mAdapter.removeData(position);
                OrderRecord orderRecord = getOrderRecord(orderRecords.get(position));
                mApplication.mDaoSession.getOrderRecordDao().delete(orderRecord);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(40));

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
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        Resources resources = mContext.getResources();
        mRefreshLayout.setColorSchemeColors(resources.getColor(R.color.materialRed),
                resources.getColor(R.color.materialBlue),
                resources.getColor(R.color.materialYellow),
                resources.getColor(R.color.materialGreen));
        //下拉刷新
        mRefreshLayout.setOnRefreshListener(refreshListener);

    }

    private OrderDetail getOrderDetail(OrderRecord orderRecord){
        OrderDetail orderDetail = new OrderDetail();
        return orderDetail;
    }

    private void initToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("历史订单");
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OrderHistoryActivity.this.finish();
            }
        });
    }

    private void loadMoreData() {
        isLoading = true;
        // TODO: 加载数据
        SocketClient.getInstance().getHistoryOrders("old", Constants.ORDER_PAGE_NUM, currentOrderId, new ResponseHandler(Looper.getMainLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                isLoading = false;
                HistoryOrdersResponse responseModel = null;
                if (!TextUtils.isEmpty(messageBody)) {
//                    responseModel = FastJsonTools.getObject(messageBody, HistoryOrderResponseModel.class);
                    responseModel = JSON.parseObject(messageBody, HistoryOrdersResponse.class);
                }
                List<Order> orders = null;
                if (responseModel != null) {
                    orders = responseModel.getOrder_list();
                    Collections.sort(orders, new Comparator<Order>() {
                        @Override
                        public int compare(Order lhs, Order rhs) {
                            Long lhsId = lhs.getId();
                            Long rhsId = rhs.getId();
                            if (lhsId < rhsId) return 1;
                            return -1;
                        }
                    });
                    if (orders.size() != 0) {
                        orderRecords.addAll(orders);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        /*if(item.getItemId()==android.R.id.home)
        {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
                return false;
            }
        }*/
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case HANDLE_REFRESH_OVER:
                mRefreshLayout.setRefreshing(false);
                showToast("加载完成");
                isRefreshing = false;
                break;
            default:
                break;
        }
        return false;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
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

    /*
     *  判断网络是否可用
     * @return
     */
    protected boolean isNetworkAvailable() {
        boolean isNetConnected = CommonUtil.isNetworkAvailable(mContext);
        if (!isNetConnected) {
            showToast("当前网络不可用，请检查您的网络...");
        }
        return isNetConnected;
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
