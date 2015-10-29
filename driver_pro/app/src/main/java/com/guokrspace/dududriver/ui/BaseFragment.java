package com.guokrspace.dududriver.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.guokrspace.dududriver.DuduDriverApplication;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.util.CommonUtil;
import com.guokrspace.dududriver.view.LoadingDialog;

/**
 * Created by hyman on 15/10/22.
 */
public class BaseFragment extends Fragment {

    LoadingDialog loadingDialog;
    public static String TAG = "BaseFragment";
    DuduDriverApplication mApplication;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mApplication = DuduDriverApplication.getInstance();
    }

    @Override
    public void onDestroyView() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        super.onDestroyView();
    }

    Toast mToast;

    public void showToast(String text) {
        if (!TextUtils.isEmpty(text)) {
            if (mToast == null) {
                mToast = Toast.makeText(getActivity().getApplicationContext(), text,
                        Toast.LENGTH_SHORT);
            } else {
                mToast.setText(text);
            }
            mToast.show();
        }
    }

    public void showToast(int resId) {
        if (mToast == null) {
            mToast = Toast.makeText(getActivity().getApplicationContext(), resId,
                    Toast.LENGTH_SHORT);
        } else {
            mToast.setText(resId);
        }
        mToast.show();
    }

    public void showCustomToast(CharSequence text) {
        Toast toast = new Toast(getActivity());

        LayoutInflater inflate = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.de_ui_toast, null);
        toast.setView(v);
        TextView tv = (TextView) v.findViewById(android.R.id.message);
        tv.setText(text);

        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    protected void showLoadingDialog(Context contex, boolean show, boolean cancelable) {
        if (show) {
            if (contex != null) {
                if (loadingDialog == null) {
                    loadingDialog = new LoadingDialog(getActivity(), cancelable);
                }
                if (!loadingDialog.isShowing()) {
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

    protected boolean isNetworkAvailable() {
        boolean isNetConnected = CommonUtil.isNetworkAvailable(getActivity());
        if (!isNetConnected) {
            showToast("当前网络不可用，请检查您的网络...");
        }
        return isNetConnected;
    }
}
