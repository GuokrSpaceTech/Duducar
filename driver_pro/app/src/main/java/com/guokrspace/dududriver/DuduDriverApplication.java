package com.guokrspace.dududriver;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DatabaseUtils;

import com.baidu.mapapi.SDKInitializer;
import com.guokrspace.dududriver.ui.DisplayUtil;

/**
 * Created by macbook on 15/10/13.
 */
public class DuduDriverApplication extends Application{


    private SDKReceiver mReceiver;

    private static DuduDriverApplication instance = null;

    public static DuduDriverApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        //初始化显示工具类
        DisplayUtil.init(getApplicationContext());
        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(this);

        // 注册 SDK 广播监听者
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE);
        mReceiver = new SDKReceiver();
        registerReceiver(mReceiver, iFilter);
    }

    /**
     * 构造广播监听类，监听 SDK key 验证以及网络异常广播
     */
    public class SDKReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();
//            Log.d(LTAG, "action: " + s);
//            TextView text = (TextView) findViewById(R.id.text_Info);
//            text.setTextColor(Color.RED);
            if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
//                text.setText("key 验证出错! 请在 AndroidManifest.xml 文件中检查 key 设置");
            } else if (s
                    .equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
//                text.setText("网络出错");
            }
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(mReceiver);
    }
}
