package com.guokrspace.dududriver.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.model.OrderListItem;

import java.util.List;

/**
 * Created by hyman on 15/10/24.
 */
public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.OrderViewHolder> {

    private List<OrderListItem> mItems = null;

    public OrderListAdapter(List<OrderListItem> data) {
        this.mItems = data;
    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_list_item, parent, false);
        OrderViewHolder orderViewHolder = new OrderViewHolder(view);
        return orderViewHolder;
    }

    @Override
    public void onBindViewHolder(OrderViewHolder holder, final int position) {
        if (mItems != null) {
            holder.tvDate.setText(mItems.get(position).date);
            holder.tvDescription.setText(mItems.get(position).description);
        }
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItems.remove(position);
                notifyItemRemoved(position);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {

        public final TextView tvDate;

        public final TextView tvDescription;

        public final ImageButton btnDelete;

        public OrderViewHolder(View itemView) {
            super(itemView);
            tvDate = (TextView) itemView.findViewById(R.id.order_time);
            tvDescription = (TextView) itemView.findViewById(R.id.description_tv);
            btnDelete = (ImageButton) itemView.findViewById(R.id.delete_btn);
        }
    }

}
