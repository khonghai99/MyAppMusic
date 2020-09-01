package com.bkav.android.mymusic;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.bkav.android.mymusic.models.Song;

import java.util.ArrayList;
import java.util.List;

public class SongLoader {

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
                    String title = cursor.getString(0);
                    String artist = cursor.getString(1);
                    String duration = cursor.getString(2);
                    String path = cursor.getString(3);
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
