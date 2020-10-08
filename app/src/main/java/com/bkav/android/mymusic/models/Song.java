package com.bkav.android.mymusic.models;

import android.annotation.SuppressLint;

import java.util.concurrent.TimeUnit;

public class Song {
    private int mID;
    private String mTitle;
    private String mArtist;
    private String mDuration;
    private String mPath;

    public Song(int mID, String mTitle, String mArtist, String mDuration, String mPath) {
        this.mID = mID;
        this.mTitle = mTitle;
        this.mArtist = mArtist;
        this.mDuration = mDuration;
        this.mPath = mPath;
    }

    /**
     * call timeUnitToFullTime to convert
     *
     * @param millisecond milliseconds to transfer
     * @return "m:s"
     */

    public static String millisecondToFullTime(long millisecond) {
        return timeUnitToFullTime(millisecond, TimeUnit.MILLISECONDS);
    }

    /**
     * convert time to string ("m:s")
     *
     * @param time     duration of song
     * @param timeUnit Object TimeUnit
     * @return time formatted
     */
    @SuppressLint("DefaultLocale")
    public static String timeUnitToFullTime(long time, TimeUnit timeUnit) {
        long hour = timeUnit.toHours(time) % 24;
        long minute = timeUnit.toMinutes(time) % 60;
        long second = timeUnit.toSeconds(time) % 60;
        if (hour > 0) {
            return String.format("%d:%02d:%02d", hour, minute, second);
        } else if (minute > 0) {
            return String.format("%d:%02d", minute, second);
        } else {
            return String.format("%d:%02d", minute, second);
        }
    }

    public int getID() {
        return mID;
    }

    public void setID(int mID) {
        this.mID = mID;
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

    public String getDurationReal() {
        return mDuration;
    }

    public String getDuration() {
        return millisecondToFullTime(Long.parseLong(mDuration));
    }

    public void setDuration(String mDuration) {
        this.mDuration = mDuration;
    }

    public long getTimeEnd() {
        return Long.parseLong(mDuration);
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String mPath) {
        this.mPath = mPath;
    }
}
