package com.example.moviqoplay.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
    private long playingSongId = RecyclerView.NO_ID;

    public SongAdapter(OnSongClickListener listener) {
        this.listener = listener;
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
        playingSongId = songId;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return songs.get(position).getId();
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item, parent, false);
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
            holder.albumArt.setImageURI(Uri.parse(song.getAlbumArtUri()));
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
