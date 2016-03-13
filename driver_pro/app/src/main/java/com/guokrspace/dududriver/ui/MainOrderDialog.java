package com.guokrspace.dududriver.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.common.Constants;
import com.guokrspace.dududriver.common.VoiceCommand;
import com.guokrspace.dududriver.model.OrderItem;
import com.guokrspace.dududriver.net.ResponseHandler;
import com.guokrspace.dududriver.net.SocketClient;
import com.guokrspace.dududriver.net.message.MessageTag;
import com.guokrspace.dududriver.util.CommonUtil;
import com.guokrspace.dududriver.util.VoiceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by hyman on 15/11/2.
 */
@SuppressLint("ValidFragment")
public class MainOrderDialog extends DialogFragment implements View.OnClickListener, Handler.Callback{

    @Bind(R.id.distance_tv)
    TextView tvDistance;
    @Bind(R.id.order_type)
    TextView tvOrderType;
    @Bind(R.id.order_origin)
    TextView tvOrderOrigin;
    @Bind(R.id.order_destination)
    TextView tvOrderDestination;
    @Bind(R.id.order_second)
    TextView tvOrderSecond;
    @Bind(R.id.accept_rl)
    RelativeLayout acceptLayout;
    @Bind(R.id.order_cancel)
    Button btnCancel;
    @Bind(R.id.show_map)
    Button btnShowMap;

    private Context context;
    private MyTimeTick myTimeTick;
    private Handler mHandler;

    private OrderItem order;

    private static final int MAX_TIME = 15;
    private static final int HANDLE_TIMERTICK = 999;
    private static final int HANDLER_TIMER_TIMEOUT = 888;
    private static final int INTENT_TO_PICKUP = 898;
    private static final int ORDER_CANCELED = 899;

    public MainOrderDialog(Context context) {
        this.context = context;
    }

