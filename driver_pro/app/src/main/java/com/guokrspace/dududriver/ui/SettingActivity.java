package com.guokrspace.dududriver.ui;

import android.graphics.Color;
import android.media.tv.TvContract;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.guokrspace.dududriver.view.ListenProgressView;

/**
 * Created by hyman on 15/10/24.
 */
public class SettingActivity extends BaseActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RelativeLayout rl = new RelativeLayout(this);
        rl.setBackgroundColor(Color.parseColor("#4C8D89"));
        TextView textView = new TextView(this);
        textView.setText("setting");
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -1);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        textView.setLayoutParams(layoutParams);
        rl.addView(textView);
        ListenProgressView progressView = new ListenProgressView(SettingActivity.this);
        RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(300, 300);
        layoutParams1.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressView.setLayoutParams(layoutParams1);
        rl.addView(progressView);
        setContentView(rl);

    }
}
