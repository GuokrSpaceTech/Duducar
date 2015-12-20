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
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.widgets.Dialog;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.common.Constants;
import com.guokrspace.dududriver.common.VoiceCommand;
import com.guokrspace.dududriver.model.OrderItem;
import com.guokrspace.dududriver.net.ResponseHandler;
import com.guokrspace.dududriver.net.SocketClient;
import com.guokrspace.dududriver.util.CommonUtil;
import com.guokrspace.dududriver.util.SharedPreferencesUtils;
import com.guokrspace.dududriver.util.VoiceUtil;
import com.guokrspace.dududriver.view.CircleImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
    @Bind(R.id.low_speed_time)
    TextView tvLowSpeedTime;
    @Bind(R.id.low_speed_cost)
    TextView tvLowSpeedCost;
    @Bind(R.id.height_speed_cost_minus)
    ImageButton ibHeightSpeedCostMinus;
    @OnClick(R.id.height_speed_cost_minus)
    public void minusSpeed(){
        addAdditionalCost(0, 0);
    }
    @Bind(R.id.height_speed_cost)
    TextView tvHeightSpeedCost;
    @Bind(R.id.height_speed_cost_add)
    ImageButton ibHeightSpeedCostAdd;
    @OnClick(R.id.height_speed_cost_add)
    public void addSpeed(){
        addAdditionalCost(0, 1);
    }
    @Bind(R.id.bridge_cost_minus)
    ImageButton ibBridgeCostMinus;
    @OnClick(R.id.bridge_cost_minus)
    public void minusBridge(){
        addAdditionalCost(1, 0);
    }
    @Bind(R.id.bridge_cost)
    TextView tvBridgeCost;
    @Bind(R.id.bridge_cost_add)
    ImageButton ibBridgeCostAdd;
    @OnClick(R.id.bridge_cost_add)
    public void addBridge(){
        addAdditionalCost(1, 1);
    }
    @Bind(R.id.park_cost_minus)
    ImageButton ibParkCostMinus;
    @OnClick(R.id.park_cost_minus)
    public void minusPark(){
        addAdditionalCost(2, 0);
    }
    @Bind(R.id.park_cost)
    TextView tvParkCost;
    @Bind(R.id.park_cost_add)
    ImageButton ibParkCostAdd;
    @OnClick(R.id.park_cost_add)
    public void addPark(){
        addAdditionalCost(2, 1);
    }
    @Bind(R.id.confirm_button)
    ButtonRectangle btnConfirm;
    private Context context;
    private Dialog dialog;

    private OrderItem orderItem;

    private Handler mHandler;
    private double price;
    private float lowcost;
    private float milecost;
    private double curDistance;

    private int[] addPrice = new int[]{0,0,0};

    private final int PAY_OVER = 0X001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.ativity_confirmbill);
        context = ConfirmBillActivity.this;
        ButterKnife.bind(this);
        initView();
        mHandler = new Handler(this);
    }

    private void initView() {
        toolbar.setTitle("确认账单");
//        toolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.return_icon));
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setHomeButtonEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnConfirm.setButtonText("确认账单");

        Bundle bundle = getIntent().getExtras();
        orderItem = (OrderItem) bundle.get("orderItem");
        curDistance = bundle.getDouble("mileage");
        final int lowSpeedTime= bundle.getInt("lowspeed");
        final float lowSpeedPrice = Float.parseFloat((String) SharedPreferencesUtils.getParam(getApplicationContext(), Constants.PREFERENCE_KEY_DRIVER_LOW_SPEED_PRICE, "0.55"));
        final float startPrice = Float.parseFloat((String) SharedPreferencesUtils.getParam(getApplicationContext(), Constants.PREFERENCE_KEY_DRIVER_STARTING_PRICE, "6.5"));
        final float startDistance = Float.parseFloat((String) SharedPreferencesUtils.getParam(getApplicationContext(), Constants.PREFERENCE_KEY_DRIVER_STARTING_DISTANCE, "6.0"));

        price = CommonUtil.countPrice(curDistance, lowSpeedTime);
        price = new BigDecimal(price).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();

        lowcost = lowSpeedTime * lowSpeedPrice;
        if(curDistance > startDistance && price - lowcost > startPrice){
            milecost = (float)price - lowcost;
        } else {
            milecost = 0;
        }

        tvMyPosition.setText(orderItem.getOrder().getStart());
        tvPassengerPosition.setText(orderItem.getOrder().getDestination());
        tvBillSum.setText(price+"");

        tvStartPrice.setText(String.format(getResources().getString(R.string.start_price), startPrice));
        tvMileage.setText(String.format(getResources().getString(R.string.mileage_text), (curDistance / 1000.0f)));
        tvMileageCost.setText(String.format(getResources().getString(R.string.mileage_cost), milecost));
        tvLowSpeedTime.setText(String.format(getResources().getString(R.string.low_speed_text), lowSpeedTime));
        tvLowSpeedCost.setText(String.format(getResources().getString(R.string.low_speed_cost), lowcost));

        initDialog();
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                VoiceUtil.startSpeaking(VoiceCommand.CONFIRM_CHARGE);

                dialog.show();
                dialog.getButtonAccept().setButtonText("确认");
                dialog.getButtonCancel().setButtonText("取消");
                //选择乘客支付
                dialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
//                        VoiceUtil.startSpeaking(VoiceCommand.CONTINUE_WAIT)
                        final double addPriced[] = new double[]{
                                new BigDecimal(addPrice[0] / 100d).setScale(2, RoundingMode.HALF_UP).doubleValue(),
                                new BigDecimal(addPrice[1] / 100d).setScale(2, RoundingMode.HALF_UP).doubleValue(),
                                new BigDecimal(addPrice[2] / 100d).setScale(2, RoundingMode.HALF_UP).doubleValue()};

                        SocketClient.getInstance().endOrder(price + "", curDistance + "", lowSpeedTime + "", addPriced[0] + "", addPriced[1] + "", addPriced[2] + "", new ResponseHandler(Looper.myLooper()) {
                            @Override
                            public void onSuccess(String messageBody) {
                                Log.e("PickUpPassengerAct", "success " + messageBody);
                                try {
                                    JSONObject object = new JSONObject(messageBody);
                                    String orderNum = object.getString("orderNum");
                                    double sumPrice = object.getDouble("sumprice");
                                    Intent intent = new Intent(context, OrderDetailActivity.class);
                                    intent.putExtra("orderItem", orderItem);
                                    intent.putExtra("mileage", curDistance);
                                    intent.putExtra("lowspeed", lowSpeedTime);
                                    intent.putExtra("price", price);
                                    intent.putExtra("sumprice", sumPrice);
                                    intent.putExtra("orderNum", orderNum);
                                    intent.putExtra("lowcost", lowcost);
                                    intent.putExtra("addPrice1", addPriced[0]);
                                    intent.putExtra("addPrice2", addPriced[1]);
                                    intent.putExtra("addPrice3", addPriced[2]);
                                    startActivity(intent);
                                    finish();
                                    dialog.dismiss();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                VoiceUtil.startSpeaking(VoiceCommand.WAIT_FOR_PAY);
                            }

                            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                            @Override
                            public void onFailure(String error) {
                                Log.e("PickUpPassengerAct", "end order failure" + error);
                                if(error.contains("not Order")){ // 订单异常
                                    VoiceUtil.startSpeaking(VoiceCommand.ORDER_STATUS_EXCEPTION);
                                    finish();
                                }
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
            }
        });
    }



    private void initDialog() {
        dialog = new Dialog(context, getString(R.string.confirm_dialog_content));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setOnCancelButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                return;
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

                    CommonUtil.addTodayDoneWork();
                    CommonUtil.addTodayCash(Float.parseFloat(String.valueOf(price)));

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
                            VoiceUtil.startSpeaking(VoiceCommand.HOLD_CAR);
//                            startActivity(new Intent(context, MainActivity.class));
                            finish();
                            dialog.dismiss();
                        }
                    });

                    btnConfirm.setButtonText("继续听单");
                    btnConfirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
                            VoiceUtil.startSpeaking(VoiceCommand.WAIT_FOR_ORDER);
//                            startActivity(new Intent(context, MainActivity.class));
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == event.getKeyCode()
                || KeyEvent.KEYCODE_MENU == event.getKeyCode()) {
            return false;
        }
        return true;
    }

    public void addAdditionalCost(int feeType, int type){
        TextView[] addPriceText = new TextView[]{tvHeightSpeedCost, tvBridgeCost, tvParkCost};

        switch (type){
            case 1://+
                addPrice[feeType] += 1;
                break;
            case 0://-
                if(addPrice[feeType] >= 1){
                    addPrice[feeType] -= 1;
                }
                break;
            default:break;
        }

        if(addPrice[feeType] < 10){
            addPriceText[feeType].setText(addPrice[feeType] + ".0元");
        } else {
            addPriceText[feeType].setText(addPrice[feeType] + "元");
        }
    }
}
