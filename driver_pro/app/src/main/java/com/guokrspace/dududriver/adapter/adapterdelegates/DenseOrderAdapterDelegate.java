package com.guokrspace.dududriver.adapter.adapterdelegates;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
import com.guokrspace.dududriver.model.DenseOrderNotice;
import com.guokrspace.dududriver.ui.PostOrderActivity;
import com.hannesdorfmann.adapterdelegates.AbsAdapterDelegate;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by hyman on 15/11/13.
 */
public class DenseOrderAdapterDelegate extends AbsAdapterDelegate<List<BaseNoticeItem>> {

    private LayoutInflater mInflater;

    private Context context;

    private RecyclerView.Adapter mAdapter;

    public DenseOrderAdapterDelegate(RecyclerView.Adapter adapter, Activity activity, int viewType) {
        super(viewType);
        context = activity;
        mAdapter = adapter;
        mInflater = activity.getLayoutInflater();
    }

    @Override
    public boolean isForViewType(List<BaseNoticeItem> items, int position) {
        return items.get(position) instanceof DenseOrderNotice;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new DenseOrderViewHolder(mInflater.inflate(R.layout.notice_item_dense_order, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final List<BaseNoticeItem> items, final int position, @NonNull RecyclerView.ViewHolder viewHolder) {

        DenseOrderViewHolder dovh = (DenseOrderViewHolder) viewHolder;
        DenseOrderNotice notice = (DenseOrderNotice) items.get(position);

        dovh.tvDate.setText(notice.date);
        dovh.tvDescription.setText(notice.description);

        dovh.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QueryBuilder query = DuduDriverApplication.getInstance().mDaoSession.getBaseNoticeDao().queryBuilder().where(BaseNoticeDao.Properties.NoticeId.eq(((DenseOrderNotice) items.get(position)).message_notice_id)).limit(1);
                if(query.list().get(0) != null){
                    BaseNotice notice = (BaseNotice)query.list().get(0);
                    notice.setOutOfTime(true);
                    DuduDriverApplication.getInstance().mDaoSession.getBaseNoticeDao().update(notice);
                }
                items.remove(position);
                mAdapter.notifyItemRemoved(position);
                mAdapter.notifyDataSetChanged();
            }
        });
        dovh.llItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, PostOrderActivity.class));
            }
        });


    }

    static class DenseOrderViewHolder extends RecyclerView.ViewHolder {

        public final LinearLayout llItem;
        public final TextView tvDate;
        public final TextView tvDescription;
        public final ImageButton btnDelete;

        public DenseOrderViewHolder(View itemView) {
            super(itemView);
            llItem = (LinearLayout)itemView.findViewById(R.id.itemLLayout);
            tvDate = (TextView) itemView.findViewById(R.id.order_time);
            tvDescription = (TextView) itemView.findViewById(R.id.description_tv);
            btnDelete = (ImageButton) itemView.findViewById(R.id.delete_btn);
        }
    }
}
