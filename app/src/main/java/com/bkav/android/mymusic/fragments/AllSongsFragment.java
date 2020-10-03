package com.bkav.android.mymusic.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.StorageUtil;
import com.bkav.android.mymusic.adapters.SongAdapter;
import com.bkav.android.mymusic.models.Song;
import com.bkav.android.mymusic.providers.FavoriteSongsProvider;
import com.bkav.android.mymusic.providers.MusicDBHelper;

import java.util.ArrayList;
import java.util.Objects;

public class AllSongsFragment extends BaseSongListFragment implements LoaderManager.LoaderCallbacks<Cursor>, SongAdapter.OnClickPopupListener, PopupMenu.OnMenuItemClickListener {
    public static final int NUMBER_ID = 0;
    public static final int NUMBER_TITLE = 1;
    public static final int NUMBER_ARTIST = 2;
    public static final int NUMBER_DURATION = 3;
    public static final int NUMBER_DATA = 4;


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
                MediaStore.Audio.AudioColumns._ID,
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
        mAllSongList = new ArrayList<>();

        if (cursor != null) {
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                int id = cursor.getInt(NUMBER_ID);
                String title = cursor.getString(NUMBER_TITLE);
                String artist = cursor.getString(NUMBER_ARTIST);
                String duration = cursor.getString(NUMBER_DURATION);
                String path = cursor.getString(NUMBER_DATA);
                cursor.moveToNext();
                if (path != null && path.endsWith(".mp3")) {
                    mAllSongList.add(new Song(id, title, artist, duration, path));
                }
            }

        }
        StorageUtil storageUtil = new StorageUtil(getContext());
        storageUtil.storeAllSongList(mAllSongList);
        mSongAdapter.updateSongList(mAllSongList);
        mSongAdapter.setOnClickPopup(this);
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {

    }

    /**
     * get all id song favorite from DB
     *
     * @return array list id song favorite
     */
    private ArrayList<Integer> getAllIdFavorite() {
        String selection = MusicDBHelper.IS_FAVORITE + " = 2";
        String[] projection = {MusicDBHelper.ID_PROVIDER};
        ArrayList<Integer> idList = new ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Cursor cursor = getContext().getContentResolver().query(FavoriteSongsProvider.CONTENT_URI, projection, selection, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    idList.add(cursor.getInt(NUMBER_ID));
                    cursor.moveToNext();
                }
            }
            cursor.close();
        }
        return idList;
    }

    @Override
    public void onClickPopup(View view, int position) {
        boolean isFavorite = false;
        mPositionPopup = position;
        mPopup = new PopupMenu(getContext(), view.findViewById(R.id.popup_one_row));
        for (Integer id : getAllIdFavorite()) {
            if (mAllSongList.get(position).getID() == id) {
                isFavorite = true;
                break;
            }
        }
        if (isFavorite) {
            mPopup.getMenuInflater().inflate(R.menu.menu_popup_not_favorite, mPopup.getMenu());
        } else {
            mPopup.getMenuInflater().inflate(R.menu.menu_popup_favorite, mPopup.getMenu());
        }
        mPopup.setOnMenuItemClickListener(this);
        mPopup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        FavoriteSongsProvider favoriteSongsProvider = new FavoriteSongsProvider(getContext());
        Song song = mAllSongList.get(mPositionPopup);
        int id = song.getID();
        switch (item.getItemId()) {
            case R.id.popup_remove_favorite:
                favoriteSongsProvider.updateFavorite(id, FavoriteSongsProvider.IS_NUMBER_NOT_FAVORITE);
                favoriteSongsProvider.updateCount(id, FavoriteSongsProvider.NUMBER_COUNT_DEFAULT);
                Toast.makeText(getContext(), R.string.title_remove_favorite, Toast.LENGTH_SHORT).show();
                break;
            case R.id.popup_add_favorite:
                favoriteSongsProvider.updateFavorite(id, FavoriteSongsProvider.IS_NUMBER_FAVORITE);
                Toast.makeText(getContext(), R.string.title_add_to_favorite, Toast.LENGTH_SHORT).show();
                break;
        }
        mSongAdapter.notifyDataSetChanged();
        return true;
    }
}
