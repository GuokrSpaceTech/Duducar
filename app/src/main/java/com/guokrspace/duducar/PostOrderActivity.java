package com.guokrspace.duducar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.alibaba.fastjson.JSON;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.guokrspace.duducar.adapter.CancelReasonAdapter;
import com.guokrspace.duducar.common.Constants;
import com.guokrspace.duducar.communication.ResponseHandler;
import com.guokrspace.duducar.communication.SocketClient;
import com.guokrspace.duducar.communication.fastjson.FastJsonTools;
import com.guokrspace.duducar.communication.http.model.IdAndValueModel;
import com.guokrspace.duducar.communication.message.ChargeDetail;
import com.guokrspace.duducar.communication.message.DriverDetail;
import com.guokrspace.duducar.communication.message.DriverInfo;
import com.guokrspace.duducar.communication.message.MessageTag;
import com.guokrspace.duducar.communication.message.NearByCars;
import com.guokrspace.duducar.communication.message.OrderDetail;
import com.guokrspace.duducar.communication.message.SearchLocation;
import com.guokrspace.duducar.communication.message.TripOver;
import com.guokrspace.duducar.communication.message.TripStart;
import com.guokrspace.duducar.database.CommonUtil;
import com.guokrspace.duducar.database.OrderRecord;
import com.guokrspace.duducar.ui.DriverInformationView;
import com.guokrspace.duducar.util.SharedPreferencesUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.drakeet.materialdialog.MaterialDialog;

public class PostOrderActivity extends AppCompatActivity {

    //UI
    Context mContext = this;
    String orderStatusString = "正在预约车辆";
    TextView mDestTextView;
    TextView mStartTextView;
    TextView mFab;
    TextView mCurrentChargeView;
    DriverInformationView driverView;
    boolean isFirstLoc = true;// 是否首次定位
    boolean isStartFollow = false;
    ProgressBar mProgressBar;
    Toolbar mToolbar;
    Button cancelButton;

    MaterialDialog cancelDialog;
    ProgressDialog progressDialog;

    int state;

    private IdAndValueModel reasonSelected;

    //Baidu Map Location
    MapView mMapView = null;
    BaiduMap mBaiduMap = null;
    BitmapDescriptor mCurrentMarker;

    MarkerOptions markerOptions;
    Marker marker;

    LocationClient mLocClient;
    public MyLocationListener myListener = new MyLocationListener();
    private LatLng mPrevLatLng;//上一次的经纬度地址
    private LatLng mCurrLatLng;//当前的经纬度地址
    private boolean mIsFirstDraw = true;
    private long wait4Car;


    //Data
    SearchLocation start;
    SearchLocation dest;
    DriverInfo driver;
    TripStart order_start;
    ChargeDetail charge_detail;
    TripOver order_finish;
    LatLng currentLocation;
    LatLng prevLocation;
    DuduApplication mApplication;

    //Activity Start RequestCode
    public final static int ACTIVITY_SEARCH_DEST_REQUEST = 0x1001;

    final static int WAITING_FOR_ORDER_CONFIRM = 1;
    final static int WAITING_FOR_DRIVER_COME = 2;
    final static int ORDER_CANCELLING = 3;
    final static int ORDER_CANCEL_CONFIRMED = 4;
    final static int INCAR = 5;
    final static int ORDER_COMPETED = 6;
    final static int CANCEL_ORDER_SUCCESS = 7;
    final static int CANCEL_ORDER_FAILURE = 8;
    final static int CANCEL_ORDER_TIMEOUT = 9;


