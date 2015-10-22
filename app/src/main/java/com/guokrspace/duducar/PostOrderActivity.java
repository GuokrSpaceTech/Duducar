package com.guokrspace.duducar;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ZoomControls;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.guokrspace.duducar.communication.message.MessageTag;

public class PostOrderActivity extends AppCompatActivity {

    String orderStatusString = "正在预约车辆";
    MapView mMapView = null;
    BaiduMap mBaiduMap = null;
    Context mContext = this;
    FrameLayout mSearchDestLLayout;
    FloatingActionButton mFab;
    int state;

    final static int WAITING_FOR_ORDER_CONFIRM = 1;
    final static int WAITING_FOR_DRIVER_COME = 2;
    final static int ORDER_CANCELLING = 3;
    final static int ORDER_CANCEL_CONFIRMED = 4;
    final static int INCAR = 5;
    final static int ORDER_COMPETED = 6;

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what)
            {
                case MessageTag.MESSAGE_ORDER_CANCEL_CONFIRMED:
                    if(state == ORDER_CANCELLING) {
                        getSupportActionBar().setTitle("订单已经取消");
                        finish();
                    }
                    break;
                case MessageTag.MESSAGE_ORDER_DISPATCHED:
                    if(state == WAITING_FOR_ORDER_CONFIRM)
                    {
//                        DriverInformationView driverView = new DriverInformationView(mContext);

//                        RelativeLayout.LayoutParams layoutParams =
//                                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//                        layoutParams.addRule(RelativeLayout.BELOW, mMapView.getId());
//                        RelativeLayout container = (RelativeLayout)findViewById(R.id.container);
//                        container.addView(driverView, layoutParams);
//
//                        ViewGroup.LayoutParams paramRL = mMapView.getLayoutParams();
//                        paramRL.height = dpToPx(getResources(),350);
//                        mMapView.requestLayout();
//
//                        container.requestLayout();


                    }
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_cab);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(orderStatusString);

        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        // 隐藏缩放控件
        int childCount = mMapView.getChildCount();
        View zoom = null;
        for (int i = 0; i < childCount; i++) {
            View child = mMapView.getChildAt(i);
            if (child instanceof ZoomControls) {
                zoom = child;
                break;
            }
        }
        zoom.setVisibility(View.GONE);

        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(16.0f);
        mBaiduMap.setMapStatus(msu);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "订单取消中...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                state = ORDER_CANCELLING;
                mHandler.sendEmptyMessage(MessageTag.MESSAGE_ORDER_CANCEL_CONFIRMED); //Simulate the cancel is success

            }
        });

        mSearchDestLLayout = (FrameLayout)findViewById(R.id.destSearchLinearLayout);
        mSearchDestLLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, SearchActivity.class);
                startActivityForResult(intent, 0x6003);
            }
        });

        state = WAITING_FOR_ORDER_CONFIRM;

        getSupportActionBar().setTitle("正在预约中...");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mHandler.sendEmptyMessage(MessageTag.MESSAGE_ORDER_DISPATCHED);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public int dpToPx(Resources res, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
    }
}
