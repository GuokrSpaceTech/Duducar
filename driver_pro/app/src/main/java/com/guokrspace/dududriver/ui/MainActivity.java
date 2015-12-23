package com.guokrspace.dududriver.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.gc.materialdesign.widgets.ProgressDialog;
import com.google.gson.Gson;
import com.guokrspace.dududriver.DuduDriverApplication;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.adapter.TabPagerAdapter;
import com.guokrspace.dududriver.common.Constants;
import com.guokrspace.dududriver.common.VoiceCommand;
import com.guokrspace.dududriver.database.PersonalInformation;
import com.guokrspace.dududriver.model.BaseInfo;
import com.guokrspace.dududriver.model.OrderItem;
import com.guokrspace.dududriver.net.DuduService;
import com.guokrspace.dududriver.net.ResponseHandler;
import com.guokrspace.dududriver.net.SocketClient;
import com.guokrspace.dududriver.net.message.MessageTag;
import com.guokrspace.dududriver.util.AppExitUtil;
import com.guokrspace.dududriver.util.CommonUtil;
import com.guokrspace.dududriver.util.DisplayUtil;
import com.guokrspace.dududriver.util.FastJsonTools;
import com.guokrspace.dududriver.util.LogUtil;
import com.guokrspace.dududriver.util.SharedPreferencesUtils;
import com.guokrspace.dududriver.util.VoiceUtil;
import com.guokrspace.dududriver.view.ListenProgressView;
import com.viewpagerindicator.TabPageIndicator;

import org.json.JSONObject;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by hyman on 15/10/22.
 */
public class MainActivity extends BaseActivity implements Handler.Callback {

    @Bind(R.id.pattern_btn)
    Button btnPattern;
    @OnClick(R.id.pattern_btn) public void changePattern(){
        //   voice guide on/off
    }
    private Context context;

    private ViewPager pager;
    private TabPagerAdapter mAdapter;
    private TabPageIndicator mIndicator;

    private MainOrderDialog dialog = null;

    private View buttonGroup;
    private ListenProgressView listenProgressView;
    private Button btnOver;



    private boolean isOnline = false;
    private boolean isVisiable = false;
    private boolean isListeneing = false;
    private boolean isOverButtonVisiable = false;
    private long lastOrderTime;

    private Handler mHandler;
    private Intent duduService;
    private ServiceReceiver receiver;
    private ServiceReceiver messageReceiver;

    private static final int HANDLE_LOGIN_FAILURE = 100;
    private static final int NEW_ORDER_ARRIVE = 101;
    private static final int HANDLE_BASEINFO = 103;
    private static final int ORDER_CANCELED = 104;
    private static final int ADJUST_STATUS = 105;
    private static final int UPDATE_GRABORDER = 106;
    //TODO:OTHER thing

    private PersonalInformation userInfo;

//    private OrderItem orderItem = null;
    private BaseInfo baseInfo;

    //所有登录判断以及操作都将在Service中进行执行

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        context = this;

        initView();

        AppExitUtil.getInstance().addActivity(this);
        mHandler = new Handler(this);

        /*
         * Start Location & Send Heartbeat  Service
         */
        duduService = new Intent(getBaseContext(), DuduService.class);
        startService(duduService);

        List localUsers = DuduDriverApplication.getInstance().
                mDaoSession.getPersonalInformationDao().
                queryBuilder().list();
        if (localUsers != null && localUsers.size() > 0) {
            userInfo = (PersonalInformation) localUsers.get(0);
            if(!CommonUtil.isServiceOn()){
                startService(duduService);
            }
//            doLogin(userInfo);
        } else {
            //用户信息不存在,重新注册页面
            startActivity(new Intent(this, LoginActivity.class));
//            finish();
        }
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
        if (DuduDriverApplication.getInstance().initPersonalInformation()) {
            if (!isNetworkAvailable()) {
                showToast("网络不可用，现在是离线状态");
            }
        }

