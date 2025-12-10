package com.example.clientsellingmedicine.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.interfaces.IOnFeedItemClickListener;
import com.example.clientsellingmedicine.DTO.Feed;


import java.util.List;

public class feedAdapter extends RecyclerView.Adapter <feedAdapter.ViewHolder> {
    private List<Feed> mFeeds;
    private Context mContext;

    private IOnFeedItemClickListener mListener;


    public feedAdapter(List<Feed> list, IOnFeedItemClickListener listener) {
        this.mFeeds = list;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View newsView = inflater.inflate(R.layout.healthy_news_item, parent, false);

        feedAdapter.ViewHolder viewHolder = new feedAdapter.ViewHolder(newsView,context);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Feed feed = mFeeds.get(position);
        if (feed == null) {
            return;
        }
        holder.tvTitle.setText(feed.getTitle());
        holder.tvDescription.setText(feed.getDescription());
        holder.tvDate.setText(feed.getPubDate());
        Glide.with(holder.itemView.getContext())
                .load(feed.getImage())
                .placeholder(R.drawable.loading_icon) // Hình ảnh thay thế khi đang tải
                .error(R.drawable.error_image) // Hình ảnh thay thế khi có lỗi
                .into(holder.ivFeed);

        holder.layoutFeedItem.setOnClickListener(view -> mListener.onItemClick(feed));
    }

    @Override
    public int getItemCount() {
        return mFeeds.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTitle,tvDescription,tvDate;
        public ImageView ivFeed;

        private LinearLayout layoutFeedItem;
        public ViewHolder(View itemView, Context context) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivFeed = itemView.findViewById(R.id.ivFeed);
            layoutFeedItem = itemView.findViewById(R.id.layoutFeedItem);
        }
    }
}
