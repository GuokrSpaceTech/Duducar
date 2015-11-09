package com.guokrspace.duducar;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.guokrspace.duducar.communication.message.SearchLocation;

import org.w3c.dom.Text;

public class CostEstimateActivity extends AppCompatActivity {

    private RelativeLayout startPointRLayout;
    private RelativeLayout destPointRLayout;
    private SearchLocation startLoc;
    private SearchLocation endLoc;
    private TextView costEstTextView;
    private TextView startTextView;
    private TextView endTextView;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cost_estimate);
        getSupportActionBar().setTitle("费用估算");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        costEstTextView = (TextView)findViewById(R.id.textViewCostEst);
        startTextView = (TextView)findViewById(R.id.startPointTextView);
        endTextView = (TextView)findViewById(R.id.destPointTextView);

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
            int price_high = (int) (distance * 1.3 / 1000); //meter -> km
            int price_low  = (int) (distance * 1.1 / 1000); //meter -> km

            String estimation = String.format("%d-%d",price_low,price_high);
            costEstTextView.setText(estimation);

            startTextView.setText(startLoc.getAddress());
            endTextView.setText(endLoc.getAddress());
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case android.R.id.home:
                setResult(RESULT_OK);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
