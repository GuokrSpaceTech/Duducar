package com.guokrspace.duducar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
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
import com.google.gson.Gson;
import com.guokrspace.duducar.common.Constants;
import com.guokrspace.duducar.communication.DuduService;
import com.guokrspace.duducar.communication.ResponseHandler;
import com.guokrspace.duducar.communication.SocketClient;
import com.guokrspace.duducar.communication.fastjson.FastJsonTools;
import com.guokrspace.duducar.communication.http.model.WebViewUrls;
import com.guokrspace.duducar.communication.message.NearByCars;
import com.guokrspace.duducar.communication.message.OrderDetail;
import com.guokrspace.duducar.communication.message.SearchLocation;
import com.guokrspace.duducar.database.CommonUtil;
import com.guokrspace.duducar.database.PersonalInformation;
import com.guokrspace.duducar.ui.DrawerView;
import com.guokrspace.duducar.ui.OrderConfirmationView;
import com.guokrspace.duducar.ui.WinToast;
import com.guokrspace.duducar.util.SharedPreferencesUtils;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import org.json.JSONException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import me.drakeet.materialdialog.MaterialDialog;

public class PreOrderActivity extends AppCompatActivity
        implements
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        OnGetGeoCoderResultListener,
        OrderHistoryActivity.OnFragmentInteractionListener {
    private Context mContext = this;
    MapView mMapView = null;
    BaiduMap mBaiduMap = null;


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
    protected SlidingMenu sidingMenu;
    DrawerView drawerView;
    Toolbar mToolbar;
    ImageView tempImageView;

    private Bitmap tempBitmap;

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

    private PersonalInformation person;
    private ServiceReceiver receiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApplication = (DuduApplication) getApplicationContext();

        AppExitUtil.getInstance().addActivity(this);

        duduService = new Intent(this, DuduService.class);
        startService(duduService);
        Log.e("daddy", "oncreate");
        List persons = mApplication.mDaoSession.getPersonalInformationDao().queryBuilder().list();
        if (persons.size() <= 0) { //Not Logged in
            Log.e("daddy", "person size < 0");
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivityForResult(intent, ACTVITY_LOGIN_REQUEST);
//            finish();
        }

        /*
         * Init the UI
         */
        setContentView(R.layout.activity_main);
        requestLocButton = (Button) findViewById(R.id.buttonLoc);
        startLocButton = (TextView) findViewById(R.id.startButton);
        destLocButton = (TextView) findViewById(R.id.destButton);
        nearByCarsTextView = (TextView) findViewById(R.id.nearByCarsTextView);
        callCabButton = (Button) findViewById(R.id.callaCabButton);
        tempImageView = (ImageView) findViewById(R.id.mapview_temp);

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
        //
        Log.e("daddy", "init baidu map");
        LatLng initLoc = new LatLng(28.173, 112.9584);
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(initLoc);
        mBaiduMap.animateMapStatus(u);
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(16.0f);
        mBaiduMap.setMapStatus(msu);

        /*
         * Init various listener
         */
        initListener();
        Log.e("DADDY", "initListener");

        /*//Setup the Drawer
        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
        mNavigationDrawerFragment.onHiddenChanged(true);
        mTitle = getTitle();*/
        //init slidingmenu
        initSlidingMenu();
        //init toolbar
        initToolBar();


        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker));

        //给slidingMenu添加打开时的响应事件
        sidingMenu.setOnOpenListener(new SlidingMenu.OnOpenListener() {
            @Override
            public void onOpen() {
                //打开的时候截图并隐藏map
                mMapView.setVisibility(View.GONE);
                tempImageView.setVisibility(View.VISIBLE);
                /*mBaiduMap.snapshot(new BaiduMap.SnapshotReadyCallback() {
                    @Override
                    public void onSnapshotReady(Bitmap bitmap) {
                        PreOrderActivity.this.tempBitmap = bitmap;
                        tempImageView.setImageBitmap(tempBitmap);
                        mMapView.setVisibility(View.GONE);
                        tempImageView.setVisibility(View.VISIBLE);
                    }
                });*/
            }
        });
        sidingMenu.setOnCloseListener(new SlidingMenu.OnCloseListener() {
            @Override
            public void onClose() {
                //用临时图片替换百度地图
                mMapView.setVisibility(View.GONE);
                tempImageView.setVisibility(View.VISIBLE);
            }
        });

        sidingMenu.setOnOpenedListener(new SlidingMenu.OnOpenedListener() {
            @Override
            public void onOpened() {
                mMapView.setVisibility(View.VISIBLE);
                tempImageView.setVisibility(View.GONE);

            }
        });
        //关闭以后，截取地图，并还原回来
        sidingMenu.setOnClosedListener(new SlidingMenu.OnClosedListener() {
            @Override
            public void onClosed() {
                mMapView.setVisibility(View.VISIBLE);
                tempImageView.setVisibility(View.GONE);
                /*if (tempBitmap != null && !tempBitmap.isRecycled()) {
                    tempBitmap.recycle();
                    tempBitmap = null;
                }
                tempImageView.setVisibility(View.GONE);
                mMapView.setVisibility(View.VISIBLE);*/
            }
        });


        // 定位初始化
        initLocation();
        mLocClient.start();
        Log.e("daddy", "start initlocatioin");
        // GeoCoder init
        mGeoCoder = GeoCoder.newInstance();
        mGeoCoder.setOnGetGeoCodeResultListener(this);



        /*
         * Init the data
         */
        start = new SearchLocation();
    }

    private void initToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(getString(R.string.app_name));
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_drawericon));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sidingMenu.isMenuShowing()) {
                    sidingMenu.showContent();
                } else {
                    sidingMenu.showMenu();
                }
            }
        });
    }

    private void initSlidingMenu() {
        drawerView = new DrawerView(this);
        sidingMenu = drawerView.initSlidingMenu();
    }

    private void initListener() {

        List persons = mApplication.mDaoSession.getPersonalInformationDao().queryBuilder().limit(1).list();
        if (persons.size() == 1) {
            person = (PersonalInformation) persons.get(0);
            CommonUtil.setPersion(person);
            doLogin(person);
        }

        callCabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                List persons = mApplication.mDaoSession.getPersonalInformationDao().queryBuilder().list();
                if (persons.size() <= 0) { //Not Logged in
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    startActivityForResult(intent, ACTVITY_LOGIN_REQUEST);
                    finish();
                } else if (dest == null || destLocButton.getText().length() < 1) {
                    WinToast.toast(PreOrderActivity.this, "请先输入目的地");
                } else {
                    mApplication.mPersonalInformation = (PersonalInformation) persons.get(0);
                    Intent intent = new Intent(mContext, PostOrderActivity.class);
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
                destLocButton.setClickable(false);
                destLocButton.setEnabled(false);
                SocketClient.getInstance().pullNotPaidOrder(Constants.PASSENGER_ROLE, new ResponseHandler(Looper.myLooper()) {
                    @Override
                    public void onSuccess(String messageBody) {

                        JSONObject noPaid = JSONObject.parseObject(messageBody);
                        if (((String) noPaid.get("order_status")).equals("1")) {//存在未支付的账单
                            final OrderDetail notPaidOrder = new Gson().fromJson((String) noPaid.get("order"), OrderDetail.class);
                            if ("1".equals(notPaidOrder.getPay_role())) { //司机代付
                                //;
                            } else {
                                final MaterialDialog dialog = new MaterialDialog(PreOrderActivity.this);
                                dialog.setTitle("账单欠费").setMessage("您还有支付的订单, 请尽快完成支付, 否则将无法继续为您提供服务!")
                                        .setCanceledOnTouchOutside(false).setNegativeButton("稍后支付", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                    }
                                }).setPositiveButton("立即支付", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(PreOrderActivity.this, RatingActivity.class);
                                        intent.putExtra("order", notPaidOrder);
                                        startActivity(intent);
                                        dialog.dismiss();

                                    }
                                }).show();
                                destLocButton.setClickable(true);
                                destLocButton.setEnabled(true);
                                return;
                            }
                        }  //正常跳转
                        Intent intent = new Intent(mContext, SearchActivity.class);
                        intent.putExtra("location", start); //Search nearby from the start
                        intent.putExtra("city", city);
                        startActivityForResult(intent, ACTIVITY_SEARCH_DEST_REQUEST);
                        destLocButton.setClickable(true);
                        destLocButton.setEnabled(true);
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(PreOrderActivity.this, "状态异常, 请稍后尝试", Toast.LENGTH_SHORT).show();
                        destLocButton.setClickable(true);
                        destLocButton.setEnabled(true);
                    }

                    @Override
                    public void onTimeout() {
                        //TODO
//                        Intent intent = new Intent(mContext, SearchActivity.class);
//                        intent.putExtra("location", start); //Search nearby from the start
//                        intent.putExtra("city", city);
//                        startActivityForResult(intent, ACTIVITY_SEARCH_DEST_REQUEST);
                        destLocButton.setClickable(true);
                        destLocButton.setEnabled(true);
                    }
                });
