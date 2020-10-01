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
    private final String STATE_REPEAT = "com.bkav.android.mymusic.STATE_REPEAT";
    private final String STATE_SHUFFLE = "com.bkav.android.mymusic.STATE_SHUFFLE";
    private SharedPreferences preferences;
    private Context context;

    public StorageUtil(Context context) {
        this.context = context;
    }

    public void storeAllSongList(ArrayList<Song> arrayList) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString(ALL_SONG_LIST, json);
        editor.apply();
    }

    public ArrayList<Song> loadAllSongList() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString(ALL_SONG_LIST, null);
        Type type = new TypeToken<ArrayList<Song>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void storeFavoriteSongList(ArrayList<Song> arrayList) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString(FAVORITE_SONG_LIST, json);
        editor.apply();
    }

    public ArrayList<Song> loadFavoriteSongList() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString(FAVORITE_SONG_LIST, null);
        Type type = new TypeToken<ArrayList<Song>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void storeRepeat(int stateRepeat) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(STATE_REPEAT, stateRepeat);
        editor.apply();
    }

    public int loadStateRepeat() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt(STATE_REPEAT, 0);
    }

    public void storeShuffle(boolean stateShuffle) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(STATE_SHUFFLE, stateShuffle);
        editor.apply();
    }

    public boolean loadStateShuffle() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getBoolean(STATE_SHUFFLE, false);
    }


    public void storeAudioIndex(int index) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(SONG_INDEX, index);
        editor.apply();
    }

    public int loadAudioIndex() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt(SONG_INDEX, -1);//return -1 if no data found
    }

    public void storeAudioID(int index) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(SONG_ID, index);
        editor.apply();
    }

    public int loadAudioID() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt(SONG_ID, -1);//return -1 if no data found
    }

    public void clearCachedAudioPlaylist() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }
}