    private Timer timer;

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case MessageTag.MESSAGE_ORDER_CANCEL_CONFIRMED:
                    if (state == ORDER_CANCELLING) {
                        getSupportActionBar().setTitle("订单已经取消");
                        finish();
                    }
                    break;
                case MessageTag.MESSAGE_ORDER_CANCEL_TIMEOUT:
                    if (state == ORDER_CANCELLING) {
                        getSupportActionBar().setTitle("消息发送失败,请检查网络连接");
//                        mFab.setClickable(false);
//                        mFab.setVisibility(View.GONE);
//                        mFab.setEnabled(false);
                    }
                    break;
                case MessageTag.MESSAGE_ORDER_CANCEL_ERROR:
                    if(state == ORDER_CANCELLING) {
                        getSupportActionBar().setTitle("订单已取消");
                        finish();
                    }
                    break;
                case MessageTag.MESSAGE_ORDER_DISPATCHED:
                    if (state == WAITING_FOR_ORDER_CONFIRM) {   //The driverview moves up by 140dp
                        LinearLayout bmapLayout = (LinearLayout) findViewById(R.id.bmapLayout);
                        RelativeLayout.LayoutParams paramRL = (RelativeLayout.LayoutParams) bmapLayout.getLayoutParams();
                        paramRL.height = bmapLayout.getHeight() - dpToPx(getResources(), 120);
                        bmapLayout.requestLayout();

                        driverView = (DriverInformationView) findViewById(R.id.driverView);
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) driverView.getLayoutParams();

                        RelativeLayout container = (RelativeLayout) findViewById(R.id.container);
                        container.requestLayout();

                        orderStatusString = "出租车即将到达";
                        getSupportActionBar().setTitle(orderStatusString);
                        cancelButton.setVisibility(View.VISIBLE);

