package com.guokrspace.dududriver.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.guokrspace.dududriver.R;

import java.util.Timer;
import java.util.TimerTask;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by hyman on 15/12/12.
 */
public class FeedBackActivity extends AppCompatActivity {

    private Context context;

    private Toolbar mToolbar;
    private EditText feedbackEditText;
    private ProgressBar mProgressBar;
    private Button publishButton;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mProgressBar.setVisibility(View.INVISIBLE);
                    final MaterialDialog dialog = new MaterialDialog(context);
                    dialog.setMessage("提交成功");
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.setPositiveButton("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FeedBackActivity.this.finish();
                        }
                    });
                    dialog.show();
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        context = FeedBackActivity.this;
        initView();
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FeedBackActivity.this.finish();
            }
        });

        feedbackEditText = (EditText) findViewById(R.id.feedback_edittext);
        feedbackEditText.requestFocus();

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        publishButton = (Button) findViewById(R.id.publish_button);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

                           public void run() {
                               InputMethodManager inputManager =
                                       (InputMethodManager) feedbackEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                               inputManager.showSoftInput(feedbackEditText, 0);
                           }

                       },
                1000);
    }

    /*
     *  提交反馈
     */
    public void publishFeedBack(View view) {
        if (TextUtils.isEmpty(feedbackEditText.getText())) {
            return;
        }
        mProgressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            int i = 0;

            @Override
            public void run() {
                while (i <= 2) {
                    i++;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                mHandler.sendEmptyMessage(1);
            }
        }).start();
    }
}
