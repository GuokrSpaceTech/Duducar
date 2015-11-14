package com.guokrspace.dududriver.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.widgets.Dialog;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.common.Constants;
import com.guokrspace.dududriver.common.VoiceCommand;
import com.guokrspace.dududriver.model.BaseInfo;
import com.guokrspace.dududriver.model.OrderItem;
import com.guokrspace.dududriver.net.ResponseHandler;
import com.guokrspace.dududriver.net.SocketClient;
import com.guokrspace.dududriver.net.message.MessageTag;
import com.guokrspace.dududriver.util.CommonUtil;
import com.guokrspace.dududriver.util.SharedPreferencesUtils;
import com.guokrspace.dududriver.util.VoiceUtil;
import com.guokrspace.dududriver.view.CircleImageView;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by hyman on 15/11/3.
 */
public class ConfirmBillActivity extends BaseActivity implements Handler.Callback {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.passenger_avatar)
    CircleImageView civPassengerAvatar;
    @Bind(R.id.myposition_textview)
    TextView tvMyPosition;
    @Bind(R.id.passengerposition_textview)
    TextView tvPassengerPosition;
    @Bind(R.id.passenger_info)
    ImageButton ibPassengerInfo;
    @Bind(R.id.bill_sum)
    TextView tvBillSum;
    @Bind(R.id.start_price_tv)
    TextView tvStartPrice;
    @Bind(R.id.mileage_textview)
    TextView tvMileage;
    @Bind(R.id.mileage_cost)
    TextView tvMileageCost;
    @Bind(R.id.low_speed_cost)
    TextView tvLowSpeedCost;
    @Bind(R.id.height_speed_cost_minus)
    ImageButton ibHeightSpeedCostMinus;
    @Bind(R.id.height_speed_cost)
    TextView tvHeightSpeedCost;
    @Bind(R.id.height_speed_cost_add)
    ImageButton ibHeightSpeedCostAdd;
    @Bind(R.id.bridge_cost_minus)
    ImageButton ibBridgeCostMinus;
    @Bind(R.id.bridge_cost)
    TextView tvBridgeCost;
    @Bind(R.id.bridge_cost_add)
    ImageButton ibBridgeCostAdd;
    @Bind(R.id.park_cost_minus)
    ImageButton ibParkCostMinus;
    @Bind(R.id.park_cost)
    TextView tvParkCost;
    @Bind(R.id.park_cost_add)
    ImageButton ibParkCostAdd;
    @Bind(R.id.confirm_button)
    ButtonRectangle btnConfirm;
    private Context context;
    private Dialog dialog;

    private OrderItem orderItem;

    private Handler mHandler;

    private final int PAY_OVER = 0X001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ativity_confirmbill);
        context = ConfirmBillActivity.this;
        ButterKnife.bind(this);
        initView();
        mHandler = new Handler(this);

        SocketClient.getInstance().registerServerMessageHandler(MessageTag.PAY_OVER, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                Log.e(ConfirmBillActivity.class + "", "pay over success!");
                mHandler.sendEmptyMessage(PAY_OVER);
            }

            @Override
            public void onFailure(String error) {
                Log.e(ConfirmBillActivity.class + "", "pay over failure!");
                Toast.makeText(context, "对方支付失败!", Toast.LENGTH_SHORT);
            }

            @Override
            public void onTimeout() {
                Log.e(ConfirmBillActivity.class + "", "pay over time out!");
            }
        });
    }

    private void initView() {
        toolbar.setTitle("确认账单");
        toolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.return_icon));
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnConfirm.setButtonText("确认账单");

        Bundle bundle = getIntent().getExtras();
        orderItem = (OrderItem) bundle.get("orderItem");
        final double curDistance = bundle.getDouble("mileage");
        final double lowSpeedTime= bundle.getInt("lowspeed");
        tvMyPosition.setText(orderItem.getOrder().getStart());
        tvPassengerPosition.setText(orderItem.getOrder().getDestination());

        initDialog();
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.show();
                dialog.getButtonAccept().setButtonText("等待乘客付款");
                dialog.getButtonCancel().setButtonText("司机代付");
                dialog.getButtonAccept().setClickable(false);
                dialog.getButtonAccept().setEnabled(false);
                dialog.getButtonCancel().setClickable(false);
                dialog.getButtonCancel().setEnabled(false);
                dialog.setCancelable(false);

                OnKeyListener keylistener = new OnKeyListener() {
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                };
                dialog.setOnKeyListener(keylistener);

                SocketClient.getInstance().endOrder(countPrice(curDistance, lowSpeedTime) + "", curDistance + "", new ResponseHandler(Looper.myLooper()) {
                    @Override
                    public void onSuccess(String messageBody) {
                        Log.e("PickUpPassengerAct", "success " + messageBody);
                        dialog.getButtonCancel().setEnabled(true);
                        dialog.getButtonCancel().setClickable(true);
                        VoiceUtil.startSpeaking(VoiceCommand.WAIT_FOR_PAY);
                    }

                    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                    @Override
                    public void onFailure(String error) {
                        Log.e("PickUpPassengerAct", "end order failure" + error);
                        Toast.makeText(context, "正在发送账单...", Toast.LENGTH_SHORT);
                        btnConfirm.callOnClick();
                    }

                    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                    @Override
                    public void onTimeout() {
                        Log.e("PickUpPassengerAct", "end order time out");
                        Toast.makeText(context, "网络状况较差!", Toast.LENGTH_SHORT);
                        VoiceUtil.startSpeaking(VoiceCommand.TIME_OUT_ALERT);
                        btnConfirm.callOnClick();
                    }
                });


            }
        });
    }

    private double countPrice(double mileage, double lowtime) {
        BaseInfo baseInfo = (BaseInfo) SharedPreferencesUtils.getParam(context, "baseinfo", new BaseInfo());
        if(baseInfo != null){
            int starting_price = Integer.parseInt(baseInfo.getCharge_rule().getStarting_price());
            double starting_distance = Double.parseDouble(baseInfo.getCharge_rule().getStarting_distance());
            double km_price = Double.parseDouble(baseInfo.getCharge_rule().getKm_price());
            double low_speed_price = Double.parseDouble(baseInfo.getCharge_rule().getLow_speed_price());
            mileage = mileage/1000.0d;
            if(mileage <= starting_distance + 1){
                return starting_price + low_speed_price * lowtime;
            }
            mileage = mileage - starting_distance;
            return starting_price + mileage * km_price + lowtime * low_speed_price ;
        } else {
            return mileage / 1000.0d * 8 + lowtime * 0.1  + 0.01;
        }
    }

    private void initDialog() {
        dialog = new Dialog(context, getString(R.string.confirm_dialog_content));
        dialog.setCancelable(false);
        dialog.setOnCancelButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 进入付款详情界面，乘客未付款则需要司机代付
                VoiceUtil.startSpeaking(VoiceCommand.DRIVER_PAY);
                startActivity(new Intent(context, OrderDetailActivity.class));
                finish();
            }
        });
        dialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
                dialog.dismiss();
                startActivity(new Intent(context, MainActivity.class));
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if(dialog != null)
                dialog.dismiss();
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case PAY_OVER:
                if(null != dialog && dialog.isShowing()){
                    Toast.makeText(context, "用户支付完成!", Toast.LENGTH_SHORT);

                    VoiceUtil.startSpeaking(VoiceCommand.PAY_OVER);

                    dialog.getButtonAccept().setButtonText("继续听单");
                    dialog.getButtonCancel().setButtonText("收车");
                    dialog.getButtonAccept().setClickable(true);
                    dialog.getButtonAccept().setEnabled(true);
                    dialog.getButtonCancel().setClickable(true);
                    dialog.getButtonCancel().setEnabled(true);

                    dialog.getButtonCancel().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CommonUtil.changeCurStatus(Constants.STATUS_HOLD);
                            dialog.dismiss();
                            startActivity(new Intent(context, MainActivity.class));
                            finish();
                        }
                    });
                }
                break;
            default:
                return false;
        }
        return false;
    }
}
