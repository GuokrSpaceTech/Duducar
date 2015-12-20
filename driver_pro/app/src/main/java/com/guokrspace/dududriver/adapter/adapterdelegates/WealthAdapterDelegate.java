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

import com.guokrspace.dududriver.DuduDriverApplication;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.database.BaseNotice;
import com.guokrspace.dududriver.database.BaseNoticeDao;
import com.guokrspace.dududriver.model.BaseNoticeItem;
import com.guokrspace.dududriver.model.WealthNotice;
import com.guokrspace.dududriver.util.DateUtil;
import com.hannesdorfmann.adapterdelegates.AbsAdapterDelegate;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

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

        holder.tvTime.setText(DateUtil.dateFormat(notice.pay_time + ""));
        Log.e("daddy message", notice.order_id + " " + notice.passenger_mobile + "  " + notice.sumprice);
        if(notice.pay_role != 1) {
            holder.tvContent.setText(String.format(context.getResources().getString(R.string.wealth_add_notice), notice.passenger_mobile == null ? "未知号码" : notice.passenger_mobile.substring(7), Float.parseFloat(notice.sumprice == "null" ? "0.01" : notice.sumprice)));
        } else {
            holder.tvContent.setText("成功代付了 " + notice.sumprice + "元");
        }
        holder.ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QueryBuilder query = DuduDriverApplication.getInstance().mDaoSession.getBaseNoticeDao().queryBuilder().where(BaseNoticeDao.Properties.NoticeId.eq(((WealthNotice) items.get(position)).message_notice_id)).limit(1);
                if(query.list().size() > 0 && query.list().get(0) != null){
                    BaseNotice notice = (BaseNotice)query.list().get(0);
                    notice.setOutOfTime(true);
                    DuduDriverApplication.getInstance().mDaoSession.getBaseNoticeDao().update(notice);
                }
                items.remove(position);
                mAdapter.notifyItemRemoved(position);
                mAdapter.notifyDataSetChanged();
            }
        });
        holder.llItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                context.startActivity(new Intent(context, PostOrderActivity.class));
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
