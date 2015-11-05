package com.guokrspace.dududriver.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;


import com.guokrspace.dududriver.DuduDriverApplication;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.database.PersonalInformation;
import com.guokrspace.dududriver.net.ResponseHandler;
import com.guokrspace.dududriver.net.SocketClient;
import com.guokrspace.dududriver.util.SharedPreferencesUtils;
import com.guokrspace.dududriver.view.EditTextHolder;
import com.guokrspace.dududriver.view.LoadingDialog;
import com.guokrspace.dududriver.view.WinToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class LoginActivity extends BaseActivity implements
        View.OnClickListener, Handler.Callback,
        EditTextHolder.OnEditTextFocusChangeListener {
    private static final String TAG = "LoginActivity";
    /**
     * 用户账户
     */
    private EditText mUserNameEt;
    /**
     * 密码
     */
    private EditText mPassWordEt;
    /**
     * 登录button
     */
    private Button mSignInBt;

    /**
     * 验证码Button
     */
    private Button mRegcodeBt;

    /**
     * 设备id
     */
    private String mDeviceId;
    /**
     * 忘记密码
     */
    /**
     * 输入用户名删除按钮
     */
    private FrameLayout mFrUserNameDelete;
    /**
     * 输入密码删除按钮
     */
    private FrameLayout mFrPasswordDelete;
    /**
     * logo
     */
    private ImageView mLoginImg;
    /**
     * 软键盘的控制
     */
    private InputMethodManager mSoftManager;

    private Thread thread;
    private boolean threadStopFlag = false;

    private int messageid;

    /**
     * 第一次在手机上登陆需要验证码验证身份，验证成功就登陆成功了；
     * 之后如果在该手机重新进入应用直接后台进行登陆即可，不用在提交验证码验证。
     */
    private static final int HANDLER_LOGIN_SUCCESS = 1;//登陆成功
    private static final int HANDLER_LOGIN_FAILURE = 2;//登陆失败

    private static final int HANDLER_REGISTER_SUCCESS = 7;//获取验证码成功
    private static final int HANDLER_REGISTER_FAILURE = 8;//获取验证码失败

    private static final int HANDLER_LOGIN_HAS_FOCUS = 3;
    private static final int HANDLER_LOGIN_HAS_NO_FOCUS = 4;

    private static final int HANDLER_TIMERTICK = 5;
    private static final int HANDLER_TIMER_TIMEOUT = 6;

    private static final int HANDLER_CHECK_CODE_SUCCESS = 9;//验证码校验成功
    private static final int HANDLER_CHECK_CODE_FAILURE = 10;//验证码校验失败

    private Handler mHandler;

    private ImageView mImgBackgroud;

    String userName;
    DuduDriverApplication mApplication;
    private LoadingDialog mDialog;
    private EditTextHolder mEditUserNameEt;
    private EditTextHolder mEditPassWordEt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = (DuduDriverApplication) getApplicationContext();

        setContentView(R.layout.activity_login);

        initView();
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    protected void onPause() {
        super.onPause();
        if (mSoftManager == null) {
            mSoftManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        if (getCurrentFocus() != null) {
            mSoftManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);// 隐藏软键盘
        }
    }

    protected void initView() {
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.hide();

        mSoftManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mLoginImg = (ImageView) findViewById(R.id.de_login_logo);
        mUserNameEt = (EditText) findViewById(R.id.app_username_et);
        mPassWordEt = (EditText) findViewById(R.id.app_password_et);
        mSignInBt = (Button) findViewById(R.id.app_sign_in_bt);
        mRegcodeBt = (Button) findViewById(R.id.app_regcode_bt);
        mImgBackgroud = (ImageView) findViewById(R.id.de_img_backgroud);
        mFrUserNameDelete = (FrameLayout) findViewById(R.id.fr_username_delete);
        mFrPasswordDelete = (FrameLayout) findViewById(R.id.fr_pass_delete);

        mSignInBt.setOnClickListener(this);
        mRegcodeBt.setOnClickListener(this);


        mHandler = new Handler(this);
        mDialog = new LoadingDialog(this);

        mEditUserNameEt = new EditTextHolder(mUserNameEt, mFrUserNameDelete, null);
        mEditPassWordEt = new EditTextHolder(mPassWordEt, mFrPasswordDelete, null);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Animation animation = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.translate_anim);
                mImgBackgroud.startAnimation(animation);
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.app_regcode_bt://获取验证码
                userName = mUserNameEt.getEditableText().toString();
                if (TextUtils.isEmpty(userName)) {
                    WinToast.toast(this, R.string.login_erro_is_null);
                    return;
                }

                if (mDialog != null && !mDialog.isShowing()) {
                    mDialog.show();
                }

                mRegcodeBt.setEnabled(false);
                TimerTick(60);

                messageid = SocketClient.getInstance().sendRegcodeRequst(userName, "1", new ResponseHandler(Looper.myLooper()) {
                    @Override
                    public void onSuccess(String messageBody) {
                        mHandler.sendEmptyMessage(HANDLER_REGISTER_SUCCESS);
                    }

                    @Override
                    public void onFailure(String error) {
                        mHandler.sendEmptyMessage(HANDLER_REGISTER_FAILURE);
                    }

                    @Override
                    public void onTimeout() {
                        mHandler.sendEmptyMessage(HANDLER_REGISTER_FAILURE);
                    }
                });


                break;
            case R.id.app_sign_in_bt://提交验证码获得token，验证成功同时就表示登陆成功了
                userName = mUserNameEt.getEditableText().toString();
                final String passWord = mPassWordEt.getEditableText().toString();
                if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(passWord)) {
                    WinToast.toast(this, R.string.login_erro_is_null);
                    return;
                }
                if (mDialog != null && !mDialog.isShowing()) {
                    mDialog.show();
                }

                messageid = SocketClient.getInstance().sendVerifyRequst(userName, "1", passWord, new ResponseHandler(Looper.getMainLooper()) {
                    @Override
                    public void onSuccess(String messageBody) {
                        try {
                            String token = "";
                            JSONObject jsonObject = new JSONObject(messageBody);
                            if (jsonObject.has("token")) token = (String) jsonObject.get("token");
                            PersonalInformation person = new PersonalInformation();
                            person.setMobile(userName);
                            person.setToken(token); //Server didn't response Token yet
                            mApplication.mDaoSession.getPersonalInformationDao().insert(person);
                            mHandler.sendEmptyMessage(HANDLER_LOGIN_SUCCESS);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        mHandler.sendEmptyMessage(HANDLER_LOGIN_FAILURE);
                    }

                    @Override
                    public void onTimeout() {
                        mHandler.sendEmptyMessage(HANDLER_LOGIN_FAILURE);
                    }
                });

                break;
            case R.id.app_username_et:
            case R.id.app_password_et:
                Message mess = Message.obtain();
                mess.what = HANDLER_LOGIN_HAS_FOCUS;
                mHandler.sendMessage(mess);
                break;

        }
    }

    @Override
    public void onEditTextFocusChange(View v, boolean hasFocus) {
        Message mess = Message.obtain();
        switch (v.getId()) {
            case R.id.app_username_et:
            case R.id.app_password_et:
                if (hasFocus) {
                    mess.what = HANDLER_LOGIN_HAS_FOCUS;
                }
                mHandler.sendMessage(mess);
                break;
        }
    }

    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what) {
            case HANDLER_REGISTER_SUCCESS:
                if (mDialog != null) mDialog.dismiss();
                mPassWordEt.requestFocus();
                break;
            case HANDLER_REGISTER_FAILURE:
                WinToast.toast(LoginActivity.this, "获取验证码失败");
                break;
            case HANDLER_CHECK_CODE_SUCCESS:
                //验证码提交正确，获得token进行自动登录
            case HANDLER_LOGIN_SUCCESS:
                if (mDialog != null) mDialog.dismiss();
                WinToast.toast(LoginActivity.this, R.string.login_success);
                //将sharedpreferences中是否登陆的状态改为true
                SharedPreferencesUtils.setParam(LoginActivity.this, SharedPreferencesUtils.LOGIN_STATE, true);
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
            case HANDLER_LOGIN_FAILURE:
                if (mDialog != null) mDialog.dismiss();
                WinToast.toast(LoginActivity.this, R.string.login_failure);
                break;
            case HANDLER_CHECK_CODE_FAILURE:
                if (mDialog != null) mDialog.dismiss();
                WinToast.toast(LoginActivity.this, R.string.check_code_failure);
                break;
            case HANDLER_TIMERTICK:
                mRegcodeBt.setText((String) msg.obj);
                mRegcodeBt.setEnabled(false);
                break;
            case HANDLER_TIMER_TIMEOUT:
                mRegcodeBt.setText("获取验证码");
                mRegcodeBt.setEnabled(true);
                break;
            case HANDLER_LOGIN_HAS_FOCUS:
                mLoginImg.setVisibility(View.GONE);
                break;
            case HANDLER_LOGIN_HAS_NO_FOCUS:
                mLoginImg.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
        return false;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {

            final AlertDialog.Builder alterDialog = new AlertDialog.Builder(this);
            alterDialog.setMessage("确定退出应用？");
            alterDialog.setCancelable(true);

            alterDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Process.killProcess(Process.myPid());
                }
            });
            alterDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alterDialog.show();
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                mSoftManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                Message mess = Message.obtain();
                mess.what = HANDLER_LOGIN_HAS_NO_FOCUS;
                mHandler.sendMessage(mess);
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        event.getKeyCode();
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_ESCAPE:
                Message mess = Message.obtain();
                mess.what = HANDLER_LOGIN_HAS_NO_FOCUS;
                mHandler.sendMessage(mess);
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    private void TimerTick(final int max_seconds) {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int seconds_left = max_seconds;
                while (seconds_left > 0 && !threadStopFlag) {
                    seconds_left--;
                    mHandler.sendMessage(mHandler.obtainMessage(HANDLER_TIMERTICK, seconds_left + "秒"));
                    try {
                        thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mHandler.sendEmptyMessage(HANDLER_TIMER_TIMEOUT);
            }
        });
        if (!thread.isAlive()) {
            thread.start();
        }
    }

}
