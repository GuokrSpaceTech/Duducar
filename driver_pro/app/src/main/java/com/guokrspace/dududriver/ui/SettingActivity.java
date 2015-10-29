package com.guokrspace.dududriver.ui;

import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by hyman on 15/10/24.
 */
public class SettingActivity extends BaseActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RelativeLayout rl = new RelativeLayout(this);
        TextView textView = new TextView(this);
        textView.setText("setting");
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -1);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        textView.setLayoutParams(layoutParams);
        rl.addView(textView);
        setContentView(rl);

    }
}
