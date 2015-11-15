package com.guokrspace.duducar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
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
import com.guokrspace.duducar.communication.DuduService;
import com.guokrspace.duducar.communication.ResponseHandler;
import com.guokrspace.duducar.communication.SocketClient;
import com.guokrspace.duducar.communication.fastjson.FastJsonTools;
import com.guokrspace.duducar.communication.message.NearByCars;
import com.guokrspace.duducar.communication.message.SearchLocation;
import com.guokrspace.duducar.database.CommonUtil;
import com.guokrspace.duducar.database.PersonalInformation;
import com.guokrspace.duducar.ui.OrderConfirmationView;
import com.guokrspace.duducar.ui.WinToast;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PreOrderActivity extends AppCompatActivity
        implements
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        OnGetGeoCoderResultListener,
        OrderHistoryFragment.OnFragmentInteractionListener,
        PersonalInfoFragment.OnFragmentInteractionListener {
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
    TextView startLocButton;
    TextView destLocButton;
    TextView nearByCarsTextView;
    TextView callCabButton;
    OrderConfirmationView orderConfirmationView;

    //地址解析
    GeoCoder mGeoCoder = null;

    //Data
    public SearchLocation start = null;
    public SearchLocation dest = null;
    Order mOrder = new Order();
    String city = null;
    DuduApplication mApplication;

    //State Machine
    int state;
    boolean isShowingOrderConfirmatinoView = false;

    //Activity Start RequestCode
    public final static int ACTIVITY_SEARCH_DEST_REQUEST = 0x1001;
    public final static int ACTIVITY_SEARCH_START_REQUEST = 0x1002;
    public final static int ACTVITY_CONFIRM_ORDER_REQUEST = 0x1003;
    public final static int ACTVITY_COST_ESTIMATE_REQUEST = 0x1004;
    public final static int ACTVITY_LOGIN_REQUEST = 0x1005;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private Timer timer;

    private Intent duduService;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApplication = (DuduApplication) getApplicationContext();
        List persons = mApplication.mDaoSession.getPersonalInformationDao().queryBuilder().list();
        if (persons.size() <= 0) { //Not Logged in
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivityForResult(intent, ACTVITY_LOGIN_REQUEST);
            finish();
        }

        /*
         * Init the UI
         */
        setContentView(R.layout.activity_main);
        requestLocButton = (Button) findViewById(R.id.buttonLoc);
        startLocButton = (TextView) findViewById(R.id.startButton);
        destLocButton = (TextView) findViewById(R.id.destButton);
        nearByCarsTextView = (TextView)findViewById(R.id.nearByCarsTextView);
        callCabButton = (Button)findViewById(R.id.callaCabButton);

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

        LatLng initLoc = new LatLng(28.173,112.9584);
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(initLoc);
        mBaiduMap.animateMapStatus(u);
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

        /*
         * Login if socket connected
         */

        Thread thead = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true)
                {
                    if(mTcpClient==null) continue;

                    if(mTcpClient.isSocketConnected()==false)
                        continue;
                    else {
                        sendLoginRequest(mTcpClient);
                        break;
                    }
                }
            }
        });
        thead.start();

        /*
         * Init the data
         */
        start = new SearchLocation();
    }

    private void initListener() {
        callCabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                List persons = mApplication.mDaoSession.getPersonalInformationDao().queryBuilder().list();
                if (persons.size() <= 0) { //Not Logged in
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    startActivityForResult(intent, ACTVITY_LOGIN_REQUEST);
                    finish();
                } else if(dest==null) {
                    WinToast.toast(PreOrderActivity.this, "请先输入目的地");
                }else{
                    mApplication.mPersonalInformation = (PersonalInformation) persons.get(0);
                    Intent intent = new Intent(mContext,PostOrderActivity.class);
                    intent.putExtra("start", start);
                    intent.putExtra("dest", dest);
                    startActivityForResult(intent, 0x6002);
                }
            }
        });
        requestLocButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                mLocClient.start();
                isFirstLoc = true;
                mLocClient.requestLocation();
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

        startLocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mContext, SearchActivity.class);
                intent.putExtra("location", start);
                intent.putExtra("city", city);
                startActivityForResult(intent, ACTIVITY_SEARCH_START_REQUEST);
            }
        });

        destLocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mContext, SearchActivity.class);
                intent.putExtra("location", start); //Search nearby from the start
                intent.putExtra("city", city);
                startActivityForResult(intent, ACTIVITY_SEARCH_DEST_REQUEST);
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

        if (position == 0) {
            FragmentTransaction transaction;
            transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.container, PersonalInfoFragment.newInstance());
            transaction.addToBackStack("personalinfo");
            transaction.commit();
        }
        //TripOverOrder Records
        else if (position == 1) {
            FragmentTransaction transaction;
            transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.container, OrderHistoryFragment.newInstance());
            transaction.addToBackStack("orderrecords");
            transaction.commit();
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 0:
                mTitle = getString(R.string.title_section2);
                break;
            case 1:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_drawericon);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
