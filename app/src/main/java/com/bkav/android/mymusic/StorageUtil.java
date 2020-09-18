package com.bkav.android.mymusic;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.bkav.android.mymusic.models.Song;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class StorageUtil {
    private static final String AUDIO_ID = "com.bkav.android.mymusic..AUDIO_ID";
    ;
    private final String STORAGE = "com.bkav.android.mymusic..STORAGE";
    private final String AUDIO_LIST = "com.bkav.android.mymusic.AUDIO_LIST";
    private final String AUDIO_INDEX = "com.bkav.android.mymusic.AUDIO_INDEX";
    private SharedPreferences preferences;
    private Context context;

    public StorageUtil(Context context) {
        this.context = context;
    }

    public void storeAudio(ArrayList<Song> arrayList) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(arrayList);

        editor.putString(AUDIO_LIST, json);
        editor.apply();
    }

    public ArrayList<Song> loadAudio() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString(AUDIO_LIST, null);
        Log.i("json", json);
        Type type = new TypeToken<ArrayList<Song>>() {
        }.getType();
        Log.i("gson", gson.fromJson(json, type).toString());
        return gson.fromJson(json, type);
    }

    public void storeAudioIndex(int index) {
        Log.d("HaiKH", "storeAudioIndex: on");
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(AUDIO_INDEX, index);
        editor.apply();
    }

    public int loadAudioIndex() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt(AUDIO_INDEX, -1);//return -1 if no data found
    }

    public void storeAudioId(int id) {
        Log.d("HaiKH", "storeAudioIndex: on");
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(AUDIO_ID, id);
        editor.apply();
    }

    public int loadAudioId() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt(AUDIO_ID, -1);
    }

    public void clearCachedAudioPlaylist() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }
}
