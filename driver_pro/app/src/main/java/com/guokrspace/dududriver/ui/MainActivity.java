package com.guokrspace.dududriver.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.guokrspace.dududriver.DuduDriverApplication;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.adapter.TabPagerAdapter;
import com.guokrspace.dududriver.common.Constants;
import com.guokrspace.dududriver.database.PersonalInformation;
import com.guokrspace.dududriver.model.BaseInfo;
import com.guokrspace.dududriver.model.OrderItem;
import com.guokrspace.dududriver.net.DuduService;
import com.guokrspace.dududriver.net.ResponseHandler;
import com.guokrspace.dududriver.net.SocketClient;
//import com.guokrspace.dududriver.net.DuduService;
import com.guokrspace.dududriver.net.message.HeartBeatMessage;
import com.guokrspace.dududriver.net.message.MessageTag;
import com.guokrspace.dududriver.util.CommonUtil;
import com.guokrspace.dududriver.util.FastJsonTools;
import com.guokrspace.dududriver.util.LogUtil;
import com.guokrspace.dududriver.util.SharedPreferencesUtils;
import com.guokrspace.dududriver.view.ListenProgressView;
import com.viewpagerindicator.TabPageIndicator;

import org.json.JSONException;
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
    private Context context;

    private ViewPager pager;
    private TabPagerAdapter mAdapter;
    private TabPageIndicator mIndicator;

    private MainOrderDialog dialog = null;

    private View buttonGroup;

    private SocketClient mTcpClient = null;
    private connectTask conctTask = null;

    private boolean isOnline = false;
    private boolean isVisiable = false;
    private boolean isListeneing = false;

    private Handler mHandler;
    private Intent duduService;

    private static final int HANDLE_LOGIN_FAILURE = 100;
    private static final int NEW_ORDER_ARRIVE = 101;
    private static final int HANDLE_BASEINFO = 103;
    //TODO:OTHER thing

    private PersonalInformation userInfo;

    private OrderItem orderItem = null;
    private BaseInfo baseInfo = null;

    // 定位相关
//    LocationClient mLocClient;
//    public MyLocationListener myListener = new MyLocationListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        context = this;
        CommonUtil.changeCurStatus(Constants.STATUS_HOLD);
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
//        initLocation();
//        mLocClient.start();

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
                SocketClient.getInstance().pullBaseInfo(new ResponseHandler(Looper.myLooper()) {
                    @Override
                    public void onSuccess(String messageBody) {
                        baseInfo = (BaseInfo)FastJsonTools.getObject(messageBody, BaseInfo.class);
                        mHandler.sendEmptyMessage(HANDLE_BASEINFO);
                    }

                    @Override
                    public void onFailure(String error) {
                        //返回基础信息失败
                    }

                    @Override
                    public void onTimeout() {
                        //超时
                    }
                });
            }
        }

        //注册派单监听
        SocketClient.getInstance().registerServerMessageHandler(MessageTag.PATCH_ORDER, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                Log.e("Mainactivity", "confirm order handler");
                orderItem = FastJsonTools.getObject(messageBody, OrderItem.class);
                Log.e("Daddy ", messageBody + "  " + orderItem.getCMD() + " "+ orderItem.getOrder().getDestination_lat() + "::" + orderItem.getOrder().getDestination_lng());
//                if()
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

        //监听派单取消的通知
        SocketClient.getInstance().registerServerMessageHandler(MessageTag.ORDER_CANCEL, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                try {
                    JSONObject mCancel = new JSONObject(messageBody);
                    if(orderItem == null || mCancel.get("order_no") != orderItem.getOrder().getId()
                            || CommonUtil.getCurrentStatus() != Constants.STATUS_HOLD){
                        return;
                    }
                    orderItem = null;
                    if(dialog != null){
                        dialog.dismiss();
                        dialog = null;
                    }
                    CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
                } catch (JSONException e){
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(String error) {

            }

            @Override
            public void onTimeout() {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisiable = false;
        mHandler.removeMessages(HANDLE_LOGIN_FAILURE);
    }

    //进行自动登陆
    private void doLogin(PersonalInformation user) {
        Log.e("hyman", user.getMobile() + " " + user.getToken() + " " + user.getId());
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
                isOnline = true;
            }

            @Override
            public void onFailure(String error) {
                showCustomToast("登陆失败");
                Log.e("login in failure!", "errorbody " + error);
                isOnline = false;
                mHandler.sendMessageDelayed(mHandler.obtainMessage(HANDLE_LOGIN_FAILURE), 500);
            }

            @Override
            public void onTimeout() {
                Log.e("hyman", "登陆超时");
                isOnline = false;
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

        buttonGroup = (View) findViewById(R.id.button_group_layout);
        final ListenProgressView listenProgressView = (ListenProgressView) buttonGroup.findViewById(R.id.listenprogressview);
        listenProgressView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                String currStatus = CommonUtil.getCurrentStatus();
                if (currStatus == Constants.STATUS_RUN || currStatus == Constants.STATUS_GOT) {
                    //error stats
                    LogUtil.e("MainActivity ", "runing can not click the button ");
                    return true;
                }
                Log.e("daddy", "start is listener" + isListeneing);
                isListeneing = !isListeneing;
                if (isListeneing) {

                    if (!isOnline && userInfo != null) {
                        CommonUtil.changeCurStatus(Constants.STATUS_HOLD);
                        doLogin(userInfo);
                        isListeneing = !isListeneing;
                        return false;
                    }
                    CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
                } else {
                    CommonUtil.changeCurStatus(Constants.STATUS_HOLD);
                }
                listenProgressView.changeViewStatus();
                return false;
            }
        });
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
                    isListeneing = true;
                    break;
                }
                LatLng startLoaction = new LatLng(
                        Double.valueOf(orderItem.getOrder().getStart_lat()), Double.valueOf(orderItem.getOrder().getStart_lng()));
                LatLng endLoaction = new LatLng(
                        Double.valueOf(orderItem.getOrder().getDestination_lat()), Double.valueOf(orderItem.getOrder().getDestination_lng()));
                orderItem.setDistance(String.valueOf(DistanceUtil.getDistance(startLoaction, endLoaction)));
                //显示派单dialog
                if(CommonUtil.getCurrentStatus() == Constants.STATUS_WAIT){
                    dialog = new MainOrderDialog(context, orderItem);
                    Log.e("Daddy m", "orderItem"+ orderItem.getOrder().getStart() + " "+ orderItem.getOrder().getDestination() + " ");
                    dialog.setCancelable(true);
                    dialog.show(getSupportFragmentManager(), "mainorderdialog");
                    //选择界面不听单
                    CommonUtil.changeCurStatus(Constants.STATUS_HOLD);
                } else {
                    Log.e("MainActivity ", "wrong status to get new order!");
                }

                break;
            case HANDLE_BASEINFO:
                if(baseInfo == null){

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
}