package com.guokrspace.dududriver.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.guokrspace.dududriver.DuduDriverApplication;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.alipay.PayResult;
import com.guokrspace.dududriver.alipay.SignUtils;
import com.guokrspace.dududriver.common.Constants;
import com.guokrspace.dududriver.common.VoiceCommand;
import com.guokrspace.dududriver.database.PersonalInformation;
import com.guokrspace.dududriver.model.OrderItem;
import com.guokrspace.dududriver.net.ResponseHandler;
import com.guokrspace.dududriver.net.SocketClient;
import com.guokrspace.dududriver.net.http.DuDuResultCallBack;
import com.guokrspace.dududriver.net.http.HttpUrls;
import com.guokrspace.dududriver.net.http.model.UnifiedorderResp;
import com.guokrspace.dududriver.util.AppExitUtil;
import com.guokrspace.dududriver.util.CommonUtil;
import com.guokrspace.dududriver.util.VoiceUtil;
import com.guokrspace.dududriver.wxapi.WePayUtil;
import com.squareup.okhttp.Request;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.zhy.http.okhttp.request.OkHttpRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class PayCostActivity extends ActionBarActivity implements View.OnClickListener{

    public static final String TAG = PayCostActivity.class.getSimpleName();

    Context mContext;
    /*  微信支付相关  */
    PayReq req;
    final IWXAPI msgApi = WXAPIFactory.createWXAPI(this, null);

    String body;
    String tradeNo;
    String totalFee;

    PersonalInformation person;

    /*  支付宝支付相关  */
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

    private OrderItem tripOverOrderDetail;
    private double price;
    private double mileage;
    private String orderNum;
    private Button payButton;
    private TextView feeTextView;
    private RelativeLayout alipayLayout;
    private RelativeLayout wxpayLayout;
    private RadioButton aliRadioButton;
    private RadioButton wxRadioButton;
    private RadioButton checkedRadioButton;

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
                        Toast.makeText(PayCostActivity.this, "代付成功", Toast.LENGTH_SHORT).show();
                        if(CommonUtil.curOrderId == Integer.parseInt(tripOverOrderDetail.getOrder().getId())){
                            CommonUtil.curOrderStatus = 5;
                        }
                        CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
                        finish();
                    } else {
//                             判断resultStatus 为非“9000”则代表可能支付失败
                        // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        if (TextUtils.equals(resultStatus, "8000")) {
                            Toast.makeText(PayCostActivity.this, "支付结果确认中",
                                    Toast.LENGTH_SHORT).show();
                        } else if(TextUtils.equals(resultStatus, "6001")) {
                            // 中途停止支付
                            Toast.makeText(PayCostActivity.this, "请尽快完成代付", Toast.LENGTH_SHORT).show();
                            CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
                            finish();
                        } else {
                            // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                            Toast.makeText(PayCostActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                }
                case SDK_CHECK_FLAG: {
                    Toast.makeText(PayCostActivity.this, "检查结果为：" + msg.obj, Toast.LENGTH_SHORT).show();
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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_alipay);

        mContext = this;

        req = new PayReq();
        //注册应用
        msgApi.registerApp(WePayUtil.APP_ID);

        //UI
        payButton = (Button)findViewById(R.id.buttonPayConfirm);
        feeTextView = (TextView)findViewById(R.id.textViewFee);
        aliRadioButton = (RadioButton) findViewById(R.id.radioButtonAlipay);
        wxRadioButton = (RadioButton) findViewById(R.id.radioButtonWechat);
        alipayLayout = (RelativeLayout) findViewById(R.id.alipay_layout);
        wxpayLayout = (RelativeLayout) findViewById(R.id.wxpay_layout);
        alipayLayout.setOnClickListener(this);
        wxpayLayout.setOnClickListener(this);

        if (aliRadioButton.isChecked()) {
            checkedRadioButton = aliRadioButton;
        } else if (wxRadioButton.isChecked()) {
            checkedRadioButton = wxRadioButton;
        }


        //Get Arguments
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null)
        {
            tripOverOrderDetail = (OrderItem) bundle.get("orderItem");
            price = bundle.getDouble("sumprice");
            mileage = bundle.getDouble("mileage");
            orderNum = bundle.getString("orderNum");
            feeTextView.setText(price+"");
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        AppExitUtil.getInstance().addActivity(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
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
            CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * call alipay sdk pay. 调用SDK支付
     */
    public void pay(View v) {
        SocketClient.getInstance().checkIfPaid(Integer.parseInt(tripOverOrderDetail.getOrder().getId()), new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                //未支付
                if (aliRadioButton.isChecked()) {
                    aliPay();
                } else if (wxRadioButton.isChecked()) {
                    weixinPay();
                }
            }

            @Override
            public void onFailure(String error) {
                if(error.contains("login")){
                    // 账户问题
                    Toast.makeText(PayCostActivity.this, "正在连接服务器, 请稍后尝试..", Toast.LENGTH_SHORT).show();
                } else {
                    // 已支付
                    Toast.makeText(PayCostActivity.this, "订单已支付", Toast.LENGTH_SHORT).show();
                    VoiceUtil.startSpeaking(VoiceCommand.ORDER_PAID);
                    CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
                    finish();
                }
            }

            @Override
            public void onTimeout() {

            }
        });

    }

    private void weixinPay() {
        //1、 通过socket获取到订单号，cmd: order_end
        if (tripOverOrderDetail != null) {
            body = tripOverOrderDetail.getOrder().getDestination() + "，行程" + CommonUtil.formatDecimal(mileage + "") + "公里";
//            tradeNo = tripOverOrderDetail.getOrder().getId();
            // TODO:这里应为sumprice
            totalFee = price+"";
        }
        //2、 发起http请求，获得预支付id和签名， 请求参数body、tradeno、total_fee
        Map<String, String> params = new HashMap<>();
//        Log.e("daddy trade", orderNum);
        params.put("orderNum", orderNum);
        params.put("body", body);
        params.put("total_fee", totalFee);
        List persons = ((DuduDriverApplication)getApplicationContext()).mDaoSession.getPersonalInformationDao().queryBuilder().limit(1).list();
        if(persons.size()==1) {
            person = (PersonalInformation) persons.get(0);
        }
        params.put("token", person.getToken());
        params.put("mobile", person.getMobile());
        params.put("role", "1");
        Log.e("weixinpay", orderNum + " " + body + " " + totalFee + " " + person.getToken() + " " + person.getMobile());

        new OkHttpRequest.Builder().url(HttpUrls.getUrl(HttpUrls.WX_PAY_WXUNIFIEDORDER)).params(params).post(new DuDuResultCallBack<UnifiedorderResp>(mContext) {
            @Override
            public void onError(Request request, Exception e) {
                Log.e(TAG, "onError, e = " + e.getLocalizedMessage());
            }

            @Override
            public void onResponse(UnifiedorderResp unifiedorderResp) {
                //3、 发起支付请求
                Log.e("TAG", unifiedorderResp.toString());
                if (unifiedorderResp.status == -1) {
                    Toast.makeText(mContext, "用户信息验证失败或者微信请求失败；", Toast.LENGTH_SHORT).show();
                    return;
                }
                req.appId = WePayUtil.APP_ID;
                req.partnerId = WePayUtil.MCH_ID;
                req.prepayId = unifiedorderResp.prepayid;
                req.packageValue = "Sign=WXPay";//固定值
                req.nonceStr = unifiedorderResp.noncestr;
                req.timeStamp = unifiedorderResp.timestamp;
                req.sign = unifiedorderResp.sign;
                msgApi.registerApp(WePayUtil.APP_ID);
                msgApi.sendReq(req);
                Log.e("hyman123", "req=" + req.checkArgs());
            }
        });



    }

    private void aliPay() {
        if (TextUtils.isEmpty(PARTNER) || TextUtils.isEmpty(RSA_PRIVATE)
                || TextUtils.isEmpty(SELLER)) {
            new AlertDialog.Builder(this)
                    .setTitle("警告")
                    .setMessage("需要配置PARTNER | RSA_PRIVATE| SELLER")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialoginterface, int i) {
                                    CommonUtil.changeCurStatus(Constants.STATUS_WAIT);
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
                PayTask alipay = new PayTask(PayCostActivity.this);

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
    public String getOrderInfo(OrderItem tripOverOrderDetail){
        String notifyUrl = "http://120.24.237.15:81/api/Pay/getAlipayResult";

        // 签约合作者身份ID
        String orderInfo = "partner=" + "\"" + PARTNER + "\"";

        // 签约卖家支付宝账号
        orderInfo += "&seller_id=" + "\"" + SELLER + "\"";

        // 商户网站唯一订单号
        orderInfo += "&out_trade_no=" + "\"" + orderNum + "\"";

        // 商品名称
        orderInfo += "&subject=" + "\"" + tripOverOrderDetail.getOrder().getDestination() + "\"";

        // 商品详情
        orderInfo += "&body=" + "\"" +  tripOverOrderDetail.getOrder().getId() + "\"";

        // 商品金额
        orderInfo += "&total_fee=" + "\"" + price + "\"";

        // 服务器异步通知页面路径
        orderInfo += "&notify_url=" + "\"" + notifyUrl + "\"";

        // 服务接口名称， 固定值
        orderInfo += "&service=\"mobile.securitypay.pay\"";

        // 支付类型， 固定值
        orderInfo += "&payment_type=\"2\"";

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.alipay_layout:
                checkedRadioButton.setChecked(false);
                aliRadioButton.setChecked(true);
                Log.e("daddy", "alipay layout clicked");
                checkedRadioButton = aliRadioButton;
                break;
            case R.id.wxpay_layout:
                checkedRadioButton.setChecked(false);
                wxRadioButton.setChecked(true);
                Log.e("daddy", "wxpay layout clicked");
                checkedRadioButton = wxRadioButton;
                break;
            default:
                break;
        }
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        return;
    }
}
