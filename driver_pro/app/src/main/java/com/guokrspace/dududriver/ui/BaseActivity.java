package com.guokrspace.dududriver.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.guokrspace.dududriver.DuduDriverApplication;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.util.CommonUtil;
import com.guokrspace.dududriver.view.LoadingDialog;

/**
 * Created by hyman on 15/10/22.
 */
public class BaseActivity extends AppCompatActivity {

    LoadingDialog loadingDialog;
    public static String tag = "BaseActivity";
    DuduDriverApplication mApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = DuduDriverApplication.getInstance();

    }

    @Override
    protected void onDestroy() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }

        super.onDestroy();
    }

    public void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        Log.d(tag, msg);
    }

    Toast mToast;

    public void showToast(String text) {
        if (!TextUtils.isEmpty(text)) {
            if (mToast == null) {
                mToast = Toast.makeText(getApplicationContext(), text,
                        Toast.LENGTH_SHORT);
            } else {
                mToast.setText(text);
            }
            mToast.show();
        }
    }

    public void showToast(int resId) {
        if (mToast == null) {
            mToast = Toast.makeText(getApplicationContext(), resId,
                    Toast.LENGTH_SHORT);
        } else {
            mToast.setText(resId);
        }
        mToast.show();
    }

    public void showCustomToast(CharSequence text) {
        Toast toast = new Toast(getApplicationContext());

        LayoutInflater inflate = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.de_ui_toast, null);
        toast.setView(v);
        TextView tv = (TextView) v.findViewById(android.R.id.message);
        tv.setText(text);

        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    protected void showLoadingDialog(Context contex, boolean show,
                                     boolean cancelable) {
        if (show) {
            if (contex != null) {
                if (loadingDialog == null) {
                    loadingDialog = new LoadingDialog(this, cancelable);
                }
                if (!loadingDialog.isShowing() && !((Activity)contex).isFinishing()) {
                    loadingDialog.show();
                }
            }

        } else {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (loadingDialog != null
                                && loadingDialog.isShowing()) {
                            loadingDialog.dismiss();
                        }
                    }
                }, 200);
            }

        }
    }

    protected void showLoadingDialog(Context context, boolean show) {
        showLoadingDialog(context, show, false);
    }

    public void hideSoftInputView() {
        InputMethodManager manager = ((InputMethodManager) this
                .getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    protected boolean isNetworkAvailable() {
        boolean isNetConnected = CommonUtil.isNetworkAvailable(this);
        if (!isNetConnected) {
            showToast("当前网络不可用，请检查您的网络..");
        }
        return isNetConnected;
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        return;
    }
}