                        if (driver != null) {
                            updateDriverUI(driver);
                            mApplication.mDriverDetail = driver.getDriver();
                            TextView cancelPrompt = (TextView) findViewById(R.id.cancelPromptTextView);
                            cancelPrompt.setVisibility(View.GONE);
                            mProgressBar.setVisibility(View.GONE);
                            mFab.setVisibility(View.GONE);

                        }
                    }

                    timer.cancel();

                    break;
                case MessageTag.MESSAGE_CAR_ARRIVED:
                    if (cancelDialog != null) cancelDialog.dismiss();
                    cancelButton.setVisibility(View.GONE);
                    orderStatusString = "已经上车";
                    getSupportActionBar().setTitle(orderStatusString);

                    //修正上车地点
                    if (order_start != null) {
//                        Double lat = order_start.getOrder().getStart_lat();
//                        Double lng = order_start.getOrder().getStart_lng();
//                        LatLng ll = new LatLng(lat, lng);
//
//                        mBaiduMap.clear();
//                        mBaiduMap.addOverlay(new MarkerOptions()
//                                .position(ll)
//                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_position_pin)));
//                        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(ll));
                    }
                    mCurrentChargeView.setVisibility(View.VISIBLE);
                    mBaiduMap.clear();
                    mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.caricon);
                    mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true, mCurrentMarker));

                    isStartFollow = true;
                    break;
                case MessageTag.MESSAGE_UPDATE_CHARGE:
                    //TODO : 更新费用信息
                    if(charge_detail != null) {
                        mCurrentChargeView.setText(
                                "当前车费: " + charge_detail.getCurrent_charge() + "元\n" +
                                "行驶里程: " + charge_detail.getCurrent_mile() + "公里\n" +
                                "低速行驶: " + charge_detail.getLow_speed_time() + "分钟"
                        );
                    }
                    break;
                case MessageTag.MESSAGE_ORDER_COMPLETED:
                    if (order_finish != null) {
                        orderStatusString = String.format("到达%s", order_finish.getOrder().getDestination());
                        getSupportActionBar().setTitle(orderStatusString);

                        Intent intent = new Intent(mContext, PayCostActivity.class);
                        order_finish.getOrder().setDriver(JSON.toJSONString(driver.getDriver()));
                        intent.putExtra("order",order_finish.getOrder());
                        startActivity(intent);
                        isStartFollow = false;
                        finish();
                    }
                    break;
                case MessageTag.MESSAGE_UPDATE_TRACK:
                    Log.e("daddy", "update track");
                    if(currentLocation == null)
                        return;
                    if(mIsFirstDraw){
                        prevLocation = currentLocation;
                        mIsFirstDraw = false;
                    }

                    if(Math.abs(prevLocation.latitude- currentLocation.latitude) > 0.001
                            ||Math.abs(prevLocation.longitude- currentLocation.longitude) > 0.001 )
                        //异常定位
                        return;
                    drawLine(mBaiduMap, prevLocation, currentLocation);
                    prevLocation = currentLocation;
                    break;
                case CANCEL_ORDER_SUCCESS:
                    String messageBody = (String) message.obj;
                    progressDialog.dismiss();
                    showToast("订单已取消！");
                    PostOrderActivity.this.finish();
                    break;
                case CANCEL_ORDER_FAILURE:
                    progressDialog.dismiss();
                    String error = (String) message.obj;
                    if (!TextUtils.isEmpty(error)) {
                        com.alibaba.fastjson.JSONObject errorJson = JSON.parseObject(error);
                        String status = errorJson.getString("status");
                        if (TextUtils.equals(status, "-101")) {
                            showToast("原本没有订单！");

                        } else if (TextUtils.equals(status, "-102")) {
                            showToast("已经开始计费，无法取消！");
                        }
                        return;
                    }
                    showToast("提交失败~");
                    break;
                case CANCEL_ORDER_TIMEOUT:
                    progressDialog.dismiss();
                    showToast("提交超时~");
                    break;
                default:
                    break;
            }
        }
    };

    private void initOrder(OrderDetail orderDetail){
        start = new SearchLocation();
        start.setLat(Double.parseDouble(orderDetail.getStart_lat()));
        start.setLng(Double.parseDouble(orderDetail.getStart_lng()));
        start.setAddress(orderDetail.getStart());
        dest = new SearchLocation();
        dest.setLat(Double.parseDouble(orderDetail.getDestination_lat()));
        dest.setLng(Double.parseDouble(orderDetail.getDestination_lng()));
        dest.setAddress(orderDetail.getDestination());
    }

    private void initBaiduMap(){
        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, mCurrentMarker));

        LatLng initLoc = new LatLng(CommonUtil.getCurLat(), CommonUtil.getCurLng());
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(initLoc);
        mBaiduMap.animateMapStatus(u);

        // 定位初始化
        initLocation();
        mLocClient.start();

        // 隐藏缩放控件
        int childCount = mMapView.getChildCount();
        View zoom = null;
        for (int i = 0; i < childCount; i++) {
            View child = mMapView.getChildAt(i);
            if (child instanceof ZoomControls) {
                zoom = child;
                break;
            }
        }
        zoom.setVisibility(View.GONE);

        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(18.0f);
        mBaiduMap.setMapStatus(msu);
    }

    private void initBaseUI(){

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mFab = (Button) findViewById(R.id.fab);

        mCurrentChargeView = (TextView) findViewById(R.id.currentChargeView);
        mStartTextView = (TextView) findViewById(R.id.textViewStart);
        mDestTextView = (TextView) findViewById(R.id.textViewDestination);
        if (start != null)
            mStartTextView.setText(start.getAddress());
        if (dest != null)
            mDestTextView.setText(dest.getAddress());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mApplication = (DuduApplication) getApplicationContext();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_cab);

        initToolBar();
        AppExitUtil.getInstance().addActivity(this);

        //Get Args
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            boolean isRecover = bundle.getBoolean("isRecover", false);
            if(isRecover){
                String status = bundle.getString("status");
                if(status.equals("1")){ // '1-订单初始化 2-接单 3-开始 4-结束 5-取消’,
                    //初始化订单信息
                    initOrder((OrderDetail)bundle.getSerializable("order_detail"));
                    //重新发送
                    cancelOrder();
                    requestCar();
                    //加载界面
                } else if(status.equals("2")){
                    OrderDetail orderDetail = (OrderDetail) bundle.getSerializable("order_detail");
                    initOrder(orderDetail);
                    driver = new DriverInfo();
                    driver.setDriver(new Gson().fromJson(orderDetail.getDriver(), DriverDetail.class));
                    state = WAITING_FOR_ORDER_CONFIRM;
                    initBaiduMap();
                    initBaseUI();
                    mHandler.sendEmptyMessage(MessageTag.MESSAGE_ORDER_DISPATCHED);
                    return;
                } else if(status.equals("3")){
                    OrderDetail orderDetail = (OrderDetail) bundle.getSerializable("order_detail");
                    initOrder(orderDetail);
                    driver = new DriverInfo();
                    driver.setDriver(new Gson().fromJson(orderDetail.getDriver(), DriverDetail.class));
                    initBaiduMap();
                    initBaseUI();

                    isWaitForCar = false;
                    mApplication.mDriverDetail = driver.getDriver();
                    TextView cancelPrompt = (TextView) findViewById(R.id.cancelPromptTextView);
                    cancelPrompt.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.GONE);
                    mFab.setVisibility(View.GONE);
                    order_start = new TripStart();
                    order_start.setOrder(orderDetail);
                    mHandler.sendEmptyMessage(MessageTag.MESSAGE_CAR_ARRIVED);
                    return;
                } else {
                    Log.e("daddy", "recover in error status");
                    finish();
                }
            } else {
                start = (SearchLocation) bundle.get("start");
                dest = (SearchLocation) bundle.get("dest");

                Log.e("daddy", "request car");
                wait4Car = System.currentTimeMillis();
                requestCar();
            }
        }

        //初始化百度地图
        initBaiduMap();

        //UI
        initBaseUI();

        mFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Snackbar.make(view, "订单取消中...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                state = ORDER_CANCELLING;

                SocketClient.getInstance().sendOrderCancel(Constants.PASSENGER_ROLE,
                        new ResponseHandler(Looper.myLooper()) {
                            @Override
                            public void onSuccess(String messageBody) {
                                //取消成功
                                mHandler.sendEmptyMessage(MessageTag.MESSAGE_ORDER_CANCEL_CONFIRMED);
                            }

                            @Override
                            public void onFailure(String error) {
                                mHandler.sendEmptyMessage(MessageTag.MESSAGE_ORDER_CANCEL_ERROR);
                            }

                            @Override
                            public void onTimeout() {
                                mHandler.sendEmptyMessage(MessageTag.MESSAGE_ORDER_CANCEL_TIMEOUT);
                            }
                        });
                return false;
            }
        });

        mCurrentChargeView.setVisibility(View.GONE);
        state = WAITING_FOR_ORDER_CONFIRM;

