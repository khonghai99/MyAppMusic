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

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmArtist() {
        return mArtist;
    }

    public void setmArtist(String mArtist) {
        this.mArtist = mArtist;
    }

    public String getmDuration() {
        return mDuration;
    }

    public void setmDuration(String mDuration) {
        this.mDuration = mDuration;
    }

    public String getmPath() {
        return mPath;
    }

    public void setmPath(String mPath) {
        this.mPath = mPath;
    }
}
