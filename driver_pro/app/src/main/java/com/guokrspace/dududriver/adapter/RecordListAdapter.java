package com.guokrspace.dududriver.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.model.RecordListItem;

import java.util.List;

/**
 * Created by hyman on 15/10/31.
 */
public class RecordListAdapter extends RecyclerView.Adapter<RecordListAdapter.RecordViewHolder> {

    private List<RecordListItem> mItems = null;
    private Context context;

    public RecordListAdapter(Context context, List<RecordListItem> data) {
        this.context = context;
        this.mItems = data;
    }

    @Override
    public RecordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_list_item, parent, false);
        RecordViewHolder holder = new RecordViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecordViewHolder holder, int position) {
        if (mItems != null) {
            holder.tvDate.setText(mItems.get(position).date);
            holder.tvOrigin.setText(mItems.get(position).origin);
            holder.tvDest.setText(mItems.get(position).destination);
            holder.tvStatus.setText(mItems.get(position).status);
        }
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    public class RecordViewHolder extends RecyclerView.ViewHolder {

        public TextView tvDate;

        public ImageView ivOrigin;

        public TextView tvOrigin;

        public ImageView ivDest;

        public TextView tvDest;

        public TextView tvStatus;

        public RecordViewHolder(View itemView) {
            super(itemView);
            tvDate = (TextView) itemView.findViewById(R.id.record_date);
            ivOrigin = (ImageView) itemView.findViewById(R.id.origin_icon);
            tvOrigin = (TextView) itemView.findViewById(R.id.record_origin);
            ivDest = (ImageView) itemView.findViewById(R.id.destination_icon);
            tvDest = (TextView) itemView.findViewById(R.id.record_destination);
            tvStatus = (TextView) itemView.findViewById(R.id.record_status);
        }
    }

}
