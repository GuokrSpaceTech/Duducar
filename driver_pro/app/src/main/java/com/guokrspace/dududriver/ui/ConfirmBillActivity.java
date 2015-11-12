package com.guokrspace.dududriver.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.widgets.Dialog;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.common.Constants;
import com.guokrspace.dududriver.model.OrderItem;
import com.guokrspace.dududriver.net.ResponseHandler;
import com.guokrspace.dududriver.net.SocketClient;
import com.guokrspace.dududriver.net.message.MessageTag;
import com.guokrspace.dududriver.util.CommonUtil;
import com.guokrspace.dududriver.util.SharedPreferencesUtils;
import com.guokrspace.dududriver.view.CircleImageView;

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
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnConfirm.setButtonText("确认账单");

        Bundle bundle = getIntent().getExtras();
        orderItem = (OrderItem) bundle.get("orderItem");
        tvMyPosition.setText(orderItem.getOrder().getStart());
        tvPassengerPosition.setText(orderItem.getOrder().getDestination());

        initDialog();
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                dialog.getButtonAccept().setButtonText("等待付款");
                dialog.getButtonCancel().setButtonText("自己支付");
                dialog.getButtonAccept().setClickable(false);
                dialog.getButtonAccept().setEnabled(false);
                dialog.setCancelable(false);

                OnKeyListener keylistener = new OnKeyListener(){
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode==KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==0) {
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
        dialog.setCancelable(false);
        dialog.setOnCancelButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 进入付款详情界面，乘客未付款则需要司机代付
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
                    dialog.getButtonAccept().setButtonText("完成收款");
                    dialog.getButtonAccept().setClickable(true);
                    dialog.getButtonAccept().setEnabled(true);
                }
                break;
            default:
                return false;
        }
        return false;
    }
}
