package com.example.moviqoplay.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviqoplay.R;
import com.example.moviqoplay.model.Song;
import com.example.moviqoplay.util.MediaUtils;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    public interface OnSongClickListener {
        void onSongClick(Song song, int position);
    }

    private final List<Song> songs = new ArrayList<>();
    private final OnSongClickListener listener;
    private final boolean isCardView;
    private long playingSongId = RecyclerView.NO_ID;

    public SongAdapter(OnSongClickListener listener) {
        this(listener, false);
    }

    public SongAdapter(OnSongClickListener listener, boolean isCardView) {
        this.listener = listener;
        this.isCardView = isCardView;
        setHasStableIds(true);
    }

    public void submitSongs(List<Song> newSongs) {
        songs.clear();
        if (newSongs != null) {
            songs.addAll(newSongs);
        }
        notifyDataSetChanged();
    }

    public void setPlayingSongId(long songId) {
        if (playingSongId == songId) {
            return;
        }
        long previousId = playingSongId;
        playingSongId = songId;
        for (int i = 0; i < songs.size(); i++) {
            long id = songs.get(i).getId();
            if (id == previousId || id == songId) {
                notifyItemChanged(i);
            }
        }
    }

    @Override
    public long getItemId(int position) {
        return songs.get(position).getId();
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = isCardView ? R.layout.item_music_card : R.layout.song_item;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.title.setText(song.getTitle());
        holder.artist.setText(song.getArtist());
        holder.duration.setText(MediaUtils.formatDuration(song.getDuration()));
        holder.playingIndicator.setVisibility(song.getId() == playingSongId ? View.VISIBLE : View.INVISIBLE);
        if (song.getAlbumArtUri() != null) {
            Glide.with(holder.albumArt)
                    .load(song.getAlbumArtUri())
                    .centerCrop()
                    .placeholder(R.drawable.sample_album)
                    .error(R.drawable.sample_album)
                    .into(holder.albumArt);
        } else {
            holder.albumArt.setImageResource(R.drawable.sample_album);
        }
        holder.itemView.setOnClickListener(view -> {
            if (listener != null) {
                listener.onSongClick(song, holder.getBindingAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        final ImageView albumArt;
        final TextView title;
        final TextView artist;
        final TextView duration;
        final View playingIndicator;

        SongViewHolder(@NonNull View itemView) {
            super(itemView);
            albumArt = itemView.findViewById(R.id.imgSongArt);
            title = itemView.findViewById(R.id.txtSongTitle);
            artist = itemView.findViewById(R.id.txtSongArtist);
            duration = itemView.findViewById(R.id.txtSongDuration);
            playingIndicator = itemView.findViewById(R.id.viewPlayingIndicator);
        }
    }
}