//                Intent intent = new Intent(mContext, SearchActivity.class);
//                intent.putExtra("location", start); //Search nearby from the start
//                intent.putExtra("city", city);
//                startActivityForResult(intent, ACTIVITY_SEARCH_DEST_REQUEST);
            }
        });
    }

    public class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case Constants.SERVICE_ACTION_RELOGIN:
                    if (person != null) { // 用户登陆出错
                        doLogin(person);
                    } else {
                        Intent loginIntent = new Intent(mContext, LoginActivity.class);
                        startActivityForResult(loginIntent, ACTVITY_LOGIN_REQUEST);
                    }
                    break;
                default:
                    return;
            }
        }
    }

    private void checkNotPaid() {

        SocketClient.getInstance().pullNotPaidOrder(Constants.PASSENGER_ROLE, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {

                JSONObject noPaid = JSONObject.parseObject(messageBody);
                if (((String) noPaid.get("order_status")).equals("1")) {//存在未支付的账单
//                        final OrderDetail notPaidOrder = FastJsonTools.getObject((String)noPaid.get("order"), OrderDetail.class);
                    Log.e("daddy fang", (String)noPaid.get("order"));
                    final OrderDetail notPaidOrder = new Gson().fromJson((String)noPaid.get("order"), OrderDetail.class);
                    if("1".equals(notPaidOrder.getPay_role())){ //司机代付
                        return;
                    }
                    final MaterialDialog dialog = new MaterialDialog(PreOrderActivity.this);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setTitle("账单欠费").setMessage("您还有支付的订单, 请尽快完成支付, 否则将无法继续为您提供服务!")
                            .setCanceledOnTouchOutside(false).setNegativeButton("稍后支付", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    }).setPositiveButton("立即支付", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(PreOrderActivity.this, RatingActivity.class);
                            intent.putExtra("order", notPaidOrder);
                            Log.e("daddy", notPaidOrder.getStatus() + "");
                            startActivity(intent);
                            dialog.dismiss();
                        }
                    }).show();
                }

            }

            @Override
            public void onFailure(String error) {
                Log.e("daddy", "message error " + error);

            }

            @Override
            public void onTimeout() {
                Log.e("daddy", "message time out ");
            }
        });
    }

    private void doLogin(final PersonalInformation person) {

        SocketClient.getInstance().sendLoginReguest(person.getMobile(), Constants.PASSENGER_ROLE, person.getToken(), new ResponseHandler(Looper.getMainLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                Log.e("daddy login ", "success");
                WinToast.toast(PreOrderActivity.this, "登陆成功");
                //TODO: init userinfo;
                //检测是否存在正在进行的订单
                JSONObject object = JSONObject.parseObject(messageBody);
                String hasActive = (String) object.get("has_active_order");
                if (hasActive.equals("1")) {
                    //存在正在执行的订单
                    String status = (String) object.get("order_status");
                    if (status.equals("5")) {//订单已经取消
                        return;
                    }
                    mApplication.mPersonalInformation = person;
                    Log.e("daddy fang", (String) object.get("active_order"));
                    OrderDetail orderDetail = new Gson().fromJson((String) object.get("active_order"), OrderDetail.class);
                    Log.e("daddy fang", orderDetail.getId() + "");
                    if (status.equals("1")) {
                        Toast.makeText(PreOrderActivity.this, "您有订单正在等待派发", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(PreOrderActivity.this, PostOrderActivity.class);
                        intent.putExtra("isRecover", true);
                        intent.putExtra("status", "1");
                        intent.putExtra("order_detail", orderDetail);
                        startActivityForResult(intent, 0x6002);
                    } else if (status.equals("2")) {
                        Toast.makeText(PreOrderActivity.this, "我们已经为你指派司机", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(PreOrderActivity.this, PostOrderActivity.class);
                        intent.putExtra("isRecover", true);
                        intent.putExtra("status", "2");
                        intent.putExtra("order_detail", orderDetail);
                        intent.putExtra("driver_detail", orderDetail.getDriver());
                        startActivityForResult(intent, 0x6002);
                    } else if (status.equals("3")) {
                        Toast.makeText(PreOrderActivity.this, "您有正在执行的行程", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(PreOrderActivity.this, PostOrderActivity.class);
                        intent.putExtra("isRecover", true);
                        intent.putExtra("status", "3");
                        intent.putExtra("order_detail", orderDetail);
                        intent.putExtra("driver_detail", orderDetail.getDriver());
                        startActivityForResult(intent, 0x6002);
                    } else if (status.equals("4")) { //存在未支付的订单
                        checkNotPaid();
                    } else {
                        //订单已经取消
                        Log.e("daddy", "bad status");
                        return;
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e("daddy login ", "failure");
                WinToast.toast(PreOrderActivity.this, "用户信息异常, 请重新登陆!");
                startActivity(new Intent(PreOrderActivity.this, LoginActivity.class));
                finish();
            }

            @Override
            public void onTimeout() {
                Log.e("daddy login", "timeout ");
                WinToast.toast(PreOrderActivity.this, "登陆超时,请检查网络连接情况");
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
//        // update the main content by replacing fragments
//        FragmentManager fragmentManager = getSupportFragmentManager();
//
//        if (position == 0) {
//            FragmentTransaction transaction;
//            transaction = fragmentManager.beginTransaction();
//            transaction.replace(R.id.container, PersonalInfoFragment.newInstance());
//            transaction.addToBackStack("personalinfo");
//            transaction.commit();
//        }
//        //TripOverOrder Records
//        else if (position == 1) {
//            FragmentTransaction transaction;
//            transaction = fragmentManager.beginTransaction();
//            transaction.replace(R.id.container, OrderHistoryActivity.newInstance());
//            transaction.addToBackStack("orderrecords");
//            transaction.commit();
//        }
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

    /*public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_drawericon);
        actionBar.setTitle(mTitle);

    }*/


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        if (!mNavigationDrawerFragment.isDrawerOpen()) {
//            // Only show items in the action bar relevant to this screen
//            // if the drawer is not showing. Otherwise, let the drawer
//            // decide what to show in the action bar.
////            getMenuInflater().inflate(R.menu.main, menu);
////            restoreActionBar();
//            return true;
//        }
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
            /*if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
                return true; //Just return, do not toggle the drawer
            }*/
        }

        //Let the super handle the rest, toggle the drawer
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();

        if (mCurrentMarker != null) mCurrentMarker.recycle();
        mLocClient.stop();

//        sidingMenu.
        ViewGroup viewGroup = (ViewGroup) getWindow().getDecorView();
        viewGroup.removeView(sidingMenu);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        timer.cancel();
        timer.purge();
        unregisterReceiver(receiver);
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

        registerBroadcastReceiver();

        super.onResume();
    }

    //监听service传来的消息
    private void registerBroadcastReceiver() {
        receiver = new ServiceReceiver();
        IntentFilter filter = new IntentFilter(Constants.SERVICE_BROADCAST);
        filter.addAction(Constants.SERVICE_ACTION_RELOGIN);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                if (requestCode == ACTVITY_LOGIN_REQUEST) {
                    drawerView.refreshMenuView();
                    //获取baseinfo信息
                    SocketClient.getInstance().getBaseInfoRequest("2", new ResponseHandler(Looper.myLooper()) {
                        @Override
                        public void onSuccess(String messageBody) {
                            String comments = "";
                            String complaints = "";
                            String cancel_reasons = "";
                            String help_url = "";
                            String about_url = "";
                            String clause_url = "";
                            if (!TextUtils.isEmpty(messageBody)) {
                                JSONObject responseObj = JSON.parseObject(messageBody);
                                if (responseObj != null) {
                                    if (responseObj.containsKey("comments")) {
                                        JSONArray commentsList = responseObj.getJSONArray("comments");
                                        comments = commentsList.toJSONString();
                                    }
                                    if (responseObj.containsKey("complaint")) {
                                        JSONArray complaintList = responseObj.getJSONArray("complaint");
                                        complaints = complaintList.toJSONString();
                                    }
                                    if (responseObj.containsKey("cancel_order_reason")) {
                                        JSONArray reasonList = responseObj.getJSONArray("cancel_order_reason");
                                        cancel_reasons = reasonList.toJSONString();
                                    }
                                    JSONObject webviewObj = null;
                                    if (responseObj.containsKey("webview")) {
                                        webviewObj = responseObj.getJSONObject("webview");
                                    }
                                    if (webviewObj != null) {
                                        help_url = webviewObj.getString("help");
                                        about_url = webviewObj.getString("about");
                                        clause_url = webviewObj.getString("clause");
                                    }

                                    Map<String, Object> baseinfo = new HashMap<>();
                                    baseinfo.put(SharedPreferencesUtils.BASEINFO_COMMENTS, comments);
                                    baseinfo.put(SharedPreferencesUtils.BASEINFO_COMPLAINTS, complaints);
                                    baseinfo.put(SharedPreferencesUtils.BASEINFO_CANCEL_REASONS, cancel_reasons);
                                    baseinfo.put(SharedPreferencesUtils.URL_HELP, help_url);
                                    baseinfo.put(SharedPreferencesUtils.URL_ABOUT, about_url);
                                    baseinfo.put(SharedPreferencesUtils.URL_CLAUSE, clause_url);
                                    SharedPreferencesUtils.setParams(mContext, baseinfo);
                                }
                            }

                        }

                        @Override
                        public void onFailure(String error) {
                            showToast("获取baseinfo失败~");
                        }

                        @Override
                        public void onTimeout() {
                            showToast("获取baseinfo超时...");

                        }
                    });
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
                    final List persons = mApplication.mDaoSession.getPersonalInformationDao().queryBuilder().list();
                    if (persons.size() <= 0) { //Not Logged in
                        Intent intent = new Intent(mContext, LoginActivity.class);
                        startActivityForResult(intent, ACTVITY_LOGIN_REQUEST);
                        finish();
                    } else if (dest == null) {
                        WinToast.toast(PreOrderActivity.this, "请先输入目的地");
                    } else {
                        mApplication.mPersonalInformation = (PersonalInformation) persons.get(0);
                        Intent intent = new Intent(mContext, PostOrderActivity.class);
                        intent.putExtra("start", start);
                        intent.putExtra("dest", dest);
                        startActivityForResult(intent, 0x6002);

                    }
                }
                break;
            case RESULT_CANCELED:
                destLocButton.setText("");
                break;
            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showToast(String msg) {
        Toast.makeText(PreOrderActivity.this, msg, Toast.LENGTH_SHORT).show();
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
            } catch (Exception e) {
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


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {

            if (sidingMenu.isMenuShowing()) {
                sidingMenu.showContent();
                return true;
            }

            /*if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
                return false;
            }*/

            final AlertDialog.Builder alterDialog = new AlertDialog.Builder(this);
            alterDialog.setMessage("确定退出应用？");
            alterDialog.setCancelable(true);

            alterDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (duduService != null) {
                        stopService(duduService);
                    }
                    AppExitUtil.getInstance().exit();
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


    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            if (start != null) {

                if (System.currentTimeMillis() - CommonUtil.getCurTime() > 1000 * 60) {
                    //TODO :百度地图定位 60秒超时
                }

                if (CommonUtil.isLocationSuccess()) {//定位成功

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
