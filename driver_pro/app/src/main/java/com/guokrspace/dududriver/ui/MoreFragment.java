package com.guokrspace.dududriver.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.adapter.OptionGridAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.paulburke.itemtouchhelper.MarginDecoration;
import co.paulburke.itemtouchhelper.OnStartDragListener;
import co.paulburke.itemtouchhelper.SimpleItemTouchHelperCallback;

/**
 * Created by hyman on 15/10/23.
 */
public class MoreFragment extends BaseFragment implements OnStartDragListener {

    @Bind(R.id.more_rv)
    RecyclerView mRecyclerView;

    private ItemTouchHelper mItemTouchHelper;
    private OptionGridAdapter adapter;

    public static MoreFragment newInstance() {
        final MoreFragment moreFragment = new MoreFragment();
        return moreFragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);
        ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void initView() {

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new OptionGridAdapter(getActivity(), this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new MarginDecoration(getActivity()));
        mRecyclerView.setAdapter(adapter);


        final int spanCount = getResources().getInteger(R.integer.grid_item);
        final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), spanCount);
        mRecyclerView.setLayoutManager(layoutManager);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

}
