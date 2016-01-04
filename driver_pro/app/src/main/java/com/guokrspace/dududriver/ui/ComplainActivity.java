package com.guokrspace.dududriver.ui;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.views.ProgressBarIndeterminate;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.common.Constants;
import com.guokrspace.dududriver.common.NewOrderReceiver;
import com.guokrspace.dududriver.util.CommonUtil;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by hyman on 15/11/30.
 */
public class ComplainActivity extends BaseActivity implements Handler.Callback{

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.bad_manner_btn)
    Button badMannerBtn;
    @Bind(R.id.false_order_btn)
    Button falseOrderBtn;
    @Bind(R.id.overchange_btn)
    Button overchangeBtn;
    @Bind(R.id.detour_btn)
    Button detourBtn;
    @Bind(R.id.charge_timeout_btn)
    Button chargeTimeoutBtn;
    @Bind(R.id.break_order_btn)
    Button breakOrderBtn;
    @Bind(R.id.call_button)
    ButtonRectangle callButton;
    @Bind(R.id.progressBarIndeterminate)
    ProgressBarIndeterminate mProgressBar;

    @OnClick(R.id.call_button)
    public void callCustomService() {
        //拨打客服电话
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "4006887312"));
        startActivity(callIntent);
    }

    @OnClick({R.id.bad_manner_btn, R.id.false_order_btn, R.id.overchange_btn,
            R.id.detour_btn, R.id.charge_timeout_btn, R.id.break_order_btn})
    public void publishComplain(View v) {
        for (Button button : complainBtns) {
            if (button != v) {
                button.setClickable(false);
            }
        }
        mProgressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            int i = 0;
            @Override
            public void run() {
                while (i <= 2) {
                    i++;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                mHandler.sendEmptyMessage(1);
            }
        }).start();

    }
    private Context context;
    private NewOrderReceiver receiver;
    private MainOrderDialog dialog;
    private Button[] complainBtns;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complain);
        ButterKnife.bind(this);
        context = this;
        mHandler = new Handler(this);
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

        complainBtns = new Button[] {badMannerBtn, falseOrderBtn, overchangeBtn, detourBtn, chargeTimeoutBtn, breakOrderBtn};
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBroadcastReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    //监听service传来的消息
    private void registerBroadcastReceiver(){
        receiver = new NewOrderReceiver(mHandler);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_NEW_ORDER);
        filter.setPriority(1000);
        registerReceiver(receiver, filter);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case 0:
                break;
            case 1:
                mProgressBar.setVisibility(View.INVISIBLE);
                final MaterialDialog mDialog = new MaterialDialog(context);
                mDialog.setMessage("提交成功");
                mDialog.setCanceledOnTouchOutside(true);
                mDialog.setPositiveButton("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ComplainActivity.this.finish();
                    }
                });
                mDialog.show();
                break;
            case Constants.MESSAGE_NEW_ORDER:
                if(CommonUtil.getCurOrderItem() == null){
                    return false;
                }
                if(dialog == null || !dialog.isVisible()){
                    dialog = new MainOrderDialog(context, CommonUtil.getCurOrderItem());
                    dialog.show(getSupportFragmentManager(), "mainorderdialog");
                }
                break;
        }
        return false;
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        return;
    }
}
