package com.guokrspace.dududriver.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
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
import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.views.ButtonRectangle;
import com.guokrspace.dududriver.R;
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
    @Bind(R.id.passenger_info)
    Button btnPassengerInfo;
    @Bind(R.id.call_passenger)
    Button btnCallPassenger;
    @Bind(R.id.illustration_textview)
    TextView tvIllustration;
    @Bind(R.id.navi_passenger)
    ImageButton ibNaviPassenger;
    @Bind(R.id.pickup_mapview)
    MapView mMapview;
    @Bind(R.id.confirm_button)
    ButtonRectangle btnConfirm;
    @OnClick(R.id.confirm_button) public void enterConfirmPage() {
        startActivity(new Intent(this, ConfirmBillActivity.class));
        this.finish();
    }
    private Context context;
    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;
    private LocationClientOption mLocationClientOption;
    private BitmapDescriptor mCurrentMarker = null;
    private OverlayOptions myOptions;
    private OverlayOptions passengerOptions;
    private boolean isFirstLoc = true;
    private Marker myMarker;
    private Marker passengerMarker;
    private BitmapDescriptor passengerDescriptor = null;

    private LatLng passengerLatLng = new LatLng(28.169544, 112.957194);

    //绘制路线相关变量
    private RoutePlanSearch routePlanSearch;
    private boolean customDefaultIcon = true;
    private PlanNode st = null;
    private PlanNode ed = PlanNode.withLocation(passengerLatLng);
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
                showToast("抱歉，未找到结果");
            }
            if (drivingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                //起终点或途经点地址有歧义，通过以下接口获取建议查询信息
//                drivingRouteResult.getSuggestAddrInfo();
                showToast("查询地址有歧义");
                return;
            }

            if (drivingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
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

            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
//            mBaiduMap.setMyLocationData(locData);//设置定位数据
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            /*if (myMarker != null) {
                myMarker.remove();
            }
            myOptions = new MarkerOptions()//
                    .position(ll)
                    .icon(mCurrentMarker)
                    .zIndex(9);
            myMarker = (Marker) mBaiduMap.addOverlay(myOptions);*/
            st = PlanNode.withLocation(ll);
            routePlanSearch.drivingSearch(new DrivingRoutePlanOption().from(st).to(ed));



            if (isFirstLoc) {
                isFirstLoc = false;
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll, 16);//设置中心及缩放级别
                mBaiduMap.animateMapStatus(u);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickuppassenger);
        context = PickUpPassengerActivity.this;
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        toolbar.setTitle("去送乘客");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        routePlanSearch = RoutePlanSearch.newInstance();
        routePlanSearch.setOnGetRoutePlanResultListener(routePlanResultListener);


        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(16.0f);
        mBaiduMap.setMapStatus(msu);

        //禁止转动地图
        mBaiduMap.getUiSettings().setRotateGesturesEnabled(false);

        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        mCurrentMarker = BitmapDescriptorFactory.fromResource(R.mipmap.myposition);
//        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker));
        passengerDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.passenger_position);
//        passengerOptions = new MarkerOptions().position(passengerLatLng).icon(passengerDescriptor).zIndex(9);
//        mBaiduMap.addOverlay(passengerOptions);
        // 定位初始化
        initLocation();
        mLocationClient.start();
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
