package com.bkav.android.mymusic.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.bkav.android.mymusic.StorageUtil;
import com.bkav.android.mymusic.adapters.SongAdapter;
import com.bkav.android.mymusic.models.Song;

import java.util.ArrayList;
import java.util.Objects;

public class AllSongsFragment extends BaseSongListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int NUMBER_TITLE = 0;
    private static final int NUMBER_ARTIST = 1;
    private static final int NUMBER_DURATION = 2;
    private static final int NUMBER_DATA = 3;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getLoaderManager().initLoader(0, null, this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Loader onCreateLoader(int id, @Nullable Bundle args) {
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.ARTIST,
                MediaStore.Audio.AudioColumns.DURATION,
                MediaStore.Audio.AudioColumns.DATA
        };
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        return new CursorLoader(Objects.requireNonNull(getContext()), uri, projection, selection, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        ArrayList<Song> songList = new ArrayList<>();

        if (cursor != null) {
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                String title = cursor.getString(NUMBER_TITLE);
                String artist = cursor.getString(NUMBER_ARTIST);
                String duration = cursor.getString(NUMBER_DURATION);
                String path = cursor.getString(NUMBER_DATA);
                cursor.moveToNext();
                if (path != null && path.endsWith(".mp3")) {
                    songList.add(new Song(title, artist, duration, path));
                }
            }
        }
        StorageUtil storageUtil = new StorageUtil(getContext());
        storageUtil.storeAudio(songList);

        mSongAdapter = new SongAdapter(getContext(), songList, this);
        mRecyclerView.setAdapter(mSongAdapter);
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {

    }

}
