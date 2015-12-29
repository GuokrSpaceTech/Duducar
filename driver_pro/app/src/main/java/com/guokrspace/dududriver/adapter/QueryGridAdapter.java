package com.guokrspace.dududriver.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.util.DisplayUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hyman on 15/12/29.
 */
public class QueryGridAdapter extends RecyclerView.Adapter<QueryGridAdapter.QueryViewHolder>{

    private Context context;

    private String[] mItemText = null;

    private int[] mItemResId = null;

    private final List<ItemData> mItems = new ArrayList<>();

    private OnItemClickListener mOnItemClickListener;

    public QueryGridAdapter(Context context) {
        this.context = context;
        mItemText = context.getResources().getStringArray(R.array.query_item_text);
        mItemResId = new int[]{R.mipmap.ic_profit,
                R.mipmap.ic_search,
                R.mipmap.ic_bill};
        for (int i = 0; i < mItemText.length; i++) {
            mItems.add(new ItemData(mItemText[i], mItemResId[i]));
        }
    }

    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    @Override
    public QueryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.query_recyclerview_item, parent, false);
        int h = DisplayUtil.SCREEN_WIDTH_PIXELS / 3;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, h);
        view.setLayoutParams(layoutParams);
        QueryViewHolder mViewHolder = new QueryViewHolder(view);
        return mViewHolder;
    }

    @Override
    public void onBindViewHolder(QueryViewHolder holder, final int position) {
        holder.itemText.setText(mItems.get(position % mItems.size()).text);
        holder.itemIcon.setImageResource(mItems.get(position % mItems.size()).resId);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    public class QueryViewHolder extends RecyclerView.ViewHolder {

        public final TextView itemText;

        public final ImageView itemIcon;

        public QueryViewHolder(View itemView) {
            super(itemView);
            itemText = (TextView) itemView.findViewById(R.id.item_text);
            itemIcon = (ImageView) itemView.findViewById(R.id.item_icon);
        }
    }

    class ItemData {

        public ItemData(String text, int resId) {
            this.text = text;
            this.resId = resId;
        }

        public String text;

        public int resId;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
