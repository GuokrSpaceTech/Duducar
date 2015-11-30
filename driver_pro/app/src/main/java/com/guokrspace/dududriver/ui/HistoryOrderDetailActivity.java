package com.guokrspace.dududriver.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonFlat;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.common.VoiceCommand;
import com.guokrspace.dududriver.database.OrderRecord;
import com.guokrspace.dududriver.util.VoiceUtil;
import com.guokrspace.dududriver.view.CircleImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by hyman on 15/11/26.
 */
public class HistoryOrderDetailActivity extends BaseActivity {

    @Bind(R.id.history_detail_toolbar)
    Toolbar mToolbar;
    @Bind(R.id.passenger_avatar)
    CircleImageView passengerAvatar;
    @Bind(R.id.start_textview)
    TextView startTextView;
    @Bind(R.id.destination_textview)
    TextView destinationTextView;
    @Bind(R.id.call_passenger_btn)
    ImageButton callPassengerButton;
    @Bind(R.id.order_status)
    TextView statusTextView;
    @Bind(R.id.bill_sum)
    TextView sumPriceTextView;
    @Bind(R.id.check_detail_ll)
    LinearLayout checkDetailLayout;
    @Bind(R.id.substitute_pay)
    ButtonFlat substitutePayButton;
    @Bind(R.id.send_complain_btn)
    Button complainButton;
    @OnClick(R.id.send_complain_btn)
    public void enterComplainPage() {
        startActivity(new Intent(context, ComplainActivity.class));
    }
    @OnClick(R.id.call_passenger_btn)
    public void callPassenger() {
        String mobile = orderDetail.getPassenger_mobile();
        if(mobile != null && mobile.length() == 11){
            VoiceUtil.startSpeaking(VoiceCommand.CALL_PASSENEGER);
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mobile));
            startActivity(callIntent);
        } else {
            Log.e("PickUpPassengerActivity", "乘客手机号码问题" + mobile);
        }
    }

    private Context context;

    private OrderRecord orderDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historyorderdetail);
        ButterKnife.bind(this);
        context = this;
        Intent intent = getIntent();
        orderDetail = (OrderRecord) intent.getSerializableExtra("orderDetail");
        initView();
    }

    private void initView() {
        mToolbar.setTitle("");
        mToolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.return_white));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (orderDetail != null) {
            startTextView.setText(orderDetail.getStart());
            destinationTextView.setText(orderDetail.getDestination());
            int status = 0;
            if (orderDetail.getStatus() != null) {
                status = Integer.parseInt(orderDetail.getStatus());
            }
            String statusStr = "";
            // TODO: 确定status取值和账单支付情况的对应
            switch (status) {
                case 4:
                    statusStr = "已支付";
                    substitutePayButton.setVisibility(View.INVISIBLE);
                    break;
                case 5:
                    statusStr = "已取消";
                    substitutePayButton.setVisibility(View.INVISIBLE);
                    break;
                default:
                    statusStr = "未支付";
                    break;
            }
            statusTextView.setText(statusStr);
            sumPriceTextView.setText(orderDetail.getSumprice());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