        if(!CommonUtil.isServiceOn()){
            startService(duduService);
        }

        registerBroadcastReceiver();

        /*
        * 调整circle状态
        * */
        mHandler.sendEmptyMessage(ADJUST_STATUS);
        /*
        * 更新每天的记录
        * */
//        CommonUtil.updateToday();
        mHandler.sendEmptyMessage(UPDATE_GRABORDER);

//        if(isOnline){
//            pullOrder();
//        }
        pullBaseInfo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisiable = false;
//        CommonUtil.changeCurStatus(Constants.STATUS_HOLD);
        unregisterReceiver(receiver);
        mHandler.removeMessages(HANDLE_LOGIN_FAILURE);
    }


    //监听service传来的消息
    private void registerBroadcastReceiver(){
        receiver = new ServiceReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.SERVICE_ACTION_RELOGIN);
        filter.setPriority(1000);
        registerReceiver(receiver, filter);

        messageReceiver = new ServiceReceiver();
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(Constants.SERVICE_ACTION_MESAGE);
        mFilter.addAction(Constants.SERVICE_ACTION_NEW_ORDER);
        mFilter.addAction(Constants.SERVICE_ACTION_NETWORK_OUT);
        mFilter.setPriority(1000);
        registerReceiver(messageReceiver, mFilter);
    }
    //进行自动登陆
    private void doLogin(PersonalInformation user, final boolean recover) {
        if (user == null) {
            return;
        }
        Log.e("daddy", user.getMobile() + " " + user.getToken() + " " + user.getId());
        SocketClient.getInstance().autoLoginRequest(user.getMobile(), "1", user.getToken(), new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {

                showCustomToast("登陆成功");
                Log.e("login in success!", "messageBody" + messageBody);
                SharedPreferencesUtils.setParam(MainActivity.this, SharedPreferencesUtils.LOGIN_STATE, true);

                pullBaseInfo();
//                pullOrder();
                mHandler.sendEmptyMessage(MessageTag.MESSAGE_UPDATE_MESSAGE);
                isOnline = true;
                if(!recover){
                    return;
                }
                try {
                    JSONObject object = new JSONObject(messageBody);
                    String active = (String) object.get("has_active_order");
                    if (active.equals("1")) {//存在正在执行的订单
                        String status = (String) object.get("order_status");
                        String orderDetail = (String) object.get("active_order");
                        if (status.equals("1")) {//发送放弃接单消息给服务器
                            //TODO
                        } else if (status.equals("6")) {//订单已经取消
                            //TODO
                        } else if (status.equals("2")) {//接单去接乘客
                            ProgressDialog dialog = new ProgressDialog(MainActivity.this, "检测到异常退出");
                            VoiceUtil.startSpeaking(VoiceCommand.LAST_TIME_EXIT_EXCEPTION);
                            dialog.setCanceledOnTouchOutside(false);
                            dialog.show();
                            OrderItem orderItem = FastJsonTools.getObject(orderDetail, OrderItem.class);
                            Intent intent = new Intent(MainActivity.this, PickUpPassengerActivity.class);
                            intent.putExtra("orderItem", orderItem);
                            intent.putExtra("isRecover", false);
                            startActivity(intent);
                            dialog.dismiss();
                            //选择界面不听单
                            CommonUtil.changeCurStatus(Constants.STATUS_GET);
                        } else if (status.equals("3")) {//去送乘客的路上
                            if(SharedPreferencesUtils.getParam(MainActivity.this, Constants.PREFERENCE_KEY_ORDER_STATUS, Constants.STATUS_RUN).equals(Constants.STATUS_REACH)){
                                ProgressDialog dialog = new ProgressDialog(MainActivity.this, "还有账单未发送");
                                VoiceUtil.startSpeaking(VoiceCommand.LAST_TIME_BILL_NOT_SENT);
                                dialog.show();
                                dialog.setCanceledOnTouchOutside(false);
                                OrderItem orderItem = FastJsonTools.getObject(orderDetail, OrderItem.class);
                                Intent intent = new Intent(MainActivity.this, ConfirmBillActivity.class);
                                JSONObject lastCharge = new JSONObject((String)object.get("last_charge"));
                                intent.putExtra("orderItem", orderItem);
                                intent.putExtra("lowspeed", Integer.parseInt((String) lastCharge.get("low_speed_time")));
                                intent.putExtra("mileage", Double.parseDouble((String) lastCharge.get("current_mile")));
                                startActivity(intent);
                                CommonUtil.changeCurStatus(Constants.STATUS_RUN);
                                dialog.dismiss();
                            } else {
                                ProgressDialog dialog = new ProgressDialog(MainActivity.this, "检测到未完结的订单");
                                VoiceUtil.startSpeaking(VoiceCommand.LAST_TIME_ORDER_NOT_END);
                                dialog.setCanceledOnTouchOutside(false);
                                dialog.show();
                                OrderItem orderItem = FastJsonTools.getObject(orderDetail, OrderItem.class);
                                Intent intent = new Intent(MainActivity.this, PickUpPassengerActivity.class);
                                intent.putExtra("orderItem", orderItem);
                                intent.putExtra("isRecover", true);
                                intent.putExtra("lastCharge", (String) object.get("last_charge"));
                                startActivity(intent);
                                CommonUtil.changeCurStatus(Constants.STATUS_RUN);
                                dialog.dismiss();
                            }
                        } else if (status.equals("4")) {//已完成订单未支付
                            //TODO
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String error) {
                showCustomToast("登陆失败, 请重新登陆");
                Log.e("login in failure!", "errorbody " + error);
                isOnline = false;
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
//                finish();
            }

            @Override
            public void onTimeout() {
                Log.e("hyman", "登陆超时");
                isOnline = false;
                mHandler.sendMessageDelayed(mHandler.obtainMessage(HANDLE_LOGIN_FAILURE), 10000);
            }
        });
    }

//    private void pullOrder() {
//        //注册派单监听
//        SocketClient.getInstance().registerServerMessageHandler(MessageTag.PATCH_ORDER, new ResponseHandler(Looper.myLooper()) {
//            @Override
//            public void onSuccess(String messageBody) {
//                Log.e("Mainactivity", "confirm order handler");
//                orderItem = FastJsonTools.getObject(messageBody, OrderItem.class);
//                Log.e("Daddy ", messageBody + "  " + orderItem.getCMD() + " " + orderItem.getOrder().getDestination_lat() + "::" + orderItem.getOrder().getDestination_lng());
//                mHandler.sendEmptyMessage(NEW_ORDER_ARRIVE);
//            }
//
//            @Override
//            public void onFailure(String error) {
//                Log.e("Mainactivity", "register order handler error");
//            }
//
//            @Override
//            public void onTimeout() {
//                Log.e("Mainactivity", "register order handler time out");
//            }
//        });
//
//    }

    private void pullBaseInfo(){

        SocketClient.getInstance().pullBaseInfo(new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                Log.e("daddy_baseinfo", "base info " + messageBody);
                baseInfo = (BaseInfo) new Gson().fromJson(messageBody, BaseInfo.class);
//                Log.e("daddy", "base" + baseInfo.getWebivew().getAbout().length());
                mHandler.sendEmptyMessage(HANDLE_BASEINFO);
                Log.e("daddy", "send message");
            }

            @Override
            public void onFailure(String error) {
                //返回基础信息失败,
                Log.e("daddy", "error " + error);
            }

            @Override
            public void onTimeout() {
                //超时 重试
                Log.e("daddy", "time out ");
                pullBaseInfo();
            }
        });

    }

    private void initView() {
        mIndicator = (TabPageIndicator) findViewById(R.id.indicator);

        pager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new TabPagerAdapter(getSupportFragmentManager());
        pager.setOffscreenPageLimit(3);//设置预加载页数
        //要求每次的加载都需要保证列表是最新的，当然就缓存一页
        pager.setAdapter(mAdapter);
        mIndicator.setViewPager(pager);
        mIndicator.setCurrentItem(1);//设置启动首先显示的抢单界面

        buttonGroup = (View) findViewById(R.id.button_group_layout);
        listenProgressView = (ListenProgressView) buttonGroup.findViewById(R.id.listenprogressview);

        listenProgressView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.e("daddy", "start is listener" + isListeneing);
                isListeneing = !isListeneing;

                String currStatus = CommonUtil.getCurrentStatus();
                if (currStatus == Constants.STATUS_RUN || currStatus == Constants.STATUS_DEAL
                        || currStatus == Constants.STATUS_GET) {
                    //error stats
                    LogUtil.e("MainActivity ", "runing can not click the button ");
                }

                if (isListeneing) {
                    if (!CommonUtil.isNetworkAvailable(context)) {
                        CommonUtil.changeCurStatus(Constants.STATUS_HOLD);
                        VoiceUtil.startSpeaking(VoiceCommand.NETWORK_NOT_AVAILABLE);
                        isListeneing = !isListeneing;
                        return false;
                    }
                    if (!SocketClient.getInstance().getSocket().isConnected() || SocketClient.getInstance().getSocket().isClosed()) {
                        // socket 断网
                        CommonUtil.changeCurStatus(Constants.STATUS_HOLD);
                        VoiceUtil.startSpeaking(VoiceCommand.CONNECT_SERVER);
                        isListeneing = !isListeneing;
                        return false;
                    }
                    if (!isOnline && userInfo != null) {
                        CommonUtil.changeCurStatus(Constants.STATUS_HOLD);
                        doLogin(userInfo, true);
                        VoiceUtil.startSpeaking(VoiceCommand.CONNECT_SERVER);
                        isListeneing = !isListeneing;
                        return false;
                    }
                    if (!isOverButtonVisiable) {
                        // TODO: 将这个动画放到听单按钮触发成功的逻辑处
                        initStartAnim();
                    }
                    VoiceUtil.startSpeaking(VoiceCommand.WAIT_FOR_ORDER);
                    CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
                } else {
                    VoiceUtil.startSpeaking(VoiceCommand.FINISH_LISTENERING);
                    CommonUtil.changeCurStatus(Constants.STATUS_HOLD);
//                    initStopAnim();
                }
                if (listenProgressView.isCircling() != isListeneing) {
                    listenProgressView.changeViewStatus();
                }

                return false;
            }
        });
        btnOver = (Button) buttonGroup.findViewById(R.id.over_btn);
