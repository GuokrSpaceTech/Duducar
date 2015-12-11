package com.guokrspace.dududriver.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.model.BillInfo;
import com.guokrspace.dududriver.util.CommonUtil;

import java.util.List;

/**
 * Created by hyman on 15/12/10.
 */
public class BillListAdapter extends BaseAdapter {

    private Context context;

    private LayoutInflater inflater;

    private List<BillInfo> items;

    public BillListAdapter(Context context, List<BillInfo> items) {
        this.context = context;
        this.items = items;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.balance_list_item, null, false);
            holder = new ViewHolder();
            holder.tvSum = (TextView) convertView.findViewById(R.id.trade_cash);
            holder.tvAccount = (TextView) convertView.findViewById(R.id.trade_account);
            holder.tvDescription = (TextView) convertView.findViewById(R.id.trade_description);
            holder.tvTime = (TextView) convertView.findViewById(R.id.trade_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvSum.setText(items.get(position).sum);
        holder.tvAccount.setText(CommonUtil.phoneNumFormat(items.get(position).account));
        holder.tvDescription.setText(items.get(position).desctiption);
        holder.tvTime.setText(CommonUtil.dateFormat(items.get(position).tradeTime, CommonUtil.YEAR_MONTH_DAY));

        return convertView;
    }

    private static class ViewHolder {

        TextView tvSum;

        TextView tvAccount;

        TextView tvDescription;

        TextView tvTime;
    }
}
