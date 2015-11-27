package com.guokrspace.dududriver.net.http;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.guokrspace.dududriver.R;
import com.squareup.okhttp.Request;
import com.zhy.http.okhttp.callback.ResultCallback;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by hyman on 15/11/23.
 */
public abstract class DuDuResultCallBack<T> extends ResultCallback<T> {


    MaterialDialog mMaterialDialog;

    public DuDuResultCallBack(Context context) {
        mMaterialDialog = new MaterialDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.progressbar_item, null, false);
        mMaterialDialog.setView(view);
    }

    @Override
    public void onBefore(Request request) {
        super.onBefore(request);
        if (mMaterialDialog != null) {
            mMaterialDialog.show();
        }

    }

    @Override
    public void onAfter() {
        super.onAfter();
        if (mMaterialDialog != null) {
            mMaterialDialog.dismiss();
        }
    }
}
