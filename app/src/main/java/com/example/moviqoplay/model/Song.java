package com.example.moviqoplay.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Song implements Parcelable {
    private final long id;
    private final String title;
    private final String artist;
    private final String album;
    private final long duration;
    private final String path;
    private final String albumArtUri;

    public Song(long id, String title, String artist, String album, long duration, String path, String albumArtUri) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.path = path;
        this.albumArtUri = albumArtUri;
    }

    protected Song(Parcel in) {
        id = in.readLong();
        title = in.readString();
        artist = in.readString();
        album = in.readString();
        duration = in.readLong();
        path = in.readString();
        albumArtUri = in.readString();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public long getDuration() {
        return duration;
    }

    public String getPath() {
        return path;
    }

    public String getAlbumArtUri() {
        return albumArtUri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeLong(duration);
        dest.writeString(path);
        dest.writeString(albumArtUri);
    }
}
