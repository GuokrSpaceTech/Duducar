package com.guokrspace.dududriver.net;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.guokrspace.dududriver.DuduDriverApplication;
import com.guokrspace.dududriver.common.Constants;
import com.guokrspace.dududriver.database.PersonalInformation;
import com.guokrspace.dududriver.model.OrderItem;
import com.guokrspace.dududriver.net.message.HeartBeatMessage;
import com.guokrspace.dududriver.net.message.MessageTag;
import com.guokrspace.dududriver.util.CommonUtil;
import com.guokrspace.dududriver.util.FastJsonTools;
import com.guokrspace.dududriver.util.SharedPreferencesUtils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/*
* get baidu map location
* send heartbeat
* */
public class DuduService extends Service {

    private volatile boolean discard = false; //Volatile修饰的成员变量在每次被线程访问时，都强迫从共享内存中重读该成员变量的值。
    private volatile SocketClient mTcpClient = null;
    private volatile connectTask conctTask = null;
    private volatile int connectDelay = 1;
    // 记录上次超时的时间, 时间间隔超过1分钟重新计时
    private volatile long preTime = 0;
    private volatile int currTimeOut = 0;

    private Timer heartBeatTimer;
    private TimerTask heartBeatTask;

    public DuduService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化百度定位
        initLocation();
        //开始请求定位
        mLocClient.start();
        mLocClient.requestLocation();
        final Looper looper = Looper.myLooper();
        heartBeatTimer = new Timer();
        heartBeatTask = new TimerTask() {
            @Override
            public void run() {
                sendHeartBeat(looper);
            }
        };
        //,发送心跳包 5s一次
        heartBeatTimer.schedule(heartBeatTask, 1000, 5000);

