package com.guokrspace.dududriver.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.OverlayOptions;
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
import com.gc.materialdesign.views.ButtonRectangle;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.common.Constants;
import com.guokrspace.dududriver.model.OrderItem;
import com.guokrspace.dududriver.net.ResponseHandler;
import com.guokrspace.dududriver.net.SocketClient;
import com.guokrspace.dududriver.util.CommonUtil;
import com.guokrspace.dududriver.view.CircleImageView;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by hyman on 15/11/2.
 */
public class PickUpPassengerActivity extends BaseActivity {

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
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:111111"));
        startActivity(callIntent);
    }
    @Bind(R.id.pickup_mapview)
    MapView mMapview;
    @Bind(R.id.confirm_button)
    Button btnConfirm;
    @OnClick(R.id.confirm_button) public void enterConfirmPage() {

        if(orderItem == null){
            showToast("订单出现异常,请重新等候派单!");
            CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
            //TODO: 回退
            return;
        }
        SocketClient.getInstance().startOrder(orderItem.getOrder().getId(), new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                //进入最后导航界面, 开始计费
                CommonUtil.changeCurStatus(Constants.STATUS_GOT);
                initGoDestView();
                startCharging();
            }

            @Override
            public void onFailure(String error) {

            }

            @SuppressLint("NewApi")
            @Override
            public void onTimeout() {
                Log.e("PickUp", "time out! ");
                //服务器未响应,重新发送请求
                btnConfirm.callOnClick();
            }
        });

    }
    private Context context;

    private OrderItem orderItem;
    private BDLocation mLoaction;

    private BaiduMap mBaiduMap;
    private BitmapDescriptor mCurrentMarker = null;
    private OverlayOptions myOptions;
    private OverlayOptions passengerOptions;
    private boolean isFirstLoc = true;
    private boolean isSeconLoc = false;
    private Marker myMarker;
    private Marker passengerMarker;
    private BitmapDescriptor passengerDescriptor = null;

    private LatLng passengerLatLng = new LatLng(28.169544, 112.957194);

    //绘制路线相关变量
    private RoutePlanSearch routePlanSearch;
    private boolean customDefaultIcon = true;
    private PlanNode st = null;
    private PlanNode ed = null;
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
    private double preLat;
    private double preLng;
    private double preTime;
    private double curDistance;
    private int lowSpeedTime;
    private int minutes;
    private int times;
    private double tmpDistance;
    private double lowSpeedDistance = 0.3333;
    private Timer calTimer;
    private TimerTask calTimerTask;

    private void startCharging(){
            //TODO: to charge
        curDistance = 0.0d;
        lowSpeedTime = 0;
        minutes = 0;
        preLat = CommonUtil.getCurLat();
        preLng = CommonUtil.getCurLng();
        preTime= CommonUtil.getCurTime();

        times = 0;
        tmpDistance=0d;
        calTimer = new Timer();
        calTimerTask = new TimerTask() {
            @Override
            public void run() {
                Log.e("daddy", "task is runnin as " + CommonUtil.getCurrentStatus());
                tmpDistance += DistanceUtil.getDistance(new LatLng(preLat, preLng), new LatLng(CommonUtil.getCurLat(), CommonUtil.getCurLng()));
                if (CommonUtil.getCurrentStatus() == Constants.STATUS_RUN) {//开车中
                    if (++times == 6) {//1 min
                        minutes++;
                        if (tmpDistance <= lowSpeedDistance) {//这一分钟内是低速行驶
                            lowSpeedTime++;
                        }
                        curDistance += tmpDistance;
                        tmpDistance = 0;
                        times = 0;
                    }
                    preLat = CommonUtil.getCurLat();
                    preLng = CommonUtil.getCurLng();
                } else { //非法状态下停止
                    stopCharging();
                }
            }
        };
        calTimer.schedule(calTimerTask, 1000, 10000);
    }

    private void stopCharging(){
        Log.e("daddy", "start to stop");
        if(calTimer != null){
            calTimer.cancel();
            calTimer = null;
        }
        if(calTimerTask != null){
            calTimerTask.cancel();
            calTimerTask = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickuppassenger);
        context = PickUpPassengerActivity.this;
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        if(bundle!=null) {
            orderItem = (OrderItem) bundle.getSerializable("orderItem");
            initGetPassView();
        } else {
            Log.e("PickUpPassengerActivity", "wrong params in order");
        }

    }

    private void initGetPassView() {
        toolbar.setTitle("去接乘客");
        toolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.return_icon));
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvMyPosition.setText(orderItem.getOrder().getStart());
        tvPassengerPosition.setText(orderItem.getOrder().getDestination());

        passengerLatLng =  new LatLng(
                Double.valueOf(orderItem.getOrder().getStart_lat()), Double.valueOf(orderItem.getOrder().getStart_lng()));
        ed = PlanNode.withLocation(passengerLatLng);

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
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        mCurrentMarker = BitmapDescriptorFactory.fromResource(R.mipmap.myposition);
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker));

        st = PlanNode.withLocation(new LatLng(CommonUtil.getCurLat(), CommonUtil.getCurLng()));
        routePlanSearch = RoutePlanSearch.newInstance();
        routePlanSearch.drivingSearch(new DrivingRoutePlanOption().from(st).to(ed));
        routePlanSearch.setOnGetRoutePlanResultListener(routePlanResultListener);
    }

    private void initGoDestView(){

        toolbar.setTitle("订单开始");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CommonUtil.changeCurStatus(Constants.STATUS_RUN);

        btnConfirm.setText("确认完成订单");
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:无论如何都要结束订单
                stopCharging();
                Log.e("daddy", "stop thread");
                SocketClient.getInstance().endOrder(CommonUtil.countPrice(curDistance, lowSpeedTime) + "", curDistance + "", new ResponseHandler(Looper.myLooper()) {
                    @Override
                    public void onSuccess(String messageBody) {
                        //
                        Intent intent = new Intent(PickUpPassengerActivity.this, ConfirmBillActivity.class);
                        intent.putExtra("orderItem", orderItem);
                        startActivity(intent);
                        CommonUtil.changeCurStatus(Constants.STATUS_HOLD);
                        finish();
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e("PickUpPassengerAct", "end order failure" + error);
                        Toast.makeText(context, "订单出现意外!", Toast.LENGTH_SHORT);
                        Intent intent = new Intent(PickUpPassengerActivity.this, ConfirmBillActivity.class);
                        intent.putExtra("orderItem", orderItem);
                        CommonUtil.changeCurStatus(Constants.STATUS_HOLD);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onTimeout() {
                        Log.e("PickUpPassengerAct", "end order time out");
                        Toast.makeText(context, "网络状况较差!", Toast.LENGTH_SHORT);
                        Intent intent = new Intent(PickUpPassengerActivity.this, ConfirmBillActivity.class);
                        intent.putExtra("orderItem", orderItem);
                        CommonUtil.changeCurStatus(Constants.STATUS_HOLD);
                        startActivity(intent);
                    }
                });

            }
        });

        tvPassengerPosition.setText(orderItem.getOrder().getDestination());

        passengerLatLng = new LatLng(
                Double.valueOf(orderItem.getOrder().getDestination_lat()), Double.valueOf(orderItem.getOrder().getDestination_lng()));
        ed = PlanNode.withLocation(passengerLatLng);

        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(16.0f);
        mBaiduMap.setMapStatus(msu);

        // 定位再次初始化
        st = PlanNode.withLocation(new LatLng(CommonUtil.getCurLat(), CommonUtil.getCurLng()));
        routePlanSearch.drivingSearch(new DrivingRoutePlanOption().from(st).to(ed));
    }


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
        mMapview.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapview.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
//        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        if (mMapview != null) {
            mMapview.onDestroy();
            mMapview = null;
        }
    }

}
