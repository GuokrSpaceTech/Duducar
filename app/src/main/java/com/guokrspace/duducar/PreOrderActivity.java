package com.guokrspace.duducar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.*;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.guokrspace.duducar.communication.fastjson.FastJsonTools;
import com.guokrspace.duducar.communication.message.MessageTag;
import com.guokrspace.duducar.communication.ResponseHandler;
import com.guokrspace.duducar.communication.SocketClient;
import com.guokrspace.duducar.communication.StateMachine;
import com.guokrspace.duducar.communication.message.NearByCars;
import com.guokrspace.duducar.communication.message.SearchLocation;
import com.guokrspace.duducar.ui.OrderConfirmationView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PreOrderActivity extends AppCompatActivity
        implements
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        OnGetGeoCoderResultListener {
    private Context mContext = this;
    MapView mMapView = null;
    BaiduMap mBaiduMap = null;

    private SocketClient mTcpClient = null;
    private connectTask conctTask = null;

    // 定位相关
    LocationClient mLocClient;
    public MyLocationListener myListener = new MyLocationListener();
    private MyLocationConfiguration.LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    LatLng mCurrentLocation;
    boolean isFirstLoc = true;// 是否首次定位

    //UI
    Button requestLocButton;
    Button searchLocButton;
    Button callForCarButton;
    OrderConfirmationView orderConfirmationView;

    //地址解析
    GeoCoder mGeoCoder = null;

    //Data
    public LatLng mReqLoc = null;
    public String mReqAddress = "";
    public SearchLocation start = null;
    public SearchLocation dest = null;
    Order mOrder = new Order();
    String city = null;
    int messageid;

    //State Machine
    int state;

    //Activity Start RequestCode
    public final static int ACTIVITY_SEARCH_DEST_REQUEST = 0x1001;
    public final static int ACTIVITY_SEARCH_START_REQUEST = 0x1002;
    public final static int ACTVITY_CONFIRM_ORDER_REQUEST = 0x1003;
    public final static int ACTVITY_COST_ESTIMATE_REQUEST = 0x1004;

    //信息窗口
    private InfoWindow mInfoWindow;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private Handler mHandler = new Handler(new Handler.Callback() {

//        HashMap<Integer, Response> map = ResponseHandler.getInstance().responseParserMap;

        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case MessageTag.MSG_TOGGLE_CONFIRMVIEW:
                    if(orderConfirmationView!=null)
                        orderConfirmationView = new OrderConfirmationView(mContext);
                    final LinearLayout container = (LinearLayout) findViewById(R.id.container);
                    final FrameLayout mainmapview = (FrameLayout) findViewById(R.id.mainmapview);
                    container.post(new Runnable() {
                        @Override
                        public void run() {
                            ViewGroup.LayoutParams paramRL = mainmapview.getLayoutParams();
                            paramRL.height = dpToPx(getResources(), 350);
                            mainmapview.requestLayout();
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
                            container.addView(orderConfirmationView, 1, params);
                            container.requestLayout();
                        }
                    });
                    break;
                case MessageTag.MESSAGE_CREATE_ORDER_FAILURE:
                    break;
                case MessageTag.MESSAGE_CREATE_ORDER_SUCCESS:
                    break;
                case MessageTag.MESSAGE_TIMEOUT:
                    break;
                case MessageTag.MESSAGE_SUCCESS:
                    break;
                case MessageTag.MESSAGE_FAILURE:
                    break;
                case MessageTag.GET_NEAR_CAR_RESP:
                    if (state == StateMachine.STATE_FREE) {

                    }

                    break;
            }

            return false;
        }
    });

    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Init the UI
         */
        setContentView(R.layout.activity_main);
        requestLocButton = (Button) findViewById(R.id.buttonLoc);
        searchLocButton = (Button) findViewById(R.id.search_button);
        callForCarButton = (Button) findViewById(R.id.buttonPin);

        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

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

        /*
         * Init various listener
         */
        initListener();

        //Setup the Drawer
        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
        mTitle = getTitle();

        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker));


        // 定位初始化
        initLocation();
        mLocClient.start();

        // GeoCoder init
        mGeoCoder = GeoCoder.newInstance();
        mGeoCoder.setOnGetGeoCodeResultListener(this);

        /*
         * Init the SocketClient
         */
        mTcpClient = null;
        conctTask = new connectTask(); //Connect to server
        conctTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void initListener() {
        callForCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (false) //Not login
                {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    startActivityForResult(intent, 0x6001);
                } else {

                    orderConfirmationView = new OrderConfirmationView(mContext);
                    final LinearLayout container = (LinearLayout) findViewById(R.id.container);
                    final FrameLayout mainmapview = (FrameLayout) findViewById(R.id.mainmapview);

                    container.post(new Runnable() {
                        @Override
                        public void run() {
                            ViewGroup.LayoutParams paramRL = mainmapview.getLayoutParams();
                            paramRL.height = dpToPx(getResources(), 350);
                            mainmapview.requestLayout();
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
                            container.addView(orderConfirmationView, 1, params);
                            container.requestLayout();
                        }
                    });
                }
            }
        });
        requestLocButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                mLocClient.start();
                isFirstLoc = true;
                mLocClient.requestLocation();
