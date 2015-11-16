package com.guokrspace.dududriver.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
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

    private SocketClient mTcpClient = null;
    private connectTask conctTask = null;

    private boolean isOnline = false;
    private boolean isVisiable = false;
    private boolean isListeneing = false;
    private boolean isOverButtonVisiable = false;

    private Handler mHandler;
    private Intent duduService;
    private ServiceReceiver receiver;

    private static final int HANDLE_LOGIN_FAILURE = 100;
    private static final int NEW_ORDER_ARRIVE = 101;
    private static final int HANDLE_BASEINFO = 103;
    private static final int ORDER_CANCELED = 104;
    private static final int ADJUST_STATUS = 105;
    //TODO:OTHER thing

    private PersonalInformation userInfo;

    private OrderItem orderItem = null;
    private BaseInfo baseInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        context = this;

        initView();

        AppExitUtil.getInstance().addActivity(this);
        mHandler = new Handler(this);

        /*
         * Check if use has logined
         */
        if (!DuduDriverApplication.getInstance().initPersonalInformation()) {
            Log.e("daddy", "oncreate second");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        /*
         * Init the SocketClient
         */
        mTcpClient = null;
        conctTask = new connectTask(); //Connect to server
        conctTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        /*
         * Start Location & Send Heartbeat  Service
         */
        duduService = new Intent(getBaseContext(), DuduService.class);
        startService(duduService);
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
            List localUsers = DuduDriverApplication.getInstance().
                    mDaoSession.getPersonalInformationDao().
                    queryBuilder().list();
            if (localUsers != null && localUsers.size() > 0) {
                userInfo = (PersonalInformation) localUsers.get(0);
                doLogin(userInfo);

            }
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

        //注册派单监听
        SocketClient.getInstance().registerServerMessageHandler(MessageTag.PATCH_ORDER, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                Log.e("Mainactivity", "confirm order handler");
                orderItem = FastJsonTools.getObject(messageBody, OrderItem.class);
                Log.e("Daddy ", messageBody + "  " + orderItem.getCMD() + " " + orderItem.getOrder().getDestination_lat() + "::" + orderItem.getOrder().getDestination_lng());
                mHandler.sendEmptyMessage(NEW_ORDER_ARRIVE);
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

    @Override
    protected void onPause() {
        super.onPause();
        isVisiable = false;
        unregisterReceiver(receiver);
        mHandler.removeMessages(HANDLE_LOGIN_FAILURE);
    }


    //监听service传来的消息
    private void registerBroadcastReceiver(){
        receiver = new ServiceReceiver();
        IntentFilter filter = new IntentFilter(Constants.SERVICE_BROADCAST);
        filter.addAction(Constants.SERVICE_ACTION_RELOGIN);
        registerReceiver(receiver, filter);
    }
    //进行自动登陆
    private void doLogin(PersonalInformation user) {
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
                isOnline = true;
            }

            @Override
            public void onFailure(String error) {
                showCustomToast("登陆失败, 请重新登陆");
                Log.e("login in failure!", "errorbody " + error);
                isOnline = false;
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
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
                baseInfo = (BaseInfo) FastJsonTools.getObject(messageBody, BaseInfo.class);
                Log.e("daddy", "base" + baseInfo.getDriver().getName());
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
        pager.setOffscreenPageLimit(2);//设置预加载页数
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
                    if (!isOnline && userInfo != null) {
                        CommonUtil.changeCurStatus(Constants.STATUS_HOLD);
                        doLogin(userInfo);
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
                    initStopAnim();
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
                    doLogin(userInfo);
                }
                break;
            case NEW_ORDER_ARRIVE:
                if (orderItem == null) {
                    Log.e("daddy", "orderItem fail ");
                    isListeneing = true;
                    break;
                }
                LatLng startLoaction = new LatLng(
                        Double.valueOf(orderItem.getOrder().getStart_lat()), Double.valueOf(orderItem.getOrder().getStart_lng()));
                LatLng endLoaction = new LatLng(
                        CommonUtil.getCurLat(), CommonUtil.getCurLng());
//                        Double.valueOf(orderItem.getOrder().getDestination_lat()), Double.valueOf(orderItem.getOrder().getDestination_lng()));
                orderItem.setDistance(String.valueOf(DistanceUtil.getDistance(startLoaction, endLoaction)));
                //显示派单dialog
                if(CommonUtil.getCurrentStatus() == Constants.STATUS_WAIT){

                    dialog = new MainOrderDialog(context, orderItem);
                    Log.e("Daddy m", "orderItem"+ orderItem.getOrder().getStart() + " "+ orderItem.getOrder().getDestination() + " ");
                    dialog.setCancelable(true);
                    dialog.show(getSupportFragmentManager(), "mainorderdialog");
                    CommonUtil.addTodayAllWork();
                    //选择界面不听单
                    CommonUtil.changeCurStatus(Constants.STATUS_DEAL);
                } else {
                    Log.e("MainActivity ", "wrong status to get new order!");
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

        stopService(duduService);
    }

    public class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case Constants.SERVICE_ACTION_RELOGIN:
                    if(userInfo != null) { // 用户登陆出错
                       doLogin(userInfo);
                    } else {
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
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

            final String tmp = CommonUtil.getCurrentStatus();
            CommonUtil.changeCurStatus(Constants.STATUS_HOLD);

            final AlertDialog.Builder alterDialog = new AlertDialog.Builder(this);
            alterDialog.setMessage("确定退出应用？");
            alterDialog.setCancelable(true);

            alterDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
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