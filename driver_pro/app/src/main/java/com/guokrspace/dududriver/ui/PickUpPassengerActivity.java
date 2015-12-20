package com.guokrspace.dududriver.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRouteGuideManager;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.baidu.navisdk.comapi.routeplan.RoutePlanParams;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.common.Constants;
import com.guokrspace.dududriver.common.VoiceCommand;
import com.guokrspace.dududriver.model.OrderItem;
import com.guokrspace.dududriver.net.ChargeService;
import com.guokrspace.dududriver.net.ResponseHandler;
import com.guokrspace.dududriver.net.SocketClient;
import com.guokrspace.dududriver.net.message.MessageTag;
import com.guokrspace.dududriver.util.CommonUtil;
import com.guokrspace.dududriver.util.SharedPreferencesUtils;
import com.guokrspace.dududriver.util.VoiceUtil;
import com.guokrspace.dududriver.view.CircleImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by hyman on 15/11/2.
 */
public class PickUpPassengerActivity extends BaseActivity implements Handler.Callback {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.passenger_avatar)
    CircleImageView civPassengerAvatar;
    @Bind(R.id.myposition_textview)
    TextView tvMyPosition;
    @Bind(R.id.passengerposition_textview)
    TextView tvPassengerPosition;
    @Bind(R.id.call_passenger)
    Button btnCallPassenger;
    @OnClick(R.id.call_passenger)
    public void callPassenger() {
        String mobile = orderItem.getOrder().getPassenger_mobile();
        if(mobile != null && mobile.length() == 11){
            VoiceUtil.startSpeaking(VoiceCommand.CALL_PASSENEGER);
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+mobile));
            startActivity(callIntent);
        } else {
            Log.e("PickUpPassengerActivity", "乘客手机号码问题" + mobile);
        }
    }
    @Bind(R.id.navi_button)
    Button btnNavi;
    @Bind(R.id.pickup_naviview)
    FrameLayout mNaviFrame;
    @Bind(R.id.pickup_mapnavi)
    FrameLayout mMapNavi;
    @Bind(R.id.pickup_mapview)
    MapView mMapview;
    @Bind(R.id.pickup_btnll)
    LinearLayout pickupLl;
    @Bind(R.id.confirm_button)
    Button btnConfirm;
    @OnClick(R.id.confirm_button) public void enterConfirmPage() {

        if(orderItem == null){
            showToast("订单出现异常,请重新等候派单!");
            VoiceUtil.startSpeaking(VoiceCommand.EXCEPTION);
            CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
            finish();
            //TODO: 回退
            return;
        }

        SocketClient.getInstance().startOrder(orderItem.getOrder().getId(), new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                //进入最后导航界面, 开始计费
                VoiceUtil.startSpeaking(VoiceCommand.PICKUP_DONE);
                CommonUtil.changeCurStatus(Constants.STATUS_RUN);
                initGoDestView();
                startCharging();
            }

            @Override
            public void onFailure(String error) {
                if(error.contains("not Order")){ // 订单异常
                    VoiceUtil.startSpeaking(VoiceCommand.ORDER_STATUS_EXCEPTION);
                    mBaiduMap.setMyLocationEnabled(false);
                    if (mMapview != null) {
                        mMapview.onDestroy();
                        mMapview = null;
                    }
                    if(isNavigationNow){
                        BNRouteGuideManager.getInstance().onDestroy();
                    }
                    if(timer != null){
                        timer.cancel();
                    }
                    finish();
                }
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            @Override
            public void onTimeout() {
                Log.e("PickUp", "time out! ");
                //服务器未响应,重新发送请求
                VoiceUtil.startSpeaking(VoiceCommand.TIME_OUT_ALERT);
//                btnConfirm.callOnClick();
            }
        });

    }
    private Context context;
    public Handler mHandler;

    private double curCharge = 0;
    public final int UPDATE_CHARGE = 0x101;
    public final int ORDER_NOT_EXIST = 0x102;

    private OrderItem orderItem;
    private BDLocation mLoaction;

    private String mSDCardPath = null;
    private static final String APP_FOLDER_NAME = "com.guokrspace.dududriver";
    private BaiduMap mBaiduMap;
    private BitmapDescriptor mCurrentMarker = null;
    private View naviView = null;

    private LatLng passengerLatLng = new LatLng(28.169544, 112.957194);

    //绘制路线相关变量
    private RoutePlanSearch routePlanSearch;
    private boolean customDefaultIcon = true;
    private PlanNode st = null;
    private PlanNode ed = null;
    private BNRoutePlanNode mBNRoutePlanNode = null;
    private Timer timer = null;

    private double baseDistance;
    private double baseCharge;
    private int baseLowTime;

    private OnGetRoutePlanResultListener routePlanResultListener = new OnGetRoutePlanResultListener() {
        @Override
        public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
            //获取步行路线规划结果
        }

        @Override
        public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
            //获取公交换乘路径规划结果
        }

        @Override
        public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
            //获取驾车线路规划结果
            if (drivingRouteResult == null || drivingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Log.e("DADDY", drivingRouteResult + ".." + drivingRouteResult.error);
                showToast("抱歉，未找到结果");
            }
            if (drivingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                //起终点或途经点地址有歧义，通过以下接口获取建议查询信息
                showToast("查询地址有歧义");
                return;
            }

            if (drivingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                mBaiduMap.clear();
                DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaiduMap);
                mBaiduMap.setOnMarkerClickListener(overlay);
                overlay.setData(drivingRouteResult.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
            }
        }
    };

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){

            case UPDATE_CHARGE:
                Log.e("daddy", "current charge");
                double price = CommonUtil.curPrice;
                if(curCharge <= price) {
                    curCharge = new BigDecimal(price).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
                    btnConfirm.setText(curCharge + "元   到达目的地");
//                    SocketClient.getInstance().sendCurrentChargeDetail(curCharge, curDistance/1000, lowSpeedTime, new ResponseHandler(Looper.myLooper()) {
//                        @Override
//                        public void onSuccess(String messageBody) {
//                        }
//                        @Override
//                        public void onFailure(String error) {
//                            if(error.contains("not Order")){ // 订单异常
//                                VoiceUtil.startSpeaking(VoiceCommand.ORDER_STATUS_EXCEPTION);
//                                unbindService(connCharge);
//                                finish();
//                            }
//                        }
//                        @Override
//                        public void onTimeout() {
//                        }
//                    });
                }
                Log.e("daddy", "current charge");
                break;
            case ORDER_NOT_EXIST://订单出现异常
                VoiceUtil.startSpeaking(VoiceCommand.ORDER_STATUS_EXCEPTION);
                stopService(chargeService);
                finish();
                break;
            case MessageTag.MESSAGE_UPDATE_TRACK:
                Log.e("daddy", "start track");
                if(CommonUtil.getCurLatLng() == null){
                    break;
                }
                Log.e("daddy", "start track latlng not null");
                if(isFirstTrack){
                    currentLatLng = CommonUtil.getCurLatLng();
                    isFirstTrack = false;
                }

                if(Math.abs(currentLatLng.latitude- CommonUtil.getCurLatLng().latitude) > 0.0005
                        ||Math.abs(currentLatLng.longitude - CommonUtil.getCurLatLng().longitude) > 0.0005 )
                    //异常定位
                    break;
                Log.e("daddy", "start track no exception");
                drawLine(mBaiduMap, currentLatLng, CommonUtil.getCurLatLng());
                currentLatLng = CommonUtil.getCurLatLng();
                break;
            default:
                break;
        }
        return false;
    }

    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (customDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.mipmap.myposition);
            }
            return super.getStartMarker();
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (customDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.mipmap.passenger_position);
            }
            return super.getTerminalMarker();
        }
    }

    private MyLocationConfiguration.LocationMode mCurrentMode;
