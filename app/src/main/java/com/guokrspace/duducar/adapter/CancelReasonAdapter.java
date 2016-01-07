package com.guokrspace.duducar.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.guokrspace.duducar.R;
import com.guokrspace.duducar.model.IdAndValueModel;
import com.guokrspace.duducar.ui.CheckBox;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by hyman on 15/12/12.
 */
public class CancelReasonAdapter extends BaseAdapter {

    private Context context;

    private OnItemCheckedListener mOnItemCheckedListener;

    private List<IdAndValueModel> mItems = null;

    private LayoutInflater mInflater;

    private CheckBox LastCheckBox;
    private TextView LastTextView;
    private Set<ViewHolder> checkItemSet = new HashSet<>();

    public CancelReasonAdapter(Context context, List<IdAndValueModel> mItems) {
        this.context = context;
        this.mItems = mItems;
        mInflater = LayoutInflater.from(context);
    }

    public void setOnItemCheckedListener(OnItemCheckedListener onItemCheckedListener) {
        mOnItemCheckedListener = onItemCheckedListener;
    }

    @Override
    public int getCount() {
        return mItems == null ? 0 : mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.reason_list_item, parent, false);
            holder = new ViewHolder();
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
            holder.descTextView = (TextView) convertView.findViewById(R.id.reson_desc);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        checkItemSet.add(holder);
        final ViewHolder finalHolder = holder;
        holder.descTextView.setText(mItems.get(position).value);
        holder.checkBox.setOncheckListener(new CheckBox.OnCheckListener() {
            @Override
            public void onCheck(CheckBox view, boolean check) {
                clearCheckRecord();
                if (mOnItemCheckedListener != null) {
                    mOnItemCheckedListener.onItemChecked(position);
                }
                finalHolder.checkBox.setChecked(true);
                finalHolder.descTextView.setTextColor(Color.parseColor("#1E88E5"));
                /*synchronized (CancelReasonAdapter.this) {
                    if (mOnItemCheckedListener != null) {
                        mOnItemCheckedListener.onItemChecked(position);
                    }
                    if (check) {
                        if (LastCheckBox != null && LastCheckBox != view) {
                            LastCheckBox.setChecked(!LastCheckBox.isCheck());
                        }
                        if (LastTextView != null) {
                            LastTextView.setTextColor(Color.BLACK);
                        }
                        finalHolder.descTextView.setTextColor(Color.parseColor("#1E88E5"));
                        LastTextView = finalHolder.descTextView;
                        LastCheckBox = view;
                    }
                }*/

            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearCheckRecord();
                if (mOnItemCheckedListener != null) {
                    mOnItemCheckedListener.onItemChecked(position);
                }
                finalHolder.checkBox.setChecked(true);
                finalHolder.descTextView.setTextColor(Color.parseColor("#1E88E5"));
                /*synchronized (CancelReasonAdapter.this) {
                    if (mOnItemCheckedListener != null) {
                        mOnItemCheckedListener.onItemChecked(position);
                    }
                    if (LastCheckBox != null) {
                        LastCheckBox.setChecked(!LastCheckBox.isCheck());
                    }
                    if (LastTextView != null) {
                        LastTextView.setTextColor(Color.BLACK);
                    }
                    finalHolder.checkBox.setChecked(true);
                    finalHolder.descTextView.setTextColor(Color.parseColor("#1E88E5"));
                    LastCheckBox = finalHolder.checkBox;
                    LastTextView = finalHolder.descTextView;
                }*/
            }
        });

        return convertView;
    }

    public void clearCheckRecord() {
        /*LastCheckBox.setChecked(false);
        LastTextView.setTextColor(Color.BLACK);*/
        Iterator checkItemIterator = checkItemSet.iterator();
        for(; checkItemIterator.hasNext();) {
            ViewHolder _holder = (ViewHolder) checkItemIterator.next();
            _holder.checkBox.setChecked(false);
            _holder.descTextView.setTextColor(Color.BLACK);
        }
    }

    private static class ViewHolder {

        CheckBox checkBox;

        TextView descTextView;
    }

    /*
     *用于回调每个item的选中事件
     */
    public interface OnItemCheckedListener {

        void onItemChecked(int position);

    }
}
