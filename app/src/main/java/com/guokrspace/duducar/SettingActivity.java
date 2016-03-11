package com.guokrspace.duducar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sevenheaven.iosswitch.ShSwitchView;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

import java.util.List;

/**
 * Created by hyman on 15/12/3.
 */
public class SettingActivity extends AppCompatActivity implements View.OnClickListener{

    private Context context;
    private DuduApplication mApplication;

    private Toolbar mToolbar;
    private ShSwitchView switchView;
    private TextView tvVersionName;

    private RelativeLayout soundSettingLayout;
    private RelativeLayout updateApkLayout;
    private RelativeLayout feedbackLayout;
    private RelativeLayout aboutUsLayout;
    private RelativeLayout passengerGuideLayout;
    private RelativeLayout legalItemLayout;
    private Button quitButton;
    private RelativeLayout commonAddrLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        context = this;
        mApplication = (DuduApplication) getApplication();
        initView();
    }

    private void initView() {
        //init toolbar
        initToolBar();
        switchView = (ShSwitchView) findViewById(R.id.switch_view);
        switchView.setOn(true, false);

        tvVersionName = (TextView) findViewById(R.id.version_name);
        tvVersionName.setText("(" + getAPKVersion(context) + ")");

        commonAddrLayout = (RelativeLayout) findViewById(R.id.common_address_layout);
        soundSettingLayout = (RelativeLayout) findViewById(R.id.sound_setting_layout);
        updateApkLayout = (RelativeLayout) findViewById(R.id.apk_update_layout);
        feedbackLayout = (RelativeLayout) findViewById(R.id.feedback_layout);
        aboutUsLayout = (RelativeLayout) findViewById(R.id.about_us_layout);
        passengerGuideLayout = (RelativeLayout) findViewById(R.id.passenger_guide_layout);
        legalItemLayout = (RelativeLayout) findViewById(R.id.legal_items_layout);
        quitButton = (Button) findViewById(R.id.quit_button);
        commonAddrLayout.setOnClickListener(this);
        soundSettingLayout.setOnClickListener(this);
        updateApkLayout.setOnClickListener(this);
        feedbackLayout.setOnClickListener(this);
        aboutUsLayout.setOnClickListener(this);
        passengerGuideLayout.setOnClickListener(this);
        legalItemLayout.setOnClickListener(this);
        quitButton.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
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
            case R.id.common_address_layout:
                startActivity(new Intent(this, CommonAddrActivity.class));
                break;
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
            case R.id.passenger_guide_layout:
                Intent guideIntent = new Intent(this, WebViewActivity.class);
                guideIntent.putExtra(WebViewActivity.WEBVIEW_TYPE, WebViewActivity.WEBVIEW_HELP);
                startActivity(guideIntent);
                break;
            case R.id.feedback_layout:
                startActivity(new Intent(this, FeedBackActivity.class));
                break;
            case R.id.legal_items_layout:
                Intent clauseIntent = new Intent(this, WebViewActivity.class);
                clauseIntent.putExtra(WebViewActivity.WEBVIEW_TYPE, WebViewActivity.WEBVIEW_CLAUSE);
                startActivity(clauseIntent);
                break;
            case R.id.about_us_layout:
                Intent aboutIntent = new Intent(this, WebViewActivity.class);
                aboutIntent.putExtra(WebViewActivity.WEBVIEW_TYPE, WebViewActivity.WEBVIEW_ABOUT);
                startActivity(aboutIntent);
                break;
            case R.id.quit_button:
                List<Activity> activities = AppExitUtil.getInstance().getActivityList();
                for (Activity activity : activities) {
                    if (activity instanceof PreOrderActivity) continue;
                    activity.finish();

                }
                //清空用户数据
                mApplication.mDaoSession.getPersonalInformationDao().deleteAll();
                //退出，跳到新的主界面
                startActivity(new Intent(context, PreOrderActivity.class));
                finish();
                break;
            default:
                break;
        }
    }

    private void showToast(String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /*
     * 返回当前应用的版本名称
     * @param context
     * @return
     */
    public String getAPKVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }
}
