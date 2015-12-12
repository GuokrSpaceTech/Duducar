package com.guokrspace.duducar;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.sevenheaven.iosswitch.ShSwitchView;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

/**
 * Created by hyman on 15/12/3.
 */
public class SettingActivity extends AppCompatActivity implements View.OnClickListener{

    private Context context;

    private Toolbar mToolbar;
    private ShSwitchView switchView;
    private RelativeLayout soundSettingLayout;
    private RelativeLayout updateApkLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        context = this;
        initView();
    }

    private void initView() {
        //init toolbar
        initToolBar();
        switchView = (ShSwitchView) findViewById(R.id.switch_view);
        switchView.setOn(true, false);

        soundSettingLayout = (RelativeLayout) findViewById(R.id.sound_setting_layout);
        updateApkLayout = (RelativeLayout) findViewById(R.id.apk_update_layout);
        soundSettingLayout.setOnClickListener(this);
        updateApkLayout.setOnClickListener(this);

    }

    private void initToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingActivity.this.finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sound_setting_layout:
                switchView.setOn(!switchView.isOn(), true);
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
            default:
                break;
        }
    }

    private void showToast(String msg) {
        Toast toast = new Toast(context);
        toast.setText(msg);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }
}
