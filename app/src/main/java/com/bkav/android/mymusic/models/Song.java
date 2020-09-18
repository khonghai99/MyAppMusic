package com.bkav.android.mymusic.models;

import java.util.concurrent.TimeUnit;

public class Song {
    private int mId;
    private String mTitle;
    private String mArtist;
    private String mDuration;
    private String mPath;

    public Song(int mId, String mTitle, String mArtist, String mDuration, String mPath) {
        this.mId = mId;
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
    public static String timeUnitToFullTime(long time, TimeUnit timeUnit) {
        long day = timeUnit.toDays(time);
        long hour = timeUnit.toHours(time) % 24;
        long minute = timeUnit.toMinutes(time) % 60;
        long second = timeUnit.toSeconds(time) % 60;
        if (day > 0) {
            return String.format("%dday %02d:%02d:%02d", day, hour, minute, second);
        } else if (hour > 0) {
            return String.format("%d:%02d:%02d", hour, minute, second);
        } else if (minute > 0) {
            return String.format("%d:%02d", minute, second);
        } else {
            return String.format("%d:%02d", minute, second);
        }
    }

    public int getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
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
        return millisecondToFullTime(Long.parseLong(mDuration));
    }
    public int getTimeEnd(){
        return Integer.parseInt(mDuration);
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
