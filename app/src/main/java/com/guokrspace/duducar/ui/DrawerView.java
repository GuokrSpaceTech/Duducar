package com.guokrspace.duducar.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.guokrspace.duducar.DuduApplication;
import com.guokrspace.duducar.OrderHistoryActivity;
import com.guokrspace.duducar.PersonalInfoActivity;
import com.guokrspace.duducar.R;
import com.guokrspace.duducar.SettingActivity;
import com.guokrspace.duducar.database.PersonalInformation;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by hyman on 15/12/3.
 */
public class DrawerView implements View.OnClickListener {

    private DuduApplication mApplication;
    private final Activity activity;

    private PersonalInformation personalInformation;

    private RelativeLayout historyOrderItem;
    private RelativeLayout settingItem;
    private RelativeLayout noticeItem;
    private CircleImageView civAvatar;
    private TextView tvMoble;

    SlidingMenu localSlidingMenu;
    public DrawerView(Activity activity) {
        this.activity = activity;
        mApplication = (DuduApplication) activity.getApplicationContext();
        List<PersonalInformation> personalInformations = mApplication.mDaoSession.getPersonalInformationDao().queryBuilder().list();
        if (personalInformations.size() != 0) {
            personalInformation = mApplication.mDaoSession.getPersonalInformationDao().queryBuilder().list().get(0);
        }
    }

    public SlidingMenu initSlidingMenu() {
        localSlidingMenu = new SlidingMenu(activity);
        localSlidingMenu.setMode(SlidingMenu.LEFT);
        localSlidingMenu.setTouchModeAbove(SlidingMenu.SLIDING_WINDOW);
        localSlidingMenu.setTouchModeBehind(SlidingMenu.LEFT);
//        localSlidingMenu.setTouchModeBehind(SlidingMenu.TOUCHMODE_FULLSCREEN);
        localSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        localSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        localSlidingMenu.attachToActivity(activity, SlidingMenu.LEFT);
        localSlidingMenu.setFadeEnabled(true);
        localSlidingMenu.setFadeDegree(0.35f);
        localSlidingMenu.setMenu(R.layout.drawer_left_menu);
        localSlidingMenu.setOnOpenedListener(new SlidingMenu.OnOpenedListener() {
            public void onOpened() {

            }
        });
        localSlidingMenu.setOnClosedListener(new SlidingMenu.OnClosedListener() {

            @Override
            public void onClosed() {
                // TODO Auto-generated method stub

            }
        });
        initView();
        return localSlidingMenu;
    }

    private void initView() {
        civAvatar = (CircleImageView) localSlidingMenu.findViewById(R.id.my_avatar);
        tvMoble = (TextView) localSlidingMenu.findViewById(R.id.my_account);
        if (personalInformation != null) {
            tvMoble.setText(personalInformation.getMobile());
        }
        historyOrderItem = (RelativeLayout) localSlidingMenu.findViewById(R.id.history_orders_layout);
        settingItem = (RelativeLayout) localSlidingMenu.findViewById(R.id.setting_layout);
        noticeItem = (RelativeLayout) localSlidingMenu.findViewById(R.id.notice_layout);

        civAvatar.setOnClickListener(this);
        historyOrderItem.setOnClickListener(this);
        settingItem.setOnClickListener(this);
        noticeItem.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.my_avatar:
                enterActivity(PersonalInfoActivity.class);
                break;
            case R.id.history_orders_layout:
                enterActivity(OrderHistoryActivity.class);
                break;
            case R.id.setting_layout:
                enterActivity(SettingActivity.class);

                break;
            case R.id.notice_layout:

                break;

            default:
                break;
        }
    }

    private void enterActivity(Class<? extends AppCompatActivity> activityClass) {
        activity.startActivity(new Intent(activity, activityClass));
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * 更新menu界面中的信息
     */
    public void refreshMenuView() {
        List<PersonalInformation> personalInformations = mApplication.mDaoSession.getPersonalInformationDao().queryBuilder().list();
        if (personalInformations.size() != 0) {
            personalInformation = mApplication.mDaoSession.getPersonalInformationDao().queryBuilder().list().get(0);
        }
        if (personalInformation != null) {
            tvMoble.setText(personalInformation.getMobile());
        }
    }


}
