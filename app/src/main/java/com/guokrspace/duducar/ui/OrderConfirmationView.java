package com.guokrspace.duducar.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.guokrspace.duducar.CostEstimateActivity;
import com.guokrspace.duducar.PreOrderActivity;
import com.guokrspace.duducar.PostOrderActivity;
import com.guokrspace.duducar.R;
import com.guokrspace.duducar.SearchActivity;
import com.guokrspace.duducar.communication.ResponseHandler;
import com.guokrspace.duducar.communication.SocketClient;
import com.guokrspace.duducar.communication.message.SearchLocation;

/**
 * TODO: document your custom view class.
 */
public class OrderConfirmationView extends LinearLayout {

    private LinearLayout mSelectPaymentView;
    private LinearLayout mCostEstimateView;
    private LinearLayout mCuponUseView;
    private Button       mOrderCabBtn;
    private TextView     mTimeToArriveText;
    private String       mMinToArrive;
    private String       mCurrentAdress;
    private LatLng       mCurrentLoc;
    private PreOrderActivity activity;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            return false;
        }
    });

    public OrderConfirmationView(Context context) {
        super(context);
        init(context);
        setListeners(context);
    }

    public OrderConfirmationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        setListeners(context);
    }

    public OrderConfirmationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        setListeners(context);
    }

    private void init(Context context)
    {
        LayoutInflater.from(context).inflate(R.layout.order_confirmation_view, this, true);
        mSelectPaymentView = (LinearLayout)findViewById(R.id.selectPaymentLLayout);
        mCostEstimateView = (LinearLayout)findViewById(R.id.costEstimateLLayout);
        mCuponUseView = (LinearLayout)findViewById(R.id.cuponLLayout);
        mOrderCabBtn = (Button)findViewById(R.id.orderCabButton);
        mTimeToArriveText = (TextView)findViewById(R.id.timeToArriveTextView);
        activity = (PreOrderActivity)context;
    }

    private void setListeners(final Context context)
    {
        mSelectPaymentView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //Todo start payment selection activity
            }
        });

        mCostEstimateView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SearchActivity.class);

                SearchLocation location = new SearchLocation();
                location.setAddress(((PreOrderActivity)context).mReqAddress);
                location.setLat(((PreOrderActivity) context).mReqLoc.latitude);
                location.setLng(((PreOrderActivity) context).mReqLoc.longitude);
                intent.putExtra("location", location);

                ((PreOrderActivity)context).startActivityForResult(intent, PreOrderActivity.ACTIVITY_SEARCH_DEST_REQUEST);
            }
        });

        mCuponUseView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SearchActivity.class);
                ((PreOrderActivity)context).startActivityForResult(intent, PreOrderActivity.ACTIVITY_SEARCH_DEST_REQUEST);
            }
        });

        mOrderCabBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String startAddr = (activity.start==null) ? "":activity.start.getAddress();
                String destAddr = (activity.dest==null) ? "":activity.dest.getAddress();
                Double startLat =  (activity.start==null) ? 0D:activity.start.getLat();
                Double destLat = (activity.dest==null) ? 0D:activity.dest.getLat();
                Double startLng =  (activity.start==null) ? 0D:activity.start.getLng();
                Double destLng = (activity.dest==null) ? 0D:activity.dest.getLat();
                SocketClient.getInstance().sendCarRequest(
                        "2",
                        startAddr,
                        destAddr,
                        startLat,
                        startLng,
                        destLat,
                        destLng,
                        "",
                        "",
                        "1",
                        new ResponseHandler(Looper.getMainLooper()) {
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

//                Intent intent = new Intent(context,PostOrderActivity.class);
//                ((PreOrderActivity) context).startActivityForResult(intent, 0x6002);
            }
        });
    }

    public void setmMinToArrive(String mMinToArrive) {
        this.mMinToArrive = mMinToArrive;
    }

    public void setmCurrentAdress(String mCurrentAdress) {
        this.mCurrentAdress = mCurrentAdress;
    }

    public void setmCurrentLoc(LatLng mCurrentLoc) {
        this.mCurrentLoc = mCurrentLoc;
    }
}