//        btnOver.setClickable(false);
        btnOver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO:收车逻辑，并执行隐藏收车按钮动画
                initStopAnim();
                VoiceUtil.startSpeaking(VoiceCommand.HOLD_CAR);
                CommonUtil.changeCurStatus(Constants.STATUS_HOLD);
                if (listenProgressView.isCircling()) {
                    isListeneing = !isListeneing;
                    listenProgressView.changeViewStatus();
                }
            }
        });


    }

    //收车按钮收起动画
    private void initStopAnim() {
        AnimationSet animSet = new AnimationSet(true);
        TranslateAnimation btnOutTransAnim = new TranslateAnimation(0, -DisplayUtil.SCREEN_WIDTH_PIXELS / 2 + DisplayUtil.dp2px(40), 0, 0);
        animSet.addAnimation(btnOutTransAnim);
        AlphaAnimation btnOutAlphaAnim = new AlphaAnimation(1, 0);
        animSet.addAnimation(btnOutAlphaAnim);
        animSet.setRepeatCount(0);
        animSet.setDuration(1000);
        animSet.setFillAfter(true);
        animSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                btnOver.setVisibility(View.INVISIBLE);
                btnOver.setClickable(false);
                isOverButtonVisiable = !isOverButtonVisiable;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        btnOver.startAnimation(animSet);
    }

    //收车按钮显示动画
    private void initStartAnim() {
        AnimationSet animSet = new AnimationSet(true);
        TranslateAnimation btnOutTransAnim = new TranslateAnimation(-DisplayUtil.SCREEN_WIDTH_PIXELS / 2 + DisplayUtil.dp2px(40), 0, 0, 0);
        animSet.addAnimation(btnOutTransAnim);
        AlphaAnimation btnOutAlphaAnim = new AlphaAnimation(0, 1);
        animSet.addAnimation(btnOutAlphaAnim);
        animSet.setRepeatCount(0);
        animSet.setDuration(1000);
        animSet.setFillAfter(true);
        animSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                btnOver.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                btnOver.setClickable(true);
                isOverButtonVisiable = !isOverButtonVisiable;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        btnOver.startAnimation(animSet);
    }


    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case HANDLE_LOGIN_FAILURE:
                if (isVisiable) {
                    doLogin(userInfo, true);
                }
                break;
            case NEW_ORDER_ARRIVE:
                if(System.currentTimeMillis() - lastOrderTime < 6 * 1000){ // 连续的订单
                    break;
                }
                if(System.currentTimeMillis() - lastOrderTime > 10 * 1000){
                    if(CommonUtil.getCurrentStatus() == Constants.STATUS_DEAL){
                        //一直处于状态2
                        CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
                    }
                }
                OrderItem orderItem = CommonUtil.getCurOrderItem();
                if (CommonUtil.getCurOrderItem() == null) {
                    Log.e("daddy", "orderItem fail ");
                    isListeneing = true;
                    break;
                }
                LatLng startLoaction = new LatLng(
                        Double.valueOf(orderItem.getOrder().getStart_lat()), Double.valueOf(orderItem.getOrder().getStart_lng()));
                LatLng endLoaction = new LatLng(
                        CommonUtil.getCurLat(), CommonUtil.getCurLng());
                orderItem.setDistance(String.valueOf(DistanceUtil.getDistance(startLoaction, endLoaction)));
                //显示派单dialog
                if(CommonUtil.getCurrentStatus() == Constants.STATUS_WAIT){
                    lastOrderTime = System.currentTimeMillis();
                    dialog = new MainOrderDialog(context, orderItem);
                    Log.e("Daddy m", "orderItem" + orderItem.getOrder().getStart() + " " + orderItem.getOrder().getDestination() + " ");
                    dialog.setCancelable(true);
                    if(isVisiable){
                        if(!dialog.isVisible()) {
                            dialog.show(getSupportFragmentManager(), "mainorderdialog");
                        }
                    } else { //处于其他页面或
                        CommonUtil.setCurOrderItem(orderItem);
                        Intent intent = new Intent();
                        intent.setAction(Constants.ACTION_NEW_ORDER);
                        sendBroadcast(intent);
                    }
                    CommonUtil.addTodayAllWork();
                    //选择界面不听单
                    CommonUtil.changeCurStatus(Constants.STATUS_DEAL);
                } else {
                    Log.e("MainActivity ", "wrong status to get new order!"+ CommonUtil.getCurrentStatus());
                }

                break;
            case HANDLE_BASEINFO:
                if(baseInfo == null){
                    pullBaseInfo();
                    break;
                }
                SharedPreferencesUtils.setParams(getApplicationContext(), baseInfo.getBaseInfo());
                updateMeFragmentBaseinfo(baseInfo);
                break;
            case UPDATE_GRABORDER:
                updateGrabOrderFragment(MessageTag.MESSAGE_UPDATE_GRABORDER);
                break;
            case MessageTag.MESSAGE_UPDATE_MESSAGE:
                //TODO:刷新消息列表
                updateGrabOrderFragment(MessageTag.MESSAGE_UPDATE_MESSAGE);
                break;
            case ADJUST_STATUS:
                if(CommonUtil.getCurrentStatus() == Constants.STATUS_WAIT) {
                    isListeneing = true;
                    if (!listenProgressView.isCircling()) {
                        listenProgressView.changeViewStatus();
                    }
                    if(btnOver.getVisibility() != View.VISIBLE){
                        initStartAnim();
                    }
                } else {
                    isListeneing = false;
                    if(listenProgressView.isCircling()){
                        listenProgressView.changeViewStatus();
                    }
                    if(btnOver.getVisibility() == View.VISIBLE){
                        initStopAnim();
                    }
                }
                break;
            default:
                break;
        }
        return false;
    }


    private void updateMeFragmentBaseinfo(BaseInfo info){

        List<Fragment> list = MainActivity.this.getSupportFragmentManager().getFragments();
        for(Fragment fragment : list){
            if(fragment instanceof MeFragment){//
                Message msg = new Message();
                msg.obj = info;
                msg.what = MeFragment.LOAD_BASEINFO;
                ((MeFragment) fragment).getHanlder().sendMessage(msg);
            }
        }
    }

    private void updateGrabOrderFragment(int what){
        List<Fragment> list = MainActivity.this.getSupportFragmentManager().getFragments();
        if(list == null){
            Log.e("daddy message", "no fragment");
            return;
        }

        for(Fragment fragment : list){
            if(fragment instanceof GrabOrderFragment){//
                Log.e("daddy message", "graborder start");
                ((GrabOrderFragment) fragment).getHanlder().sendEmptyMessage(what);
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(messageReceiver);
        SharedPreferencesUtils.setParam(this, SharedPreferencesUtils.LOGIN_STATE, false);
        isOnline = false;
    }

    public class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case Constants.SERVICE_ACTION_RELOGIN:
                    if(userInfo != null) { // 用户登陆出错
                       doLogin(userInfo, true);
                    } else {
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    }
                    break;
                case Constants.SERVICE_ACTION_MESAGE:
                    // 服务器有通知推送,
                    Log.e("daddy mesage", "handle new message");
                    updateGrabOrderFragment(MessageTag.MESSAGE_UPDATE_MESSAGE);
                    abortBroadcast();
                    break;
                case Constants.ACTION_NEW_ORDER:
                    if(dialog == null || !dialog.isVisible()){
                        dialog = new MainOrderDialog(context, CommonUtil.getCurOrderItem());
                        dialog.show(getSupportFragmentManager(), "mainorderdialog");
                    }
                    abortBroadcast();
                    break;
                case Constants.SERVICE_ACTION_NEW_ORDER: //收到新的订单  来自服务的消息
                    Log.e("daddy", "got a new order");
                    mHandler.sendEmptyMessage(NEW_ORDER_ARRIVE);
                    abortBroadcast();
                    break;
                case Constants.SERVICE_ACTION_NETWORK_OUT: // 断开连接
                    //无语音提示
                    Toast.makeText(MainActivity.this, VoiceCommand.NETWORK_DISCONNECT + VoiceCommand.NETWORK_NOT_AVAILABLE, Toast.LENGTH_SHORT).show();
                    if(CommonUtil.getCurrentStatus() == Constants.STATUS_WAIT){//
                        CommonUtil.changeCurStatus(Constants.STATUS_HOLD);
                        mHandler.sendEmptyMessage(ADJUST_STATUS);
                    }
                    abortBroadcast();
                    break;
                default:
                    return;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {

            final String tmp = CommonUtil.getCurrentStatus();
            CommonUtil.changeCurStatus(Constants.STATUS_HOLD);

            final AlertDialog.Builder alterDialog = new AlertDialog.Builder(this);
            alterDialog.setMessage("确定退出应用？");
            alterDialog.setCancelable(true);

            alterDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    stopService(duduService);
                    AppExitUtil.getInstance().exit();
                }
            });
            alterDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (tmp == Constants.STATUS_WAIT) {
                        CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
                    }
                    dialog.cancel();
                }
            });
            alterDialog.show();
        }

        return false;
    }
}