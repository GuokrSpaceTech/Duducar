package com.guokrspace.dududriver.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.adapter.NoticeAdapter;
import com.guokrspace.dududriver.model.BaseNoticeItem;
import com.guokrspace.dududriver.model.DenseOrderNotice;
import com.guokrspace.dududriver.model.UnKnowNotice;
import com.guokrspace.dududriver.model.WealthNotice;
import com.guokrspace.dududriver.util.CommonUtil;
import com.guokrspace.dududriver.view.DividerItemDecoration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by hyman on 15/10/22.
 */
public class GrabOrderFragment extends BaseFragment {

    @Bind(R.id.date_tv)
    TextView tvDate;
    @Bind(R.id.ordernum_tv)
    TextView tvOrderNum;
    @Bind(R.id.onlinetime_tv)
    TextView tvOnlineTime;
    @Bind(R.id.income_tv)
    TextView tvIncome;
    @Bind(R.id.turnover_rate_tv)
    TextView tvTurnoverRate;
    @Bind(R.id.graborder_rv)
    RecyclerView mRecyclerView;
    private Context context;

    private NoticeAdapter mAdapter;


    public static GrabOrderFragment newInstance() {
        final GrabOrderFragment grabOrderFragment = new GrabOrderFragment();
//        final Bundle args = new Bundle();
        return grabOrderFragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("daddy","oncreate" + CommonUtil.getTodayDoneWork());

    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
        Log.e("daddy", "onattach" + CommonUtil.getTodayDoneWork());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graborder, container, false);
        ButterKnife.bind(this, view);
        Log.e("daddy", "oncreateview" + CommonUtil.getTodayDoneWork());
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e("daddy", "onviewcreated" + CommonUtil.getTodayDoneWork());
        initView();
    }

    private void initView() {
        mAdapter = new NoticeAdapter(getActivity(), initData());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM月dd日 EEEE");
        String today = dateFormat.format(date);
        tvDate.setText(today);

        tvOrderNum.setText("已完成 " + CommonUtil.getTodayDoneWork() + " 单");
        tvOnlineTime.setText("");
        tvIncome.setText(CommonUtil.getTodayCash() + "");
        tvTurnoverRate.setText( CommonUtil.getTodayWorkRate() + " %");

    }

    private List<BaseNoticeItem> initData() {
        List<BaseNoticeItem> data = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            data.add(new DenseOrderNotice("10-09 15:52", "发现订单密集区域，点击查看~" + i));
            data.add(new WealthNotice("11-13 21:29", "4188", "34.40"));
            if (i < 3) {
                data.add(new UnKnowNotice());
            }
        }
        Collections.shuffle(data);
        return data;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
