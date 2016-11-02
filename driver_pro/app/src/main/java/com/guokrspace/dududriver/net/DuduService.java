package com.guokrspace.dududriver.net;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.google.gson.Gson;
import com.guokrspace.dududriver.DuduDriverApplication;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.common.Constants;
import com.guokrspace.dududriver.database.PersonalInformation;
import com.guokrspace.dududriver.model.OrderItem;
import com.guokrspace.dududriver.net.message.HeartBeatMessage;
import com.guokrspace.dududriver.net.message.MessageTag;
import com.guokrspace.dududriver.ui.PickUpPassengerActivity;
import com.guokrspace.dududriver.util.CommonUtil;
import com.guokrspace.dududriver.util.LogUtil;
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

    private long preBeat = 0;
    private Timer heartBeatTimer;
    private TimerTask heartBeatTask;

    private StopServiceReceiver mStopReceiver;

    public static final String STOP_SERVICE = "dudu.action.stop_service";


    public DuduService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化百度定位
        initLocation();
        preBeat = System.currentTimeMillis();
        //开始请求定位
        mLocClient.start();
        mLocClient.requestLocation();


        //停止服务广播接收器
        mStopReceiver = new StopServiceReceiver();
        IntentFilter filter = new IntentFilter(STOP_SERVICE);
        filter.setPriority(1000);
        registerReceiver(mStopReceiver, filter);



        //注册网络状态监听
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, mFilter);
        preTime = System.currentTimeMillis();
        LogUtil.e("daddy", "service create");
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

        Intent notificationIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification noti;
        if (Build.VERSION.SDK_INT < 16) {
            noti  = new Notification.Builder(this)
                    .setContentTitle("嘟嘟播报")
                    .setContentText("记得每日签到, 高级司机会获得更高的分成比例!")
                    .setSmallIcon(R.drawable.caricon)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true).getNotification();
        } else {
            noti = new Notification.Builder(this)
                    .setContentTitle("嘟嘟播报")
                    .setContentText("记得每日签到, 高级司机会获得更高的分成比例!")
                    .setSmallIcon(R.drawable.caricon)
                    .setContentIntent(pendingIntent)
                    .setOnlyAlertOnce(true)
                    .build();
        }
        startForeground(12349, noti);

        LogUtil.e("daady", "start command");
        if(mTcpClient == null){
            conctTask = new connectTask(); //Connect to server
            conctTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

//        if(heartBeatTimer != null){
//            heartBeatTimer.cancel();
//        }
//        if(heartBeatTask != null){
//            heartBeatTask.cancel();
//        }
//        heartBeatTimer = new Timer();
//        heartBeatTask = new TimerTask() {
//            @Override
//            public void run() {
//                sendHeartBeat(Looper.myLooper());
//            }
//        };
        //发送心跳包 5s一次
//        LogUtil.e("daddy service", "start heart beat" );
//        heartBeatTimer.schedule(heartBeatTask, 1000, 5 * 1000);

        pullOrder();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(), "service stoped", Toast.LENGTH_SHORT).show();
        if(isRunningApp(getApplicationContext())){
            // 应用正常运行, 服务意外终止, 重新启动
            startService(new Intent(getApplicationContext(), DuduService.class));
            return;
        }
        super.onDestroy();

        unregisterReceiver(mReceiver);
        unregisterReceiver(mStopReceiver);
        mStopReceiver = null;

        if(null != mLocClient){
            mLocClient.stop();
        }
        LogUtil.e("daddy", "service done" );
        try {
            mTcpClient.stopClient();
            conctTask.cancel(true);
            conctTask = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if(heartBeatTimer != null){
//            heartBeatTimer.cancel();
//            heartBeatTimer = null;
//        }
//        if(heartBeatTask != null){
//            heartBeatTask.cancel();
//            heartBeatTask = null;
//        }

        CommonUtil.setIsServiceOn(false);
    }

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
            CommonUtil.setCurSpeed(curLocaData.speed + "");
            CommonUtil.setCurDirction(curLocaData.direction);
            if(!isDuduServiceRunning()){
                LogUtil.e("daddy service ", " dudu service is stopped and restarting");
                startService(new Intent(getApplicationContext(), DuduService.class));
            }
            if(System.currentTimeMillis() - preBeat >= 4 * 1000){ // 5s一次发送heartbeat
                sendHeartBeat(Looper.myLooper());
                preBeat = System.currentTimeMillis();
            }
            LogUtil.e("service", "current location update");
        }
    }

    private void pullOrder() {
        //注册派单监听
        SocketClient.getInstance().registerServerMessageHandler(MessageTag.PATCH_ORDER, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                LogUtil.e("confirm order handler");
                OrderItem orderItem = new Gson().fromJson(messageBody, OrderItem.class);
                LogUtil.e("daddy duduservice >>> new order come");
                if (CommonUtil.getCurOrderItem() != null) {
                    if (CommonUtil.getCurOrderItem().getOrder().getId().equals(orderItem.getOrder().getId())) {
                        LogUtil.e("daddy error>>> dudu service the same order");
                        //同一个单
                        return;
                    }
                }
                if (CommonUtil.getCurrentStatus() != Constants.STATUS_WAIT) {
                    LogUtil.e("daddy error>>> dudu service not right status");
                    return;
                }
                CommonUtil.setCurOrderItem(orderItem);
                LogUtil.e(messageBody + "  " + orderItem.getCMD() + " " + orderItem.getOrder().getDestination_lat() + "::" + orderItem.getOrder().getDestination_lng());
                //向主界面发送广播
                sendBroadCast(Constants.SERVICE_ACTION_NEW_ORDER);
            }

            @Override
            public void onFailure(String error) {
                LogUtil.e("register order handler error");
            }

            @Override
            public void onTimeout() {
                LogUtil.e("register order handler time out");
            }
        });

    }

    //后台不断发送心跳包
    private void sendHeartBeat(Looper looper) {
        if(!isRunningApp(getApplicationContext())){
            // 程序完全退出
            LogUtil.e("DuduService >>> service stop self");
            stopSelf();
            return;
        }
        HeartBeatMessage msg = new HeartBeatMessage();
        msg.setCmd("heartbeat");
        msg.setStatus(CommonUtil.getCurrentStatus());
        msg.setLat(String.valueOf(CommonUtil.getCurLat()));
        msg.setLng(String.valueOf(CommonUtil.getCurLng()));
        msg.setSpeed(String.valueOf(CommonUtil.getCurSpeed()));
        msg.setDirection(String.valueOf(CommonUtil.getCurDirction()));

        SocketClient.getInstance().sendHeartBeat(msg, new ResponseHandler(looper) {
            @Override
            public void onSuccess(String messageBody) {
                LogUtil.i("HeartBeat Response", messageBody);
                //将登陆状态置为true
                SharedPreferencesUtils.setParam(DuduService.this, SharedPreferencesUtils.LOGIN_STATE, true);
                if(PickUpPassengerActivity.isNetworkOut){
                    sendBroadCast(Constants.SERVICE_ACTION_NEWWORK_RECONNET);
                }

                if(!discard){
                    registerDuduMessageListener();
                    discard = true;
                }
            }

            @Override
            public void onFailure(String error) {
                LogUtil.e("HeartBeat Response", error);
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
                    if(localUsers != null && localUsers.size() > 0 && ((PersonalInformation)localUsers.get(0)).getToken() != null){
                        PersonalInformation user = (PersonalInformation) localUsers.get(0);
                        SocketClient.getInstance().autoLoginRequest(user.getMobile(), "1", user.getToken(), new ResponseHandler(Looper.myLooper()) {
                            @Override
                            public void onSuccess(String messageBody) {
                                LogUtil.e("login in service: ", "duduservice sucess");
                                SharedPreferencesUtils.setParam(DuduService.this, SharedPreferencesUtils.LOGIN_STATE, true);
                                pullOrder();
                            }

                            @Override
                            public void onFailure(String error) { LogUtil.e("login in failure!", "errorbody " + error); }

                            @Override
                            public void onTimeout() {
                                LogUtil.e("hyman", "登陆超时");
                            }
                        });
                    }
                    //向主界面发送广播
                    sendBroadCast(Constants.SERVICE_ACTION_RELOGIN);

                }
            }

            @Override
            public void onTimeout() {
                LogUtil.e("HeartBeat", "Response Timeout");
                if(System.currentTimeMillis() - preTime < 1000 * 10){ // 连续超时
                    currTimeOut ++;
                } else {
                    currTimeOut = 0;
                }
                preTime = System.currentTimeMillis();
                if(currTimeOut > 3){//5次心跳超时, 默认断开连接 ,尝试重连
                    reConnectServer();
                    currTimeOut = 0;
                }
                LogUtil.e("daddy heart beat", "time out " + currTimeOut);
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
        } else {
            conctTask = new connectTask(); //Connect to server
            conctTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                    //网络连接
                    int type = netInfo.getType();
                    //网络连接切换, 一般重新连接
                    if(type == ConnectivityManager.TYPE_WIFI){
                        //WiFi网络
                    }else if(type == ConnectivityManager.TYPE_ETHERNET){

                    }else if(type == ConnectivityManager.TYPE_MOBILE){
                        //3g网络
                    }
                    reConnectServer();
                    sendBroadCast(Constants.SERVICE_ACTION_NEWWORK_RECONNET);
                } else {
                    //网络断开
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
        sendOrderedBroadcast(intent, null);
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
            mTcpClient.getInstance().run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }

    private void registerDuduMessageListener(){

        //TODO: 监听服务器新消息的通知
        LogUtil.e("register new message");
        mTcpClient.registerServerMessageHandler(MessageTag.NEW_MESSAGE, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                // 广播通知
                LogUtil.e("got a new message notice");
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

    public static boolean isRunningApp(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
        for (ActivityManager.RunningTaskInfo info : list) {
            if (info.baseActivity.getPackageName().equals(Constants.PACKAGE_NAME)) {
                return true;
            }
        }
        return false;
    }

    /*
     *  该广播接收器用于关闭service
     *
     */
    private class StopServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopSelf();
        }
    }

    protected boolean isDuduServiceRunning(){
        ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service :manager.getRunningServices(Integer.MAX_VALUE)) {
            if(service.service.getClassName().contains(".net.DuduService")){// service 没死
                return true;
            }
        }
        return false;
    }

}

