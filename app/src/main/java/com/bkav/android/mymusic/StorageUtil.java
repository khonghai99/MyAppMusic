package com.bkav.android.mymusic;

import android.content.Context;
import android.content.SharedPreferences;

import com.bkav.android.mymusic.models.Song;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class StorageUtil {
    private final String STORAGE = "com.bkav.android.mymusic..STORAGE";
    private final String ALL_SONG_LIST = "com.bkav.android.mymusic.ALL_SONG_LIST";
    private final String FAVORITE_SONG_LIST = "com.bkav.android.mymusic.FAVORITE_SONG_LIST";
    private final String SONG_INDEX = "com.bkav.android.mymusic.SONG_INDEX";
    private final String SONG_ID = "com.bkav.android.mymusic.SONG_ID";

    private final String STATE_FAVORITE = "com.bkav.android.mymusic.STATE";
    private final String STATE_REPEAT = "com.bkav.android.mymusic.STATE_REPEAT";
    private final String STATE_SHUFFLE = "com.bkav.android.mymusic.STATE_SHUFFLE";

    private SharedPreferences preferences;
    private Context context;

    public StorageUtil(Context context) {
        this.context = context;
    }

    /**
     * save list all song of device
     *
     * @param arrayList is list song of device
     */
    public void storeSongList(ArrayList<Song> arrayList) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString(ALL_SONG_LIST, json);
        editor.apply();
    }

    /**
     * get list all song of device
     *
     * @return list all song
     */
    public ArrayList<Song> loadSongList() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString(ALL_SONG_LIST, null);
        Type type = new TypeToken<ArrayList<Song>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    /**
     * save list favorite song
     *
     * @param arrayList is list favorite song
     */
    public void storeFavoriteSongList(ArrayList<Song> arrayList) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString(FAVORITE_SONG_LIST, json);
        editor.apply();
    }

    /**
     * get list favorite song
     *
     * @return list favorite song
     */
    public ArrayList<Song> loadFavoriteSongList() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString(FAVORITE_SONG_LIST, null);
        Type type = new TypeToken<ArrayList<Song>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    /**
     * save state repeat
     *
     * @param stateRepeat is state repeat
     */
    public void storeRepeat(int stateRepeat) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(STATE_REPEAT, stateRepeat);
        editor.apply();
    }

    /**
     * get state repeat
     *
     * @return state repeat
     */
    public int loadStateRepeat() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt(STATE_REPEAT, 0);
    }

    /**
     * save state shuffle
     *
     * @param stateShuffle is state shuffle
     */
    public void storeShuffle(boolean stateShuffle) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(STATE_SHUFFLE, stateShuffle);
        editor.apply();
    }

    /**
     * get state shuffle
     *
     * @return state shuffle
     */
    public boolean loadStateShuffle() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getBoolean(STATE_SHUFFLE, false);
    }

    /**
     * save position of song
     *
     * @param index is position song
     */
    public void storeAudioIndex(int index) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(SONG_INDEX, index);
        editor.apply();
    }

    /**
     * get position of song
     *
     * @return position song
     */
    public int loadAudioIndex() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt(SONG_INDEX, -1);//return -1 if no data found
    }

    /**
     * save id of song
     *
     * @param id is id song
     */
    public void storeAudioID(int id) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(SONG_ID, id);
        editor.apply();
    }

    /**
     * get id of song
     *
     * @return id song
     */
    public int loadAudioID() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt(SONG_ID, -1);//return -1 if no data found
    }

    /**
     * delete cache
     */
    public void clearCachedAudioPlaylist() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * load song active follow id
     *
     * @return song active
     */
    public Song loadSongActive() {
        Song songActive = null;
        for (Song song : loadSongList()) {
            if (song.getID() == loadAudioID()) {
                songActive = song;
            }
        }
        return songActive;
    }

    /**
     * save state of screen favorite
     *
     * @param b true or false
     */
    public void storeStateFavorite(boolean b) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(STATE_FAVORITE, b);
        editor.apply();
    }

    /**
     * get state of screen favorite
     *
     * @return true or false
     */
    public boolean loadStateFavorite() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getBoolean(STATE_FAVORITE, false);//return false if no data found
    }

}
