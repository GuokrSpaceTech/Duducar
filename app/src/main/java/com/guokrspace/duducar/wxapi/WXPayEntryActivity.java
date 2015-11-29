package com.guokrspace.duducar.wxapi;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.guokrspace.duducar.AppExitUtil;
import com.guokrspace.duducar.PayCostActivity;
import com.guokrspace.duducar.R;
import com.guokrspace.duducar.communication.http.HttpUrls;
import com.guokrspace.duducar.communication.http.model.TradeResult;
import com.guokrspace.duducar.util.SharedPreferencesUtils;
import com.squareup.okhttp.Request;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.zhy.http.okhttp.callback.ResultCallback;
import com.zhy.http.okhttp.request.OkHttpRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.drakeet.materialdialog.MaterialDialog;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
	
	private static final String TAG = "WXPayEntryActivity";

	private Context mContext;
	
    private IWXAPI api;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_result);

		mContext = this;

    	api = WXAPIFactory.createWXAPI(this, WePayUtil.APP_ID);

        api.handleIntent(getIntent(), this);
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
	}

	/**
	 * 支付完成后，微信APP会返回到商户APP并回调onResp函数，
	 * 开发者需要在该函数中接收通知，判断返回错误码，
	 * 如果支付成功则去后台查询支付结果再展示用户实际支付结果。
	 * 注意一定不能以客户端返回作为用户支付的结果，
	 * 应以服务器端的接收的支付通知或查询API返回的结果为准。
	 * @param resp
	 */
	@Override
	public void onResp(BaseResp resp) {
		Log.d(TAG, "onPayFinish, errCode = " + resp.errCode);

		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			int code = resp.errCode;
			String msg = "";
			switch (code) {
				case 0:
					msg = "支付成功！";
					//去后台查询支付结果再展示用户实际支付结果
					Map<String, String> params = new HashMap<>();
					params.put("orderNum", (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.OUT_TRADE_NO, ""));
					Log.e("hyman_sid12", (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.COMFIRM_TRADE_RESULT_SID, ""));
					params.put("sid", (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.COMFIRM_TRADE_RESULT_SID, ""));
					final String finalMsg = msg;
					new OkHttpRequest.Builder().url(HttpUrls.getUrl(HttpUrls.WX_PAY_GETWXPAYRESULT)).params(params).post(new ResultCallback<TradeResult>() {
						@Override
						public void onError(Request request, Exception e) {
							Log.e(TAG, "onError, e = " + e.getLocalizedMessage());
						}

						@Override
						public void onResponse(final TradeResult tradeResult) {
							//对返回的数据进行判断，根据实际支付结果进行展示
							Log.e("hyman_result", tradeResult.modelToString());
							String trade_result = "";
							if (tradeResult.status == 1) {
								trade_result = "操作成功！已支付，请查看您的账单";
							} else if (tradeResult.status == -1) {
								trade_result = "支付失败可能是sid错误，一分钱都不给我！";
							}
							final MaterialDialog dialog = new MaterialDialog(WXPayEntryActivity.this);
							dialog.setMessage(trade_result);
							dialog.setTitle("支付详情");
							dialog.setPositiveButton("OK", new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									if (tradeResult.status == 1) {
										List<Activity> activities = AppExitUtil.getInstance().getActivityList();
										for (Activity activity : activities) {
											if (activity instanceof PayCostActivity) {
												activity.finish();
												break;
											}
										}
									}
									dialog.dismiss();
									WXPayEntryActivity.this.finish();
								}
							});
							dialog.show();
						}
					});
					break;
				case -1:
					msg = "支付失败！";
					break;
				case -2:
					msg = "您取消了支付！";
					break;
				default:
					msg = "支付失败！";
					break;
			}

		}
	}
}