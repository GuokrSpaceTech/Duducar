package com.guokrspace.dududriver.net;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.common.Constants;
import com.guokrspace.dududriver.ui.PickUpPassengerActivity;
import com.guokrspace.dududriver.util.CommonUtil;

import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;

public class ChargeService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, PickUpPassengerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification noti = new Notification.Builder(this)
                .setContentTitle("嘟嘟提醒")
                .setContentText("请合理安排线路,感谢您提供的优质服务")
                .setSmallIcon(R.drawable.caricon)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(12346, noti);

        return Service.START_STICKY;
    }

    int times;
    double preDis;
    double secDistance;
    double tmpDistance;
    Timer calTimer;
    TimerTask calTimerTask;
    double preLat,preLng;
    int minutes;
    double curCharge = 0d;
    final int UPDATE_CHARGE = 0x101;
    public int getMinutes(){
        return minutes;
    }
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_CHARGE:
                    if(curCharge <=  CommonUtil.curPrice) {
                        curCharge = new BigDecimal(CommonUtil.curPrice).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
                        SocketClient.getInstance().sendCurrentChargeDetail(curCharge, CommonUtil.curDistance/1000, CommonUtil.curLowSpeedTime, new ResponseHandler(Looper.myLooper()) {
                            @Override
                            public void onSuccess(String messageBody) {
                            }
                            @Override
                            public void onFailure(String error) {
                                if(error.contains("not Order")){ // 订单异常
                                    sendBroadCast(Constants.SERVICE_ACTION_ORDER_NOT_EXISTS);
                                    stopCharging();
                                }
                            }
                            @Override
                            public void onTimeout() {
                            }
                        });
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        CommonUtil.curDistance = 0.0d;
        CommonUtil.curLowSpeedTime = 0;
        startCharging(CommonUtil.curBaseDistance, CommonUtil.curBaseLowTime);
    }

    public void startCharging(double baseDistance, int baseLowTime){
        Log.e("daddy", "start charge SERVICE");
        CommonUtil.curDistance = 0.0d + baseDistance;
        CommonUtil.curLowSpeedTime = 0 + baseLowTime;

        minutes = 0;
        preLat = CommonUtil.getCurLat();
        preLng = CommonUtil.getCurLng();

        times = 0;
        preDis = 0;
        secDistance=0d;
        tmpDistance=0d;
        calTimer = new Timer();
        calTimerTask = new TimerTask() {
            @Override
            public void run() {
                Log.e("daddy", "task is runnin as " + CommonUtil.getCurrentStatus());

                secDistance = DistanceUtil.getDistance(new LatLng(preLat, preLng), new LatLng(CommonUtil.getCurLat(), CommonUtil.getCurLng()));
                if ((secDistance * 1000 / (System.currentTimeMillis() - CommonUtil.getCurTime())) >= Constants.STRANGEDISTANCE) { //这一次的距离跳转异常
                    //drop it
                    tmpDistance += secDistance;
                    preDis = secDistance;
                } else {
                    tmpDistance += secDistance;
                    preDis = secDistance;
                }

                if (CommonUtil.getCurrentStatus() == Constants.STATUS_RUN) {//开车中
                    if (++times == 12) {//1 min
                        minutes++;
                        if (tmpDistance <= Constants.LOWSPEEDDISTANACE) {//这一分钟内是低速行驶
                            CommonUtil.curLowSpeedTime++;
                        }

                        CommonUtil.curDistance += tmpDistance;
                        tmpDistance = 0;
                        times = 0;
                    }

                    preLat = CommonUtil.getCurLat();
                    preLng = CommonUtil.getCurLng();
                    //TODO:通知界面更新,发送到乘客端
                    CommonUtil.curPrice = CommonUtil.countPrice(CommonUtil.curDistance, CommonUtil.curLowSpeedTime);
                    sendBroadCast(Constants.SERVICE_ACTION_UPDATE_CHARGE);
                    mHandler.sendEmptyMessage(UPDATE_CHARGE);
                } else { //非法状态下停止
                    stopCharging();
                }
            }
        };
        calTimer.schedule(calTimerTask, 1000, 5000);
    }

    public void stopCharging(){
        Log.e("daddy ", "stop charging service");
        if(calTimer != null){
            calTimer.cancel();
            calTimer = null;
        }
        if(calTimerTask != null){
            calTimerTask.cancel();
            calTimerTask = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private void sendBroadCast(String action){
        Intent intent = new Intent();
        intent.setAction(action);
        sendBroadcast(intent);
    }
}
