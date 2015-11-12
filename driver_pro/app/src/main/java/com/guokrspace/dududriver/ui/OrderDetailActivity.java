package com.guokrspace.dududriver.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.views.ButtonRectangle;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.common.Constants;
import com.guokrspace.dududriver.util.CommonUtil;
import com.guokrspace.dududriver.view.CircleImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by hyman on 15/11/3.
 */
public class OrderDetailActivity extends BaseActivity {

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
    @Bind(R.id.order_status)
    TextView tvOrderStatus;
    @Bind(R.id.bill_sum)
    TextView tvBillSum;
    @Bind(R.id.check_detail_ll)
    LinearLayout checkDetailLayout;
    @Bind(R.id.substitute_pay)
    ButtonFlat btnSubstitutePay;
    @Bind(R.id.over_order_acccept)
    ButtonRectangle btnOverOrderAcccept;
    @Bind(R.id.continue_order_acccept)
    ButtonRectangle btnContinueOrderAcccept;
    private Context context;
    @OnClick(R.id.continue_order_acccept) public void goBackHome() {
        CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orderdetail);
        context = OrderDetailActivity.this;
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        toolbar.setTitle("订单详情");
        toolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.return_icon));
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnOverOrderAcccept.setButtonText("收车");
        btnOverOrderAcccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtil.changeCurStatus(Constants.STATUS_HOLD);
                finish();
            }
        });
        btnContinueOrderAcccept.setButtonText("完成,继续听单!");

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
}
