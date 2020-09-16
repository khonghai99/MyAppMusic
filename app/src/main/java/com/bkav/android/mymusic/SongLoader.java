package com.bkav.android.mymusic;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.bkav.android.mymusic.models.Song;

import java.util.ArrayList;

public class SongLoader {
    private final int NUMBER_TITLE = 0;
    private final int NUMBER_ARTIST = 1;
    private final int NUMBER_DURATION = 2;
    private final int NUMBER_DATA = 3;

    public ArrayList<Song> getAllSongDevice(Context context) {
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.ARTIST,
                MediaStore.Audio.AudioColumns.DURATION,
                MediaStore.Audio.AudioColumns.DATA


        };

        ArrayList<Song> listSong = new ArrayList<>();
        Cursor cursor = null;
        try {

            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            cursor = context.getContentResolver().query(uri, projection, selection, null, null);
            if (cursor != null) {
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    String title = cursor.getString(NUMBER_TITLE);
                    String artist = cursor.getString(NUMBER_ARTIST);
                    String duration = cursor.getString(NUMBER_DURATION);
                    String path = cursor.getString(NUMBER_DATA);
                    cursor.moveToNext();
                    if (path != null && path.endsWith(".mp3")) {
                        listSong.add(new Song(title, artist, duration, path));
                    }
                }
            }

        } catch (Exception e) {
            Log.e("TAG", e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return listSong;
    }
}
