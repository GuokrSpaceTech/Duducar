package com.guokrspace.duducar.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.guokrspace.duducar.PreOrderActivity;
import com.guokrspace.duducar.PostOrderActivity;
import com.guokrspace.duducar.R;

/**
 * TODO: document your custom view class.
 */
public class DriverInformationView extends LinearLayout {

    private LinearLayout mSelectPaymentView;
    private LinearLayout mCostEstimateView;
    private LinearLayout mCuponUseView;
    private Button       mOrderCabBtn;
    private TextView     mTimeToArriveText;
    private String       mMinToArrive;
    private String       mCurrentAdress;
    private LatLng       mCurrentLoc;
    GestureDetectorCompat mDetector;
    private int         viewPortHeight;
    private LinearLayout root;
    private ImageView    mDriver;
    private ImageView    mCar;
    private LinearLayout  mDriverDesc;
    private LinearLayout  mCarDesc;
    String DEBUG_TAG = "DriverInformation";

    private Handler mHandler = new Handler(new Handler.Callback() {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
        @Override
        public boolean handleMessage(Message message) {

            if(message.what == 0x1001)
            {
                float distancY = (float)message.obj;

                if(distancY > 0) //Scroll up
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                        root.animate().translationY(-400);
                        mDriver.animate().translationX(200);
                        mCar.animate().translationX(-200);

                        mDriverDesc.animate().scaleX(0.5f);
                        mDriverDesc.animate().translationX(100);

                        mCarDesc.setVisibility(VISIBLE);
                        mCarDesc.animate().scaleX(0.5f);
                        mCarDesc.animate().translationX(-100);
                    }
                } else { //Scroll down
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                        root.animate().translationY(0);

                        mDriver.animate().translationX(0);
                        mCar.animate().translationX(0);

                        mDriverDesc.animate().scaleX(0);
                        mDriverDesc.animate().translationX(0);
                        mDriverDesc.setVisibility(VISIBLE);

                        mCarDesc.setVisibility(INVISIBLE);
                        mCarDesc.animate().scaleX(0);
                        mCarDesc.animate().translationX(0);
                    }
                }
            }
            return false;
        }
    });

    public DriverInformationView(Context context) {
        super(context);
        init(context);
        setListeners(context);
    }

    public DriverInformationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        setListeners(context);
    }

    public DriverInformationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        setListeners(context);
    }

    private void init(Context context) {
        root = (LinearLayout)LayoutInflater.from(context).inflate(R.layout.driver_information_view, this, true);
        mDriver = (ImageView)findViewById(R.id.imageViewDriver);
        mCar    = (ImageView)findViewById(R.id.imageViewCar);
        mDriverDesc = (LinearLayout)findViewById(R.id.driverDescLLayout);
        mCarDesc = (LinearLayout)findViewById(R.id.carDescLLayout);

        viewPortHeight = getHeight();
        mDetector = new GestureDetectorCompat(context, new MyGestureListener());



    };

    @Override
    public boolean onTouchEvent(MotionEvent event){
        boolean retVal = mDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return retVal || super.onTouchEvent(event);
    }


    private void setListeners(final Context context)
    {
//        mSelectPaymentView.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //Todo start payment selection activity
//            }
//        });
//
//        mCostEstimateView.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
//
//        mCuponUseView.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
//
//        mOrderCabBtn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                SocketClient.getInstance().sendCarRequest("1",mCurrentAdress,"",mCurrentLoc.latitude, mCurrentLoc.longitude
////                ,0D,0D,"","","1",mHandler);
//
//                Intent intent = new Intent(context,PostOrderActivity.class);
//                ((PreOrderActivity) context).startActivityForResult(intent, 0x6002);
//            }
//        });
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


    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onDown(MotionEvent event) {
            Log.d(DEBUG_TAG,"onDown: " + event.toString());
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            Message message = mHandler.obtainMessage();
            message.what = 0x1001;
            message.obj = distanceY;
            mHandler.sendMessage(message);

         return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            Log.d(DEBUG_TAG, "onFling: " + event1.toString()+event2.toString());
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return super.onSingleTapUp(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }

        @Override
        public void onShowPress(MotionEvent e) {
            super.onShowPress(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return super.onDoubleTapEvent(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onContextClick(MotionEvent e) {
            return super.onContextClick(e);
        }
    }
}
