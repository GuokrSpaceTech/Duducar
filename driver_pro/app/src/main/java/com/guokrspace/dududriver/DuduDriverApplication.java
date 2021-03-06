package com.guokrspace.dududriver;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;

import com.baidu.mapapi.SDKInitializer;
import com.guokrspace.dududriver.database.DaoMaster;
import com.guokrspace.dududriver.database.DaoSession;
import com.guokrspace.dududriver.database.PersonalInformation;
import com.guokrspace.dududriver.database.PersonalInformationDao;
import com.guokrspace.dududriver.util.CommonUtil;
import com.guokrspace.dududriver.util.DisplayUtil;
import com.guokrspace.dududriver.util.VoiceUtil;
import static com.guokrspace.dududriver.util.RSAEncrypt.loadPublicKeyByFile;

import java.io.InputStream;
import java.util.List;

/**
 * Created by macbook on 15/10/13.
 */
public class DuduDriverApplication extends Application{

    public DaoMaster.DevOpenHelper mDBhelper;
    public DaoMaster mDaoMaster;
    public DaoSession mDaoSession;

    public PersonalInformation mPersonalInformation;

    private SDKReceiver mReceiver;

    public final String KEY_PATH="publicKey.keystore";

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
        //初始化语音播报类
        VoiceUtil.init(getApplicationContext());
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
            e.printStackTrace();
        }

            initDB();

        if(!CommonUtil.isGpsOpen(getApplicationContext())){
             //TODO GPS未打开

        }

        initPersonalInformation();
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

    public void initDB() {
        SQLiteDatabase db;
        if (mDBhelper != null) mDBhelper.close();

        mDBhelper = new DaoMaster.DevOpenHelper(this, "Duducar-db", null);

        db = mDBhelper.getWritableDatabase();

        mDaoMaster = new DaoMaster(db);
        mDaoSession = mDaoMaster.newSession();
    }

    public boolean initPersonalInformation() {
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
