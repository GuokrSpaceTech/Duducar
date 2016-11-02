package com.guokrspace.duducar;

import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.guokrspace.duducar.communication.ResponseHandler;
import com.guokrspace.duducar.communication.SocketClient;
import com.umeng.analytics.MobclickAgent;

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
        MobclickAgent.onResume(this);
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

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    /*
         *  提交反馈
         */
    public void publishFeedBack(View view) {
        String feedback = feedbackEditText.getText().toString().trim();
        if (TextUtils.isEmpty(feedback)) {
            showToast("反馈内容不可为空哦~");
            return;
        }

        publishButton.setEnabled(false);
//        isEnable = false;
        // 提交
        SocketClient.getInstance().publishFeedback(feedback, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                showFeedbackDialog("提交成功！");
            }

            @Override
            public void onFailure(String error) {
                showFeedbackDialog("提交失败，请稍后尝试！");
            }

            @Override
            public void onTimeout() {
                showFeedbackDialog("超时，请稍后尝试！");
            }
        });
//        mProgressBar.setVisibility(View.VISIBLE);
        /*new Thread(new Runnable() {
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
        }).start();*/
    }

    private void showFeedbackDialog(String msg) {
        mProgressBar.setVisibility(View.INVISIBLE);
        final MaterialDialog dialog = new MaterialDialog(context);
        dialog.setMessage(msg);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setPositiveButton("OK", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feedbackEditText.setText("");
                dialog.dismiss();
            }
        });
        dialog.show();
        publishButton.setEnabled(true);
    }

    private void showToast(String msg) {
       Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
