package com.guokrspace.duducar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.guokrspace.duducar.communication.ResponseHandler;
import com.guokrspace.duducar.communication.SocketClient;
import com.guokrspace.duducar.communication.message.DriverDetail;
import com.guokrspace.duducar.communication.message.OrderDetail;
import com.guokrspace.duducar.ui.WinToast;
import com.squareup.picasso.Picasso;

public class RatingActivity extends ActionBarActivity {

    private ImageView avatarImageView;
    private TextView  driverNameTextView;
    private TextView  carPlateNumberTextView;
    private TextView  carDescriptionTextView;
    private RatingBar ratingBarSmall;
    private RatingBar ratingBarBig;
    private ImageView phoneImageView;
    private TextView  priceTextView;

    private OrderDetail mOrder;
    private DriverDetail mDriver;

    private Handler mHandler= new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    default:
                        break;
                }
            }
        };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        AppExitUtil.getInstance().addActivity(this);

        avatarImageView = (ImageView)findViewById(R.id.driverAvatar);
        driverNameTextView = (TextView)findViewById(R.id.driverName);
        carPlateNumberTextView = (TextView)findViewById(R.id.carPlateNumber);
        carDescriptionTextView = (TextView)findViewById(R.id.carDescription);
        ratingBarSmall = (RatingBar)findViewById(R.id.ratingBarSmall);
        ratingBarBig = (RatingBar)findViewById(R.id.ratingBarBig);
        phoneImageView = (ImageView)findViewById(R.id.phone);
        priceTextView = (TextView)findViewById(R.id.price);

        //get Arg
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null)
        {
            mOrder = (OrderDetail)bundle.get("order");
        }
        mDriver = ((DuduApplication)getApplicationContext()).mDriverDetail;

        //Update UI
        if(mDriver.getAvatar()!=null)
        {
            Picasso.with(this).load(mDriver.getAvatar()).fit().centerCrop().into(avatarImageView);
        }
        driverNameTextView.setText(mDriver.getName());
        carPlateNumberTextView.setText(mDriver.getPlate());
        carDescriptionTextView.setText(mDriver.getDescription());
        ratingBarSmall.setRating(mDriver.getRating());
        phoneImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Call the number
            }
        });
        if(mOrder!=null) {
            priceTextView.setText(mOrder.getOrg_price());
        }
        ratingBarBig.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                //Send Rating Request
                ratingBar.setRating(v);
                SocketClient.getInstance().sendRatingRequest(mOrder.getId(), (int) v, new ResponseHandler(Looper.getMainLooper()) {
                    @Override
                    public void onSuccess(String messageBody) {
                    }

                    @Override
                    public void onFailure(String error) {
                    }

                    @Override
                    public void onTimeout() {
                    }
                });

                WinToast.toast(RatingActivity.this, "谢谢评价。");
                startActivity(new Intent(RatingActivity.this, PreOrderActivity.class));
                finish();

            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
