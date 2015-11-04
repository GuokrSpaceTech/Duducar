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
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RatingBar;
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
import com.guokrspace.duducar.communication.message.DriverInfo;
import com.guokrspace.duducar.communication.message.MessageTag;
import com.guokrspace.duducar.communication.message.SearchLocation;
import com.guokrspace.duducar.communication.message.TripOver;
import com.guokrspace.duducar.communication.message.TripStart;
import com.guokrspace.duducar.database.OrderRecord;
import com.guokrspace.duducar.ui.DriverInformationView;
import com.guokrspace.duducar.ui.WinToast;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PostOrderActivity extends AppCompatActivity {

    //UI
    Context mContext = this;
    String orderStatusString = "正在预约车辆";
    FrameLayout mSearchDestLLayout;
    TextView mDestTextView;
    TextView mStartTextView;
    FloatingActionButton mFab;
    DriverInformationView driverView;
    boolean isFirstLoc = true;// 是否首次定位

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
    TripOver  order_finish;
    DuduApplication mApplication;

    //Activity Start RequestCode
    public final static int ACTIVITY_SEARCH_DEST_REQUEST = 0x1001;

    final static int WAITING_FOR_ORDER_CONFIRM = 1;
    final static int WAITING_FOR_DRIVER_COME = 2;
    final static int ORDER_CANCELLING = 3;
    final static int ORDER_CANCEL_CONFIRMED = 4;
    final static int INCAR = 5;
    final static int ORDER_COMPETED = 6;

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what)
            {
                case MessageTag.MESSAGE_ORDER_CANCEL_CONFIRMED:
                    if(state == ORDER_CANCELLING) {
                        getSupportActionBar().setTitle("订单已经取消");
                        finish();
                    }
                    break;
                case MessageTag.MESSAGE_ORDER_DISPATCHED:
                    if(state == WAITING_FOR_ORDER_CONFIRM)
                    {   //The driverview moves up by 140dp
                        LinearLayout bmapLayout = (LinearLayout)findViewById(R.id.bmapLayout);
                        ViewGroup.LayoutParams paramRL = bmapLayout.getLayoutParams();
                        paramRL.height =  bmapLayout.getHeight() - dpToPx(getResources(),140);
                        bmapLayout.requestLayout();

                        driverView = (DriverInformationView)findViewById(R.id.driverView);

                        RelativeLayout container = (RelativeLayout)findViewById(R.id.container);

                        container.requestLayout();

                        orderStatusString = "出租车即将到达";
                        getSupportActionBar().setTitle(orderStatusString);

                        if(driver!=null)
                           updateDriverUI(driver);

                        mFab.setVisibility(View.GONE);

                    }
                    break;
                case MessageTag.MESSAGE_CAR_ARRIVED:
                    orderStatusString = "已经上车";
                    getSupportActionBar().setTitle(orderStatusString);
                    //修正上车地点
                    if(order_start!=null)
                    {
                        Double lat = Double.parseDouble(order_start.getOrder().getStart_lat());
                        Double lng = Double.parseDouble(order_start.getOrder().getStart_lng());
                        LatLng ll = new LatLng(lat, lng);

                        mBaiduMap.clear();
                        mBaiduMap.addOverlay(new MarkerOptions()
                                .position(ll)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding)));
                        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(ll));
                    }

                    break;

                case MessageTag.MESSAGE_ORDER_COMPLETED:
                    if(order_finish!=null) {
                        orderStatusString = String.format("到达%s", order_finish.getOrder().getDestination());
                        getSupportActionBar().setTitle(orderStatusString);

                        startActivity(new Intent(mContext, AlipayActivity.class));

                        String toastString = String.format("本次行程%s公里，支付%s元，请对本次服务评价。",
                                order_finish.getOrder().getMileage(), order_finish.getOrder().getPrice());
                        WinToast.toast(mContext, toastString);

                        driverView.mRatingBar.setEnabled(true);
                        driverView.mRatingBar.setStepSize(0.5f);
                        driverView.mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                            @Override
                            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                                WinToast.toast(mContext, "谢谢评价。");
                                driverView.mRatingBar.setEnabled(false);
                                finish();
                            }
                        });

                        mDestTextView.setText(order_finish.getOrder().getDestination());

                    }
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_cab);

        // ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(orderStatusString);

        //Get Args
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            start = (SearchLocation)bundle.get("start");
            dest = (SearchLocation)bundle.get("dest");
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
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(16.0f);
        mBaiduMap.setMapStatus(msu);

        //UI
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Snackbar.make(view, "订单取消中...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                state = ORDER_CANCELLING;
                mHandler.sendEmptyMessage(MessageTag.MESSAGE_ORDER_CANCEL_CONFIRMED); //Simulate the cancel is success
                return false;
            }
        });

        mSearchDestLLayout = (FrameLayout)findViewById(R.id.destSearchLinearLayout);
        mSearchDestLLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, SearchActivity.class);
                intent.putExtra("location",start); //start must have been set
                startActivityForResult(intent, ACTIVITY_SEARCH_DEST_REQUEST);
            }
        });

        mStartTextView = (TextView)findViewById(R.id.textViewStart);
        mDestTextView = (TextView)findViewById(R.id.textViewDestination);
        if(start!=null)
            mStartTextView.setText(start.getAddress());
        if(dest!=null)
            mDestTextView.setText(dest.getAddress());

        state = WAITING_FOR_ORDER_CONFIRM;

        getSupportActionBar().setTitle("正在预约中...");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mApplication = (DuduApplication)getApplicationContext();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SocketClient.getInstance().registerServerMessageHandler(MessageTag.CREATE_ORDER_RESP, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                driver = FastJsonTools.getObject(messageBody, DriverInfo.class);
                mHandler.sendEmptyMessage(MessageTag.MESSAGE_ORDER_DISPATCHED);
            }

            @Override
            public void onFailure(String error) {}

            @Override
            public void onTimeout() {}
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
                orderRecord.setStartLat(order_finish.getOrder().getStart_lat());
                orderRecord.setStartLng(order_finish.getOrder().getStart_lng());
                orderRecord.setDestLat(order_finish.getOrder().getDestination_lat());
                orderRecord.setDestLng(order_finish.getOrder().getDestination_lng());
                orderRecord.setMileage(order_finish.getOrder().getMileage());
                orderRecord.setPrice(order_finish.getOrder().getPrice());
                orderRecord.setCarType(order_finish.getOrder().getCar_type());
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

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(mCurrentMarker!=null)
            mCurrentMarker.recycle();
        if(mLocClient!=null)
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

    private void requestCar()
    {
        String startAddr = (start==null) ? "":start.getAddress();
        String destAddr = (dest==null) ? "":dest.getAddress();
        Double startLat =  (start==null) ? 0D:start.getLat();
        Double destLat = (dest==null) ? 0D:dest.getLat();
        Double startLng =  (start==null) ? 0D:start.getLng();
        Double destLng = (dest==null) ? 0D:dest.getLat();
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
                    public void onSuccess(String messageBody) {}

                    @Override
                    public void onFailure(String error) {
                        Log.i("","");
                        //Cancel Order
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

    private void cancelOrder()
    {
        //Cancel Order
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

    private void updateDriverUI(DriverInfo driver)
    {
        if(driverView!=null)
        {
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

            if(locData!=null)
                mBaiduMap.setMyLocationData(locData);


//            if (isFirstLoc) {
//                isFirstLoc = false;
            mBaiduMap.clear();
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(new LatLng(locData.latitude,locData.longitude));
            mBaiduMap.animateMapStatus(u);
//            }

//            Log.i("BaiduLocationApiDem", sb.toString());
        }
    }
}
