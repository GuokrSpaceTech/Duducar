package com.guokrspace.duducar;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.guokrspace.duducar.communication.ResponseHandler;
import com.guokrspace.duducar.communication.SocketClient;
import com.guokrspace.duducar.communication.http.model.Order;
import com.guokrspace.duducar.communication.message.OrderDetail;
import com.guokrspace.duducar.util.ConvertUtil;
import com.guokrspace.duducar.util.SharedPreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by hyman on 15/11/30.
 */
public class ComplainActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar mToolbar;
    private Button badMannerBtn;
    private Button falseOrderBtn;
    private Button overchangeBtn;
    private Button detourBtn;
    private Button chargeTimeoutBtn;
    private Button breakOrderBtn;
    private Button callButton;
    private ProgressBar mProgressBar;
    private Context context;

    private Button[] complainBtns;

    private String complainId = "0";
    private OrderDetail orderDetail;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    break;
                case 1:
                    mProgressBar.setVisibility(View.INVISIBLE);
                    final MaterialDialog dialog = new MaterialDialog(context);
                    dialog.setMessage("提交成功");
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.setPositiveButton("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ComplainActivity.this.finish();
                        }
                    });
                    dialog.show();
                    break;
                case 2:
                    mProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(ComplainActivity.this, "提交超时,请检查网络", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    mProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(ComplainActivity.this, "请勿重复提交投诉, 如有必要,请直接联系客服", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complain);
        context = this;
        //get Arg
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null)
        {
            orderDetail= (OrderDetail)bundle.getSerializable("order");
        }
        initView();
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        badMannerBtn = (Button) findViewById(R.id.bad_manner_btn);
        falseOrderBtn = (Button) findViewById(R.id.false_order_btn);
        overchangeBtn = (Button) findViewById(R.id.overchange_btn);
        detourBtn = (Button) findViewById(R.id.detour_btn);
        chargeTimeoutBtn = (Button) findViewById(R.id.charge_timeout_btn);
        breakOrderBtn = (Button) findViewById(R.id.break_order_btn);
        callButton = (Button) findViewById(R.id.call_button);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        badMannerBtn.setOnClickListener(this);
        falseOrderBtn.setOnClickListener(this);
        overchangeBtn.setOnClickListener(this);
        detourBtn.setOnClickListener(this);
        chargeTimeoutBtn.setOnClickListener(this);
        breakOrderBtn.setOnClickListener(this);
        callButton.setOnClickListener(this);

        String complains = (String)SharedPreferencesUtils.getParam(context, SharedPreferencesUtils.BASEINFO_COMPLAINTS, "");
        JSONArray jsonArray = JSON.parseArray(complains);
        int length = jsonArray.size();


        complainBtns = new Button[]{badMannerBtn, falseOrderBtn, overchangeBtn, detourBtn, chargeTimeoutBtn, breakOrderBtn};

        try {
            for(int i=0;i<6;i++){
                com.alibaba.fastjson.JSONObject complain = (com.alibaba.fastjson.JSONObject)jsonArray.get(i);
                Log.e("daddy complain", complain.getString("value"));
                complainBtns[i].setText(complain.getString("value"));
                complainBtns[i].setTag(complain.getString("id"));
            }
        } catch (com.alibaba.fastjson.JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.call_button:
                //拨打客服电话
                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "4006887312"));
                startActivity(callIntent);
                break;
            case R.id.bad_manner_btn:
            case R.id.false_order_btn:
            case R.id.overchange_btn:
            case R.id.detour_btn:
            case R.id.charge_timeout_btn:
            case R.id.break_order_btn:
                for (Button button : complainBtns) {
                    if (button != v) {
                        button.setClickable(false);
                    }
                }
                complainId = (String)v.getTag();
                mProgressBar.setVisibility(View.VISIBLE);


                SocketClient.getInstance().sendComplain(Integer.parseInt(orderDetail.getId()), Integer.parseInt(orderDetail.getDriver_id() + ""), Integer.parseInt(complainId), new ResponseHandler(Looper.myLooper()) {
                    @Override
                    public void onSuccess(String messageBody) {
                        new Thread(new Runnable() {
                            int i = 0;
                            @Override
                            public void run() {
                                while (i <= 1) {
                                    i++;
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        }).start();
                        mHandler.sendEmptyMessage(1);
                    }

                    @Override
                    public void onFailure(String error) {
                        for (Button button : complainBtns) {
                            button.setClickable(false);
                        }
                        mHandler.sendEmptyMessage(3);
                    }

                    @Override
                    public void onTimeout() {
                        for (Button button : complainBtns) {
                            button.setClickable(true);
                        }
                        mHandler.sendEmptyMessage(2);
                    }
                });
                break;
            default:
                break;
        }
    }
}