    public MainOrderDialog(Context context, OrderItem order){
        this.context = context;
        this.order = order;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL,
                android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_main_order, container, false);
        ButterKnife.bind(this, view);
        initListener();
        initView();
        return view;
    }

    private void initListener(){

        //监听派单取消的通知
        SocketClient.getInstance().registerServerMessageHandler(MessageTag.ORDER_CANCEL, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                try {
                    JSONObject mCancel = new JSONObject(messageBody);
                    Log.e("daddy", "messageBody" + messageBody);
                    if (order == null
//                            || mCancel.get("order_no") != order.getOrder().getId()
                            || CommonUtil.getCurrentStatus() != Constants.STATUS_DEAL) {
                        //订单已经取消或者已经接到乘客  无法取消订单
                        return;
                    }
                    mHandler.sendEmptyMessage(ORDER_CANCELED);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String error) {
                mHandler.sendEmptyMessage(ORDER_CANCELED);
            }

            @Override
            public void onTimeout() {
                mHandler.sendEmptyMessage(ORDER_CANCELED);
            }
        });

    }
    private void initView() {
        btnCancel.setOnClickListener(this);
        acceptLayout.setOnClickListener(this);
        mHandler = new Handler(this);
        Log.e("daddy", "distance " + order.getDistance());
        double distance = Math.floor(Double.valueOf(order.getDistance()));
        String distanceStr = "";
        if(distance > 20000) {
            //20公里失败
        } else if(distance <= 999) {
            distanceStr = "距离你只有大约 " + distance + " 米";
        } else {
            distanceStr = "距离你大约 " + new DecimalFormat(".#").format(distance/1000.0d) + " 公里";
        }
        tvDistance.setText(distanceStr);
        tvOrderOrigin.setText(" " + order.getOrder().getStart());
        tvOrderDestination.setText(" " + order.getOrder().getDestination());

        VoiceUtil.startSpeaking(VoiceCommand.NEW_ORDER_ARRIVE + distanceStr);

        myTimeTick = new MyTimeTick(MAX_TIME);
        myTimeTick.startTimer();
    }

    private Context retContext(){
        return context;
    }
    private void disMiss(){
        myTimeTick.stopTimer();
        this.dismiss();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myTimeTick.stopTimer();
        if(CommonUtil.getCurrentStatus() != Constants.STATUS_GET){
            CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
        }
        ButterKnife.unbind(this);
    }

    @Override
    public void onClick(View v) {

        mHandler.removeMessages(HANDLE_TIMERTICK);
        switch (v.getId()) {
            case R.id.order_cancel:
                this.dismiss();
                myTimeTick.stopTimer();
                VoiceUtil.startSpeaking(VoiceCommand.ORDER_REJECT);
                //取消订单,重新听单
                CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
                break;
            case R.id.accept_rl:
                // TODO: Skip to OrderInfo pagec
                myTimeTick.stopTimer();
                 //确认接单,不听单
                SocketClient.getInstance().orderOrder(order.getOrder().getId(), new ResponseHandler(Looper.myLooper()){

                    @Override
                    public void onSuccess(String messageBody) {
                        //发送成功了
                        Toast.makeText(retContext(), "接单成功!", Toast.LENGTH_SHORT).show();
                        VoiceUtil.startSpeaking(VoiceCommand.ORDER_ACCEPT);
                        CommonUtil.changeCurStatus(Constants.STATUS_GET);
                        mHandler.sendEmptyMessage(INTENT_TO_PICKUP);
                    }

                    @Override
                    public void onFailure(String error) {
                        //接单失败
                        Toast.makeText(retContext(), "接单失败,正在为您重新派单!", Toast.LENGTH_SHORT).show();
                        VoiceUtil.startSpeaking(VoiceCommand.ORDER_FAILER);
                        CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
                        disMiss();
                    }

                    @Override
                    public void onTimeout() {
                        Toast.makeText(retContext(), "服务器响应超时,请等待下次派单!", Toast.LENGTH_SHORT).show();
                        VoiceUtil.startSpeaking(VoiceCommand.TIME_OUT_ALERT);
                        CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
                        disMiss();
                    }
                });
                break;
            default:
                break;
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case HANDLE_TIMERTICK:
                if (tvOrderSecond != null) {
                    tvOrderSecond.setText(msg.obj + "");
                    int sec = (Integer)msg.obj;
                    if(sec == 5 &&!VoiceUtil.isSpeaking()){
                        VoiceUtil.startSpeakingSound(Constants.SOUND_TIME);
                    }
                }
                break;
            case HANDLER_TIMER_TIMEOUT:
                if(this.isVisible()) {
                    this.dismiss();
                }
                //超时返回监听状态
                VoiceUtil.stopSpeaking();
                VoiceUtil.startSpeaking(VoiceCommand.ORDER_AUTO_CANCEL);
                CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
                break;
            case INTENT_TO_PICKUP:
                //确认完毕进入导航页面
                Intent intent = new Intent(context, PickUpPassengerActivity.class);
                intent.putExtra("orderItem", order);
                intent.putExtra("isRecover", false);
                startActivity(intent);
                dismiss();
                break;
            case ORDER_CANCELED:
                order = null;
                this.dismiss();
                VoiceUtil.startSpeaking(VoiceCommand.ORDER_CANCEL);
                CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
                Intent cancel = new Intent();
                cancel.setAction(Constants.ACTION_ORDER_CANCEL);
                this.getActivity().sendOrderedBroadcast(cancel, null);
                break;
            default:
                break;
        }
        return false;
    }

    class MyTimeTick {

        int timeLeft;

        MyTimeTick(int maxTime){ this.timeLeft = maxTime; }

        private Timer _Timer = new Timer();

        private MyTask _Task = new MyTask();

        public void startTimer() {
            if (_Timer == null) {
                _Timer = new Timer();
            }
            _Task = new MyTask();//新建一个任务
            _Timer.schedule(_Task, 1000, 1000);
        }

        public void stopTimer() {
            if (_Timer != null) {
                _Timer.cancel();
                _Timer = null;
            }
            if (_Task != null) {
                _Task.cancel();//将原任务从队列中移除
                _Task = null;
            }
        }

        class MyTask extends TimerTask {

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    mHandler.sendEmptyMessage(HANDLER_TIMER_TIMEOUT);
                    return;
                }
                timeLeft--;
                mHandler.sendMessage(mHandler.obtainMessage(HANDLE_TIMERTICK, timeLeft));
            }
        }
    }

}
