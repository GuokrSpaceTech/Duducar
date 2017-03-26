package com.guokrspace.duducar;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;

import com.baidu.mapapi.SDKInitializer;
import com.guokrspace.duducar.communication.message.DriverDetail;
import com.guokrspace.duducar.database.DaoMaster;
import com.guokrspace.duducar.database.DaoSession;
import com.guokrspace.duducar.database.PersonalInformation;
import com.guokrspace.duducar.database.PersonalInformationDao;
import com.squareup.okhttp.OkHttpClient;
import com.zhy.http.okhttp.OkHttpClientManager;
import static com.guokrspace.duducar.communication.tools.RSAEncrypt.loadPublicKeyByFile;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by macbook on 15/10/13.
 */
public class DuduApplication extends Application{

    private SDKReceiver mReceiver;

    public DaoMaster.DevOpenHelper mDBhelper;
    public DaoMaster mDaoMaster;
    public DaoSession mDaoSession;

    public PersonalInformation mPersonalInformation;
    public DriverDetail mDriverDetail;

    public final String KEY_PATH="publicKey.keystore";

    private static DuduApplication instance = null;

    public static DuduApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // 初始化对OkHttpClientManager中的OkHttpClient对象的一些设置
        initOkHttpClient();

        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(this);

        // 注册 SDK 广播监听者
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE);
        mReceiver = new SDKReceiver();
        registerReceiver(mReceiver, iFilter);

        try {
            AssetManager am = getAssets();
            InputStream is = am.open(KEY_PATH);
            loadPublicKeyByFile(is);
        } catch (Exception e) {
            android.os.Process.killProcess(android.os.Process.myPid());
        }

        initDB();

        InitPersonalInformation();
    }

    private void initOkHttpClient() {
        OkHttpClient client = OkHttpClientManager.getInstance().getOkHttpClient();
        client.setConnectTimeout(10, TimeUnit.SECONDS);
        client.setWriteTimeout(10, TimeUnit.SECONDS);
        client.setReadTimeout(30, TimeUnit.SECONDS);
    }

    /**
     * 构造广播监听类，监听 SDK key 验证以及网络异常广播
     */
    public class SDKReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();

            if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
            } else if (s.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
            }
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(mReceiver);
    }

    public void initDB() {
        SQLiteDatabase db;
        if (mDBhelper != null) mDBhelper.close();

        mDBhelper = new DaoMaster.DevOpenHelper(this, "Duducar-db", null);

        db = mDBhelper.getWritableDatabase();

        mDaoMaster = new DaoMaster(db);
        mDaoSession = mDaoMaster.newSession();
    }

    public boolean InitPersonalInformation() {
        boolean retCode = false;
        PersonalInformationDao personDao = mDaoSession.getPersonalInformationDao();
        List persons = personDao.queryBuilder().list();
        if (persons.size() != 0) {
            mPersonalInformation = (PersonalInformation) persons.get(0);
            retCode = true;
        }

        return retCode;
    }
}
