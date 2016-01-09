package com.guokrspace.dududriver.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.guokrspace.dududriver.DuduDriverApplication;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.common.Constants;
import com.guokrspace.dududriver.database.PersonalInformation;
import com.guokrspace.dududriver.net.DuduService;
import com.guokrspace.dududriver.net.ResponseHandler;
import com.guokrspace.dududriver.net.SocketClient;
import com.guokrspace.dududriver.util.AppExitUtil;
import com.guokrspace.dududriver.util.CommonUtil;
import com.guokrspace.dududriver.util.SharedPreferencesUtils;
import com.guokrspace.dududriver.view.EditTextHolder;
import com.guokrspace.dududriver.view.LoadingDialog;
import com.guokrspace.dududriver.view.WinToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class LoginActivity extends BaseActivity implements
        View.OnClickListener, Handler.Callback,
        EditTextHolder.OnEditTextFocusChangeListener {
    private static final String TAG = "LoginActivity";
    @Bind(R.id.tv_regisister)
    TextView tvRegisister;
    @OnClick(R.id.tv_regisister) void r_click(){ changeTab(REGISTER); }
    @Bind(R.id.tv_login)
    TextView tvLogin;
    @OnClick(R.id.tv_login) void l_click(){ changeTab(LOGIN); }
    @Bind(R.id.tv_findpwd)
    TextView tvFindpwd;
    @OnClick(R.id.tv_findpwd) void p_click(){ changeTab(PWD); }
    @Bind(R.id.app_password_et)
    EditText appPasswordEt;
    @Bind(R.id.fr_pass_delete)
    FrameLayout frPassDelete;
    @Bind(R.id.ll_register)
    LinearLayout llRegister;
    @Bind(R.id.app_login_username_et)
    EditText appLoginUsernameEt;
    @Bind(R.id.fr_login_username_delete)
    FrameLayout frLoginUsernameDelete;
    @Bind(R.id.app_login_password_et)
    EditText appLoginPasswordEt;
    @Bind(R.id.fr_login_pass_delete)
    FrameLayout frLoginPassDelete;
    @Bind(R.id.app_sign_up_bt)
    Button appSignUpBt;
    @OnClick(R.id.app_sign_up_bt) void signup(){
        userName = appLoginUsernameEt.getEditableText().toString();
        final String passWord = appLoginPasswordEt.getEditableText().toString();
        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(passWord)) {
            WinToast.toast(this, R.string.login_erro_is_null);
            return;
        }
        if (mDialog != null && !mDialog.isShowing()) {
            mDialog.show();
        }

        messageid = SocketClient.getInstance().autoLoginRequestWithPwd(userName, "1", passWord, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                try {
                    String token = "";
                    JSONObject jsonObject = new JSONObject(messageBody);
                    if (jsonObject.has("new_token")) token = (String) jsonObject.get("new_token");
                    PersonalInformation person = new PersonalInformation();
                    person.setMobile(userName);
                    person.setPassword(passWord);
                    person.setToken(token); //Server didn't response Token yet
                    //TODO: 每次成功登陆之后,都自动切换当前的用户.直到下次用户手动切换用户
                    mApplication.mDaoSession.getPersonalInformationDao().deleteAll();
                    //添加新的当前用户信息
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

    }
    @Bind(R.id.ll_login)
    LinearLayout llLogin;
    @Bind(R.id.app_pwd_username_et)
    EditText appPwdUsernameEt;
    @Bind(R.id.fr_pwd_username_delete)
    FrameLayout frPwdUsernameDelete;
    @Bind(R.id.app_pwd_regcode_bt)
    Button appPwdRegcodeBt;
    @OnClick(R.id.app_pwd_regcode_bt) void pwd_regcode(){
        userName = appPwdUsernameEt.getEditableText().toString();
        if (TextUtils.isEmpty(userName)) {
            WinToast.toast(this, R.string.login_erro_is_null);
            return;
        }

        if (mDialog != null && !mDialog.isShowing()) {
            mDialog.show();
        }

        appPwdRegcodeBt.setEnabled(false);
        TimerTick(60, PWD);

        messageid = SocketClient.getInstance().sendRegcodeRequst(userName, "1", new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                Log.e("LoginActicity daddy", "success");
                mHandler.sendEmptyMessage(HANDLER_PWD_REGISTER_SUCCESS);
            }

            @Override
            public void onFailure(String error) {
                Log.e("LoginActivity daddy", "error");
                mHandler.sendEmptyMessage(HANDLER_PWD_REGISTER_FAILURE);
            }

            @Override
            public void onTimeout() {
                Log.e("LoginActivity daddy", "time out");
                mHandler.sendEmptyMessage(HANDLER_PWD_LOGIN_TIMEOUT);
            }
        });
    }
    @Bind(R.id.app_pwd_password_et)
    EditText appPwdPasswordEt;
    @Bind(R.id.fr_pwd_pass_delete)
    FrameLayout frPwdPassDelete;
    @Bind(R.id.app_pwd_register_et)
    EditText appPwdRegisterEt;
    @Bind(R.id.fr_pwd_register_delete)
    FrameLayout frPwdRegisterDelete;
    @Bind(R.id.app_sign_pwd_bt)
    Button appSignPwdBt;
    @OnClick(R.id.app_sign_pwd_bt) void signPwd() {
        userName = appPwdUsernameEt.getEditableText().toString();
        final String passWord = appPwdPasswordEt.getEditableText().toString();
        final String regCode = appPwdRegisterEt.getEditableText().toString();
        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(passWord) || TextUtils.isEmpty(regCode)) {
            WinToast.toast(this, R.string.login_erro_is_null);
            return;
        }
        if (mDialog != null && !mDialog.isShowing()) {
            mDialog.show();
        }

        messageid = SocketClient.getInstance().sendVerifyRequst(userName, "1", passWord, regCode, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                try {
                    String token = "";
                    JSONObject jsonObject = new JSONObject(messageBody);
                    if (jsonObject.has("token")) token = (String) jsonObject.get("token");
                    PersonalInformation person = new PersonalInformation();
                    person.setMobile(userName);
                    person.setPassword(passWord);
                    person.setToken(token); //Server didn't response Token yet
                    //TODO: 每次成功登陆之后,都自动切换当前的用户.直到下次用户手动切换用户
                    mApplication.mDaoSession.getPersonalInformationDao().deleteAll();
                    //添加新的当前用户信息
                    mApplication.mDaoSession.getPersonalInformationDao().insert(person);
                    mHandler.sendEmptyMessage(HANDLER_LOGIN_SUCCESS);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String error) {
                mHandler.sendEmptyMessage(HANDLER_RESET_FAILURE);
            }

            @Override
            public void onTimeout() {
                mHandler.sendEmptyMessage(HANDLER_PWD_LOGIN_TIMEOUT);
            }
        });

    }
    @Bind(R.id.ll_pwd)
    LinearLayout llPwd;

    private static int GRAY;
    private static int WHITE;


    private final int REGISTER = 0x01;
    private final int LOGIN = 0x02;
    private final int PWD = 0x03;


    private void changeTab(int type){
        reSetTab();
        switch (type){
            case REGISTER://选择注册
                llRegister.setVisibility(View.VISIBLE);
                tvRegisister.setTextColor(WHITE);
                break;
            case LOGIN://登陆
                llLogin.setVisibility(View.VISIBLE);
                tvLogin.setTextColor(WHITE);
                break;
            case PWD://重置密码
                llPwd.setVisibility(View.VISIBLE);
                tvFindpwd.setTextColor(WHITE);
                break;
            default:
                break;
        }
    }

    private void reSetTab(){
        llPwd.setVisibility(View.GONE);
        llLogin.setVisibility(View.GONE);
        llRegister.setVisibility(View.GONE);
        tvFindpwd.setTextColor(GRAY);
        tvLogin.setTextColor(GRAY);
        tvRegisister.setTextColor(GRAY);
    }
    /**
     * 用户账户
     */
    private EditText mUserNameEt;
    /**
     * 密码
     */
    private EditText mPassWordEt;
    private EditText mVerifyEt;
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
    private Thread thread2;
    private boolean threadStopFlag = false;
    private boolean threadStopFlag2  = false;

    private int messageid;

    /**
     * 第一次在手机上登陆需要验证码验证身份，验证成功就登陆成功了；
     * 之后如果在该手机重新进入应用直接后台进行登陆即可，不用在提交验证码验证。
     */
    private static final int HANDLER_LOGIN_SUCCESS = 1;//登陆成功
    private static final int HANDLER_LOGIN_FAILURE = 2;//登陆失败
    private static final int HANDLER_LOGIN_TIMEOUT = 0;//登陆超时

    private static final int HANDLER_REGISTER_SUCCESS = 7;//获取验证码成功
    private static final int HANDLER_REGISTER_FAILURE = 8;//获取验证码失败
    private static final int HANDLER_PWD_REGISTER_SUCCESS = 11;//重置密码获取验证码成功
    private static final int HANDLER_PWD_REGISTER_FAILURE = 12;//重置密码获取验证码失败
    private static final int HANDLER_PWD_LOGIN_TIMEOUT = 13;//超时
    private static final int HANDLER_RESET_FAILURE = 15;//设置密码失败



    private static final int HANDLER_LOGIN_HAS_FOCUS = 3;
    private static final int HANDLER_LOGIN_HAS_NO_FOCUS = 4;

    private static final int HANDLER_TIMERTICK = 5;
    private static final int HANDLER_TIMERTICK2 = 16;
    private static final int HANDLER_TIMER_TIMEOUT = 6;
    private static final int HANDLER_PWD_TIMER_TIMEOUT = 14;

    private static final int HANDLER_CHECK_CODE_SUCCESS = 9;//验证码校验成功
    private static final int HANDLER_CHECK_CODE_FAILURE = 10;//验证码校验失败

    private Handler mHandler;

    private ImageView mImgBackgroud;

    String userName;
    DuduDriverApplication mApplication;
    private LoadingDialog mDialog;
    private PersonalInformation person;
    private EditTextHolder mEditUserNameEt;
    private EditTextHolder mEditPassWordEt;
    private EditTextHolder mEditRegCodeEt;
    private EditTextHolder mEditLoginNameEt;
    private EditTextHolder mEditLoginPwdEt;
    private EditTextHolder mEditPWDNameEt;
    private EditTextHolder mEditPWDPwdEt;
    private EditTextHolder mEditPWDRecEt;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("daddy", "create");
        mApplication = (DuduDriverApplication) getApplicationContext();

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        GRAY = this.getResources().getColor(R.color.translucent_white);
        WHITE = this.getResources().getColor(R.color.de_action_white);

        initView();

        AppExitUtil.getInstance().addActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //加载用户密码
        List localUsers = DuduDriverApplication.getInstance().
                mDaoSession.getPersonalInformationDao().
                queryBuilder().list();
        if(localUsers.size() > 0) {
            //非注册
            changeTab(LOGIN);
            person = (PersonalInformation)localUsers.get(0);
            if(person.getMobile() != null) {
                appLoginUsernameEt.setText(person.getMobile());
                appPwdUsernameEt.setText(person.getMobile());
            }
            if(person.getPassword() != null) {
                appLoginPasswordEt.setText(person.getPassword());
            }
        } else {
            //第一次打开
            changeTab(REGISTER);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        Log.e("daddy", "stop");
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
        CommonUtil.changeCurStatus(Constants.STATUS_HOLD);

        mSoftManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mLoginImg = (ImageView) findViewById(R.id.de_login_logo);
        mUserNameEt = (EditText) findViewById(R.id.app_username_et);
        mPassWordEt = (EditText) findViewById(R.id.app_password_et);
        mVerifyEt = (EditText) findViewById(R.id.app_password_verify_et);
        mSignInBt = (Button) findViewById(R.id.app_sign_in_bt);
        mRegcodeBt = (Button) findViewById(R.id.app_regcode_bt);
        mImgBackgroud = (ImageView) findViewById(R.id.de_img_backgroud);
        mFrUserNameDelete = (FrameLayout) findViewById(R.id.fr_username_delete);
        mFrPasswordDelete = (FrameLayout) findViewById(R.id.fr_pass_verify_delete);

        mSignInBt.setOnClickListener(this);
        mRegcodeBt.setOnClickListener(this);

        mHandler = new Handler(this);
        mDialog = new LoadingDialog(this);

        mEditUserNameEt = new EditTextHolder(mUserNameEt, mFrUserNameDelete, null);
        mEditPassWordEt = new EditTextHolder(mVerifyEt, mFrPasswordDelete, null);
        mEditRegCodeEt = new EditTextHolder(mPassWordEt, frPassDelete, null);
        mEditLoginNameEt = new EditTextHolder(appLoginUsernameEt, frLoginUsernameDelete, null);
        mEditLoginPwdEt = new EditTextHolder(appLoginPasswordEt, frLoginPassDelete, null);
        mEditPWDNameEt = new EditTextHolder(appPwdUsernameEt, frPwdUsernameDelete, null);
        mEditPWDPwdEt = new EditTextHolder(appPwdPasswordEt, frPwdPassDelete, null);
        mEditPWDRecEt = new EditTextHolder(appPwdRegisterEt, frPwdRegisterDelete, null);

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
                    Log.e("daddy regist", "dialog is showing ");
                    mDialog.show();
                }

                mRegcodeBt.setEnabled(false);
                TimerTick(60, REGISTER);

                messageid = SocketClient.getInstance().sendRegcodeRequst(userName, "1", new ResponseHandler(Looper.myLooper()) {
                    @Override
                    public void onSuccess(String messageBody) {
                        Log.e("LoginActicity daddy", "success");
                        mHandler.sendEmptyMessage(HANDLER_REGISTER_SUCCESS);
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e("LoginActivity daddy", "error");
                        mHandler.sendEmptyMessage(HANDLER_REGISTER_FAILURE);
                    }

                    @Override
                    public void onTimeout() {
                        Log.e("LoginActivity daddy", "time out");
                        mHandler.sendEmptyMessage(HANDLER_LOGIN_TIMEOUT);
                    }
                });


                break;
            case R.id.app_sign_in_bt://提交验证码获得token，验证成功同时就表示登陆成功了
                userName = mUserNameEt.getEditableText().toString();
                final String passWord = mPassWordEt.getEditableText().toString();
                final String verifyWord = mVerifyEt.getEditableText().toString();
                if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(passWord) || TextUtils.isEmpty(verifyWord)) {
                    WinToast.toast(this, R.string.login_erro_is_null);
                    return;
                }
                if (mDialog != null && !mDialog.isShowing()) {
                    mDialog.show();
                }

                messageid = SocketClient.getInstance().sendVerifyRequst(userName, "1", passWord, verifyWord, new ResponseHandler(Looper.myLooper()) {
                    @Override
                    public void onSuccess(String messageBody) {
                        try {
                            String token = "";
                            JSONObject jsonObject = new JSONObject(messageBody);
                            if (jsonObject.has("token")) token = (String) jsonObject.get("token");
                            PersonalInformation person = new PersonalInformation();
                            person.setMobile(userName);
                            person.setPassword(passWord);
                            person.setToken(token); //Server didn't response Token yet
                            //TODO: 每次成功注册之后,都自动切换当前的用户.直到下次用户手动切换用户
                            mApplication.mDaoSession.getPersonalInformationDao().deleteAll();
                            //添加新的当前用户信息
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
//                threadStopFlag = true;
                mPassWordEt.requestFocus();
                break;
            case HANDLER_REGISTER_FAILURE:
                if (mDialog != null) mDialog.dismiss();
                threadStopFlag = true;
                WinToast.toast(LoginActivity.this, "获取验证码失败");
                break;
            case HANDLER_LOGIN_TIMEOUT:
                if (mDialog != null) mDialog.dismiss();
                WinToast.toast(LoginActivity.this, "连接服务器超时, 请检查网络");
                break;
            case HANDLER_PWD_REGISTER_SUCCESS:
                if (mDialog != null) mDialog.dismiss();
//                threadStopFlag = true;
                appPwdPasswordEt.requestFocus();
                break;
            case HANDLER_PWD_REGISTER_FAILURE:
                if (mDialog != null) mDialog.dismiss();
                threadStopFlag2 = true;
                WinToast.toast(LoginActivity.this, "获取验证码失败");
                break;
            case HANDLER_PWD_LOGIN_TIMEOUT:
                if (mDialog != null) mDialog.dismiss();
                WinToast.toast(LoginActivity.this, "连接服务器超时, 请检查网络");
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
            case HANDLER_RESET_FAILURE:
                if (mDialog != null) mDialog.dismiss();
                WinToast.toast(LoginActivity.this, R.string.reset_failure);
                break;
            case HANDLER_TIMERTICK:
                mRegcodeBt.setText((String) msg.obj);
                mRegcodeBt.setEnabled(false);
                break;
            case HANDLER_TIMERTICK2:
                appPwdRegcodeBt.setText((String) msg.obj);
                appPwdRegcodeBt.setEnabled(false);
                break;
            case HANDLER_TIMER_TIMEOUT:
                mRegcodeBt.setText("获取验证码");
                threadStopFlag = false;
                mRegcodeBt.setEnabled(true);
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                break;
            case HANDLER_PWD_TIMER_TIMEOUT:
                appPwdRegcodeBt.setText("获取验证码");
                threadStopFlag2 = false;
                appPwdRegcodeBt.setEnabled(true);
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
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
    protected void onDestroy() {
        threadStopFlag = true;
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {

            final AlertDialog.Builder alterDialog = new AlertDialog.Builder(this);
            alterDialog.setMessage("确定退出应用？");
            Log.e("daddy", "showdialog");
            alterDialog.setCancelable(true);

            alterDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.e("daddy", "exit");
                    stopService(new Intent(getApplicationContext(), DuduService.class));
                    AppExitUtil.getInstance().exit();
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

    private void TimerTick(final int max_seconds, final int type) {
        if (type == REGISTER) {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    int seconds_left = max_seconds;
                    while (seconds_left > 0) {
                        if (threadStopFlag) {
                            mHandler.sendEmptyMessage(HANDLER_TIMER_TIMEOUT);
                            return;
                        }
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
        } else {
            thread2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    int seconds_left = max_seconds;
                    while (seconds_left > 0) {
                        if (threadStopFlag2) {
                            mHandler.sendEmptyMessage(HANDLER_PWD_TIMER_TIMEOUT);
                            return;
                        }
                        seconds_left--;
                        mHandler.sendMessage(mHandler.obtainMessage(HANDLER_TIMERTICK2, seconds_left + "秒"));
                        try {
                            thread2.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    mHandler.sendEmptyMessage(HANDLER_PWD_TIMER_TIMEOUT);
                }
            });
            if (!thread2.isAlive()) {
                thread2.start();
            }
        }

    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        return;
    }
}
