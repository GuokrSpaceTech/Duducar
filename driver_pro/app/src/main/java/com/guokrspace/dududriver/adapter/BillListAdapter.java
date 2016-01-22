package com.guokrspace.dududriver.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.model.BillItem;
import com.guokrspace.dududriver.util.CommonUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by hyman on 15/12/10.
 */
public class BillListAdapter extends BaseAdapter {

    private Context context;

    private LayoutInflater inflater;

    private List<BillItem> items;

    public BillListAdapter(Context context, List<BillItem> items) {
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
//            holder.tvAccount = (TextView) convertView.findViewById(R.id.trade_account);
            holder.tvDescription = (TextView) convertView.findViewById(R.id.trade_description);
            holder.tvTime = (TextView) convertView.findViewById(R.id.trade_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

//        holder.tvAccount.setText(CommonUtil.phoneNumFormat(items.get(position).getOpposite()));
        holder.tvDescription.setText(items.get(position).getDescription());
        holder.tvTime.setText(CommonUtil.dateFormat(items.get(position).getTime(), CommonUtil.YEAR_MONTH_DAY));
        String money = items.get(position).getMoney();
        int moneyColor = -1;
        switch (items.get(position).getType()) {
            case 1:
                moneyColor = context.getResources().getColor(R.color.holo_red_light);
                money = "+" + money;
                break;
            case 2:
                moneyColor = context.getResources().getColor(R.color.orange);
                money = "+" + money;
                break;
            case 0:
                moneyColor = context.getResources().getColor(R.color.sky_blue);
                money = "-" + money;
                break;
            default:
                break;
        }
        holder.tvSum.setText(money);
        holder.tvSum.setTextColor(moneyColor);

        return convertView;
    }

    private static class ViewHolder {

        TextView tvSum;

        TextView tvDescription;

        TextView tvTime;
    }


}
