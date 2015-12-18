package com.guokrspace.dududriver.ui;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonRectangle;
import com.guokrspace.dududriver.DuduDriverApplication;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.common.Constants;
import com.guokrspace.dududriver.common.NewOrderReceiver;
import com.guokrspace.dududriver.util.CommonUtil;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;
import com.zcw.togglebutton.ToggleButton;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by hyman on 15/10/24.
 */
public class SettingActivity extends BaseActivity implements View.OnClickListener, Handler.Callback{

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.account_info_layout)
    RelativeLayout accountInfoLayout;
    @Bind(R.id.sound_setting_layout)
    RelativeLayout soundSettingLayout;
    @Bind(R.id.newest_version)
    TextView newestVersionTextView;
    @Bind(R.id.apk_update_layout)
    RelativeLayout apkUpdateLayout;
    @Bind(R.id.legal_items_layout)
    RelativeLayout legalItemsLayout;
    @Bind(R.id.about_us_layout)
    RelativeLayout aboutUsLayout;
    @Bind(R.id.feedback_layout)
    RelativeLayout feedbackLayout;
    @Bind(R.id.cantact_us_layout)
    RelativeLayout cantactUsLayout;
    @Bind(R.id.confirm_button)
    ButtonRectangle logoutButton;
    @Bind(R.id.togglebutton)
    ToggleButton mToggleButton;

    private Context context;
    private DuduDriverApplication mApplication;

    private boolean isOpenSound = true;

    private NewOrderReceiver receiver;
    private MainOrderDialog dialog;
    private Handler mHandler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        context = this;
        mApplication = DuduDriverApplication.getInstance();
        mHandler = new Handler(this);
        initView();
    }

    private void initView() {
        mToolbar.setTitle("");
        mToolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.return_black));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        accountInfoLayout.setOnClickListener(this);
        soundSettingLayout.setOnClickListener(this);
        apkUpdateLayout.setOnClickListener(this);
        feedbackLayout.setOnClickListener(this);
        cantactUsLayout.setOnClickListener(this);
        logoutButton.setOnClickListener(this);
        legalItemsLayout.setOnClickListener(this);
        aboutUsLayout.setOnClickListener(this);

        //一开始设置为音效开启
        mToggleButton.setToggleOn();
        mToggleButton.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                // TODO: 15/11/29 处理生效相关的逻辑
                isOpenSound = !isOpenSound;
                if (isOpenSound) {
                    showToast("语音提示已开启");
                } else {
                  showToast("语音提示已关闭");
                }
            }
        });

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
            case Constants.MESSAGE_NEW_ORDER:
                //有新的订单来了
                if(CommonUtil.getCurOrderItem() == null){
                    return false;
                }
                if(dialog == null || !dialog.isVisible()){
                    dialog = new MainOrderDialog(context, CommonUtil.getCurOrderItem());
                    dialog.show(getSupportFragmentManager(), "mainorderdialog");
                }
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.account_info_layout:

                break;
            case R.id.sound_setting_layout:
                mToggleButton.toggle();
                break;
            case R.id.apk_update_layout:
//                UmengUpdateAgent.setDefault();
                //请在调用update,forceUpdate,silentUpdate函数之前设置推广id
//                UmengUpdateAgent.setSlotId("54357");
                UmengUpdateAgent.setUpdateAutoPopup(false);
                UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
                    @Override
                    public void onUpdateReturned(int updateStatus,UpdateResponse updateInfo) {
                        switch (updateStatus) {
                            case UpdateStatus.Yes: // has update
                                UmengUpdateAgent.showUpdateDialog(context, updateInfo);
                                break;
                            case UpdateStatus.No: // has no update
                                showToast("没有更新");
                                break;
                            case UpdateStatus.NoneWifi: // none wifi
                                showToast("没有wifi连接， 只在wifi下更新");
                                break;
                            case UpdateStatus.Timeout: // time out
                                showToast("超时");
                                break;
                        }
                    }
                });
                UmengUpdateAgent.forceUpdate(context);
                break;
            case R.id.feedback_layout:
                startActivity(new Intent(this, FeedBackActivity.class));
                break;
            case R.id.cantact_us_layout:
                showToast("联系我们");
                break;
            case R.id.confirm_button:
                //清空用户数据
                mApplication.mDaoSession.getPersonalInformationDao().deleteAll();
                //退出，跳到登陆界面
                startActivity(new Intent(context, LoginActivity.class));
                finish();
                break;
            default:
                break;
        }
    }
}
