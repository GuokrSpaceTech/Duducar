package com.guokrspace.dududriver.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.adapter.RecordListAdapter;
import com.guokrspace.dududriver.model.RecordListItem;
import com.guokrspace.dududriver.view.CircleImageView;
import com.guokrspace.dududriver.view.DividerItemDecoration;
import com.guokrspace.dududriver.view.ListenProgressView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by hyman on 15/10/23.
 */
public class MeFragment extends BaseFragment {

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
    @Bind(R.id.balance_rl)
    RelativeLayout balanceLayout;
    @Bind(R.id.search_rl)
    RelativeLayout searchLayout;
    @Bind(R.id.order_records)
    RecyclerView mRecyclerView;
    @Bind(R.id.pattern_btn)
    Button btnPattern;
    @Bind(R.id.listenprogressview)
    ListenProgressView mListenProgressView;
    @Bind(R.id.over_btn)
    Button btnOver;
    private Context context;

    private RecordListAdapter mAdapter;

    public static MeFragment newInstance() {
        final MeFragment meFragment = new MeFragment();
        return meFragment;
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
        mAdapter = new RecordListAdapter(getActivity(), initData());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
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
}
