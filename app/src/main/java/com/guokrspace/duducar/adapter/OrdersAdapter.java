package com.guokrspace.duducar.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guokrspace.duducar.R;
import com.guokrspace.duducar.communication.http.model.Order;
import com.guokrspace.duducar.util.DisplayUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by hyman on 15/12/7.
 */
public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.RecordViewHolder> {

    private Context context;

    private List<Order> mItems = null;

    private OnItemClickListener mOnItemClickListener;

    public OrdersAdapter(Context context, List<Order> mItems) {
        this.context = context;
        this.mItems = mItems;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public RecordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.history_order_card_view, parent, false);
        RecordViewHolder holder = new RecordViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecordViewHolder holder, int position) {
        if (mItems != null) {
            holder.tvTime.setText(DisplayUtils.getShortDate(mItems.get(position).getEnd_time() + ""));
            holder.tvStart.setText(mItems.get(position).getStart());
            holder.tvDestination.setText(mItems.get(position).getDestination());
            holder.tvStatus.setText("已支付");
            if (mOnItemClickListener != null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = holder.getLayoutPosition();
                        mOnItemClickListener.onItemClick(holder.itemView, pos);
                    }
                });
                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int pos = holder.getLayoutPosition();
                        mOnItemClickListener.onItemLongClick(holder.itemView, pos);
                        return false;
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    public void removeData(int pos) {
        mItems.remove(pos);
        notifyItemRemoved(pos);
    }

    public static class RecordViewHolder extends RecyclerView.ViewHolder {

        public final TextView tvTime;

        public final TextView tvStart;

        public final TextView tvDestination;

        public final TextView tvStatus;

        public RecordViewHolder(View itemView) {
            super(itemView);
            tvTime = (TextView) itemView.findViewById(R.id.ordertime_textview);
            tvStart = (TextView) itemView.findViewById(R.id.start_textview);
            tvDestination = (TextView) itemView.findViewById(R.id.destination_textview);
            tvStatus = (TextView) itemView.findViewById(R.id.orderstatus_textview);
        }
    }


    //将时间转换成 yyyy-MM-dd HH:mm 格式
    private String dateFormat(String date) {
        if (TextUtils.isEmpty(date)) {
            return "一万年前";
        }
        Date orderDate = new Date(Long.parseLong(date));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return format.format(orderDate);
    }

    public interface OnItemClickListener {

        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }
}
