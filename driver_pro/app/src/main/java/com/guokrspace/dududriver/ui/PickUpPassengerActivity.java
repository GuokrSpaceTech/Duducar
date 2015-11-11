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
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
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
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.common.Constants;
import com.guokrspace.dududriver.model.OrderItem;
import com.guokrspace.dududriver.net.ResponseHandler;
import com.guokrspace.dududriver.net.SocketClient;
import com.guokrspace.dududriver.util.CommonUtil;
import com.guokrspace.dududriver.view.CircleImageView;

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
                //进入最后导航界面
                CommonUtil.changeCurStatus(Constants.STATUS_GOT);
                initGoDestView();
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
//    private MainActivity.OrderBrefInformation orderBrefInformation;
    private BDLocation mLoaction;

    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;
    private LocationClientOption mLocationClientOption;
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
//                drivingRouteResult.getSuggestAddrInfo();
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
    private BDLocationListener mDBLocationListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapview == null)
                return;

            mLoaction = location;
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());

            if(isFirstLoc || isSeconLoc){
                st = PlanNode.withLocation(ll);
                Log.e("daddy", isFirstLoc + " : " + isSeconLoc + " " + st + "  " + ed);
                if(isFirstLoc){
                    routePlanSearch = RoutePlanSearch.newInstance();
                    routePlanSearch.drivingSearch(new DrivingRoutePlanOption().from(st).to(ed));
                    routePlanSearch.setOnGetRoutePlanResultListener(routePlanResultListener);
                } else {
                    routePlanSearch.drivingSearch(new DrivingRoutePlanOption().from(st).to(ed));
                    Log.e("daddy", "second");
                }
                isFirstLoc = isSeconLoc = false;
                Log.e("BDLocation listener", "route");
            }
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll, 16);//设置中心及缩放级别
            mBaiduMap.animateMapStatus(u);
        }
    };

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
        LatLng startLoaction = new LatLng(
                Double.valueOf(orderItem.getOrder().getStart_lat()), Double.valueOf(orderItem.getOrder().getStart_lng()));
        passengerLatLng = startLoaction;
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
        // 定位初始化
        initLocation();
        mLocationClient.start();
    }

    private void initGoDestView(){

        Log.e("initGoDestView", "step1 ");
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
                SocketClient.getInstance().endOrder(new ResponseHandler(Looper.myLooper()) {
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
        LatLng endLoaction = new LatLng(
                Double.valueOf(orderItem.getOrder().getDestination_lat()), Double.valueOf(orderItem.getOrder().getDestination_lng()));
//
        passengerLatLng = endLoaction;
        ed = PlanNode.withLocation(passengerLatLng);

//        mBaiduMap.clear();
//        mBaiduMap = mMapview.getMap();
//        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);//普通地图
        // 隐藏缩放控件
//        int childCount = mMapview.getChildCount();
//        View zoom = null;
//        for (int i = 0; i < childCount; i++) {
//            View child = mMapview.getChildAt(i);
//            if (child instanceof ZoomControls) {
//                zoom = child;
//                break;
//            }
//        }
//        zoom.setVisibility(View.GONE);

        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(16.0f);
        mBaiduMap.setMapStatus(msu);

        //禁止转动地图
//        mBaiduMap.getUiSettings().setRotateGesturesEnabled(false);

        // 开启定位图层
//        mBaiduMap.setMyLocationEnabled(true);
//        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
//        mCurrentMarker = BitmapDescriptorFactory.fromResource(R.mipmap.myposition);
//        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker));
//        passengerDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.passenger_position);

        // 定位再次初始化
        isSeconLoc = true;
//        initLocation();
//        mLocationClient.start();
    }

    private void initLocation() {
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClientOption = new LocationClientOption();
        mLocationClientOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        mLocationClientOption.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 5000;
        mLocationClientOption.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        mLocationClientOption.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        mLocationClientOption.setOpenGps(true);//可选，默认false,设置是否使用gps
        mLocationClientOption.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        mLocationClientOption.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        mLocationClientOption.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        mLocationClientOption.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(mLocationClientOption);
        mLocationClient.registerLocationListener(mDBLocationListener);
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
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        if (mMapview != null) {
            mMapview.onDestroy();
            mMapview = null;
        }
    }

}
