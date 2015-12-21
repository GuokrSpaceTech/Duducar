package com.guokrspace.duducar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mapapi.utils.DistanceUtil;
import com.guokrspace.duducar.communication.message.SearchLocation;

public class CostEstimateActivity extends AppCompatActivity {

    private RelativeLayout startPointRLayout;
    private RelativeLayout destPointRLayout;
    private SearchLocation startLoc;
    private SearchLocation endLoc;
    private TextView costEstTextView;
    private TextView startTextView;
    private TextView endTextView;
    private Button confirmButton;
    private Toolbar mToolbar;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cost_estimate);
//        getSupportActionBar().setTitle("费用估算");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AppExitUtil.getInstance().addActivity(this);

        //init toolbar
        initToolBar();

        costEstTextView = (TextView)findViewById(R.id.textViewCostEst);
        startTextView = (TextView)findViewById(R.id.startPointTextView);
        endTextView = (TextView)findViewById(R.id.destPointTextView);
        confirmButton = (Button) findViewById(R.id.confirmButton);

        context = this;

        startTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent inent = new Intent(context, SearchActivity.class);
                startActivityForResult(inent, PreOrderActivity.ACTIVITY_SEARCH_START_REQUEST);
            }
        });

        endTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent inent = new Intent(context, SearchActivity.class);
                startActivityForResult(inent, PreOrderActivity.ACTIVITY_SEARCH_DEST_REQUEST);
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmButton.setClickable(false);
                setResult(RESULT_OK);
                finish();
            }
        });

        //Get Args
        Bundle bundle = getIntent().getExtras();
        if(bundle != null)
        {
            startLoc = (SearchLocation)bundle.get("start");
            endLoc = (SearchLocation)bundle.get("dest");
        }

        if(startLoc!=null && endLoc!=null)
        {
            Double distance = DistanceUtil.getDistance(startLoc.getLocation(), endLoc.getLocation());
            int price_high = (int) (distance * 3.2 / 1000); //meter -> km
            int price_low  = (int) (distance * 3 / 1000); //meter -> km

            String estimation = String.format("%d-%d",price_low,price_high);
            costEstTextView.setText(estimation);

            startTextView.setText(startLoc.getAddress());
            endTextView.setText(endLoc.getAddress());
        }

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
    }

    private void initToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("预估费用");
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CostEstimateActivity.this.finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
