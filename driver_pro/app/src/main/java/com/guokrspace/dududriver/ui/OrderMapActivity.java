package com.guokrspace.dududriver.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Gradient;
import com.baidu.mapapi.map.HeatMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.guokrspace.dududriver.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by hyman on 15/11/12.
 */
public class OrderMapActivity extends BaseActivity implements View.OnClickListener{

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.order_mapview)
    MapView mMapView;
    @Bind(R.id.navi_self)
    Button naviButton;

    private static final int HANDLE_SHOW_HEATMAP = 100;

    private Context context;
    private boolean isFirstLoc = true;
    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;
    private LocationClientOption mOption;
    private MyLocationConfiguration.LocationMode mCurrentMode;
    private BitmapDescriptor mCurrentMarker = null;
    private Marker selfMarker = null;
    private HeatMap heatmap = null;
    private BDLocationListener mDBLocationListener = new BDLocationListener() {
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

            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());

            if (locData != null) {
                /*mBaiduMap.clear();
                OverlayOptions overlayOptions = new MarkerOptions()//
                    .position(ll)//
                    .icon(mCurrentMarker)//
                    .zIndex(10)//
                    .draggable(true);
                selfMarker = (Marker) mBaiduMap.addOverlay(overlayOptions);*/
                mBaiduMap.setMyLocationData(locData);
            }

            if (isFirstLoc) {
                isFirstLoc = false;
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll, 20.0f);//设置中心及缩放级别
                mBaiduMap.animateMapStatus(u);
            }
        }
    };

    //热力图相关变量
    //设置渐变颜色值
    private static final int[] DEFAULT_GRADIENT_COLORS = {Color.rgb(0,0,255), Color.rgb(0,255,0), Color.rgb(255,255,0), Color.rgb(255,0,0), Color.rgb(127,62,42)};
    //设置渐变颜色起始值
    private static final float[] DEFAULT_GRADIENT_START_POINTS = { 0.2f, 0.4f, 0.6f, 0.8f ,1f };
    //构造颜色渐变对象
    private Gradient gradient = new Gradient(DEFAULT_GRADIENT_COLORS, DEFAULT_GRADIENT_START_POINTS);
    List<LatLng> randomList = new ArrayList<LatLng>();
    Random r = new Random();


    private MyHandler mHandler = new MyHandler(OrderMapActivity.this);

    private Thread loadHeatMapThread = new Thread() {
        @Override
        public void run() {
            for (int i = 0; i < 500; i++) {
                int rlat = r.nextInt(10000);
                int rlng = r.nextInt(10000);
                int lat = 28185494 + rlat;
                int lng = 112949547 + rlng;
                LatLng ll = new LatLng(lat / 1E6, lng / 1E6);
                randomList.add(ll);
            }
            //在大量热力图数据情况下，build过程相对较慢，建议放在新建线程实现
            heatmap = new HeatMap.Builder()
                    .data(randomList)
                    .gradient(gradient)
                    .build();
            mHandler.sendMessage(mHandler.obtainMessage(HANDLE_SHOW_HEATMAP));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordermap);
        ButterKnife.bind(this);
        context = OrderMapActivity.this;
        initView();
    }

    private void initView() {
        mToolbar.setTitle("订单地图");
        mToolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.return_white));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
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

        //禁止转动地图
        mBaiduMap.getUiSettings().setRotateGesturesEnabled(false);

        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.zoomTo(19.0f));

        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        mCurrentMarker = BitmapDescriptorFactory.fromResource(R.mipmap.destination);
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true, null));
        // 定位初始化
        initLocation();
        mLocationClient.start();

        naviButton.setOnClickListener(this);

        loadHeatMapThread.start();


    }

    private void initLocation() {
        mLocationClient = new LocationClient(getApplicationContext());
        mOption = new LocationClientOption();
        mOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        mOption.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 5000;
        mOption.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        mOption.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        mOption.setOpenGps(true);//可选，默认false,设置是否使用gps
        mOption.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        mOption.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        mOption.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        mOption.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(mOption);
        mLocationClient.registerLocationListener(mDBLocationListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        if (mMapView != null) {
            mMapView.onDestroy();
            mMapView = null;
        }

        if (loadHeatMapThread.isAlive()) {
            loadHeatMapThread.interrupt();
            loadHeatMapThread = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return  true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.navi_self:
                isFirstLoc = true;
                if (mLocationClient.isStarted()) {
                    mLocationClient.requestLocation();
                } else {
                    mLocationClient.start();
                }
                break;
            default:
                break;
        }
    }

    class MyHandler extends Handler {
        WeakReference<OrderMapActivity> mActivity;

        MyHandler(OrderMapActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            OrderMapActivity theActivity = mActivity.get();
            Log.e("hyman12345", "what is : " + msg.what);
            switch (msg.what) {
                case HANDLE_SHOW_HEATMAP:
                    //在地图上添加热力图
                    mBaiduMap.addHeatMap(heatmap);
                    mMapView.invalidate();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }
}
