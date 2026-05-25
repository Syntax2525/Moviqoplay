package com.example.moviqoplay.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviqoplay.R;
import com.example.moviqoplay.model.MediaItem;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {
    public interface OnMediaClickListener {
        void onMediaClick(MediaItem item);
    }

    private final List<MediaItem> items;
    private final OnMediaClickListener listener;

    public MediaAdapter(List<MediaItem> items, OnMediaClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MaterialCardView card = new MaterialCardView(parent.getContext());
        int width = parent.getResources().getDimensionPixelSize(R.dimen.album_card_size);
        int margin = parent.getResources().getDimensionPixelSize(R.dimen.space_sm);
        RecyclerView.LayoutParams cardParams = new RecyclerView.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, margin, 0);
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(parent.getContext().getColor(R.color.glass_overlay));
        card.setStrokeColor(parent.getContext().getColor(R.color.glass_stroke));
        card.setStrokeWidth(parent.getResources().getDimensionPixelSize(R.dimen.stroke_glass));
        card.setRadius(parent.getResources().getDimension(R.dimen.radius_lg));

        LinearLayout content = new LinearLayout(parent.getContext());
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = parent.getResources().getDimensionPixelSize(R.dimen.space_sm);
        content.setPadding(padding, padding, padding, padding);

        ImageView image = new ImageView(parent.getContext());
        image.setId(View.generateViewId());
        image.setImageResource(R.drawable.sample_album);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        content.addView(image, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                parent.getResources().getDimensionPixelSize(R.dimen.album_card_size)
        ));

        TextView title = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, content, false);
        title.setTextColor(parent.getContext().getColor(R.color.text_primary));
        title.setTextSize(15);
        content.addView(title);

        TextView subtitle = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, content, false);
        subtitle.setTextColor(parent.getContext().getColor(R.color.text_secondary));
        subtitle.setTextSize(13);
        content.addView(subtitle);

        card.addView(content);
        return new MediaViewHolder(card, title, subtitle);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        MediaItem item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.subtitle.setText(item.getSubtitle() + " · " + item.getMeta());
        holder.itemView.setOnClickListener(view -> {
            if (listener != null) {
                listener.onMediaClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView subtitle;

        MediaViewHolder(@NonNull View itemView, TextView title, TextView subtitle) {
            super(itemView);
            this.title = title;
            this.subtitle = subtitle;
        }
    }
}
