package com.guokrspace.dududriver.ui;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.adapter.RecordListAdapter;
import com.guokrspace.dududriver.model.RecordListItem;
import com.guokrspace.dududriver.util.SharedPreferencesUtils;
import com.guokrspace.dududriver.view.CircleImageView;
import com.guokrspace.dududriver.view.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by hyman on 15/10/23.
 */
public class MeFragment extends BaseFragment implements Handler.Callback{

    @Bind(R.id.avatar_civ)
    CircleImageView civAvatar;
    @Bind(R.id.name_tv)
    TextView tvName;
    @Bind(R.id.me_ratingbar)
    RatingBar mRatingbar;
    @Bind(R.id.rating_tv)
    TextView tvRating;
    @Bind(R.id.oder_num)
    TextView tvOderNum;
    @Bind(R.id.height_praise_rate)
    TextView tvPraiseRate;
    @Bind(R.id.balance_icon)
    ImageView ivBalance;
    @Bind(R.id.balance_tv)
    TextView tvBanlance;
    @Bind(R.id.balance_rl)
    RelativeLayout balanceLayout;
    @Bind(R.id.search_rl)
    RelativeLayout searchLayout;
    @Bind(R.id.order_records)
    RecyclerView mRecyclerView;
    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout refreshLayout;
//    @Bind(R.id.pattern_btn)
//    Button btnPattern;
//    @Bind(R.id.listenprogressview)
//    ListenProgressView mListenProgressView;
//    @Bind(R.id.over_btn)
//    Button btnOver;
    private Context context;

    public static final int LOAD_BASEINFO = 0x100;

    private static final int HANDLE_REFRESH_OVER = 111;

    private RecordListAdapter mAdapter;

    private boolean isRefreshing = false;

    private Handler mHandler = new Handler();

    public static MeFragment newInstance() {
        final MeFragment meFragment = new MeFragment();
        return meFragment;
    }

    public Handler getHanlder(){
        mHandler = new Handler(this);
        return mHandler;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, container, false);
        ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void initView() {
        //初始化历史订单列表
        mAdapter = new RecordListAdapter(getActivity(), initData());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        //初始化SwipeRefreshLayout
        Resources resources = getActivity().getResources();
        refreshLayout.setColorSchemeColors(resources.getColor(R.color.materialRed),
                resources.getColor(R.color.materialBlue),
                resources.getColor(R.color.materialYellow),
                resources.getColor(R.color.materialGreen));
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {

                if (isNetworkAvailable() && !isRefreshing) {
                    // TODO 获取历史订单数据

                }
            }

        });

    }

    private List<RecordListItem> initData() {
        List<RecordListItem> data = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            data.add(new RecordListItem("9月27日 17:30", "长沙轮渡 湘江中路东", "友阿百货 雨花区人民中路", "已支付"));
        }
        return data;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case LOAD_BASEINFO:
                String name = (String)SharedPreferencesUtils.getParam(getActivity(), "name", "李迪师傅");
                int stars = (int)Float.parseFloat((String) SharedPreferencesUtils.getParam(getActivity(), "rating", "5"));
                int total_order = Integer.parseInt((String) SharedPreferencesUtils.getParam(getActivity(), "total_order", "0"));
                float favorable_rate = Float.parseFloat((String) SharedPreferencesUtils.getParam(getActivity(), "favorable_rate", "0.0"));
                String balance = (String) SharedPreferencesUtils.getParam(getActivity(), "balance", "0.00");

                tvName.setText(name);
                mRatingbar.setMax(5);
                mRatingbar.setNumStars(stars);
                tvRating.setText(stars +  " 星");
                tvOderNum.setText(String.format(getResources().getString(R.string.my_order_num), total_order));
                tvPraiseRate.setText("好评率 " + favorable_rate + "%");
                tvBanlance.setText(" " + balance);
            break;
            default:
                break;
        }
        return false;
    }
}