//        getSupportActionBar().setTitle("正在预约中...");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mApplication = (DuduApplication) getApplicationContext();

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
    }

    private void initToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("正在预约中...");
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
            }
        });

        cancelButton = (Button) findViewById(R.id.cancel_btn);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelDialog.show();
            }
        });
        initCancelDialog();
    }

    /*
     *  初始化取消订单dialog
     *  @author hyman
     */
    private void initCancelDialog() {
        cancelDialog = new MaterialDialog(PostOrderActivity.this);
        cancelDialog.setTitle("请选择取消原因：");
        LayoutInflater _inflater = LayoutInflater.from(mContext);
        LinearLayout dialogView = (LinearLayout) _inflater.inflate(R.layout.order_cancel_dialog, null, false);
        ListView reasonList = (ListView) dialogView.findViewById(R.id.reason_list);
        final List<IdAndValueModel> idAndValueModels = new ArrayList<>();
        String reasons = (String) SharedPreferencesUtils.getParam(mContext, SharedPreferencesUtils.BASEINFO_CANCEL_REASONS, "");
        if (!TextUtils.isEmpty(reasons)) {
            Log.e("hyman_baseinfo", reasons);
//            idAndValueModels.addAll(FastJsonTools.getListObject(reasons, IdAndValueModel.class));
            List<IdAndValueModel> idAndValueModels1 = new Gson().fromJson(reasons, new TypeToken<ArrayList<IdAndValueModel>>() {
            }.getType());
            idAndValueModels.addAll(idAndValueModels1);
            Log.e("hyman_baseinfo", idAndValueModels.size() + " ");
        }
        final CancelReasonAdapter _adapter = new CancelReasonAdapter(mContext, idAndValueModels);
        _adapter.setOnItemCheckedListener(new CancelReasonAdapter.OnItemCheckedListener() {
            @Override
            public void onItemChecked(int position) {
                Toast.makeText(mContext, "选中了 " + (position + 1), Toast.LENGTH_SHORT).show();
                reasonSelected = idAndValueModels.get(position);
            }
        });
        reasonList.setAdapter(_adapter);
        cancelDialog.setContentView(dialogView);
        cancelDialog.setCanceledOnTouchOutside(false);
        cancelDialog.setNegativeButton("放弃", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _adapter.clearCheckRecord();
                cancelDialog.dismiss();
            }
        });
        cancelDialog.setPositiveButton("确定", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _adapter.clearCheckRecord();
                cancelDialog.dismiss();
                //提交取消原因
                progressDialog = new ProgressDialog(mContext, ProgressDialog.STYLE_SPINNER);
                progressDialog.show();
                SocketClient.getInstance().sendCancelOrderRequest(reasonSelected.getId(), "2", new ResponseHandler(Looper.myLooper()) {
                    @Override
                    public void onSuccess(String messageBody) {

                        mHandler.sendMessageDelayed(mHandler.obtainMessage(CANCEL_ORDER_SUCCESS, messageBody), 500l);

                    }

                    @Override
                    public void onFailure(String error) {

                        mHandler.sendMessageDelayed(mHandler.obtainMessage(CANCEL_ORDER_FAILURE, error), 500l);
                    }

                    @Override
                    public void onTimeout() {

                        mHandler.sendEmptyMessageDelayed(CANCEL_ORDER_TIMEOUT, 500l);

                    }
                });
            }
        });

    }

    private void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {
            if (isStartFollow) {
              moveTaskToBack(true);
              return true;
            }
            cancelOption();
        }
        return false;
    }

    private boolean cancelOption() {

        if(mFab.getVisibility() != View.VISIBLE){
            return true;
        }
        final AlertDialog.Builder alterDialog = new AlertDialog.Builder(this);
        alterDialog.setMessage("要撤销订单吗？");
        alterDialog.setCancelable(true);

        alterDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                state = ORDER_CANCELLING;
                SocketClient.getInstance().sendOrderCancel(Constants.PASSENGER_ROLE,
                        new ResponseHandler(Looper.myLooper()) {
                            @Override
                            public void onSuccess(String messageBody) {
                                //取消成功
                                mHandler.sendEmptyMessage(MessageTag.MESSAGE_ORDER_CANCEL_CONFIRMED);
                            }

                            @Override
                            public void onFailure(String error) {
                                mHandler.sendEmptyMessage(MessageTag.MESSAGE_ORDER_CANCEL_ERROR);
                            }

                            @Override
                            public void onTimeout() {
                                mHandler.sendEmptyMessage(MessageTag.MESSAGE_ORDER_CANCEL_TIMEOUT);
                                Toast.makeText(PostOrderActivity.this, "连接超时, 请检查网络..", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        alterDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alterDialog.show();
        return false;
    }

    private boolean isWaitForCar = false;

    @Override
    protected void onResume() {
        super.onResume();

        SocketClient.getInstance().registerServerMessageHandler(MessageTag.ORDER_ACCEPT, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                driver = FastJsonTools.getObject(messageBody, DriverInfo.class);
                isWaitForCar = true;
                mHandler.sendEmptyMessage(MessageTag.MESSAGE_ORDER_DISPATCHED);
            }

            @Override
            public void onFailure(String error) {
            }

            @Override
            public void onTimeout() {
            }
        });


        mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.caricon);
        //监听司机的未知
        SocketClient.getInstance().registerServerMessageHandler(MessageTag.DRIVER_POSITION, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                if(!isWaitForCar){
                    return;
                }
                try {
                    JSONObject position = new JSONObject(messageBody);
                    double lat = Double.parseDouble(position.getString("lat"));
                    double lng = Double.parseDouble(position.getString("lng"));
                    if(marker != null){
                        marker.remove();
                    }
                    markerOptions = new MarkerOptions().icon(mCurrentMarker).position(new LatLng(lat, lng));
                    marker = (Marker) mBaiduMap.addOverlay(markerOptions);
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String error) {

            }

            @Override
            public void onTimeout() {

            }
        });

        SocketClient.getInstance().registerServerMessageHandler(MessageTag.TRIP_START, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                isWaitForCar = false;
                order_start = FastJsonTools.getObject(messageBody, TripStart.class);
                mHandler.sendEmptyMessage(MessageTag.MESSAGE_CAR_ARRIVED);
            }

            @Override
            public void onFailure(String error) {

            }

            @Override
            public void onTimeout() {

            }
        });

        SocketClient.getInstance().registerServerMessageHandler(MessageTag.CURRENT_TRIP_FEE, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                charge_detail = FastJsonTools.getObject(messageBody, ChargeDetail.class);
                mHandler.sendEmptyMessage(MessageTag.MESSAGE_UPDATE_CHARGE);
            }

            @Override
            public void onFailure(String error) {

            }

            @Override
            public void onTimeout() {

            }
        });

        SocketClient.getInstance().registerServerMessageHandler(MessageTag.TRIP_OVER, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                order_finish = FastJsonTools.getObject(messageBody, TripOver.class);

                OrderRecord orderRecord = new OrderRecord();
                orderRecord.setStart(order_start.getOrder().getStart());
                orderRecord.setDestination(order_finish.getOrder().getDestination());
                Double startLat = Double.parseDouble(order_finish.getOrder().getStart_lat());
                Double startLng = Double.parseDouble(order_finish.getOrder().getStart_lng());
                Double destLat = Double.parseDouble(order_finish.getOrder().getDestination_lat());
                Double destLng = Double.parseDouble(order_finish.getOrder().getDestination_lng());
                orderRecord.setStart_lat(startLat == null ? 0.0 : startLat);
                orderRecord.setStart_lng(startLng == null ? 0.0 : startLng);
                orderRecord.setDestination_lat(destLat == null ? 0.0 : destLat);
                orderRecord.setDestination_lng(destLng == null ? 0.0 : destLng);
                orderRecord.setMileage(order_finish.getOrder().getMileage());
                orderRecord.setOrg_price(order_finish.getOrder().getOrg_price());
                orderRecord.setCar_type(Integer.parseInt(order_finish.getOrder().getCar_type()));
                SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm");
                String dateStr = sdf.format(new Date());
                orderRecord.setEnd_time(System.currentTimeMillis());

//                mApplication.mDaoSession.getOrderRecordDao().insert(orderRecord);
                mHandler.sendEmptyMessage(MessageTag.MESSAGE_ORDER_COMPLETED);
            }

            @Override
            public void onFailure(String error) {

            }

            @Override
            public void onTimeout() {

            }
        });

        /*
         * Start the timer to for peoriodically ask for nearby cars and draw route line
         */
        timer = new Timer();
        timer.scheduleAtFixedRate(new MyTimerTask(), 2000, 3 * 1000);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case android.R.id.home:
                return cancelOption();
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        timer.cancel();
        timer.purge();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mCurrentMarker != null)
            mCurrentMarker.recycle();
        if (mLocClient != null)
            mLocClient.stop();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == ACTIVITY_SEARCH_DEST_REQUEST) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    dest = (SearchLocation) bundle.get("location");

                    if (dest != null)
                        mDestTextView.setText(dest.getAddress());
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public int dpToPx(Resources res, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
    }

    private void requestCar() {
        String startAddr = (start == null) ? "" : start.getAddress();
        String destAddr = (dest == null) ? "" : dest.getAddress();
        Double startLat = (start == null) ? 0D : start.getLat();
        Double destLat = (dest == null) ? 0D : dest.getLat();
        Double startLng = (start == null) ? 0D : start.getLng();
        Double destLng = (dest == null) ? 0D : dest.getLng();
        SocketClient.getInstance().sendCarRequest(
                "2",
                startAddr,
                destAddr,
                startLat,
                startLng,
                destLat,
                destLng,
                "",
                "",
                "1",
                new ResponseHandler(Looper.getMainLooper()) {
                    @Override
                    public void onSuccess(String messageBody) {
                        Log.e("daddy", "request success");
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.i("", "");
                        //Cancel TripOverOrder
                        cancelOrder();
                        // Try again
                        requestCar();
                    }

                    @Override
                    public void onTimeout() {
                        //Test: Simulate Success
                        cancelOrder();
                        // Try again
                        requestCar();
                    }
                });
    }

    private void cancelOrder() {
        //Cancel TripOverOrder
        SocketClient.getInstance().cancelCarRequest(Constants.PASSENGER_ROLE, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                Log.i("", "");
            }

            @Override
            public void onFailure(String error) {
                Log.i("", "");
            }

            @Override
            public void onTimeout() {
                Log.i("", "");
            }
        });
    }

    private void updateDriverUI(DriverInfo driver) {
        if (driverView != null) {
            Picasso.with(mContext)
                    .load(driver.getDriver().getAvatar())
                    .centerCrop().fit().into(driverView.mDriverImageView);
            driverView.mDriverNameTextView.setText(driver.getDriver().getName());
            driverView.mPhoneIconImageView.setTag(driver.getDriver().getMobile());

            Picasso.with(mContext)
                    .load(driver.getDriver().getPicture())
                    .centerCrop().fit().into(driverView.mCarImageView);
            driverView.mCarDescTextView.setText(driver.getDriver().getDescription());
            driverView.mCarPlateNumberTextView.setText("车牌号" + driver.getDriver().getPlate());

            driverView.mRatingBar.setRating(Float.parseFloat(driver.getDriver().getRating()));
            driverView.mRatingBar.setEnabled(false);
        }
    }

    private void initLocation() {
        mLocClient = new LocationClient(getApplicationContext());
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 5000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setNeedDeviceDirect(true);
        option.setIsNeedAddress(true);
        option.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocClient.setLocOption(option);
        mLocClient.registerLocationListener(myListener);
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {

            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null)
                return;

            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();

            if (locData != null) {
                mBaiduMap.setMyLocationData(locData);
                currentLocation = new LatLng(locData.latitude, locData.longitude);
            }

//            Log.i("BaiduLocationApiDem", sb.toString());
        }
    }

    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            if (start != null && currentLocation != null && !isStartFollow) {
                SocketClient.getInstance().sendNearByCarRequestTest(currentLocation.latitude, currentLocation.longitude, "1", new ResponseHandler(Looper.getMainLooper()) {
                    @Override
                    public void onSuccess(String messageBody) {

                        NearByCars nearByCars = FastJsonTools.getObject(messageBody, NearByCars.class);

                        mBaiduMap.clear();
                        for (NearByCars.CarLocation loc : nearByCars.getCars()) {
                            LatLng ll = new LatLng(loc.getLat(), loc.getLng());
                            mBaiduMap.addOverlay(new MarkerOptions().position(ll).icon(BitmapDescriptorFactory.fromResource(R.drawable.caricon)));
                            mBaiduMap.addOverlay(new MarkerOptions()
                                    .position(currentLocation)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_position_pin)));
                            mBaiduMap.clear();
                            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(currentLocation);
                            mBaiduMap.animateMapStatus(u);
                        }
                    }
                    @Override
                    public void onFailure(String error) {
                    }
                    @Override
                    public void onTimeout() {
                    }
                });
            } else if (isStartFollow && currentLocation != null){
                //更新界面
                mHandler.sendEmptyMessage(MessageTag.MESSAGE_UPDATE_TRACK);
            }

        }
    }



    /**
     * 绘制实际轨迹连线
     * */
    private void drawLine(BaiduMap baiduMap,LatLng first, LatLng second){
        // 添加折线
        Log.e("daddy", "drawline " + first.latitude +"::"+second.latitude);
        List<LatLng> lineList = new ArrayList<LatLng>();
        lineList.clear();
        lineList.add(first);
        lineList.add(second);
        OverlayOptions ooPolyline = new PolylineOptions().width(10).color(0xAAFF0000).points(lineList);
        baiduMap.addOverlay(ooPolyline);
    }
}
