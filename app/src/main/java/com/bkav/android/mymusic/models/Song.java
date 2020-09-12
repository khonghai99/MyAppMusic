package com.bkav.android.mymusic.models;

public class Song {
    private String mTitle;
    private String mArtist;
    private String mDuration;
    private String mPath;


    public Song(String mTitle, String mArtist, String mDuration, String mPath) {
        this.mTitle = mTitle;
        this.mArtist = mArtist;
        this.mDuration = mDuration;
        this.mPath = mPath;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getArtist() {
        return mArtist;
    }

    public void setArtist(String mArtist) {
        this.mArtist = mArtist;
    }

    public String getDuration() {
        return mDuration;
    }

    public void setDuration(String mDuration) {
        this.mDuration = mDuration;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String mPath) {
        this.mPath = mPath;
    }
}
