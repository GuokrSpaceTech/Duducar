package com.guokrspace.dududriver.ui;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.guokrspace.dududriver.R;

/**
 * Created by hyman on 15/12/10.
 */
public class HymanScrollView extends ScrollView {

    public interface OnGetMoreListener {
        public void onGetMore();
    }

    private OnGetMoreListener onGetMoreListener;

    private LayoutInflater inflater;

    private ViewGroup contentView;

    private View footView;

    private TextView tvFootTitle;

    private ProgressBar pbFootRefreshing;

    private boolean addFooterFlag;

    private boolean isGetMoreing = false;

    public HymanScrollView(Context context) {
        this(context, null);
    }

    public HymanScrollView(Context context, AttributeSet atts) {
        this(context, atts, 0);
    }

    public HymanScrollView(Context context, AttributeSet atts, int defStyle) {
        super(context, atts, defStyle);
        init(context);
    }

    private void init(Context  context) {
        inflater = LayoutInflater.from(context);

        footView = inflater.inflate(R.layout.get_more_footview, this, false);
        tvFootTitle = (TextView) footView.findViewById(R.id.tv_foot_title);
        pbFootRefreshing = (ProgressBar) footView.findViewById(R.id.pb_foot_refreshing);

    }

    /*
	 * 重写该方法，禁止ScrollView内布局变化后自动滚动
	 */
    @Override
    protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);


    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        if (deltaY > 0 && Math.abs(deltaY) > 20) {
            doOnGetMoreListener();
        }

        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }

    public void setOnGetMoreListener (final OnGetMoreListener onGetMoreListener) {
        this.onGetMoreListener = onGetMoreListener;
        if (contentView == null) {
            contentView = (ViewGroup) getChildAt(0);
            if (!addFooterFlag) {
                addFooterFlag = true;
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -2);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                footView.setLayoutParams(layoutParams);
                footView.setVisibility(GONE);
                contentView.addView(footView);
            }

        }
    }

    /**
     * 加载完成
     */
    public void getMoreComplete() {
        isGetMoreing = false;
        footView.setVisibility(GONE);
    }

    private void doOnGetMoreListener() {
        if (contentView != null && contentView.getMeasuredHeight() <= getScrollY() + getHeight()) {
            if (onGetMoreListener != null && !isGetMoreing) {
                isGetMoreing = true;
                footView.setVisibility(VISIBLE);
                tvFootTitle.setText("正在加载...");
                onGetMoreListener.onGetMore();
            }
        }
    }
}
