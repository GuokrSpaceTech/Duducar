package com.guokrspace.duducar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.guokrspace.duducar.alipay.PayResult;
import com.guokrspace.duducar.alipay.SignUtils;
import com.guokrspace.duducar.communication.ResponseHandler;
import com.guokrspace.duducar.communication.SocketClient;
import com.guokrspace.duducar.communication.message.MessageTag;
import com.guokrspace.duducar.communication.message.OrderDetail;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class AlipayActivity extends ActionBarActivity {

    Context mContext;
    // 商户PID
    public static final String PARTNER = "2088121002293318";
    // 商户收款账号
    public static final String SELLER = "1946742250@qq.com";
    // 商户私钥，pkcs8格式
    public static final String RSA_PRIVATE =
            "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAOpsoA3pTu1jxVg4" +
            "2GVX+niS2Y0UQHe4uxn5lIDiOOvOPhBDFrPhVi8s4RTWR89PGQSOLlH6CzyUXNwV" +
            "98aFrditHLNk+zuOetm/gRU7dzA9SsZTGQ2e2oT39EWft07R4WoZmXRP7B3Xp8U8" +
            "6U8rO578M8w4ZA1KHMwkxxjqhPJ9AgMBAAECgYBADnad1obOr1iZhs76wlOa5uWz" +
            "ezkyfbQCoQRHQ4myRaUH5I0rkgNu2KCYhQUSTNbVO9TEacLwRsopCYevI5AhAxxT" +
            "ANGoL4eeYSeaYZJoBiUeYu6UpX78Hhy/GWNVDFLkm42FT9Il3Zi0bf/jtg/mVmzK" +
            "k8NzA0ePf994ALvOMQJBAPxOKrViXOh64s3n3W3cZ3F+dXLsBWhnNOzYlT5cSGoQ" +
            "K4ud0bGGIDs7LQ72poJwARXd4H0ZwJR4rwMnoQbIFF8CQQDt2204/ndcwOx38iQs" +
            "V7AkpCrK1WEg11tK2lBJE3TiaiIoZhgbWNCc9ZJO79UeTuYku5MXx8XHuw+WZs23" +
            "MkajAkEAsknuRiSO8L09njE1uNdhxcKN7jq4i5E6xg86T0nY5hItI0jPkDnudsyX" +
            "R5amDVBmg/Q5GU3kV0Z8racIVAl40wJAfsm2YOkTyzdzVUSXj6N2WzG/NbukOJNT" +
            "MIVKwolChuY4Kvyw4PLo0KH+SWGCYtN/zhjGgaiVfq/x0SQfiAWerQJBAIVWunH9" +
            "KckyXpEIFhCeIbx5blSZ2OTcDzqm++GsjP9eFxDxluqSolglnaQpJEwg6PeoWhiw" +
            "kYSGL5z3CmCjQCI=";

    // 支付宝公钥
    public static final String RSA_PUBLIC =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDI6d306Q8fIfCOaTXyiUeJHkrIvYISRcc73s3vF1ZT7XN8RNPwJxo8pWaJMmvyTn9N4HQ632qJBVHf8sxHi/fEsraprwCtzvzQETrNRwVxLO5jVmRGi60j8Ue1efIlzPXV9je9mkjzOmdssymZkh2QhUrCmZYI/FCEa3/cNMW0QIDAQAB";

    private OrderDetail tripOverOrderDetail;
    private Button payButton;
    private TextView feeTextView;
    private android.support.v7.app.AlertDialog.Builder alterDialog;
    private static final int SDK_PAY_FLAG = 1;
    private static final int SDK_CHECK_FLAG = 2;

    private Handler mHandler= new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SDK_PAY_FLAG: {
                        PayResult payResult = new PayResult((String) msg.obj);

                        // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
                        String resultInfo = payResult.getResult();

                        String resultStatus = payResult.getResultStatus();

                        // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                        if (TextUtils.equals(resultStatus, "9000")) {
                            Toast.makeText(AlipayActivity.this, "支付成功", Toast.LENGTH_SHORT).show();

                            Long timestamp = System.currentTimeMillis();
                            SocketClient.getInstance().sendPayOverRequest(tripOverOrderDetail.getId(), timestamp, tripOverOrderDetail.getOrg_price(), 1 ,new ResponseHandler(Looper.getMainLooper()) {
                                @Override
                                public void onSuccess(String messageBody) {
                                    Log.i("","");
                                }

                                @Override
                                public void onFailure(String error) {
                                    Log.i("","");
                                }

                                @Override
                                public void onTimeout() {
                                    Log.i("","");
                                }
                            });
                            Intent intent = new Intent(mContext, RatingActivity.class);
                            intent.putExtra("order", tripOverOrderDetail);
                            startActivity(intent);

                            finish();
                        } else {
//                             判断resultStatus 为非“9000”则代表可能支付失败
                            // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                            if (TextUtils.equals(resultStatus, "8000")) {
                                Toast.makeText(AlipayActivity.this, "支付结果确认中",
                                        Toast.LENGTH_SHORT).show();
                            } else if(TextUtils.equals(resultStatus, "6001")) {
                                // 中途停止支付
                                Toast.makeText(AlipayActivity.this, "请尽快完成支付", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, RatingActivity.class);
                                intent.putExtra("order", tripOverOrderDetail);
                                startActivity(intent);

                                finish();
                            } else {
                                // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                                Toast.makeText(AlipayActivity.this, "支付失败", Toast.LENGTH_SHORT).show();

                            }
                        }
                        break;
                    }
                    case SDK_CHECK_FLAG: {
                        Toast.makeText(AlipayActivity.this, "检查结果为：" + msg.obj, Toast.LENGTH_SHORT).show();
                        break;
                    }
                    default:
                        break;
                }
            }
        };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alipay);

        mContext = this;

        //UI
        payButton = (Button)findViewById(R.id.buttonPayConfirm);
        feeTextView = (TextView)findViewById(R.id.textViewFee);

        //Get Arguments
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null)
        {
            tripOverOrderDetail = (OrderDetail) bundle.get("order");
            feeTextView.setText(tripOverOrderDetail.getOrg_price());
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        AppExitUtil.getInstance().addActivity(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //注册监听司机代付信息
        SocketClient.getInstance().registerServerMessageHandler(MessageTag.DRIVER_PAY, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                /*
                * orderId
                * */
                try {
                    JSONObject mDriverPay = new JSONObject(messageBody);
                    if(tripOverOrderDetail == null){
                        finish();
                    }
                    if (Integer.parseInt((String)mDriverPay.get("order_id")) != tripOverOrderDetail.getId()) {
                        //异常情况,不是目前处理订单的消息
                        return;
                    }
                    tripOverOrderDetail = null;
                    Toast.makeText(getApplicationContext(), "司机已代付!", Toast.LENGTH_LONG).show();
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(String error) {
            }
            @Override
            public void onTimeout() {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_alipay, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home)
        {
            alterDialog = new android.support.v7.app.AlertDialog.Builder(AlipayActivity.this);
            alterDialog.setMessage("选择稍后支付或司机代付").setPositiveButton(
                    "确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).show();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * call alipay sdk pay. 调用SDK支付
     */
    public void pay(View v) {
        if (TextUtils.isEmpty(PARTNER) || TextUtils.isEmpty(RSA_PRIVATE)
                || TextUtils.isEmpty(SELLER)) {
            new AlertDialog.Builder(this)
                    .setTitle("警告")
                    .setMessage("需要配置PARTNER | RSA_PRIVATE| SELLER")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialoginterface, int i) {
                                    finish();
                                }
                            }).show();
            return;
        }
        // 订单
        String orderInfo = getOrderInfo(tripOverOrderDetail);

        Log.e("daddy pay", orderInfo);
        // 对订单做RSA 签名
        String sign = sign(orderInfo);
        try {
            // 仅需对sign 做URL编码
            sign = URLEncoder.encode(sign, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // 完整的符合支付宝参数规范的订单信息
        final String payInfo = orderInfo + "&sign=\"" + sign + "\"&"
                + getSignType();

        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask alipay = new PayTask(AlipayActivity.this);

                // 调用支付接口，获取支付结果
                String result = alipay.pay(payInfo);

                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };

        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }


    /**
     * create the order info. 创建订单信息
     */
    public String getOrderInfo(OrderDetail tripOverOrderDetail){
        String notifyUrl = "http://120.24.237.15:81/api/Pay/getAlipayResult";

        // 签约合作者身份ID
        String orderInfo = "partner=" + "\"" + PARTNER + "\"";

        // 签约卖家支付宝账号
        orderInfo += "&seller_id=" + "\"" + SELLER + "\"";

        // 商户网站唯一订单号
        orderInfo += "&out_trade_no=" + "\"" + getOutTradeNo() + "\"";

        // 商品名称
        orderInfo += "&subject=" + "\"" + tripOverOrderDetail.getDestination() + "\"";

        // 商品详情
        orderInfo += "&body=" + "\"" +  tripOverOrderDetail.getOrderNum() + "\"";

        // 商品金额
        orderInfo += "&total_fee=" + "\"" + tripOverOrderDetail.getOrg_price() + "\"";

        // 服务器异步通知页面路径
        orderInfo += "&notify_url=" + "\"" + notifyUrl + "\"";

        // 服务接口名称， 固定值
        orderInfo += "&service=\"mobile.securitypay.pay\"";

        // 支付类型， 固定值
        orderInfo += "&payment_type=\"1\"";

        // 参数编码， 固定值
        orderInfo += "&_input_charset=\"utf-8\"";

        // 设置未付款交易的超时时间
        // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
        // 取值范围：1m～15d。
        // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
        // 该参数数值不接受小数点，如1.5h，可转换为90m。
        orderInfo += "&it_b_pay=\"30m\"";

        // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
        // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

        // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
//        orderInfo += "&return_url=\"\"";

        // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
        // orderInfo += "&paymethod=\"expressGateway\"";

        return orderInfo;
    }

    /**
     * get the out_trade_no for an order. 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
     */
    public String getOutTradeNo() {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss",
                Locale.getDefault());
        Date date = new Date();
        String key = format.format(date);

        Random r = new Random();
        key = key + r.nextInt();
        key = key.substring(0, 15);
        return key;
    }

    /**
     * sign the order info. 对订单信息进行签名
     * @param content
     *            待签名订单信息
     */
    public String sign(String content) {
        return SignUtils.sign(content, RSA_PRIVATE);
    }

    /**
     * get the sign type we use. 获取签名方式
     */
    public String getSignType() {
        return "sign_type=\"RSA\"";
    }
}
