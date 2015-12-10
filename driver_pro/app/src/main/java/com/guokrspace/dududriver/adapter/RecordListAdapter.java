package com.guokrspace.dududriver.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.database.OrderRecord;
import com.guokrspace.dududriver.ui.HistoryOrderDetailActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by hyman on 15/10/31.
 */
public class RecordListAdapter extends RecyclerView.Adapter<RecordListAdapter.RecordViewHolder> {

    private List<OrderRecord> mItems = null;
    private Context context;

    public RecordListAdapter(Context context, List<OrderRecord> data) {
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
    public void onBindViewHolder(RecordViewHolder holder, final int position) {
        if (mItems != null) {

            Log.e("daddy", "endtime "+ mItems.get(position).getEnd_time());
            holder.tvDate.setText(dateFormat(mItems.get(position).getEnd_time()));
            holder.tvOrigin.setText(mItems.get(position).getStart());
            holder.tvDest.setText(mItems.get(position).getDestination());

            holder.tvStatus.setText("已完成");
            holder.orderItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent detailIntent = new Intent();
                    detailIntent.setClass(context, HistoryOrderDetailActivity.class);
                    detailIntent.putExtra("orderDetail", mItems.get(position));
                    context.startActivity(detailIntent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    //将时间转换成 MM月dd日 HH:mm 格式
    private String dateFormat(String date) {
        if (TextUtils.isEmpty(date)) {
            return "一万年前";
        }
        Date orderDate = new Date(Long.parseLong(date+"000"));
        SimpleDateFormat format = new SimpleDateFormat("MM月dd日 HH:mm");
        return format.format(orderDate);
    }

    public class RecordViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout orderItemLayout;

        public TextView tvDate;

        public ImageView ivOrigin;

        public TextView tvOrigin;

        public ImageView ivDest;

        public TextView tvDest;

        public TextView tvStatus;

        public RecordViewHolder(View itemView) {
            super(itemView);
            orderItemLayout = (LinearLayout) itemView.findViewById(R.id.history_order_item);
            tvDate = (TextView) itemView.findViewById(R.id.record_date);
            ivOrigin = (ImageView) itemView.findViewById(R.id.origin_icon);
            tvOrigin = (TextView) itemView.findViewById(R.id.record_origin);
            ivDest = (ImageView) itemView.findViewById(R.id.destination_icon);
            tvDest = (TextView) itemView.findViewById(R.id.record_destination);
            tvStatus = (TextView) itemView.findViewById(R.id.record_status);
        }
    }

}
