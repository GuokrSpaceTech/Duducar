package com.guokrspace.dududriver.adapter;

import android.app.Activity;

import com.guokrspace.dududriver.adapter.adapterdelegates.DenseOrderAdapterDelegate;
import com.guokrspace.dududriver.adapter.adapterdelegates.DuduNoticeAdapterDelegate;
import com.guokrspace.dududriver.adapter.adapterdelegates.NoticeFallbackDelegate;
import com.guokrspace.dududriver.adapter.adapterdelegates.WealthAdapterDelegate;
import com.guokrspace.dududriver.model.BaseNoticeItem;
import com.hannesdorfmann.adapterdelegates.ListDelegationAdapter;

import java.util.List;

/**
 * Created by hyman on 15/11/13.
 */
public class NoticeAdapter extends ListDelegationAdapter<List<BaseNoticeItem>> {

    public NoticeAdapter(Activity activity, List<BaseNoticeItem> items) {

        //Delegates
        this.delegatesManager.addDelegate(new DenseOrderAdapterDelegate(this, activity, 0));
        this.delegatesManager.addDelegate(new WealthAdapterDelegate(this, activity, 1));
        this.delegatesManager.addDelegate(new DuduNoticeAdapterDelegate(this, activity, 2));
        this.delegatesManager.setFallbackDelegate(new NoticeFallbackDelegate(activity));

        setItems(items);
    }
}
