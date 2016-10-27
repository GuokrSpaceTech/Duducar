package com.guokrspace.duducar.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.guokrspace.duducar.R;
import com.guokrspace.duducar.WebViewActivity;
import com.guokrspace.duducar.model.News;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by hyman on 15/12/22.
 */
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder>{

    private Context context;
    private List<News> mItems = null;

    public NewsAdapter(Context context, List<News> mItems) {
        this.context = context;
        this.mItems = mItems;
    }

    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.news_card_view, parent, false);
        NewsViewHolder holder = new NewsViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(NewsViewHolder holder, int position) {

        if (mItems != null) {
            holder.tvTitle.setText(mItems.get(position).getTitle());
            holder.tvTime.setText(mItems.get(position).getTime());
            if (!TextUtils.isEmpty(mItems.get(position).getImg())) {
                Picasso.with(context)
                        .load(mItems.get(position).getImg())
                        .centerCrop().fit().into(holder.newsImage);
            }
            holder.tvContent.setText(mItems.get(position).getContent());
            if (TextUtils.isEmpty(mItems.get(position).getImg())) {
                holder.newsImage.setVisibility(View.GONE);
            }
//            if (!TextUtils.isEmpty(mItems.get(position).getUrl())) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //进入相应的webview
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra(WebViewActivity.WEBVIEW_TYPE, 0);
                        intent.putExtra(WebViewActivity.WEBVIEW_URL, "");
                        context.startActivity(intent);
                    }
                });
//            }
        }

    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {

        public final TextView tvTitle;

        public final TextView tvTime;

        public final ImageView newsImage;

        public final TextView tvContent;


        public NewsViewHolder(View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.news_title);
            tvTime = (TextView) itemView.findViewById(R.id.news_time);
            newsImage = (ImageView) itemView.findViewById(R.id.news_img);
            tvContent = (TextView) itemView.findViewById(R.id.news_content);
        }
    }
}
