package com.example.moviqoplay.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviqoplay.R;
import com.example.moviqoplay.model.VideoItem;
import com.example.moviqoplay.util.MediaUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
    public interface OnVideoClickListener {
        void onVideoClick(VideoItem video, int position);
    }

    private final List<VideoItem> videos = new ArrayList<>();
    private final OnVideoClickListener listener;

    public VideoAdapter(OnVideoClickListener listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    public void submitVideos(List<VideoItem> newVideos) {
        videos.clear();
        if (newVideos != null) {
            videos.addAll(newVideos);
        }
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return videos.get(position).getId();
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoItem video = videos.get(position);
        holder.title.setText(video.getTitle());
        holder.folder.setText(video.getFolderName());
        holder.duration.setText(MediaUtils.formatDuration(video.getDuration()));
        holder.size.setText(formatSize(video.getSize()));
        Glide.with(holder.thumbnail)
                .load(Uri.parse(video.getThumbnailUri()))
                .centerCrop()
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(holder.thumbnail);
        holder.itemView.setOnClickListener(view -> {
            if (listener != null) {
                listener.onVideoClick(video, holder.getBindingAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    private static String formatSize(long bytes) {
        if (bytes <= 0) {
            return "0 MB";
        }
        double mb = bytes / 1024d / 1024d;
        return new DecimalFormat("#,##0.# MB").format(mb);
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        final ImageView thumbnail;
        final TextView title;
        final TextView folder;
        final TextView duration;
        final TextView size;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.imgVideoThumb);
            title = itemView.findViewById(R.id.txtVideoTitle);
            folder = itemView.findViewById(R.id.txtVideoFolder);
            duration = itemView.findViewById(R.id.txtVideoDuration);
            size = itemView.findViewById(R.id.txtVideoSize);
        }
    }
}
