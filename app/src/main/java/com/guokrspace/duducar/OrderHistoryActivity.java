package com.guokrspace.duducar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.dexafree.materialList.view.MaterialListView;
import com.guokrspace.duducar.adapter.OrdersAdapter;
import com.guokrspace.duducar.communication.message.OrderDetail;
import com.guokrspace.duducar.database.OrderRecord;

import java.util.ArrayList;
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
    private RecyclerView mRecyclerView;

    private OrdersAdapter mAdapter;
    private List<OrderRecord> orderRecords = new ArrayList<>();
    private LinearLayoutManager mLayoutManager;


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
        //init toolbar
        initToolBar();

        orderRecords.addAll(mApplication.mDaoSession.getOrderRecordDao().queryBuilder().list());
        mRecyclerView = (RecyclerView) findViewById(R.id.order_records_recyclerview);
        mAdapter = new OrdersAdapter(mContext, orderRecords);
        mAdapter.setOnItemClickListener(new OrdersAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //点击进入详情界面
                Intent intent = new Intent(mContext, RatingActivity.class);
//                OrderDetail orderDetail = getOrderDetail(orderRecords.get(position));
                intent.putExtra("order", orderRecords.get(position));
//                startActivity(intent);
                Toast.makeText(OrderHistoryActivity.this, "暂时无法提供详细内容", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View view, int position) {
                //长按删除
                mAdapter.removeData(position);
                mApplication.mDaoSession.getOrderRecordDao().delete(orderRecords.get(position));
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(40));

    }

    private OrderDetail getOrderDetail(OrderRecord orderRecord){
        OrderDetail orderDetail = new OrderDetail();
        return orderDetail;
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



    private class SpaceItemDecoration extends RecyclerView.ItemDecoration{

        private int space;

        public SpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

            if(parent.getChildPosition(view) != 0)
                outRect.top = space;
        }
    }

}
