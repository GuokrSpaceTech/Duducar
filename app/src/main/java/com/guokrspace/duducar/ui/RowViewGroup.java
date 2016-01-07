package com.guokrspace.duducar.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.guokrspace.duducar.R;
import com.guokrspace.duducar.common.CommonAddrType;
import com.guokrspace.duducar.model.AddrRowDescriptor;
import com.guokrspace.duducar.util.Trace;

import java.util.List;

/**
 * Created by hyman on 16/1/4.
 */
public class RowViewGroup extends LinearLayout {

    private Context context;
    private List<AddrRowDescriptor> descriptors;
    private OnRowTabListener onRowTabListener;

    public RowViewGroup(Context context) {
        this(context, null);
    }

    public RowViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RowViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView(context);
    }

    private void initializeView(Context context) {
        this.context = context;
        setOrientation(VERTICAL);
    }

    public void initializeData(List<AddrRowDescriptor> descriptors, OnRowTabListener listener) {
        this.descriptors = descriptors;
        this.onRowTabListener = listener;
    }

    public void notifyDataChanged() {
        if (descriptors != null && descriptors.size() > 0) {
            LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
            for (int i = 0; i < descriptors.size(); i++) {
                CommonAddressRowView rowView = new CommonAddressRowView(context);
                rowView.initializeData(descriptors.get(i));
                rowView.notifyDataChanged();
                final int finalI = i;
                rowView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onRowTabListener != null) {
                            onRowTabListener.onRowTab(CommonAddrType.getByDesc(descriptors.get(finalI).rowName));
                        }
                    }
                });
                addView(rowView);
                if (i != descriptors.size() - 1) {
                    View line = new View(context);
                    line.setLayoutParams(layoutParams);
                    line.setBackgroundColor(context.getResources().getColor(R.color.light_gray));
                    addView(line);
                }
            }
        }
    }

    public void refreshData() {
        if (descriptors != null && descriptors.size() > 0) {
            int index = 0;
            for (int i = 0; i < getChildCount(); i++) {
                View view = getChildAt(i);
                if (view instanceof CommonAddressRowView) {
                    ((CommonAddressRowView) view).initializeData(descriptors.get(index));
                    ((CommonAddressRowView) view).notifyDataChanged();
                    index++;
                }
            }
        }
    }

    public interface OnRowTabListener {
        void onRowTab(CommonAddrType type);
    }
}
