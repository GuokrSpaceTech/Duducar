package com.guokrspace.dududriver.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.common.MoreOptionType;
import com.guokrspace.dududriver.ui.SettingActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import co.paulburke.itemtouchhelper.ItemTouchHelperAdapter;
import co.paulburke.itemtouchhelper.OnStartDragListener;

/**
 * Created by hyman on 15/10/23.
 */
public class OptionGridAdapter extends RecyclerView.Adapter<OptionGridAdapter.ItemViewHolder> implements ItemTouchHelperAdapter {

    private static final int REFRESH = 200;

    private boolean isClickOver = false;

    private Timer mTimer = null;

    private String[] mItemText = null;

    private int[] mItemResId = null;

    private final List<ItemData> mItems = new ArrayList<>();
    private Context context;

    private OnStartDragListener mDragStartListener;

    public OptionGridAdapter(Context context, OnStartDragListener onStartDragListener) {
        this.context = context;
        this.mDragStartListener = onStartDragListener;
        mItemText = context.getResources().getStringArray(R.array.more_option_text);
        mItemResId = new int[]{R.mipmap.suggest,
                R.mipmap.map,
                R.mipmap.upgrade,
                R.mipmap.msg,
                R.mipmap.guide,
                R.mipmap.setting};
        for (int i = 0; i < mItemText.length; i++) {
            mItems.add(new ItemData(mItemText[i], mItemResId[i]));
        }
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.more_recyclerview_item, parent, false);
        ItemViewHolder mViewHolder = new ItemViewHolder(view);
        return mViewHolder;
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, final int position) {

        holder.tvOption.setText(mItems.get(position % mItems.size()).text);
        holder.ivOption.setImageResource(mItems.get(position % mItems.size()).resId);

        holder.contentLayout.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    isClickOver = false;
                    mTimer = new Timer();

                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            isClickOver = true;
                        }
                    }, 30);
                } else if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_UP) {
                    if (isClickOver) {
                        //跳转
                        Toast.makeText(context, "haha", Toast.LENGTH_SHORT).show();
                        aheadPage(position);

                    } else {
                        mDragStartListener.onStartDrag(holder);
                    }
                }
                return true;
            }
        });

    }

    private void aheadPage(int position) {
        switch (MoreOptionType.getByVal(position)) {
            case SUGGESTION:

                break;
            case ORDER_MAP:

                break;
            case CAR_UPDATE:

                break;
            case MESSAGE:

                break;
            case GUIDE:

                break;
            case SETTING:
                context.startActivity(new Intent(context, SettingActivity.class));
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mItems, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperAdapter {

        public final ImageView ivOption;

        public final TextView tvOption;

        public final LinearLayout contentLayout;


        public ItemViewHolder(View itemView) {
            super(itemView);
            ivOption = (ImageView) itemView.findViewById(R.id.option_icon);
            tvOption = (TextView) itemView.findViewById(R.id.option_text);
            contentLayout = (LinearLayout) itemView.findViewById(R.id.content_ll);
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            itemView.setBackgroundColor(Color.LTGRAY);
            return false;
        }

        @Override
        public void onItemDismiss(int position) {
            itemView.setBackgroundColor(0);
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

}