//    private double preLat;
//    private double preLng;
//    private double preDis;
//    private double curDistance;
//    private int lowSpeedTime;
//    private int minutes;
//    private int times;
//    private double secDistance;
//    private double tmpDistance;
//    private double LOWSPEEDDISTANACE = 333.3; // m/min
//    private double STRANGEDISTANCE = 33.3; // m/s
//    private Message msg;
//    private Bundle bundle;
//    private Timer calTimer;
//    private TimerTask calTimerTask;

//    private void startRanging(){
//        curDistance = 100000.0d;
//        calTimer = new Timer();
//        final LatLng passLatlng = new LatLng(Double.parseDouble(orderItem.getOrder().getStart_lat()), Double.parseDouble(orderItem.getOrder().getStart_lng()));
//        calTimerTask = new TimerTask() {
//            @Override
//            public void run() {
//                //计算当前到乘客的距离
//                curDistance = DistanceUtil.getDistance(new LatLng(CommonUtil.getCurLat(), CommonUtil.getCurLng()), passLatlng);
//            }
//        }
//    }

    private Intent chargeService ;

    private void startCharging(){
            //TODO: to charge
//        curDistance = 0.0d + baseDistance;
//        lowSpeedTime = 0 + baseLowTime;
//        minutes = 0;
//        preLat = CommonUtil.getCurLat();
//        preLng = CommonUtil.getCurLng();
        Log.e("daddy", "start to charge");
        chargeService = new Intent(PickUpPassengerActivity.this, ChargeService.class);
        CommonUtil.curBaseDistance = baseDistance;
        CommonUtil.curBaseLowTime = baseLowTime;
        startService(chargeService);
        Log.e("daddy", "end to charge");

//        times = 0;
//        preDis = 0;
//        secDistance=0d;
//        tmpDistance=0d;
//        calTimer = new Timer();
//        calTimerTask = new TimerTask() {
//            @Override
//            public void run() {
//                Log.e("daddy", "task is runnin as " + CommonUtil.getCurrentStatus());
//
//                secDistance = DistanceUtil.getDistance(new LatLng(preLat, preLng), new LatLng(CommonUtil.getCurLat(), CommonUtil.getCurLng()));
//                if ((secDistance * 1000 / (System.currentTimeMillis() - CommonUtil.getCurTime())) >= STRANGEDISTANCE) { //这一次的距离跳转异常
//                    //drop it
//                    tmpDistance += secDistance;
//                    preDis = secDistance;
//                } else {
//                    tmpDistance += secDistance;
//                    preDis = secDistance;
//                }
//
//                if (CommonUtil.getCurrentStatus() == Constants.STATUS_RUN) {//开车中
//                    if (++times == 12) {//1 min
//                        minutes++;
//                        if (tmpDistance <= LOWSPEEDDISTANACE) {//这一分钟内是低速行驶
//                            lowSpeedTime++;
//                        }
//
//                        curDistance += tmpDistance;
//                        tmpDistance = 0;
//                        times = 0;
//                    }
//
//                    preLat = CommonUtil.getCurLat();
//                    preLng = CommonUtil.getCurLng();
//                    //TODO:通知界面更新,发送到乘客端
//                    double price = CommonUtil.countPrice(curDistance, lowSpeedTime);
////                    if(price > curCharge){
//                        Bundle bundle = new Bundle();
//                        bundle.putDouble("charge", price);
//                        Message msg = new Message();
//                        msg.what = UPDATE_CHARGE;
//                        msg.setData(bundle);
//                        Log.e("daddy", "send message update");
//                        mHandler.sendMessage(msg);
////                    }
//                } else { //非法状态下停止
//                    Log.e("daddy", "stop charge");
//                    stopCharging();
//                }
//            }
//        };
//        calTimer.schedule(calTimerTask, 1000, 5000);
    }