//            getMenuInflater().inflate(R.menu.main, menu);
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

        if (id == android.R.id.home) {
            //If there are fragments showing
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
                return true; //Just return, do not toggle the drawer
            }
        }

        //Let the super handle the rest, toggle the drawer
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        if(duduService != null) {
            stopService(duduService);
        }
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
            if (requestCode == ACTVITY_LOGIN_REQUEST) {

            } else if (requestCode == ACTIVITY_SEARCH_START_REQUEST) {

                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    start = (SearchLocation) bundle.get("location");
                    LatLng searchLoc = start.getLocation();

                    startLocButton.setText(start.getAddress());

                    mBaiduMap.clear();
                    mBaiduMap.addOverlay(new MarkerOptions().position(searchLoc)
                            .icon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.ic_current_position_pin)));
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(searchLoc));
                    mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(searchLoc));
                }
            } else if (requestCode == ACTIVITY_SEARCH_DEST_REQUEST) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    dest = (SearchLocation) bundle.get("location");
                    destLocButton.setText(dest.getAddress());
                    destLocButton.setTextColor(getResources().getColor(android.R.color.black));
                    callCabButton.setEnabled(true);

                    Intent intent = new Intent(mContext, CostEstimateActivity.class);
                    intent.putExtra("start", start);
                    intent.putExtra("dest", dest);

                    startActivityForResult(intent, ACTVITY_COST_ESTIMATE_REQUEST);
                }
            } else if (requestCode == ACTVITY_COST_ESTIMATE_REQUEST) {
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

        start.setAddress(result.getAddress());
        start.setLat(result.getLocation().latitude);
        start.setLng(result.getLocation().longitude);

        mOrder.setStartLocation(result.getAddress(), result.getLocation().latitude, result.getLocation().longitude);

        city = result.getAddressDetail().city;
        startLocButton.setText(result.getAddress());
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

            try {
                if (locData != null)
                    mBaiduMap.setMyLocationData(locData);
            }catch (Exception e) {
                e.printStackTrace();
            }

            mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());

            CommonUtil.setCurLat(location.getLatitude());
            CommonUtil.setCurLng(location.getLongitude());
            CommonUtil.setCurTime(System.currentTimeMillis());
            CommonUtil.setLocationSuccess(true);

            if (isFirstLoc) {
                isFirstLoc = false;
                mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(mCurrentLocation));
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

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.i("", "");
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


    public void sendLoginRequest(SocketClient socketClient)
    {
        List persons = mApplication.mDaoSession.getPersonalInformationDao().queryBuilder().limit(1).list();
        if(persons.size()==1) {
            PersonalInformation person = (PersonalInformation) persons.get(0);
            socketClient.sendLoginReguest(person.getMobile(), "2", person.getToken(), new ResponseHandler(Looper.getMainLooper()) {
                @Override
                public void onSuccess(String messageBody) {
                    //登陆成功,发送心跳包
                    Log.e("daddy   ", "start service");
                    duduService = new Intent(getApplicationContext(), DuduService.class);
                    startService(duduService);
                    Log.i("","");
                }

                @Override
                public void onFailure(String error) {
                    Log.i("","");
                }

                @Override
                public void onTimeout() {
                    Log.i("","");
                }
            });
        }
    }


    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            if (start != null) {

                if(System.currentTimeMillis() - CommonUtil.getCurTime() > 1000 * 60){
                    //TODO :百度地图定位 60秒超时
                }

                if(CommonUtil.isLocationSuccess()){//定位成功

                    SocketClient.getInstance().sendNearByCarRequestTest(CommonUtil.getCurLat(), CommonUtil.getCurLng(), "1", new ResponseHandler(Looper.getMainLooper()) {
                        @Override
                        public void onSuccess(String messageBody) {

                            NearByCars nearByCars = FastJsonTools.getObject(messageBody, NearByCars.class);

                            mBaiduMap.clear();
                            for (NearByCars.CarLocation loc : nearByCars.getCars()) {
                                LatLng ll = new LatLng(loc.getLat(), loc.getLng());
                                mBaiduMap.addOverlay(new MarkerOptions().position(ll).icon(BitmapDescriptorFactory.fromResource(R.drawable.caricon)));
                            }

                            String nearbyString = String.format(getResources().getString(R.string.nearby), nearByCars.getCars().size());
                            nearByCarsTextView.setText(nearbyString);
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
}
