package com.example.moviqoplay.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviqoplay.R;
import com.example.moviqoplay.model.PlaylistItem;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {
    public interface OnPlaylistClickListener {
        void onPlaylistClick(PlaylistItem item);
    }

    private final List<PlaylistItem> items;
    private final OnPlaylistClickListener listener;

    public PlaylistAdapter(List<PlaylistItem> items, OnPlaylistClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MaterialCardView card = new MaterialCardView(parent.getContext());
        RecyclerView.LayoutParams cardParams = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int margin = parent.getResources().getDimensionPixelSize(R.dimen.space_sm);
        cardParams.setMargins(0, 0, 0, margin);
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(parent.getContext().getColor(R.color.glass_overlay));
        card.setStrokeColor(parent.getContext().getColor(R.color.glass_stroke));
        card.setStrokeWidth(parent.getResources().getDimensionPixelSize(R.dimen.stroke_glass));
        card.setRadius(parent.getResources().getDimension(R.dimen.radius_lg));

        LinearLayout content = new LinearLayout(parent.getContext());
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = parent.getResources().getDimensionPixelSize(R.dimen.space_md);
        content.setPadding(padding, padding, padding, padding);

        TextView title = new TextView(parent.getContext());
        title.setTextColor(parent.getContext().getColor(R.color.text_primary));
        title.setTextSize(18);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        content.addView(title);

        TextView subtitle = new TextView(parent.getContext());
        subtitle.setTextColor(parent.getContext().getColor(R.color.text_secondary));
        subtitle.setTextSize(14);
        content.addView(subtitle);

        card.addView(content);
        return new PlaylistViewHolder(card, title, subtitle);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        PlaylistItem item = items.get(position);
        holder.title.setText(item.getName());
        holder.subtitle.setText(item.getDescription());
        holder.itemView.setOnClickListener(view -> {
            if (listener != null) {
                listener.onPlaylistClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView subtitle;

        PlaylistViewHolder(@NonNull View itemView, TextView title, TextView subtitle) {
            super(itemView);
            this.title = title;
            this.subtitle = subtitle;
        }
    }
}
