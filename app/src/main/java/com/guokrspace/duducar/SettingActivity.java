package com.guokrspace.duducar;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.sevenheaven.iosswitch.ShSwitchView;

/**
 * Created by hyman on 15/12/3.
 */
public class SettingActivity extends AppCompatActivity {

    private Context context;

    private Toolbar mToolbar;
    private ShSwitchView switchView;


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
}
