package com.guokrspace.dududriver.adapter.adapterdelegates;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.guokrspace.dududriver.R;
import com.hannesdorfmann.adapterdelegates.AbsFallbackAdapterDelegate;

/**
 * Created by hyman on 15/11/13.
 */
public class NoticeFallbackDelegate extends AbsFallbackAdapterDelegate {

    private LayoutInflater mInflater;

    public NoticeFallbackDelegate(Activity activity) {
        mInflater = activity.getLayoutInflater();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new NoticeFallBackViewHolder(mInflater.inflate(R.layout.notice_item_unknown, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Object o, int position, @NonNull RecyclerView.ViewHolder viewHolder) {
    }

    static class NoticeFallBackViewHolder extends RecyclerView.ViewHolder {

        public NoticeFallBackViewHolder(View itemView) {
            super(itemView);
        }
    }
}