        //注册网络状态监听
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, mFilter);
        preTime = System.currentTimeMillis();
        Log.e("daddy", "service create");
        CommonUtil.setIsServiceOn(true);
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        // If we get killed, after returning from here, restart
         /*
         * Init the SocketClient
         */
        Log.e("daady", "start command");
        if(mTcpClient == null){
            conctTask = new connectTask(); //Connect to server
            conctTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
//        pullOrder();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(), "service done", Toast.LENGTH_SHORT).show();
        super.onDestroy();

        unregisterReceiver(mReceiver);

        if(null != mLocClient){
            mLocClient.stop();
        }
        Log.e("daddy", "service done" );
        try {
            mTcpClient.stopClient();
            conctTask.cancel(true);
            conctTask = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(heartBeatTimer != null){
            heartBeatTimer.cancel();
            heartBeatTimer = null;
        }
        if(heartBeatTask != null){
            heartBeatTask.cancel();
            heartBeatTask = null;
        }

        CommonUtil.setIsServiceOn(false);
    }

    /////////////
    //后台实时获取地址

    //一分钟计算一次单段走过的距离百度distance, 计算是否低速, 计算价格

    private LocationClient mLocClient;
    private MyLocationListener myListener;

    private void initLocation() {

        myListener = new MyLocationListener();
        //在主线程进行定位
        mLocClient = new LocationClient(getApplicationContext());
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        option.setScanSpan(5000);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        option.setPriority(LocationClientOption.GpsFirst);
        mLocClient.setLocOption(option);
        mLocClient.registerLocationListener(myListener);
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {

            // map view 销毁后不在处理新接收的位置
            if (location == null){
                return;
            }

            MyLocationData curLocaData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();

            //修改现在状态
            CommonUtil.setCurLng(curLocaData.longitude);
            CommonUtil.setCurLat(curLocaData.latitude);
            CommonUtil.setCurLatLng(new LatLng(curLocaData.latitude, curLocaData.longitude));
            CommonUtil.setCurAddress(location.getAddrStr());
            CommonUtil.setCurAddressDescription(location.getLocationDescribe());
            CommonUtil.setCurTime(System.currentTimeMillis());
            CommonUtil.setCurSpeed(curLocaData.speed+"");



        }
    }

    private void pullOrder() {
        //注册派单监听
        SocketClient.getInstance().registerServerMessageHandler(MessageTag.PATCH_ORDER, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                Log.e("Mainactivity", "confirm order handler");
                CommonUtil.setCurOrderItem(FastJsonTools.getObject(messageBody, OrderItem.class));
//                Log.e("Daddy ", messageBody + "  " + orderItem.getCMD() + " " + orderItem.getOrder().getDestination_lat() + "::" + orderItem.getOrder().getDestination_lng());
                //向主界面发送广播
                sendBroadCast(Constants.SERVICE_ACTION_NEW_ORDER);
            }

            @Override
            public void onFailure(String error) {
                Log.e("Mainactivity", "register order handler error");
            }

            @Override
            public void onTimeout() {
                Log.e("Mainactivity", "register order handler time out");
            }
        });

    }

    //后台不断发送心跳包
    private void sendHeartBeat(Looper looper) {
        if(!isRunningApp(getApplicationContext())){
            // 程序完全退出
            stopSelf();
            return;
        }
        HeartBeatMessage msg = new HeartBeatMessage();
        msg.setCmd("heartbeat");
        msg.setStatus(CommonUtil.getCurrentStatus());
        msg.setLat(String.valueOf(CommonUtil.getCurLat()));
        msg.setLng(String.valueOf(CommonUtil.getCurLng()));
        msg.setSpeed(String.valueOf(CommonUtil.getCurSpeed()));

        SocketClient.getInstance().sendHeartBeat(msg, new ResponseHandler(looper) {
            @Override
            public void onSuccess(String messageBody) {
                Log.i("HeartBeat Response", messageBody);
                //将登陆状态置为true
                SharedPreferencesUtils.setParam(DuduService.this, SharedPreferencesUtils.LOGIN_STATE, true);
                if(!discard){
                    registerDuduMessageListener();
                    discard = true;
                }
            }

            @Override
            public void onFailure(String error) {
                Log.i("HeartBeat Response", error);
                if(error.contains("login")){//登陆出现问题
                    //将登陆状态置为false
                    SharedPreferencesUtils.setParam(DuduService.this, SharedPreferencesUtils.LOGIN_STATE, false);
                    //后台尝试登录
                    if(!isRunningApp(getApplicationContext())){
                        //程序在后台执行,不需要进行登录操作
                        return;
                    }
                    List localUsers = DuduDriverApplication.getInstance().
                            mDaoSession.getPersonalInformationDao().
                            queryBuilder().list();
                    if(localUsers != null && localUsers.size() > 0){
                        PersonalInformation user = (PersonalInformation) localUsers.get(0);
                        SocketClient.getInstance().autoLoginRequest(user.getMobile(), "1", user.getToken(), new ResponseHandler(Looper.myLooper()) {
                            @Override
                            public void onSuccess(String messageBody) {
                                SharedPreferencesUtils.setParam(DuduService.this, SharedPreferencesUtils.LOGIN_STATE, true);
                                pullOrder();
                            }

                            @Override
                            public void onFailure(String error) {
                                Log.e("login in failure!", "errorbody " + error);
                            }

                            @Override
                            public void onTimeout() {
                                Log.e("hyman", "登陆超时");
                            }
                        });
                    }
                    //向主界面发送广播
                    sendBroadCast(Constants.SERVICE_ACTION_RELOGIN);

                }
            }

            @Override
            public void onTimeout() {
                Log.i("HeartBeat", "Response Timeout");
                if(System.currentTimeMillis() - preTime < 1000 * 10){ // 连续超时
                    currTimeOut ++;
                } else {
                    currTimeOut = 0;
                }
                preTime = System.currentTimeMillis();
                if(currTimeOut > 5){//5次心跳超时, 默认断开连接 ,尝试重连
                    reConnectServer();
                }
                Log.e("daddy heart beat", "time out " + currTimeOut);
//                if(SocketClient.getInstance().getSocket().)
                //将登陆状态置为false
                SharedPreferencesUtils.setParam(DuduService.this, SharedPreferencesUtils.LOGIN_STATE, false);
            }
        });
    }

    private void reConnectServer(){
        if(mTcpClient == null || mTcpClient.getSocket() == null){//初次连接

            conctTask = new connectTask(); //Connect to server
            conctTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } else if(mTcpClient.getSocket().isClosed()
                || !mTcpClient.getSocket().isConnected()) {
            try {
                mTcpClient.stopClient();
                conctTask.cancel(true);
                conctTask = null;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conctTask = new connectTask(); //Connect to server
                conctTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        pullOrder();
    }

    //注册监听网络状态改变的广播
    private ConnectivityManager mConnectivityManager;
    private NetworkInfo netInfo;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

                mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                netInfo = mConnectivityManager.getActiveNetworkInfo();
                if(netInfo != null && netInfo.isAvailable()) {
                    /////////////网络连接
                    int type = netInfo.getType();
                    //网络连接切换, 一般重新连接
                    if(type == ConnectivityManager.TYPE_WIFI){
                        /////WiFi网络
                    }else if(type == ConnectivityManager.TYPE_ETHERNET){

                    }else if(type == ConnectivityManager.TYPE_MOBILE){
                        /////////3g网络
                    }
                    reConnectServer();
                } else {
                    ////////网络断开
                    Toast.makeText(DuduService.this, "网络连接断开...", Toast.LENGTH_SHORT).show();
                    sendBroadCast(Constants.SERVICE_ACTION_NETWORK_OUT);
                    //TODO 网络断开
                }
            }
        }
    };

    private void sendBroadCast(String action){
        Intent intent = new Intent();
        intent.setAction(action);
        sendBroadcast(intent);
    }

    /**
     * @author Prashant Adesara
     *         receive the message from server with asyncTask
     */
    public class connectTask extends AsyncTask<String, String, SocketClient> {
        @Override
        protected SocketClient doInBackground(String... message) {
            //we create a TCPClient object and
//            mTcpClient = new SocketClient();
            if(mTcpClient == null){
                mTcpClient = new SocketClient();
            }
            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }

    private void registerDuduMessageListener(){

        //TODO: 监听服务器新消息的通知
        Log.e("daddy message", "register new message");
        mTcpClient.registerServerMessageHandler(MessageTag.NEW_MESSAGE, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                // 广播通知
                Log.e("daddy message", "got a new message notice");
                sendBroadCast(Constants.SERVICE_ACTION_MESAGE);
            }

            @Override
            public void onFailure(String error) {

            }

            @Override
            public void onTimeout() {

            }
        });
    }

    public boolean isRunningApp(Context context) {
        boolean isAppRunning = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
        for (ActivityManager.RunningTaskInfo info : list) {
            if (info.topActivity.getPackageName().equals(Constants.PACKAGE_NAME) && info.baseActivity.getPackageName().equals(Constants.PACKAGE_NAME)) {
                isAppRunning = true;
                // find it, break
                break;
            }
        }
        return isAppRunning;
    }
}

