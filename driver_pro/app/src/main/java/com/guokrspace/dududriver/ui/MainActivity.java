package com.guokrspace.dududriver.ui;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
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
        //  TODO voice guide on/off
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
    private boolean isVisiable = true;
    private boolean isListeneing = false;
    private boolean isOverButtonVisiable = false;
    private boolean isInvoke = false;
    private boolean isInvoke2 = false;
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
    private static final int NETWORK_RECONNET = 107;
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
        Intent mIntent = getIntent();
        if (mIntent != null) {
            if (TextUtils.equals(Constants.ENTER_FROM_LOGIN, mIntent.getStringExtra(LoginActivity.FROM_LOGIN_PAGE))) {
                // 说明是登陆成功，获取baseinfo
                pullBaseInfo();
            }
        }
        initView();

        /*MobclickAgent.reportError(context, "主界面报错");*/

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
        if (localUsers != null && localUsers.size() > 0 && ((PersonalInformation)localUsers.get(0)).getToken() != null) {
            userInfo = (PersonalInformation) localUsers.get(0);
            if(!CommonUtil.isServiceOn()){
                startService(duduService);
            }
        } else {
            //用户信息不存在或注销,重新注册页面
            Log.e("daddy main ", "no persion ");
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("daddy vis", "resume to true");
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
            Log.e("daddy", "resume service restart");
            startService(duduService);
        }

        registerBroadcastReceiver();

        //TODO : 恢复听单状态
        if(isListeneing){//正在听单
            CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
        }
        /*
        * 调整circle状态
        * */
        Log.e("daddy adjust", " onresume ");
        mHandler.sendEmptyMessage(ADJUST_STATUS);
        /*
        * 更新每天的记录
        * */
//        CommonUtil.updateToday();
        mHandler.sendEmptyMessage(UPDATE_GRABORDER);