//
            }
        });

        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {
            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                LatLng mCenterLatLng = mapStatus.target;
                // 反Geo搜索
                mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(mCenterLatLng));
            }
        });

        searchLocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mContext, SearchActivity.class);
                intent.putExtra("city", city);
                startActivityForResult(intent, 6002);
            }
        });
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

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        if (mCurrentMarker != null) mCurrentMarker.recycle();
        mLocClient.stop();
        try {
            mTcpClient.stopClient();
            conctTask.cancel(true);
            conctTask = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        timer.cancel();
        timer.purge();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        /*
         * Start the timer to for peoriodically ask for nearby cars
         */
        timer = new Timer();
        timer.scheduleAtFixedRate(new MyTimerTask(), 2000, 3 * 1000);
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 0x6001) {
                //Post mReqLocation
                messageid = SocketClient.getInstance().sendCarRequest("2", mOrder.start, mOrder.dest,
                        mOrder.start_lat, mOrder.start_lng, mOrder.dest_lat, mOrder.dest_lng,
                        mOrder.pre_mileage, mOrder.pre_price, mOrder.car_type, new ResponseHandler() {
                            @Override
                            public void onSuccess(String messageBody) {

                            }

                            @Override
                            public void onFailure(String error) {

                            }

                            @Override
                            public void onTimeout() {

                            }
                        });
            } else if (requestCode == 6002) {
                Double searchLat = (Double) data.getExtras().get("lat");
                Double searchLng = (Double) data.getExtras().get("lng");
                LatLng searchLoc = new LatLng(searchLat, searchLng);

                mBaiduMap.clear();
                mBaiduMap.addOverlay(new MarkerOptions().position(searchLoc)
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.icon_gcoding)));
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(searchLoc));
                mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(searchLoc));
            } else if (requestCode == ACTIVITY_SEARCH_DEST_REQUEST) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    dest = (SearchLocation) bundle.get("location");
                    start = new SearchLocation();
                    start.setAddress(mReqAddress);
                    start.setLat(mReqLoc.latitude);
                    start.setLng(mReqLoc.longitude);

                    Intent intent = new Intent(mContext, CostEstimateActivity.class);
                    intent.putExtra("start", start);
                    intent.putExtra("dest", dest);

                    startActivityForResult(intent, ACTVITY_COST_ESTIMATE_REQUEST);
                }
            } else if(requestCode == ACTVITY_COST_ESTIMATE_REQUEST) {
                mHandler.sendEmptyMessage(MessageTag.MSG_TOGGLE_CONFIRMVIEW);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }
//        mBaiduMap.clear();
//        mBaiduMap.addOverlay(new MarkerOptions().position(result.getLocation())
//                .icon(BitmapDescriptorFactory
//                        .fromResource(R.drawable.icon_marka)));
//        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
//                .getLocation()));
        String strInfo = String.format("纬度：%f 经度：%f",
                result.getLocation().latitude, result.getLocation().longitude);
        Toast.makeText(this, strInfo, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        mReqLoc = result.getLocation();
        mReqAddress = result.getAddress();
        mOrder.setStartLocation(result.getAddress(), result.getLocation().latitude, result.getLocation().longitude);

        city = result.getAddressDetail().city;
        callForCarButton.setText(result.getAddress());

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

            mBaiduMap.setMyLocationData(locData);

            mCurrentLocation = new LatLng(location.getLatitude(),
                    location.getLongitude());

            mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(mCurrentLocation));

            if (isFirstLoc) {
                isFirstLoc = false;
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(mCurrentLocation);
                mBaiduMap.animateMapStatus(u);
            }

//            Log.i("BaiduLocationApiDem", sb.toString());
        }
    }

    /**
     * @author Prashant Adesara
     *         receive the message from server with asyncTask
     */
    public class connectTask extends AsyncTask<String, String, SocketClient> {
        @Override
        protected SocketClient doInBackground(String... message) {
            //we create a TCPClient object and
            mTcpClient = new SocketClient();
            mTcpClient.run();
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {


            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
                return false;
            }

            final AlertDialog.Builder alterDialog = new AlertDialog.Builder(this);
            alterDialog.setMessage("确定退出应用？");
            alterDialog.setCancelable(true);

            alterDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            });
            alterDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alterDialog.show();
        }
        return false;
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((PreOrderActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    public class Order {
        String start = "";
        Double start_lat = 0D;
        Double start_lng = 0D;
        String dest = "";
        Double dest_lat = 0D;
        Double dest_lng = 0D;
        String pre_mileage = "";
        String pre_price = "";
        String car_type = "";

        public Order() {
        }

        public void setStartLocation(String address, Double lat, Double lng) {
            start = address;
            start_lat = lat;
            start_lng = lng;
        }

        public void setDestLocation(String address, Double lat, Double lng) {
            dest = address;
            dest_lat = lat;
            dest_lng = lng;
        }

        public void setOrderfee(String distance, String price, String car) {
            pre_mileage = distance;
            pre_price = price;
            car_type = car;
        }
    }


    public int dpToPx(Resources res, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
    }

    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            if (mReqLoc != null) {
//                    SocketClient.getInstance().sendNearByCarRequest(mReqLoc.latitude, mReqLoc.longitude, "2", mHandler);
//                SocketClient.getInstance().sendNearByCarRequestTest(28.173D, 112.9584D, "1", new ResponseHandler(Looper.getMainLooper()) {
//                    @Override
//                    public void onSuccess(String messageBody) {
//
//                        NearByCars nearByCars = FastJsonTools.getObject(messageBody, NearByCars.class);
//
//                        mBaiduMap.clear();
//                        for (NearByCars.CarLocation loc : nearByCars.getCars()) {
//                            LatLng ll = new LatLng(loc.getLat(), loc.getLng());
//                            mBaiduMap.addOverlay(new MarkerOptions().position(ll).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding)));
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(String error) {
//
//                    }
//
//                    @Override
//                    public void onTimeout() {
//
//                    }
//                });

            }
        }
    }
}
