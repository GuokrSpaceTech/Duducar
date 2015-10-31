package com.guokrspace.dududriver.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.MyLocationData;
import com.guokrspace.dududriver.DuduDriverApplication;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.adapter.TabPagerAdapter;
import com.guokrspace.dududriver.database.PersonalInformation;
import com.guokrspace.dududriver.net.ResponseHandler;
import com.guokrspace.dududriver.net.SocketClient;
import com.guokrspace.dududriver.net.message.HeartBeatMessage;
import com.guokrspace.dududriver.util.SharedPreferencesUtils;
import com.viewpagerindicator.TabPageIndicator;

import java.util.List;

import javax.security.auth.login.LoginException;

/**
 * Created by hyman on 15/10/22.
 */
public class MainActivity extends BaseActivity implements Handler.Callback{

    private Context context;

    private ViewPager pager;
    private TabPagerAdapter mAdapter;
    private TabPageIndicator mIndicator;

    private SocketClient mTcpClient = null;
    private connectTask conctTask = null;

    private boolean isOnline = false;
    private boolean isVisiable = false;

    private Handler mHandler;

    private static final int HANDLE_LOGIN_FAILURE = 100;

    private PersonalInformation userInfo;

    // 定位相关
    LocationClient mLocClient;
    public MyLocationListener myListener = new MyLocationListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        initView();

        mHandler = new Handler(this);

        /*
         * Check if use has logined
         */
        if (!DuduDriverApplication.getInstance().initPersonalInformation()) {
            startActivity(new Intent(this, LoginActivity.class));
        }

        /*
         * Init the SocketClient
         */
        mTcpClient = null;
        conctTask = new connectTask(); //Connect to server
        conctTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        /*
         * Start Location
         */
        initLocation();
        mLocClient.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isVisiable = true;
        /*
         * 用户不在线，就进行登陆
         *
         */
        isOnline = (boolean) SharedPreferencesUtils.getParam(MainActivity.this, SharedPreferencesUtils.LOGIN_STATE, false);
        if (!isOnline && DuduDriverApplication.getInstance().initPersonalInformation()) {
            if (!isNetworkAvailable()) {
                showToast("网络不可用，现在时离线状态");
            }
            List localUsers = DuduDriverApplication.getInstance().
                    mDaoSession.getPersonalInformationDao().
                    queryBuilder().list();
            if (localUsers != null && localUsers.size() > 0) {
                userInfo = (PersonalInformation) localUsers.get(0);
                doLogin(userInfo);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisiable = false;
        mHandler.removeMessages(HANDLE_LOGIN_FAILURE);
    }

    private void doLogin(PersonalInformation user) {
        Log.e("hyman", user.getMobile() + " " + user.getToken() + " " + user.getId());
        if (user == null) {
            return;
        }
        SocketClient.getInstance().autoLoginRequest(user.getMobile(), "1", user.getToken(), new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                showCustomToast("登陆成功");
                SharedPreferencesUtils.setParam(MainActivity.this, SharedPreferencesUtils.LOGIN_STATE, true);
                isOnline = true;
            }

            @Override
            public void onFailure(String error) {
                showCustomToast("登陆失败");
                mHandler.sendMessageDelayed(mHandler.obtainMessage(HANDLE_LOGIN_FAILURE), 500);
            }

            @Override
            public void onTimeout() {
                Log.e("hyman", "登陆超时");
                mHandler.sendMessageDelayed(mHandler.obtainMessage(HANDLE_LOGIN_FAILURE), 500);
            }
        });
    }

    private void initView() {
        mIndicator = (TabPageIndicator) findViewById(R.id.indicator);

        pager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new TabPagerAdapter(getSupportFragmentManager());
        pager.setOffscreenPageLimit(2);//设置预加载页数
        //要求每次的加载都需要保证列表是最新的，当然就缓存一页
        pager.setAdapter(mAdapter);
        mIndicator.setViewPager(pager);
        mIndicator.setCurrentItem(1);//设置启动首先显示的抢单界面
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case HANDLE_LOGIN_FAILURE:
                if (isVisiable) {
                    doLogin(userInfo);
                }
                break;
            default:
                break;
        }
        return false;
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
            if (isNetworkAvailable()) {
                mTcpClient.run();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferencesUtils.setParam(this, SharedPreferencesUtils.LOGIN_STATE, false);
        isOnline = false;

        try {
            mTcpClient.stopClient();
            conctTask.cancel(true);
            conctTask = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        mLocClient.stop();
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
            if (location == null)
                return;

            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();

            sendHeartBeat(locData);


//            Log.i("BaiduLocationApiDem", sb.toString());
        }

        private void sendHeartBeat(MyLocationData locData) {
            HeartBeatMessage msg = new HeartBeatMessage();
            msg.setCmd("hearbeat");
            msg.setStatus("1");
            msg.setLat(String.valueOf(locData.latitude));
            msg.setLng(String.valueOf(locData.longitude));
            msg.setSpeed(String.valueOf(locData.speed));

            SocketClient.getInstance().sendHeartBeat(msg, new ResponseHandler(Looper.myLooper()) {
                @Override
                public void onSuccess(String messageBody) {
                    Log.i("HeartBeat Response", messageBody);
                }

                @Override
                public void onFailure(String error) {
                    Log.i("HeartBeat Response", error);
                }

                @Override
                public void onTimeout() {
                    Log.i("HeartBeat", "Response Timeout");
                }
            });
        }
    }
}
