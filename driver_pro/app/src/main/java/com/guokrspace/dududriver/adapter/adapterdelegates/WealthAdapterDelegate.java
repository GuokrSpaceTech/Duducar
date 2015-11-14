package com.guokrspace.dududriver.adapter.adapterdelegates;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.FloatMath;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.model.BaseNoticeItem;
import com.guokrspace.dududriver.model.WealthNotice;
import com.guokrspace.dududriver.ui.PostOrderActivity;
import com.hannesdorfmann.adapterdelegates.AbsAdapterDelegate;

import java.util.List;

/**
 * Created by hyman on 15/11/13.
 */
public class WealthAdapterDelegate extends AbsAdapterDelegate<List<BaseNoticeItem>> {

    private LayoutInflater mInflater;

    private Context context;

    private RecyclerView.Adapter mAdapter;

    public WealthAdapterDelegate(RecyclerView.Adapter mAdapter, Activity activity, int viewType) {
        super(viewType);
        this.context = activity;
        this.mAdapter = mAdapter;
        mInflater = activity.getLayoutInflater();
    }

    @Override
    public boolean isForViewType(List<BaseNoticeItem> items, int position) {
        return items.get(position) instanceof WealthNotice;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new WealthViewHolder(mInflater.inflate(R.layout.notice_item_wealth, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final List<BaseNoticeItem> items, final int position, @NonNull RecyclerView.ViewHolder viewHolder) {

        WealthViewHolder holder = (WealthViewHolder) viewHolder;
        WealthNotice notice = (WealthNotice) items.get(position);

        holder.tvTime.setText(notice.date);
        holder.tvContent.setText(String.format(context.getResources().getString(R.string.wealth_add_notice), notice.cardNum, Float.parseFloat(notice.sum)));

        holder.ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                items.remove(position);
                mAdapter.notifyItemRemoved(position);
                mAdapter.notifyDataSetChanged();
            }
        });
        holder.llItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, PostOrderActivity.class));
            }
        });

    }

    static class WealthViewHolder extends RecyclerView.ViewHolder {

        public final LinearLayout llItem;
        public final TextView tvTime;
        public final TextView tvContent;
        public final ImageButton ibDelete;

        public WealthViewHolder(View itemView) {
            super(itemView);
            llItem = (LinearLayout)itemView.findViewById(R.id.itemLLayout);
            tvTime = (TextView) itemView.findViewById(R.id.notice_time);
            tvContent = (TextView) itemView.findViewById(R.id.notice_description_tv);
            ibDelete = (ImageButton) itemView.findViewById(R.id.notice_delete_btn);
        }
    }
}
