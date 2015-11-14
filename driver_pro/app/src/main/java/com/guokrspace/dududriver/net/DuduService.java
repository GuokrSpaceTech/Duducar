package com.guokrspace.dududriver.net;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.MyLocationData;
import com.guokrspace.dududriver.common.Constants;
import com.guokrspace.dududriver.net.message.HeartBeatMessage;
import com.guokrspace.dududriver.util.CommonUtil;
import com.guokrspace.dududriver.util.LogUtil;
import com.guokrspace.dududriver.util.SharedPreferencesUtils;

/*
* get baidu map location
* send heartbeat
* */
public class DuduService extends Service {

    private volatile int discard = 1; //Volatile修饰的成员变量在每次被线程访问时，都强迫从共享内存中重读该成员变量的值。

    public DuduService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化百度定位
        initLocation();
        //开始请求定位,发送心跳包
        mLocClient.start();
        mLocClient.requestLocation();
        LogUtil.i("DuduService location&heartbeat thread start", "Thread ID is  " + Thread.currentThread().getId());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        super.onDestroy();
        if(null != mLocClient){
            mLocClient.stop();
        }
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
            CommonUtil.setCurTime(System.currentTimeMillis());

            sendHeartBeat(curLocaData);
        }
    }

    //后台不断发送心跳包
    private void sendHeartBeat(MyLocationData locData) {
        HeartBeatMessage msg = new HeartBeatMessage();
        msg.setCmd("heartbeat");
        msg.setStatus(CommonUtil.getCurrentStatus());
        msg.setLat(String.valueOf(locData.latitude));
        msg.setLng(String.valueOf(locData.longitude));
        msg.setSpeed(String.valueOf(locData.speed));

        SocketClient.getInstance().sendHeartBeat(msg, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                Log.i("HeartBeat Response", messageBody);
                //将登陆状态置为true
                SharedPreferencesUtils.setParam(DuduService.this, SharedPreferencesUtils.LOGIN_STATE, true);
            }

            @Override
            public void onFailure(String error) {
                Log.i("HeartBeat Response", error);
                if(error.contains("login")){//登陆出现问题
                    //将登陆状态置为false
                    SharedPreferencesUtils.setParam(DuduService.this, SharedPreferencesUtils.LOGIN_STATE, false);
                    sendBroadCast(Constants.SERVICE_ACTION_RELOGIN);
                }
            }

            @Override
            public void onTimeout() {
                Log.i("HeartBeat", "Response Timeout");
                Toast.makeText(DuduService.this, "网络异常...", Toast.LENGTH_SHORT).show();
                //将登陆状态置为false
                SharedPreferencesUtils.setParam(DuduService.this, SharedPreferencesUtils.LOGIN_STATE, false);
            }
        });
    }

    private void sendBroadCast(String action){
        Intent intent = new Intent();
        intent.setAction(action);
        sendBroadcast(intent);
    }
}

