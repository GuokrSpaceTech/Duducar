package com.guokrspace.dududriver.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.guokrspace.dududriver.R;

import butterknife.ButterKnife;

/**
 * Created by hyman on 15/10/23.
 */
public class MeFragment extends BaseFragment {

    private Context context;

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

    }
}
