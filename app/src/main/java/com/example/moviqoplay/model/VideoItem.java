package com.example.moviqoplay.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class VideoItem implements Parcelable {
    private final long id;
    private final String title;
    private final long duration;
    private final String path;
    private final String thumbnailUri;
    private final String folderName;
    private final long size;

    public VideoItem(long id, String title, long duration, String path, String thumbnailUri, String folderName, long size) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.path = path;
        this.thumbnailUri = thumbnailUri;
        this.folderName = folderName;
        this.size = size;
    }

    protected VideoItem(Parcel in) {
        id = in.readLong();
        title = in.readString();
        duration = in.readLong();
        path = in.readString();
        thumbnailUri = in.readString();
        folderName = in.readString();
        size = in.readLong();
    }

    public static final Creator<VideoItem> CREATOR = new Creator<VideoItem>() {
        @Override
        public VideoItem createFromParcel(Parcel in) {
            return new VideoItem(in);
        }

        @Override
        public VideoItem[] newArray(int size) {
            return new VideoItem[size];
        }
    };

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public long getDuration() {
        return duration;
    }

    public String getPath() {
        return path;
    }

    public String getThumbnailUri() {
        return thumbnailUri;
    }

    public String getFolderName() {
        return folderName;
    }

    public long getSize() {
        return size;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeLong(duration);
        dest.writeString(path);
        dest.writeString(thumbnailUri);
        dest.writeString(folderName);
        dest.writeLong(size);
    }
}
