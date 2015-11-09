package com.guokrspace.duducar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.guokrspace.duducar.alipay.PayResult;
import com.guokrspace.duducar.alipay.SignUtils;
import com.guokrspace.duducar.communication.message.DriverDetail;
import com.guokrspace.duducar.communication.message.TripOverOrder;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class RatingActivity extends ActionBarActivity {

    private ImageView avatarImageView;
    private TextView  driverNameTextView;
    private TextView  carPlateNumberTextView;
    private TextView  carDescriptionTextView;
    private RatingBar ratingBarSmall;
    private RatingBar ratingBarBig;
    private ImageView phoneImageView;
    private TextView  priceTextView;

    private TripOverOrder mOrder;
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
            mOrder = (TripOverOrder)bundle.get("order");
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
        priceTextView.setText(mOrder.getPrice());
        ratingBarBig.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                //Send Rating Request
                ratingBar.setRating(v);
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
}