//        if(isOnline){
//            pullOrder();
//        }
//        pullBaseInfo();

        // 后台掉起的操作
        if(isInvoke){
            isInvoke2 = true;
            mHandler.sendEmptyMessageDelayed(NEW_ORDER_ARRIVE, 1 * 1000);
        } else {
            initGPS();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("daddy vis", "pause to false");
        isVisiable = false;
        unregisterReceiver(receiver);
        mHandler.removeMessages(HANDLE_LOGIN_FAILURE);
    }


    //监听service传来的消息
    private void registerBroadcastReceiver(){
        receiver = new ServiceReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.SERVICE_ACTION_RELOGIN);
        filter.addAction(Constants.ACTION_NEW_ORDER);
        filter.setPriority(1000);
        registerReceiver(receiver, filter);

        messageReceiver = new ServiceReceiver();
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(Constants.SERVICE_ACTION_MESAGE);
        mFilter.addAction(Constants.SERVICE_ACTION_NEW_ORDER);
        mFilter.addAction(Constants.SERVICE_ACTION_NETWORK_OUT);
        mFilter.addAction(Constants.SERVICE_ACTION_NEWWORK_RECONNET);
        mFilter.setPriority(1000);
        registerReceiver(messageReceiver, mFilter);
    }
    //进行自动登陆
    private void doLogin(PersonalInformation user, final boolean recover) {
        if (user == null || user.getToken() == null) { // 注销过后
            return;
        }
        Log.e("daddy", user.getMobile() + " " + user.getToken() + " " + user.getId());
        SocketClient.getInstance().autoLoginRequest(user.getMobile(), "1", user.getToken(), new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {

                showCustomToast("登陆成功");
                Log.e("login in main activity!", "messageBody" + messageBody);
                SharedPreferencesUtils.setParam(MainActivity.this, SharedPreferencesUtils.LOGIN_STATE, true);
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
                            OrderItem orderItem = new Gson().fromJson(orderDetail, OrderItem.class);
                            CommonUtil.setCurOrderItem(orderItem);
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
                                OrderItem orderItem = new Gson().fromJson(orderDetail, OrderItem.class);
                                Intent intent = new Intent(MainActivity.this, ConfirmBillActivity.class);
                                JSONObject lastCharge = new JSONObject((String)object.get("last_charge"));
                                CommonUtil.setCurOrderItem(orderItem);
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
                                OrderItem orderItem = new Gson().fromJson(orderDetail, OrderItem.class);
                                Intent intent = new Intent(MainActivity.this, PickUpPassengerActivity.class);
                                CommonUtil.setCurOrderItem(orderItem);
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

                pullBaseInfo();
//                pullOrder();
                mHandler.sendEmptyMessage(MessageTag.MESSAGE_UPDATE_MESSAGE);


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

    private void pullBaseInfo(){

        SocketClient.getInstance().pullBaseInfo(new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                Log.e("daddy_baseinfo", "base info " + messageBody);
                baseInfo = (BaseInfo) new Gson().fromJson(messageBody, BaseInfo.class);
                mHandler.sendEmptyMessage(HANDLE_BASEINFO);
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
        mAdapter = new TabPagerAdapter(getSupportFragmentManager());
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setOffscreenPageLimit(3);//设置预加载页数
        //要求每次的加载都需要保证列表是最新的，当然就缓存一页
        pager.setAdapter(mAdapter);
        mIndicator = (TabPageIndicator) findViewById(R.id.indicator);
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
                Log.e("daddy mainactivity", "new order arrive");
                if(!isInvoke2 && System.currentTimeMillis() - lastOrderTime < 6 * 1000){ // 连续的订单
                    break;
                }
                isInvoke = false;
                isInvoke2 = false;
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
//                    VoiceUtil.startSpeaking(VoiceCommand.NEW_ORDER_ARRIVE);
                    if(!isApplicationBroughtToBackground(getApplicationContext())){ //处于前台
                        Log.e("daddy invoke", "other page");
                        CommonUtil.setCurOrderItem(orderItem);
                        Intent intent = new Intent();
                        intent.setAction(Constants.ACTION_NEW_ORDER);
                        sendBroadcast(intent);
                    } else if(CommonUtil.getCurrentStatus() != Constants.STATUS_DEAL){ // 应用在后台
                        moveToFront();
                        Log.e("daddy invoke", "invoke background");
                    }
                    CommonUtil.addTodayAllWork();
                    //选择界面不听单
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
                Log.e("daddy adjust", CommonUtil.getCurrentStatus() + " " + System.currentTimeMillis());
                if(CommonUtil.getCurrentStatus() == Constants.STATUS_WAIT) {
                    isListeneing = true;

                    if (!listenProgressView.isCircling()) {
                        listenProgressView.changeViewStatus();
                    }

                    if(btnOver.getVisibility() != View.VISIBLE){
                        initStartAnim();
                    }

                } else if(CommonUtil.getCurrentStatus() == Constants.STATUS_DEAL){
                    break;
                } else {
                    Log.e("dady invoke ", " adjust status "+ CommonUtil.getCurrentStatus() +  " "  + listenProgressView.isCircling() + " " + btnOver.getVisibility());
                    isListeneing = false;
                    if(listenProgressView.isCircling()){
                        listenProgressView.changeViewStatus();
                    }
                    if(btnOver.getVisibility() == View.VISIBLE){
                        initStopAnim();
                    }
                }
                break;
            case NETWORK_RECONNET:
                if(CommonUtil.getCurrentStatus() == Constants.STATUS_WAIT){
                    listenProgressView.showConnecting();
                }
                break;
            default:
                break;
        }
        return false;
    }


    public static boolean isApplicationBroughtToBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    protected void moveToFront() {
        if (Build.VERSION.SDK_INT >= 11) { // honeycomb
            final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            final List<ActivityManager.RunningTaskInfo> recentTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

            for (int i = 0; i < recentTasks.size(); i++)
            {
                Log.d("Executed app", "Application executed : "
                        +recentTasks.get(i).baseActivity.toShortString()
                        + "\t\t ID: "+recentTasks.get(i).id+"");
                // bring to front
                if (recentTasks.get(i).baseActivity.toShortString().indexOf("com.guokrspace.dududriver") > -1) {
                    activityManager.moveTaskToFront(recentTasks.get(i).id, ActivityManager.MOVE_TASK_WITH_HOME);
                    isInvoke = true;
                    mHandler.sendEmptyMessage(ADJUST_STATUS);
                    Log.e("daddy invoke", CommonUtil.getCurrentStatus()+" status");
                }
            }
        }
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
                    abortBroadcast();
                    break;
                case Constants.SERVICE_ACTION_MESAGE:
                    // 服务器有通知推送,
                    Log.e("daddy mesage", "handle new message");
                    updateGrabOrderFragment(MessageTag.MESSAGE_UPDATE_MESSAGE);
                    abortBroadcast();
                    break;
                case Constants.ACTION_NEW_ORDER:
                    if(dialog == null || !dialog.isVisible()){
                        CommonUtil.changeCurStatus(Constants.STATUS_DEAL);
                        dialog = new MainOrderDialog(context, CommonUtil.getCurOrderItem());
                        dialog.show(getSupportFragmentManager(), "mainorderdialog");
                    }
//                    abortBroadcast();
                    break;
                case Constants.SERVICE_ACTION_NEW_ORDER: //收到新的订单  来自服务的消息
                    Log.e("daddy", "got a new order");
                    mHandler.sendEmptyMessage(NEW_ORDER_ARRIVE);
                    abortBroadcast();
                    break;
                case Constants.SERVICE_ACTION_NETWORK_OUT: // 断开连接
                    //无语音提示
                    Toast.makeText(MainActivity.this, VoiceCommand.NETWORK_DISCONNECT + VoiceCommand.NETWORK_NOT_AVAILABLE, Toast.LENGTH_SHORT).show();
                    abortBroadcast();
                    break;
                case Constants.SERVICE_ACTION_NEWWORK_RECONNET: //重新连接
                    //TODO
                    if(CommonUtil.getCurrentStatus() == Constants.STATUS_WAIT){
                        //重新连接
                        mHandler.sendEmptyMessage(NETWORK_RECONNET);
                    }
                    break;
                default:
                    return;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {

            /*final String tmp = CommonUtil.getCurrentStatus();
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
            alterDialog.show();*/
            moveTaskToBack(true);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        return;
    }

    /**
     * 监听GPS
     */
    private void initGPS() {
        LocationManager locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        // 判断GPS模块是否开启，如果没有则开启
        if (!locationManager
                .isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            if(CommonUtil.getCurrentStatus() == Constants.STATUS_WAIT) {
                CommonUtil.changeCurStatus(Constants.STATUS_HOLD);
                mHandler.sendEmptyMessage(ADJUST_STATUS);
            }
            VoiceUtil.startSpeaking(VoiceCommand.TURN_ON_GPS);
            dialog.setMessage("请先开启GPS定位功能");
            dialog.setCancelable(false);
            dialog.setPositiveButton("确定",
                    new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            // 转到手机设置界面，用户设置GPS
                            Intent intent = new Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, 0); // 设置完成后返回到原来的界面
                        }
                    });
//            dialog.setNeutralButton("取消", new android.content.DialogInterface.OnClickListener() {
//
//                @Override
//                public void onClick(DialogInterface arg0, int arg1) {
//                    arg0.dismiss();
//                }
//            });
            dialog.show();
        }
    }
}