//    private void stopCharging(){
//        Log.e("daddy", "start to stop");
//        if(calTimer != null){
//            calTimer.cancel();
//            calTimer = null;
//        }
//        if(calTimerTask != null){
//            calTimerTask.cancel();
//            calTimerTask = null;
//        }
//        if(isNavigationNow){
//            BNRouteGuideManager.getInstance().forceQuitNaviWithoutDialog();
//            isNavigationNow = false;
//        }
//    }

    ChargeServiceReceiver receiver = new ChargeServiceReceiver();

    public class ChargeServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case Constants.SERVICE_ACTION_UPDATE_CHARGE:
                    // 更新收费消息,
                    mHandler.sendEmptyMessage(UPDATE_CHARGE);
                    break;
                case Constants.SERVICE_ACTION_ORDER_NOT_EXISTS:
                    //订单不存在
                    mHandler.sendEmptyMessage(ORDER_NOT_EXIST);
//                    abortBroadcast();
                    break;
                default:
                    return;
            }
        }
    }

    private void registerBroadcastReceiver(){
        receiver = new ChargeServiceReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.SERVICE_ACTION_UPDATE_CHARGE);
        filter.addAction(Constants.SERVICE_ACTION_ORDER_NOT_EXISTS);
        filter.setPriority(1000);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_pickuppassenger);
        context = PickUpPassengerActivity.this;
        mHandler = new Handler(this);
        ButterKnife.bind(this);
        initDirs();
        initNavi();

        Bundle bundle = getIntent().getExtras();
        if(bundle!=null) {
            orderItem = (OrderItem) bundle.getSerializable("orderItem");
            boolean isRecover = bundle.getBoolean("isRecover");
            if(isRecover){//恢复到去送乘客的状态
                if(bundle.getString("lastCharge").equals("null")){
                    // 测试数据
                    baseDistance = 0;
                    baseLowTime = 1;
                } else {
                    try {
                        JSONObject lastCharge = new JSONObject(bundle.getString("lastCharge"));
                        baseDistance = Double.parseDouble((String) lastCharge.get("current_mile"));
                        baseCharge = Double.parseDouble((String) lastCharge.get("current_charge"));
                        baseLowTime = Integer.parseInt((String) lastCharge.get("low_speed_time"));
                        double baseLat = Double.parseDouble((String) lastCharge.get("current_lat"));
                        double baseLng = Double.parseDouble((String) lastCharge.get("current_lng"));
                        long baseTime = Long.parseLong((String) lastCharge.get("current_time"));

                        //之前走过的距离
                        double midDistance = DistanceUtil.getDistance(new LatLng(baseLat, baseLng), new LatLng(CommonUtil.getCurLat(), CommonUtil.getCurLng()));

                        if ((midDistance * 1000 / (System.currentTimeMillis() - baseTime)) >= Constants.STRANGEDISTANCE) { //这一次的距离跳转异常
                            //drop it, use max distance
                            baseDistance += Constants.STRANGEDISTANCE * (System.currentTimeMillis() - baseTime) / 1000;
                        } else {
                            baseDistance += midDistance;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.e("daddy", "终端");
                initBaiduMap();
                initGoDestView();
                startCharging();
            } else {
                initGetPassView();
            }
        } else {
            Log.e("PickUpPassengerActivity", "wrong params in order");
        }

    }

    private void initGetPassView() {

        toolbar.setTitle("去接乘客");
        toolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.return_icon));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertrDialog = new AlertDialog.Builder(PickUpPassengerActivity.this);
                alertrDialog.setMessage("订单正在执行, 请确认完成订单!");
                alertrDialog.setCancelable(true);

                alertrDialog.setNegativeButton("继续执行", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertrDialog.show();
            }
        });

        tvMyPosition.setText(orderItem.getOrder().getStart());
        tvPassengerPosition.setText(orderItem.getOrder().getDestination());

        passengerLatLng =  new LatLng(
                Double.valueOf(orderItem.getOrder().getStart_lat()), Double.valueOf(orderItem.getOrder().getStart_lng()));

        ed = PlanNode.withLocation(passengerLatLng);
        st = PlanNode.withLocation(new LatLng(CommonUtil.getCurLat(), CommonUtil.getCurLng()));

        initBaiduMap();

        routePlanSearch.drivingSearch(new DrivingRoutePlanOption().from(st).to(ed));

        //3秒一次  更新界面
        timer = new Timer();
        timer.scheduleAtFixedRate(new DrawLineTimerTask(), 2000, 3 * 1000);

        btnNavi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isNavigationOk){
                    Toast.makeText(PickUpPassengerActivity.this, "地图导航加载失败", Toast.LENGTH_SHORT);
                }
                if(!isNavigationNow){
                    //启动导航

                    BDLocation stLocation = CommonUtil.getMCLocation(CommonUtil.getCurLat(),CommonUtil.getCurLng());
                    BDLocation edLocation = CommonUtil.getMCLocation(Double.valueOf(orderItem.getOrder().getStart_lat()), Double.valueOf(orderItem.getOrder().getStart_lng()));
                    routeplanToNavi(new BNRoutePlanNode(stLocation.getLongitude(), stLocation.getLatitude(), CommonUtil.getCurAddress(), CommonUtil.getCurAddressDescription(), BNRoutePlanNode.CoordinateType.BD09_MC),
                            new BNRoutePlanNode(edLocation.getLongitude(), edLocation.getLatitude(), orderItem.getOrder().getStart(), "乘客所在地", BNRoutePlanNode.CoordinateType.BD09_MC));
                } else {
                    //结束导航
                    BNRouteGuideManager.getInstance().forceQuitNaviWithoutDialog();
                    mMapview.setVisibility(View.VISIBLE);
                    mNaviFrame.removeView(naviView);
                    isNavigationNow = false;
                    btnNavi.setText("开启导航");
                }
            }
        });
    }

    private void initBaiduMap(){

        mBaiduMap = mMapview.getMap();

        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);//普通地图
        // 隐藏缩放控件
        int childCount = mMapview.getChildCount();
        View zoom = null;
        for (int i = 0; i < childCount; i++) {
            View child = mMapview.getChildAt(i);
            if (child instanceof ZoomControls) {
                zoom = child;
                break;
            }
        }
        zoom.setVisibility(View.GONE);

        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(16.0f);
        mBaiduMap.setMapStatus(msu);

        //禁止转动地图
        mBaiduMap.getUiSettings().setRotateGesturesEnabled(false);

        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
        mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.caricon);
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker));

        routePlanSearch = RoutePlanSearch.newInstance();
        routePlanSearch.setOnGetRoutePlanResultListener(routePlanResultListener);
    }

    private void initGoDestView(){

        toolbar.setTitle("订单开始");
        toolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.return_icon));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferencesUtils.setParam(PickUpPassengerActivity.this, Constants.PREFERENCE_KEY_ORDER_STATUS, Constants.STATUS_RUN);
        registerBroadcastReceiver();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertrDialog = new AlertDialog.Builder(PickUpPassengerActivity.this);
                alertrDialog.setMessage("订单正在执行, 请确认完成订单!");
                alertrDialog.setCancelable(true);

                alertrDialog.setNegativeButton("继续执行", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertrDialog.show();
            }
        });


        if(timer != null){
            timer.cancel();
            timer = null;
        }

        //3秒一次  更新界面
        timer = new Timer();
        timer.scheduleAtFixedRate(new DrawLineTimerTask(), 7000, 3 * 1000);

        final String price = baseCharge > CommonUtil.getStartPrice() ? baseCharge + "" : CommonUtil.getStartPrice() + "";

        btnConfirm.setText(price + "元   到达目的地");
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:无论如何都要结束订单
                unregisterReceiver(receiver);
                if(chargeService != null) {
                    stopService(chargeService);
                }
                if(isNavigationNow){
//                    BNRouteGuideManager.getInstance().forceQuitNaviWithoutDialog();
                    isNavigationNow = false;
                }
                SharedPreferencesUtils.setParam(PickUpPassengerActivity.this, Constants.PREFERENCE_KEY_ORDER_STATUS, Constants.STATUS_REACH);

                SharedPreferencesUtils.setParam(PickUpPassengerActivity.this, Constants.PREFERENCE_KEY_DRIVER_TOTAL_ORDER, (Integer.parseInt((String)SharedPreferencesUtils.getParam(PickUpPassengerActivity.this, Constants.PREFERENCE_KEY_DRIVER_TOTAL_ORDER, "0")) + 1) + "");

                Intent intent = new Intent(PickUpPassengerActivity.this, ConfirmBillActivity.class);
                intent.putExtra("orderItem", orderItem);
                intent.putExtra("mileage", CommonUtil.curDistance);
                intent.putExtra("lowspeed", CommonUtil.curLowSpeedTime);
                startActivity(intent);
                CommonUtil.changeCurStatus(Constants.STATUS_HOLD);
                finish();
            }
        });

        tvMyPosition.setText(orderItem.getOrder().getStart());
        tvPassengerPosition.setText(orderItem.getOrder().getDestination());

        passengerLatLng = new LatLng(
                Double.valueOf(orderItem.getOrder().getDestination_lat()), Double.valueOf(orderItem.getOrder().getDestination_lng()));
        ed = PlanNode.withLocation(passengerLatLng);

        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(16.0f);
        mBaiduMap.setMapStatus(msu);
        isFirstTrack = true;

        // 定位再次初始化
        st = PlanNode.withLocation(new LatLng(CommonUtil.getCurLat(), CommonUtil.getCurLng()));
        routePlanSearch.drivingSearch(new DrivingRoutePlanOption().from(st).to(ed));

        btnNavi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNavigationOk) {
                    Toast.makeText(PickUpPassengerActivity.this, "地图导航加载失败", Toast.LENGTH_SHORT);
                }
                if (!isNavigationNow) {
                    //启动导航

                    BDLocation stLocation = CommonUtil.getMCLocation(CommonUtil.getCurLat(), CommonUtil.getCurLng());
                    BDLocation edLocation = CommonUtil.getMCLocation(Double.valueOf(orderItem.getOrder().getDestination_lat()), Double.valueOf(orderItem.getOrder().getDestination_lng()));
                    routeplanToNavi(new BNRoutePlanNode(stLocation.getLongitude(), stLocation.getLatitude(), CommonUtil.getCurAddress(), CommonUtil.getCurAddressDescription(), BNRoutePlanNode.CoordinateType.BD09_MC),
                            new BNRoutePlanNode(edLocation.getLongitude(), edLocation.getLatitude(), orderItem.getOrder().getStart(), "乘客所在地", BNRoutePlanNode.CoordinateType.BD09_MC));
                } else {
                    //结束导航
                    BNRouteGuideManager.getInstance().forceQuitNaviWithoutDialog();
                    mMapview.setVisibility(View.VISIBLE);
                    mNaviFrame.removeView(naviView);
                    isNavigationNow = false;
                    btnNavi.setText("开启导航");
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        BNRouteGuideManager.getInstance().onConfigurationChanged(newConfig);
        Log.e("daddy", "on config change");
        super.onConfigurationChanged(newConfig);
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mMapview != null){
            mMapview.onPause();
        }
        if(isNavigationNow){
            BNRouteGuideManager.getInstance().onPause();
        }

        Log.e("daddy", " gps onpause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapview.onResume();
        if (isNavigationNow) {
            BNRouteGuideManager.getInstance().onResume();
        }
        //监听派单取消的通知
        SocketClient.getInstance().registerServerMessageHandler(MessageTag.ORDER_CANCEL, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                try {
                    JSONObject mCancel = new JSONObject(messageBody);
                    if (orderItem == null ) {
                        //订单已经取消或者已经接到乘客  无法取消订单
                        return;
                    }
                    if (CommonUtil.getCurrentStatus() == Constants.STATUS_DEAL || CommonUtil.getCurrentStatus() == Constants.STATUS_GET) {
                        //选择接单阶段或者乘客还未上车阶段可以取消
                        VoiceUtil.startSpeaking(VoiceCommand.ORDER_CANCEL);
                        orderItem = null;
                        CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
                        startActivity(new Intent(PickUpPassengerActivity.this, MainActivity.class));
                        finish();
                    }
                } catch (JSONException e) {
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

        Log.e("daddy", " gps onresume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBaiduMap.setMyLocationEnabled(false);
        if (mMapview != null) {
            mMapview.onDestroy();
            mMapview = null;
        }
        if(isNavigationNow){
            BNRouteGuideManager.getInstance().onDestroy();
        }

        ButterKnife.unbind(this);
    }

    @Override
    public void onBackPressed() {
//        BNRouteGuideManager.getInstance().onBackPressed(false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == event.getKeyCode()
                || KeyEvent.KEYCODE_MENU == event.getKeyCode() ) {
            return false;
        }
        return true;
    }


    private boolean isNavigationOk = false;
    public boolean isNavigationNow = false;

    private void initNavi() {
//        BaiduNaviManager.getInstance().setNativeLibraryPath(mSDCardPath + "/BaiduNaviSDK_SO");
        BaiduNaviManager.getInstance().init(this, mSDCardPath, APP_FOLDER_NAME,
                new BaiduNaviManager.NaviInitListener() {
                    @Override
                    public void onAuthResult(int status, String msg) {
                        if (0 == status) {
                            Log.e("daddy", "auth right ");
                        } else {
                            isNavigationOk = false;
                            Log.e("daddy", "auth fail , key is wrong");
                        }
                    }

                    @Override
                    public void initStart() {
                        Log.e("daddy", "init navigation start");
                    }

                    @Override
                    public void initSuccess() {
                        Log.e("daddy", "init succes");
                        isNavigationOk = true;
                    }

                    @Override
                    public void initFailed() {
                        Log.e("daddy", "init succes");
                        isNavigationOk = false;
                    }

                }, mTTSCallback);
    }
    //导航语音回调
    private BNOuterTTSPlayerCallback mTTSCallback = new BNOuterTTSPlayerCallback() {

        @Override
        public void stopTTS() {
            // TODO Auto-generated method stub
        }

        @Override
        public void resumeTTS() {
            // TODO Auto-generated method stub
        }

        @Override
        public void releaseTTSPlayer() {
            // TODO Auto-generated method stub
        }

        @Override
        public int playTTSText(String speech, int bPreempt) {
            // TODO Auto-generated method stub
            Log.e("daddy", "tts adxt " + speech);
            return 0;
        }

        @Override
        public void phoneHangUp() {
            // TODO Auto-generated method stub
        }

        @Override
        public void phoneCalling() {
            // TODO Auto-generated method stub
        }

        @Override
        public void pauseTTS() {
            // TODO Auto-generated method stub
        }

        @Override
        public void initTTSPlayer() {
            // TODO Auto-generated method stub
        }

        @Override
        public int getTTSState() {
            // TODO Auto-generated method stub
            return 0;
        }
    };

    private boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if ( mSDCardPath == null ) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if ( !f.exists() ) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(
                Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    private void routeplanToNavi(BNRoutePlanNode sNode, BNRoutePlanNode eNode) {

        if (sNode != null && eNode != null) {
            List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
            list.add(sNode);
            list.add(eNode);

            boolean launch = BaiduNaviManager.getInstance().launchNavigator(this,
                    list, RoutePlanParams.NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME,
                    true, new DemoRoutePlanListener(sNode));
            if(launch){
                Log.e("daddy", " gps launch");
            } else {
                VoiceUtil.startSpeaking(VoiceCommand.LAUNCH_NAVIGATION_ERROR);
                isNavigationNow = false;
            }
        }
    }


    public class DemoRoutePlanListener implements BaiduNaviManager.RoutePlanListener {

        private BNRoutePlanNode mBNRoutePlanNode = null;
        public DemoRoutePlanListener(BNRoutePlanNode node) {
            mBNRoutePlanNode = node;
        }

        @Override
        public void onJumpToNavigator() {
            //TODO 进入导航模式需要调整地图
            isNavigationNow = true;
            Intent intent = new Intent(PickUpPassengerActivity.this, NaviActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("daddy", (BNRoutePlanNode) mBNRoutePlanNode);
            intent.putExtras(bundle);
            Log.e("daddy", "start navigation");
            startActivity(intent);
//            btnNavi.setText("停止导航");
//            Log.e("daddy navi", "jump to navigation");
//
//            naviView = BNRouteGuideManager.getInstance().onCreate(PickUpPassengerActivity.this, new BNRouteGuideManager.OnNavigationListener() {
//                @Override
//                public void onNaviGuideEnd() {
//                    //TODO 导航结束
//                    isNavigationNow = false;
//                }
//
//                @Override
//                public void notifyOtherAction(int i, int i1, int i2, Object o) {
//                    Log.e("daddy navi guide", "notify" + i + " " + i1 + " " + i2);
//
//                }
//
//            });
//            Log.e("daddy navigation", "view " + naviView.getWidth() + " " + naviView.getAlpha());
//            mNaviFrame.addView(naviView);
//            Log.e("daddy navigation", "add view success");
//            mMapview.removeAllViews();
//            mMapview.addView(naviView, 1);
//            naviView.refreshDrawableState();
//            mMapNavi.setVisibility(View.VISIBLE);
//            mMapNavi.addView(naviView);
//            BNavigator.getInstance().startNav();
//            mMapview.setVisibility(View.GONE);

//            Log.e("daddy navigation", "mapview gone");
        }
        @Override
        public void onRoutePlanFailed() {
            // TODO 路径规划失败,不能启动导航
            isNavigationNow = false;
            btnNavi.setText("启动导航");
            VoiceUtil.startSpeaking(VoiceCommand.ROUTE_ERROR_NO_NAVIGATION);
            Log.e("daddy navi route", "routePlanFailed");
        }
    }

    private void addCustomizedLayerItems() {
        List<BNRouteGuideManager.CustomizedLayerItem> items = new ArrayList<BNRouteGuideManager.CustomizedLayerItem>();
        BNRouteGuideManager.CustomizedLayerItem item1 = null;
        if (mBNRoutePlanNode != null) {
            item1 = new BNRouteGuideManager.CustomizedLayerItem(mBNRoutePlanNode.getLongitude(), mBNRoutePlanNode.getLatitude(),
                    mBNRoutePlanNode.getCoordinateType(), getResources().getDrawable(R.drawable.accept_btn_bg_pressed), BNRouteGuideManager.CustomizedLayerItem.ALIGN_CENTER);
            items.add(item1);

            BNRouteGuideManager.getInstance().setCustomizedLayerItems(items);
        }
        BNRouteGuideManager.getInstance().showCustomizedLayer(true);
    }

    private LatLng currentLatLng = null;
    private LatLng prevLatLng = null;
    private boolean isFirstTrack = true;

    private class DrawLineTimerTask extends TimerTask {
        @Override
        public void run() {
            if (CommonUtil.getCurrentStatus() != Constants.STATUS_RUN && CommonUtil.getCurrentStatus() != Constants.STATUS_GET) {
                //状态异常
                return;
            } else if (CommonUtil.getCurLatLng() != null){
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
