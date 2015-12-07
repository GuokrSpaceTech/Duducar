package com.guokrspace.duducar;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.dexafree.materialList.card.Card;
import com.dexafree.materialList.card.CardProvider;
import com.dexafree.materialList.card.OnActionClickListener;
import com.dexafree.materialList.card.action.WelcomeButtonAction;
import com.dexafree.materialList.view.MaterialListView;
import com.guokrspace.duducar.database.OrderRecord;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OrderHistoryActivity.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OrderHistoryActivity#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrderHistoryActivity extends AppCompatActivity{


    private DuduApplication mApplication;
    private Context mContext;
    private MaterialListView materialListView;
    private Toolbar mToolbar;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_order_records);
        mApplication = (DuduApplication)getApplicationContext();
        mContext = this;
        initView();
    }

    private void initView() {
        initToolBar();
        materialListView = (MaterialListView) findViewById(R.id.material_listview);
        List<OrderRecord> orderRecords = mApplication.mDaoSession.getOrderRecordDao().queryBuilder().list();
        if (orderRecords.size() == 0) {
            Log.e("hyman_orderrecord", "no order");
        }
        for (OrderRecord orderRecord : orderRecords) {
            Card card = new Card.Builder(mContext)
                    .setTag("WELCOME_CARD")
                    .setDismissible()
                    .withProvider(new CardProvider<>())
                    .setLayout(R.layout.material_welcome_card_layout)
                    .setTitle(orderRecord.getOrderTime())
                    .setTitleColor(Color.GRAY)
                    .setDescription(orderRecord.getStartAddr() + "-" + orderRecord.getDestAddr())
                    .setDescriptionColor(Color.GRAY)
                    .setSubtitle(orderRecord.getMileage())
                    .setSubtitleColor(Color.GRAY)
                    .setBackgroundColor(Color.WHITE)
                    .addAction(R.id.ok_button, new WelcomeButtonAction(mContext)
                            .setText("Okay!")
                            .setTextColor(Color.WHITE)
                            .setListener(new OnActionClickListener() {
                                @Override
                                public void onActionClicked(View view, Card card) {
                                    Toast.makeText(mContext, "Welcome!", Toast.LENGTH_SHORT).show();
                                }
                            }))
                    .endConfig()
                    .build();
            materialListView.getAdapter().add(card);

        }
    }

    private void initToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("历史订单");
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OrderHistoryActivity.this.finish();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*if(item.getItemId()==android.R.id.home)
        {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
                return false;
            }
        }*/
        return super.onOptionsItemSelected(item);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
