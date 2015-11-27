package com.guokrspace.dududriver.adapter.adapterdelegates;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.model.BaseNoticeItem;
import com.guokrspace.dududriver.model.DuduNotice;
import com.guokrspace.dududriver.util.DateUtil;
import com.guokrspace.dududriver.util.VoiceUtil;
import com.hannesdorfmann.adapterdelegates.AbsAdapterDelegate;

import java.util.List;

/**
 * Created by daddyfang on 15/11/27.
 */
public class DuduNoticeAdapterDelegate extends AbsAdapterDelegate<List<BaseNoticeItem>> {

    private LayoutInflater mInflater;

    private Context context;

    private RecyclerView.Adapter mAdapter;

    public DuduNoticeAdapterDelegate(RecyclerView.Adapter mAdapter, Activity activity, int viewType) {
        super(viewType);
        this.context = activity;
        this.mAdapter = mAdapter;
        mInflater = activity.getLayoutInflater();
    }

    @Override
    public boolean isForViewType(List<BaseNoticeItem> items, int position) {
        return items.get(position) instanceof DuduNotice;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new DuduNoticeViewHolder(mInflater.inflate(R.layout.notice_item_dudunotice, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final List<BaseNoticeItem> items, final int position, @NonNull RecyclerView.ViewHolder viewHolder) {

        final DuduNoticeViewHolder holder = (DuduNoticeViewHolder) viewHolder;
        final DuduNotice notice = (DuduNotice) items.get(position);

        holder.tvTime.setText(DateUtil.dateFormat(System.currentTimeMillis() + ""));
        holder.tvContent.setText(notice.title);

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
                Log.e("daddy", "click ") ;
                if(!VoiceUtil.isSpeaking()){
                    VoiceUtil.startSpeaking(notice.title);
                }
                if(holder.tvDetail.getVisibility() == View.GONE) {
                    holder.tvDetail.setVisibility(View.VISIBLE);
                 } else {
                    holder.tvDetail.setVisibility(View.GONE);
                 }
            }
        });

    }

    static class DuduNoticeViewHolder extends RecyclerView.ViewHolder {

        public final LinearLayout llItem;
        public final TextView tvTime;
        public final TextView tvContent;
        public final TextView tvDetail;
        public final ImageButton ibDelete;

        public DuduNoticeViewHolder(View itemView) {
            super(itemView);
            llItem = (LinearLayout)itemView.findViewById(R.id.itemLLayout);
            tvTime = (TextView) itemView.findViewById(R.id.notice_time);
            tvContent = (TextView) itemView.findViewById(R.id.notice_description_tv);
            ibDelete = (ImageButton) itemView.findViewById(R.id.notice_delete_btn);
            tvDetail = (TextView) itemView.findViewById(R.id.notice_detail_tv);
        }
    }
}