package com.bkav.android.mymusic.models;

public class Song {
    private int mStt;
    private String mNameSong;
    private String mTimeSong;
    private String mAuthorSong;
    private int mImageSong;
    private int mSong;

    public Song(int mStt, String mNameSong, String mTimeSong, String mAuthorSong, int mImageSong, int mSong) {
        this.mStt = mStt;
        this.mNameSong = mNameSong;
        this.mTimeSong = mTimeSong;
        this.mAuthorSong = mAuthorSong;
        this.mImageSong = mImageSong;
        this.mSong = mSong;
    }

    public int getmStt() {
        return mStt;
    }

    public void setmOrdinalNumber(int mStt) {
        this.mStt = mStt;
    }

    public String getmNameSong() {
        return mNameSong;
    }

    public void setmNameSong(String mNameSong) {
        this.mNameSong = mNameSong;
    }

    public String getmTimeSong() {
        return mTimeSong;
    }

    public void setmTimeSong(String mTimeSong) {
        this.mTimeSong = mTimeSong;
    }

    public String getmAuthorSong() {
        return mAuthorSong;
    }

    public void setmAuthorSong(String mAuthor) {
        this.mAuthorSong = mAuthorSong;
    }

    public int getmImageSong() {
        return mImageSong;
    }

    public void setmImageSong(int mImageSong) {
        this.mImageSong = mImageSong;
    }

    public int getmSong() {
        return mSong;
    }

    public void setmSong(int mSong) {
        this.mSong = mSong;
    }
}
