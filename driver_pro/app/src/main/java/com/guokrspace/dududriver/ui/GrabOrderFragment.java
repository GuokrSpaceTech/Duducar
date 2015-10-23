package com.guokrspace.dududriver.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guokrspace.dududriver.R;

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

    public static GrabOrderFragment newInstance() {
        final GrabOrderFragment grabOrderFragment = new GrabOrderFragment();
//        final Bundle args = new Bundle();
        return grabOrderFragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graborder, container, false);
        ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void initView() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
