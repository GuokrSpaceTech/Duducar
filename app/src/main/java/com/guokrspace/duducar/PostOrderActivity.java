package com.guokrspace.duducar;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.guokrspace.duducar.communication.ResponseHandler;
import com.guokrspace.duducar.communication.SocketClient;
import com.guokrspace.duducar.communication.fastjson.FastJsonTools;
import com.guokrspace.duducar.communication.message.DriverDetail;
import com.guokrspace.duducar.communication.message.DriverInfo;
import com.guokrspace.duducar.communication.message.MessageTag;
import com.guokrspace.duducar.communication.message.NearByCars;
import com.guokrspace.duducar.communication.message.OrderDetail;
import com.guokrspace.duducar.communication.message.SearchLocation;
import com.guokrspace.duducar.communication.message.TripOver;
import com.guokrspace.duducar.communication.message.TripOverOrder;
import com.guokrspace.duducar.communication.message.TripStart;
import com.guokrspace.duducar.database.OrderRecord;
import com.guokrspace.duducar.ui.DriverInformationView;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PostOrderActivity extends AppCompatActivity {

    //UI
    Context mContext = this;
    String orderStatusString = "正在预约车辆";
    TextView mDestTextView;
    TextView mStartTextView;
    FloatingActionButton mFab;
    DriverInformationView driverView;
    boolean isFirstLoc = true;// 是否首次定位
    ProgressBar mProgressBar;

    int state;

    //Baidu Map Location
    MapView mMapView = null;
    BaiduMap mBaiduMap = null;
    BitmapDescriptor mCurrentMarker;
    LocationClient mLocClient;
    public MyLocationListener myListener = new MyLocationListener();

    //Data
    SearchLocation start;
    SearchLocation dest;
    DriverInfo driver;
    TripStart order_start;
    TripOver order_finish;
    LatLng currentLocation;
    DuduApplication mApplication;

    //Activity Start RequestCode
    public final static int ACTIVITY_SEARCH_DEST_REQUEST = 0x1001;

    final static int WAITING_FOR_ORDER_CONFIRM = 1;
    final static int WAITING_FOR_DRIVER_COME = 2;
    final static int ORDER_CANCELLING = 3;
    final static int ORDER_CANCEL_CONFIRMED = 4;
    final static int INCAR = 5;
    final static int ORDER_COMPETED = 6;

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

                    break;

                case MessageTag.MESSAGE_ORDER_COMPLETED:
                    if (order_finish != null) {
                        orderStatusString = String.format("到达%s", order_finish.getOrder().getDestination());
                        getSupportActionBar().setTitle(orderStatusString);

                        Intent intent = new Intent(mContext, AlipayActivity.class);
                        intent.putExtra("order",order_finish.getOrder());
                        startActivity(intent);

                        finish();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_cab);

        // ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(orderStatusString);

        //Get Args
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            start = (SearchLocation) bundle.get("start");
            dest = (SearchLocation) bundle.get("dest");
            requestCar();
        }

        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, mCurrentMarker));

        mBaiduMap.getUiSettings().setRotateGesturesEnabled(false);
        mBaiduMap.getUiSettings().setScrollGesturesEnabled(false);
        mBaiduMap.getUiSettings().setZoomGesturesEnabled(false);

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

        //UI
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Snackbar.make(view, "订单取消中...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                state = ORDER_CANCELLING;

                mHandler.sendEmptyMessageDelayed(MessageTag.MESSAGE_ORDER_CANCEL_CONFIRMED, 2000); //Simulate the cancel is success
                return false;
            }
        });

        // Debug
        mFab.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View view) {
////                mHandler.sendEmptyMessage(MessageTag.MESSAGE_ORDER_COMPLETED);
////            }
//
//            @Override
//            public void onClick(View view) {
//                order_start = new TripStart();
//
//                OrderDetail order = new OrderDetail();
//
//                order.setStart("麓山南路36号");
//                order.setStart_lat("28.18539");
//                order.setStart_lng("112.949728");
//                order.setDestination("火车南站");
//                order.setCar_type("1");
//
//                order_start.setOrder(order);
//
//                driver = new DriverInfo();
//                DriverDetail detail = new DriverDetail();
//                detail.setName("王师傅");
//                detail.setAvatar("http://img4.imgtn.bdimg.com/it/u=1519979105,1747027397&fm=21&gp=0.jpg");
//                detail.setMobile("13522577115");
//                detail.setDescription("黑色起亚");
//                detail.setPlate("湘A-12445345");
//                detail.setPicture("http://img4.imgtn.bdimg.com/it/u=1519979105,1747027397&fm=21&gp=0.jpg");
//                detail.setRating(4);
//                driver.setDriver(detail);
//                mHandler.sendEmptyMessage(MessageTag.MESSAGE_ORDER_DISPATCHED);
//            }
//        });

            //        mFab.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View view) {
////                mHandler.sendEmptyMessage(MessageTag.MESSAGE_ORDER_COMPLETED);
////            }
//
            @Override
            public void onClick(View view) {
                order_finish = new TripOver();

                String messageBody = "{\"id\":29,\"orderNum\":\"12345678889\",\"driver_id\":3,\"passenger_id\":3,\"passenger_mobile\":\"13900000002\"," +
                        "\"start\":\"\\u6e56\\u5357\\u7701\\u957f\\u6c99\\u5e02\\u5cb3\\u9e93\\u533a\\u767b\\u9ad8\\u8def4-2\"," +
                        "\"destination\":\"\\u83ab\\u4fea\\u82b1\\u56ed\",\"start_lat\":28.185693,\"start_lng\":112.949612," +
                        "\"destination_lat\":28.185693,\"destination_lng\":112.949612,\"start_time\":1447162569," +
                        "\"end_time\":1447162579,\"pre_mileage\":\"0.00\",\"pre_price\":\"0.00\",\"car_type\":1,\"rent_type\":0,\"additional_price\":\"\"," +
                        "\"org_price\":\"0.01\",\"isCancel\":\"\"," +
                        "\"mileage\":\"0.0\",\"sumprice\":\"\",\"create_time\":1447162560,\"isCityline\":0,\"cityline_id\":\"\",\"pay_time\":\"\",\"pay_type\":\"\",\"status\":4,\"rating\":0}";

                OrderDetail detail = FastJsonTools.getObject(messageBody, OrderDetail.class);



//                order.setDestination("麓山南路36号");
//                order.setStart_lat("28.18539");
//                order.setStart_lng("112.949728");
//                order.setDestination_lat("28.18539");
//                order.setDestination_lng("112.949728");
//                order.setCar_type("1");
//
//                order_finish.setOrder(null);
//
//                driver = new DriverInfo();
//                DriverDetail detail = new DriverDetail();
//                detail.setName("王师傅");
//                detail.setAvatar("http://img4.imgtn.bdimg.com/it/u=1519979105,1747027397&fm=21&gp=0.jpg");
//                detail.setMobile("13522577115");
//                detail.setDescription("黑色起亚");
//                detail.setPlate("湘A-12445345");
//                detail.setPicture("http://img4.imgtn.bdimg.com/it/u=1519979105,1747027397&fm=21&gp=0.jpg");
//                detail.setRating(4);
//                driver.setDriver(detail);

                mApplication.mDriverDetail = driver.getDriver();

                mHandler.sendEmptyMessage(MessageTag.MESSAGE_ORDER_COMPLETED);
            }
        });

        mStartTextView = (TextView) findViewById(R.id.textViewStart);
        mDestTextView = (TextView) findViewById(R.id.textViewDestination);
        if (start != null)
            mStartTextView.setText(start.getAddress());
        if (dest != null)
            mDestTextView.setText(dest.getAddress());

        state = WAITING_FOR_ORDER_CONFIRM;

        getSupportActionBar().setTitle("正在预约中...");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mApplication = (DuduApplication) getApplicationContext();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SocketClient.getInstance().registerServerMessageHandler(MessageTag.ORDER_ACCEPT, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                driver = FastJsonTools.getObject(messageBody, DriverInfo.class);
                mHandler.sendEmptyMessage(MessageTag.MESSAGE_ORDER_DISPATCHED);
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

        SocketClient.getInstance().registerServerMessageHandler(MessageTag.TRIP_OVER, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                order_finish = FastJsonTools.getObject(messageBody, TripOver.class);

                OrderRecord orderRecord = new OrderRecord();
                orderRecord.setStartAddr(order_start.getOrder().getStart());
                orderRecord.setDestAddr(order_finish.getOrder().getDestination());
                Double startLat = order_finish.getOrder().getStart_lat();
                Double startLng = order_finish.getOrder().getStart_lng();
                Double destLat = order_finish.getOrder().getDestination_lat();
                Double destLng = order_finish.getOrder().getDestination_lng();
                orderRecord.setStartLat(startLat == null ? "0" : String.valueOf(startLat));
                orderRecord.setStartLng(startLng == null ? "0" : String.valueOf(startLng));
                orderRecord.setDestLat(destLat == null ? "0" : String.valueOf(destLat));
                orderRecord.setDestLng(destLng == null ? "0" : String.valueOf(destLng));
                orderRecord.setMileage(order_finish.getOrder().getMileage());
                orderRecord.setPrice(order_finish.getOrder().getOrg_price());
                orderRecord.setCarType(String.valueOf(order_finish.getOrder().getCar_type()));
                SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm");
                String dateStr = sdf.format(new Date());
                orderRecord.setOrderTime(dateStr);

                mApplication.mDaoSession.getOrderRecordDao().insert(orderRecord);
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
         * Start the timer to for peoriodically ask for nearby cars
         */
        timer = new Timer();
        timer.scheduleAtFixedRate(new MyTimerTask(), 2000, 3 * 1000);

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
        SocketClient.getInstance().cancelCarRequest("2", new ResponseHandler() {
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

            driverView.mRatingBar.setRating(driver.getDriver().getRating());
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
            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }

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
            if (start != null) {
                SocketClient.getInstance().sendNearByCarRequestTest(28.173D, 112.9584D, "1", new ResponseHandler(Looper.getMainLooper()) {
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

            }
        }
    }
}
