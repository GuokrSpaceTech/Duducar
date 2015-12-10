package com.guokrspace.dududriver.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.views.ButtonRectangle;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.common.Constants;
import com.guokrspace.dududriver.common.NewOrderReceiver;
import com.guokrspace.dududriver.common.VoiceCommand;
import com.guokrspace.dududriver.model.OrderItem;
import com.guokrspace.dududriver.net.ResponseHandler;
import com.guokrspace.dududriver.net.SocketClient;
import com.guokrspace.dududriver.util.CommonUtil;
import com.guokrspace.dududriver.util.VoiceUtil;
import com.guokrspace.dududriver.view.CircleImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by hyman on 15/11/3.
 */
public class OrderDetailActivity extends BaseActivity{

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

    @OnClick(R.id.passenger_info)
    public void callPassenger() {
        String mobile = orderItem.getOrder().getPassenger_mobile();
        if (mobile != null && mobile.length() == 11) {
            VoiceUtil.startSpeaking(VoiceCommand.CALL_PASSENEGER);
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mobile));
            if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                Toast.makeText(OrderDetailActivity.this, "未授权拨打电话,请在权限设置下进行修改", Toast.LENGTH_SHORT).show();
                return;
            } else {
                startActivity(callIntent);
            }
        } else {
            Log.e("PickUpPassengerActivity", "乘客手机号码问题" + mobile);
        }
    }
    @Bind(R.id.order_status)
    TextView tvOrderStatus;
    @Bind(R.id.bill_sum)
    TextView tvBillSum;
    @Bind(R.id.check_detail_ll)
    LinearLayout checkDetailLayout;
    @OnClick(R.id.check_detail_ll) public void goDetail(){
        //TODO 查看收费详细信息
    }
    @Bind(R.id.substitute_pay)
    ButtonFlat btnSubstitutePay;
    @OnClick(R.id.substitute_pay) public void driverPay(){
        //确认司机代付
        final AlertDialog.Builder alterDialog = new AlertDialog.Builder(OrderDetailActivity.this);
        alterDialog.setMessage("请务必在完成现金交易之后再选择代付, 点击确认后将由您完成线上支付!");
        alterDialog.setCancelable(true);

        alterDialog.setPositiveButton("确定代付", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                VoiceUtil.startSpeaking(VoiceCommand.DRIVER_PAY);
                //向服务器发起代付请求,无论成功失败都跳转到代付页面.
                SocketClient.getInstance().endOrderSelfPay(Integer.parseInt(orderItem.getOrder().getId()), cash + "", curDistance + "", new ResponseHandler(Looper.myLooper()) {
                    @Override
                    public void onSuccess(String messageBody) {
                    }

                    @Override
                    public void onFailure(String error) {
                    }

                    @Override
                    public void onTimeout() {
                    }
                });

                Intent intent = new Intent(context, PayCostActivity.class);
                intent.putExtra("orderItem", orderItem);
                intent.putExtra("price", cash);
                intent.putExtra("orderNum", orderNum);
                intent.putExtra("mileage", curDistance);
                startActivity(intent);

            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).show();
    }
    @Bind(R.id.over_order_acccept)
    ButtonRectangle btnOverOrderAcccept;
    @Bind(R.id.continue_order_acccept)
    ButtonRectangle btnContinueOrderAcccept;
    private Context context;

    private double cash;
    private OrderItem orderItem;
    private double curDistance;
    private String orderNum;
    private int lowSpeedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orderdetail);
        context = OrderDetailActivity.this;
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        orderItem = (OrderItem) bundle.get("orderItem");
        curDistance = bundle.getDouble("mileage");
        lowSpeedTime= bundle.getInt("lowspeed");
        cash = bundle.getDouble("price");
        orderNum = bundle.getString("orderNum");

        initView();
    }

    private void initView() {
        toolbar.setTitle("订单详情");
//        toolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.return_icon));
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setHomeButtonEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        tvMyPosition.setText(orderItem.getOrder().getStart());
        tvPassengerPosition.setText(orderItem.getOrder().getDestination());
        tvBillSum.setText(cash + "");

        btnOverOrderAcccept.setButtonText("收车");
        btnOverOrderAcccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtil.changeCurStatus(Constants.STATUS_HOLD);
                VoiceUtil.startSpeaking(VoiceCommand.HOLD_CAR);
                CommonUtil.addTodayDoneWork();
                CommonUtil.addTodayCash(Float.parseFloat(String.valueOf(cash)));
                finish();
            }
        });
        btnContinueOrderAcccept.setButtonText("继续听单!");
        btnContinueOrderAcccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
                VoiceUtil.startSpeaking(VoiceCommand.CONTINUE_WAIT);
                CommonUtil.addTodayDoneWork();
                CommonUtil.addTodayCash(Float.parseFloat(String.valueOf(cash)));
                finish();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        switch (payOver){
            case WAIT_FOR_PASS_PAY:
                tvOrderStatus.setText("未支付");
                break;
            case PASS_PAY_OVER:
                tvOrderStatus.setText("已支付");
                break;
            case DRIVER_PAY_OVER:
                tvOrderStatus.setText("代付完成");
                break;
            case WAIT_FOR_DRIVER_PAY:
                tvOrderStatus.setText("等待代付");
                break;
            default:
                break;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            CommonUtil.changeCurStatus(Constants.STATUS_HOLD);
            finish();
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == event.getKeyCode()
                || KeyEvent.KEYCODE_MENU == event.getKeyCode()) {
            return false;
        }
        return true;
    }

    public static void setPayOver(int pay){
        payOver = pay;
    }

    private static int payOver = 0;
    public static final int WAIT_FOR_PASS_PAY = 0;
    public static final int PASS_PAY_OVER = 1;
    public static final int DRIVER_PAY_OVER = 2;
    public static final int WAIT_FOR_DRIVER_PAY = 3;
}
