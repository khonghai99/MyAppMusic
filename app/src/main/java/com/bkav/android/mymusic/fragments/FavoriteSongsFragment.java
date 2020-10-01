package com.bkav.android.mymusic.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.bkav.android.mymusic.StorageUtil;
import com.bkav.android.mymusic.models.Song;
import com.bkav.android.mymusic.providers.FavoriteSongsProvider;
import com.bkav.android.mymusic.providers.MusicDBHelper;

import java.util.ArrayList;
import java.util.Objects;

public class FavoriteSongsFragment extends BaseSongListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getLoaderManager().initLoader(0, null, this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String selection = MusicDBHelper.IS_FAVORITE + " = 2";
        String[] projection = {
                MusicDBHelper.ID,
                MusicDBHelper.ID_PROVIDER,
                MusicDBHelper.IS_FAVORITE,
                MusicDBHelper.COUNT_OF_PLAY,
        };
        Uri uri = FavoriteSongsProvider.CONTENT_URI;
        return new CursorLoader(Objects.requireNonNull(getContext()), uri, projection, selection, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        ArrayList<Song> mFavoriteSongList = new ArrayList<>();

        if (cursor != null) {
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {

                int idProvider = cursor.getInt(cursor.getColumnIndex(MusicDBHelper.ID_PROVIDER));
                Log.i("HaiKH", "onLoadFinished: "+idProvider);
                Cursor cursorAllSong = getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        null, MediaStore.Audio.Media.IS_MUSIC + " != 0",
                        null, MediaStore.Audio.Media.TITLE + " ASC");
                if (cursorAllSong != null) {
                    cursorAllSong.moveToFirst();
                    while (!cursorAllSong.isAfterLast()) {
                        int id = cursorAllSong.getInt(cursorAllSong.getColumnIndex(MediaStore.Audio.AudioColumns._ID));
                        Log.i("HaiKH", "onLoadFinished:1 "+id);
                        String title = cursorAllSong.getString(cursorAllSong.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));
                        String artist = cursorAllSong.getString(cursorAllSong.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST));
                        String duration = cursorAllSong.getString(cursorAllSong.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION));
                        String path = cursorAllSong.getString(cursorAllSong.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));

                        //add Song to favoriteList
                        if (idProvider == id) {
                            Song song = new Song(id, title, artist, duration, path);
                            mFavoriteSongList.add(song);
                        }
                        cursorAllSong.moveToNext();
                    }
                    cursorAllSong.close();
                }
                cursor.moveToNext();
            }
        }
        StorageUtil storageUtil = new StorageUtil(getContext());
        storageUtil.storeFavoriteSongList(mFavoriteSongList);
        Log.i("HaiKH", "onLoadFinished: " + mFavoriteSongList.size());
        mSongAdapter.updateSongList(mFavoriteSongList);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}
