package com.guokrspace.duducar;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.guokrspace.duducar.communication.ResponseHandler;
import com.guokrspace.duducar.communication.SocketClient;
import com.guokrspace.duducar.communication.message.DriverDetail;
import com.guokrspace.duducar.communication.message.OrderDetail;
import com.guokrspace.duducar.ui.WinToast;
import com.squareup.picasso.Picasso;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

public class RatingActivity extends ActionBarActivity {

    private String[] mVals = new String[]
            {"神准时", "态度好有礼貌", "主动打电话联系", "车况良好"};

    private Context context;

    private ImageView avatarImageView;
    private TextView  driverNameTextView;
    private TextView  carPlateNumberTextView;
    private TextView  carDescriptionTextView;
    private RatingBar ratingBarSmall;
    private RatingBar ratingBarBig;
    private ImageView phoneImageView;
    private TextView  priceTextView;
    private Toolbar mToolbar;
    private TagFlowLayout mTagFlowLayout;

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

        context = RatingActivity.this;
        //init toolbar
        initToolBar();

        avatarImageView = (ImageView)findViewById(R.id.driverAvatar);
        driverNameTextView = (TextView)findViewById(R.id.driverName);
        carPlateNumberTextView = (TextView)findViewById(R.id.carPlateNumber);
        carDescriptionTextView = (TextView)findViewById(R.id.carDescription);
        ratingBarBig = (RatingBar)findViewById(R.id.ratingBarBig);
        phoneImageView = (ImageView)findViewById(R.id.phone);
        priceTextView = (TextView)findViewById(R.id.price);
        mTagFlowLayout = (TagFlowLayout) findViewById(R.id.flowlayout);

        final LayoutInflater mInflater = LayoutInflater.from(context);
        mTagFlowLayout.setAdapter(new TagAdapter<String>(mVals) {

            @Override
            public View getView(FlowLayout parent, int position, String s) {
                TextView tv = (TextView) mInflater.inflate(R.layout.flowlayout_tag,
                        mTagFlowLayout, false);
                Drawable drawable = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    drawable = context.getResources().getDrawable(R.mipmap.praise, context.getTheme());
                } else {
                    drawable = context.getResources().getDrawable(R.mipmap.praise);
                }
                drawable.setBounds(0, 0, dp2px(17), dp2px(17));
                tv.setCompoundDrawables(null, null, drawable, null);
                tv.setCompoundDrawablePadding(10);
                tv.setTextSize(sp2px(5));
                tv.setText(s);
                return tv;
            }
        });

        //get Arg
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null)
        {
            mOrder = (OrderDetail)bundle.get("order");
        }
        mDriver = ((DuduApplication)getApplicationContext()).mDriverDetail;

        //Update UI
        if (mDriver != null) {
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
        }

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
//                startActivity(new Intent(RatingActivity.this, PreOrderActivity.class));
                finish();

            }
        });

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

    }

    private void initToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                finish();
            }
        });
    }

    public void enterComplainPage(View view) {
        startActivity(new Intent(context, ComplainActivity.class));
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


    public int dp2px(float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public float sp2px(float spValue) {
        final float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return spValue * scale;
    }
}
