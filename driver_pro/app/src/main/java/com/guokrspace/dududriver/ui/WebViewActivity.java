package com.guokrspace.dududriver.ui;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.guokrspace.dududriver.DuduDriverApplication;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.common.Constants;
import com.guokrspace.dududriver.common.NewOrderReceiver;
import com.guokrspace.dududriver.database.PersonalInformation;
import com.guokrspace.dududriver.util.CommonUtil;
import com.guokrspace.dududriver.util.SharedPreferencesUtils;

import java.util.List;

/**
 * Created by hyman on 15/12/19.
 */
public class WebViewActivity extends BaseActivity implements Handler.Callback{

    public static final String WEBVIEW_TYPE = "webview_type";
    public static final int WEBVIEW_CLAUSE = 100;
    public static final int WEBVIEW_CONTACT = 101;
    public static final int WEBVIEW_ABOUT = 102;
    public static final int  WEBVIEW_JOIN = 103;
    public static final int WEBVIEW_NEWS = 104;
    public static final int WEBVIEW_BILL = 105;
    public static final int WEBVIEW_DIVIDE = 106;
    public static final int WEBVIEW_ACHIEVEMENT = 107;
    public static final int WEBVIEW_PERSONAL = 108;


    private Context context;

    private WebView mWebView;
    private Toolbar mToolbar;
    private TextView titleTextView;
    private ProgressBar mProgressBar;

    private NewOrderReceiver receiver;
    private MainOrderDialog dialog;
    private Handler mHandler;

    private int type;

    private String noticeUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置风格为进度条
//        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_webview);
        context = WebViewActivity.this;
        Intent mIntent = getIntent();
        type = mIntent.getIntExtra(WEBVIEW_TYPE, 0);
        if (type >= 104) {
            noticeUrl = mIntent.getStringExtra("url");
        }

        mHandler = new Handler(this);
        initView();
    }

    private void initView() {

        //初始化toolbar
        initToolBar();

        String title = "";
        String key = "";
        switch (type) {
            case WEBVIEW_ABOUT:
                title = "关于嘟嘟";
                key = Constants.PREFERENCE_KEY_WEBVIEW_ABOUT;
                break;
            case WEBVIEW_CONTACT:
                title = "乘客指南";
                key = Constants.PREFERENCE_KEY_WEBVIEW_CONTACT;
                break;
            case WEBVIEW_CLAUSE:
                title = "法律条款";
                key = Constants.PREFERENCE_KEY_WEBVIEW_CLAUSE;
                break;
            case WEBVIEW_JOIN:
                title = "专车指南";
                key = Constants.PREFERENCE_KEY_WEBVIEW_JOIN;
                break;
            case WEBVIEW_NEWS:
                title = "嘟嘟播报";
                key = Constants.WEBVIEW_NOTICE;
                break;
            case WEBVIEW_BILL:
                title = "账单";
                key = Constants.PREFERENCE_KEY_WEBVIEW_BILLS;
                break;
            case WEBVIEW_ACHIEVEMENT:
                title = "成绩查询";
                key = Constants.PREFERENCE_KEY_WEBVIEW_PERFORMANCE;
                break;
            case WEBVIEW_DIVIDE:
                title = "分成";
                key = Constants.PREFERENCE_KEY_WEBVIEW_MONEY;
                break;
            case WEBVIEW_PERSONAL:
                title = "个人信息";
                key = Constants.PREFERENCE_KEY_WEBVIEW_PERSONAL;
                break;
            default:
                break;
        }

        titleTextView = (TextView) findViewById(R.id.title_textview);
        titleTextView.setText(title);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setMax(100);

        mWebView = (WebView) findViewById(R.id.webview);
        WebSettings settings = mWebView.getSettings();
        // 设置WebView属性，能够执行Javascript脚本
        settings.setJavaScriptEnabled(true);
        // 设置可以访问文件
        settings.setAllowFileAccess(true);
        //支持缩放
        settings.setSupportZoom(true);
        //启用内置缩放装置
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        settings.setDefaultFontSize(18);
        settings.setDefaultTextEncodingName("utf-8");

        mWebView.setWebViewClient(new WebViewClient() {
            //当点击链接的时候覆盖，而不是打开新的窗口
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;//事件流处理完毕
            }
        });

        //点击后退按钮，让webview后退一页
        mWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
                        mWebView.goBack();//后退
                        return true;
                    }
                }
                finish();
                return false;
            }
        });

        //当webview进度改变时更新窗口进度
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                /*//Activity的进度范围是0~10000之间，而webview进度是0~100，所以需要乘以100
                AboutActivity.this.setProgress(newProgress * 100);*/
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                } else {
                    if (mProgressBar.getVisibility() == View.INVISIBLE) {
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                    mProgressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
        if(!TextUtils.isEmpty(key) && !key.equals(Constants.WEBVIEW_NOTICE)){ // 其他
            noticeUrl = (String) SharedPreferencesUtils.getParam(context, key, "");
            if(key.equals(Constants.PREFERENCE_KEY_WEBVIEW_BILLS) || key.equals(Constants.PREFERENCE_KEY_WEBVIEW_MONEY) || key.equals(Constants.PREFERENCE_KEY_WEBVIEW_PERFORMANCE) || key.equals(Constants.PREFERENCE_KEY_WEBVIEW_PERSONAL)){
                List localUsers = DuduDriverApplication.getInstance().
                        mDaoSession.getPersonalInformationDao().
                        queryBuilder().list();
                if (localUsers != null && localUsers.size() > 0 && ((PersonalInformation)localUsers.get(0)).getToken() != null) {
                    PersonalInformation  userInfo = (PersonalInformation) localUsers.get(0);
                    noticeUrl +="?mobile="+userInfo.getMobile() + "&token="+userInfo.getToken();
                }
            }
        }
        Log.e("hyman_webview", noticeUrl);

        mWebView.loadUrl(noticeUrl);
        mWebView.requestFocus();
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
                WebViewActivity.this.finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBroadcastReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    //监听service传来的消息
    private void registerBroadcastReceiver(){
        receiver = new NewOrderReceiver(mHandler);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_NEW_ORDER);
        filter.setPriority(1000);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.clearCache(true);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case Constants.MESSAGE_NEW_ORDER:
                //有新的订单来了
                if(CommonUtil.getCurOrderItem() == null){
                    return false;
                }
                if(dialog == null || !dialog.isVisible()){
                    CommonUtil.changeCurStatus(Constants.STATUS_DEAL);
                    dialog = new MainOrderDialog(context, CommonUtil.getCurOrderItem());
                    dialog.show(getSupportFragmentManager(), "mainorderdialog");
                }
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        return;
    }
}
