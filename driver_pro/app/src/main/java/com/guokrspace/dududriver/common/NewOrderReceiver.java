package com.guokrspace.dududriver.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

/**
 * Created by daddyfang on 15/12/7.
 */
public class NewOrderReceiver extends BroadcastReceiver {

    Handler mHandler;

    public NewOrderReceiver(Handler handler){
        mHandler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action){
            case Constants.ACTION_NEW_ORDER:
                mHandler.sendEmptyMessage(Constants.MESSAGE_NEW_ORDER);
                break;
            default:
                return;
        }
    }
}
