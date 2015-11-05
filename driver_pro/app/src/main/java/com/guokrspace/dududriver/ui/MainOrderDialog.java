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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.common.Constants;
import com.guokrspace.dududriver.net.ResponseHandler;
import com.guokrspace.dududriver.net.SocketClient;
import com.guokrspace.dududriver.util.CommonUtil;

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
    ImageButton btnCancel;

    private Context context;
    private Thread thread;
    private Handler mHandler;
    private boolean threadStopFlag = false;

    private MainActivity.OrderBrefInformation order;

    private static final int MAX_TIME = 15;
    private static final int HANDLE_TIMERTICK = 999;
    private static final int HANDLER_TIMER_TIMEOUT = 888;

    public MainOrderDialog(Context context) {
        this.context = context;
    }

    public MainOrderDialog(Context context, MainActivity.OrderBrefInformation order){
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
        initView();
        return view;
    }

    private void initView() {
        btnCancel.setOnClickListener(this);
        acceptLayout.setOnClickListener(this);
        mHandler = new Handler(this);
        tvDistance.setText("距离你大约 " + order.getDistance() + " 公里");
        tvOrderOrigin.setText(" " + order.getStartPoint());
        tvOrderDestination.setText(" " + order.getEndPoint());
        TimerTick(MAX_TIME);
    }

    private Context retContext(){
        return context;
    }
    private void disMiss(){
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
        ButterKnife.unbind(this);
    }

    @Override
    public void onClick(View v) {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
            thread = null;
        }
        mHandler.removeMessages(HANDLE_TIMERTICK);
        switch (v.getId()) {
            case R.id.order_cancel:
                this.dismiss();
                //取消订单,重新听单
                CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
                break;
            case R.id.accept_rl:
                // TODO: Skip to OrderInfo page
                threadStopFlag = true;
                getActivity().startActivity(new Intent(context, PickUpPassengerActivity.class));
                //确认接单,不听单
                SocketClient.getInstance().orderOrder(order.getOrder_no(), new ResponseHandler(Looper.myLooper()){

                    @Override
                    public void onSuccess(String messageBody) {
                        //发送成功了
                        Toast.makeText(retContext(), "正在等待服务器确认派单...", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        //发送失败
                        Toast.makeText(retContext(), "接单失败,请等待下次派单!", Toast.LENGTH_SHORT).show();
                        CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
                        disMiss();
                    }

                    @Override
                    public void onTimeout() {
                        Toast.makeText(retContext(), "服务器响应超时,请等待下次派单!", Toast.LENGTH_SHORT).show();
                        CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
                        disMiss();
                    }
                });
                CommonUtil.changeCurStatus(Constants.STATUS_HOLD);
                this.dismiss();
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
                }
                break;
            case HANDLER_TIMER_TIMEOUT:
                threadStopFlag = true;
                this.dismiss();
                //超时返回监听状态
                CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
                break;
            default:
                break;
        }
        return false;
    }

    private void TimerTick(final int max_seconds) {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int seconds_left = max_seconds;
                while (seconds_left > 0 && !threadStopFlag) {
                    seconds_left--;
                    mHandler.sendMessage(mHandler.obtainMessage(HANDLE_TIMERTICK, seconds_left));
                    try {
                        thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mHandler.sendEmptyMessage(HANDLER_TIMER_TIMEOUT);
            }
        });
        if (!thread.isAlive()) {
            thread.start();
        }
    }
}
