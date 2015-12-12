package com.guokrspace.duducar.communication;

/**
 * Created by daddyfang on 15/11/13.
 */

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
import com.guokrspace.duducar.DuduApplication;
import com.guokrspace.duducar.common.Constants;
import com.guokrspace.duducar.database.CommonUtil;
import com.guokrspace.duducar.database.PersonalInformation;

import java.util.List;

/*
* get baidu map location
* send heartbeat
* */
public class DuduService extends Service {

    private SocketClient mTcpClient = null;
    private connectTask conctTask = null;
    private DuduApplication mApplication;

    public DuduService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化百度定位
        Log.e("daddy," , "oncreate service");
        initLocation();
        mLocClient.start();
        mLocClient.requestLocation();

        //注册网络状态监听
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, mFilter);

        mApplication = (DuduApplication) getApplicationContext();
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
        Log.e("daddy", "service start");
        if(mTcpClient == null){
            mTcpClient = null;
            conctTask = new connectTask(); //Connect to server
            conctTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        return START_STICKY;
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
                    String name = netInfo.getTypeName();
                    //网络连接切换, 一般重新连接
                    if(netInfo.getType()==ConnectivityManager.TYPE_WIFI){
                        /////WiFi网络
                    }else if(netInfo.getType()==ConnectivityManager.TYPE_ETHERNET){

                    }else if(netInfo.getType()==ConnectivityManager.TYPE_MOBILE){
                        /////////3g网络
                    }
                    reConnectServer();
                } else {
                    ////////网络断开
                    Toast.makeText(DuduService.this, "网络连接断开...", Toast.LENGTH_SHORT).show();
                    //TODO 网络断开
                }
            }
        }
    };


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

        unregisterReceiver(mReceiver);

        try {
            mTcpClient.stopClient();
            conctTask.cancel(true);
            conctTask = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //后台实时获取地址

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
            CommonUtil.setLocationSuccess(true);
            CommonUtil.setCurTime(System.currentTimeMillis());
//
            sendHeartBeat(curLocaData);
        }
    }

    //后台不断发送心跳包
    private void sendHeartBeat(MyLocationData locData) {
        HeartBeatMessage msg = new HeartBeatMessage();
        msg.setCmd("heartbeat");
        msg.setLat(String.valueOf(locData.latitude));
        msg.setLng(String.valueOf(locData.longitude));

        SocketClient.getInstance().sendHeartBeat(msg, Constants.PASSENGER_ROLE, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                Log.i("HeartBeat Response", messageBody);
                if(messageBody.contains("login")){//登陆出现问题
                    //将登陆状态置为false
                    Log.e("daddy", "login eror in success");
                    sendBroadCast(Constants.SERVICE_ACTION_RELOGIN);

                    List persons = mApplication.mDaoSession.getPersonalInformationDao().queryBuilder().limit(1).list();
                    if(persons.size() > 0){ // 重新登陆连接
                        PersonalInformation person = (PersonalInformation)persons.get(0);
                        SocketClient.getInstance().sendLoginReguest(person.getMobile(), Constants.PASSENGER_ROLE, person.getToken(), new ResponseHandler(Looper.getMainLooper()) {
                            @Override
                            public void onSuccess(String messageBody) {
                                Log.e("daddy login ", "success");
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

            @Override
            public void onFailure(String error) {
                Log.i("HeartBeat Response", error);
                if(error.contains("login")){//登陆出现问题
                    //将登陆状态置为false
                    Log.e("daddy", "login eror in error");
                    sendBroadCast(Constants.SERVICE_ACTION_RELOGIN);
                }
            }

            @Override
            public void onTimeout() {
                Log.i("HeartBeat", "Response Timeout");
//                Toast.makeText(DuduService.this, "网络异常...", Toast.LENGTH_SHORT).show();
                //将登陆状态置为false
//                SharedPreferencesUtils.setParam(DuduService.this, SharedPreferencesUtils.LOGIN_STATE, false);
            }
        });
    }

    private void sendBroadCast(String action){
        Intent intent = new Intent();
        intent.setAction(action);
        sendBroadcast(intent);
    }

    public class HeartBeatMessage{
        String cmd;
        String lat;
        String lng;

        public String getCmd() {
            return cmd;
        }

        public void setCmd(String cmd) {
            this.cmd = cmd;
        }

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public String getLng() {
            return lng;
        }

        public void setLng(String lng) {
            this.lng = lng;
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
}


