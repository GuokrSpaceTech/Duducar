package com.guokrspace.duducar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_);

        Handler mHandler = new Handler();
        Log.e("Daddy", "first page");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, PreOrderActivity.class);
                startActivity(intent);
                finish();
            }
        }, 1200);
    }
}
