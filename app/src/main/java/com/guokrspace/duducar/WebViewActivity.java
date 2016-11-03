package com.guokrspace.duducar;

import android.content.Context;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.guokrspace.duducar.util.SharedPreferencesUtils;
import com.guokrspace.duducar.util.Trace;
import com.umeng.analytics.MobclickAgent;

import org.apache.http.util.EncodingUtils;
import org.w3c.dom.Text;

/**
 * Created by hyman on 15/12/19.
 */
public class WebViewActivity extends AppCompatActivity {

    public static final String WEBVIEW_TYPE = "webview_type";
    public static final String WEBVIEW_URL = "webview_url";
    public static final int WEBVIEW_HELP = 100;
    public static final int WEBVIEW_CLAUSE = 101;
    public static final int WEBVIEW_ABOUT = 102;

    private Context context;

    private WebView mWebView;
    private Toolbar mToolbar;
    private TextView titleTextView;
    private ProgressBar mProgressBar;

    private int type;
    private String url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置风格为进度条
//        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_webview);
        context = WebViewActivity.this;
        Intent mIntent = getIntent();
        // 类型
        type = mIntent.getIntExtra(WEBVIEW_TYPE, 0);
        // url，如果传过来的url则url不为空
        url = mIntent.getStringExtra(WEBVIEW_URL);
        Trace.e(type + " " + url);
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
                key = SharedPreferencesUtils.URL_ABOUT;
                break;
            case WEBVIEW_HELP:
                title = "乘客指南";
                key = SharedPreferencesUtils.URL_HELP;
                break;
            case WEBVIEW_CLAUSE:
                title = "法律条款";
                key = SharedPreferencesUtils.URL_CLAUSE;
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
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    if (mProgressBar.getVisibility() == View.GONE) {
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                    mProgressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });


        if (!TextUtils.isEmpty(key)) {
            url = (String) SharedPreferencesUtils.getParam(context, key, "");
        }
        if (TextUtils.isEmpty(title)) {
            mToolbar.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(url)) {
            mWebView.loadUrl(url);
        }
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
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mWebView != null) {
            mWebView.clearCache(true);
        }
    }
}
