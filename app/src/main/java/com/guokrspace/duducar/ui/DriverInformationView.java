package com.guokrspace.duducar.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.guokrspace.duducar.R;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;

/**
 * TODO: document your custom view class.
 */
public class DriverInformationView extends RelativeLayout {


    GestureDetectorCompat mDetector;

    //UI
    private RelativeLayout root;
    public ImageView mDriverImageView;
    public ImageView mCarImageView;
    public ImageView mPhoneIconImageView;
    public TextView mDriverNameTextView;
    public TextView mCarDescTextView;
    public TextView mCarPlateNumberTextView;
    public RatingBar mRatingBar;

    private LinearLayout mDriverDescLLayout;
    private LinearLayout mCarDescLLayout;


    private Handler mHandler = new Handler(new Handler.Callback() {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
        @Override
        public boolean handleMessage(Message message) {

            if (message.what == 0x1001) {
                float distancY = 0;

                if(message.obj!=null) {
                    distancY = (float) message.obj;
                }

                int rectangle_width = mCarDescLLayout.getWidth();
                int rectangle_height = mCarDescLLayout.getHeight();

                if (distancY >= 0) //Scrollup by 30dp
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                        root.animate().translationY(dpToPx(getResources(), -80));

                        mDriverImageView.animate().translationX(rectangle_width/4); //left
                        mCarImageView.animate().translationX(-rectangle_width/4); //right

                        mPhoneIconImageView.animate().translationY(rectangle_height/2); //down
                        mPhoneIconImageView.animate().translationX(-rectangle_width/2); //Left

                        mCarPlateNumberTextView.animate().translationY(rectangle_height/2); //down
                        mCarPlateNumberTextView.animate().translationX(rectangle_width/2); //Right

                        mCarDescLLayout.setVisibility(VISIBLE);
                        mCarDescLLayout.requestLayout();
                    }
                } else { //Scroll down by 60dp
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                        root.animate().translationY(0);

                        mDriverImageView.animate().translationX(0);
                        mCarImageView.animate().translationX(0);

                        mPhoneIconImageView.animate().translationX(0);
                        mPhoneIconImageView.animate().translationY(0);

                        mCarPlateNumberTextView.animate().translationX(0);
                        mCarPlateNumberTextView.animate().translationY(0);

                        mCarDescLLayout.setVisibility(INVISIBLE);
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
        root = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.driver_information_view, this, true);
        mDriverImageView = (ImageView) findViewById(R.id.imageViewDriver);
        mCarImageView = (ImageView) findViewById(R.id.imageViewCar);
        mPhoneIconImageView = (ImageView) findViewById(R.id.imageViewPhoneIcon);
        mDriverNameTextView = (TextView) findViewById(R.id.textViewDriverName);
        mCarDescTextView = (TextView) findViewById(R.id.textViewCarDesc);
        mDriverDescLLayout = (LinearLayout) findViewById(R.id.driverDescLLayout);
        mCarDescLLayout = (LinearLayout) findViewById(R.id.carDescLLayout);
        mCarPlateNumberTextView = (TextView) findViewById(R.id.textViewCarPlateNumber);
        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);

        mDetector = new GestureDetectorCompat(context, new MyGestureListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean retVal = mDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return retVal || super.onTouchEvent(event);
    }


    private void setListeners(final Context context) {

        mPhoneIconImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNum = (String) view.getTag();

                if (phoneNum != null) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + phoneNum));
                    if (checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                        context.startActivity(callIntent);
                        return;
                    }
                }
            }

        });
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
            Message message = mHandler.obtainMessage();
            message.what = 0x1001;
            message.obj = 0f;
            mHandler.sendMessage(message);
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

    public int dpToPx(Resources res, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
    }
}
