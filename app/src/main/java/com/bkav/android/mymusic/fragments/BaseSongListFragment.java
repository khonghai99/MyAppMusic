package com.bkav.android.mymusic.fragments;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bkav.android.mymusic.ImageSong;
import com.bkav.android.mymusic.MediaPlaybackStatus;
import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.StorageUtil;
import com.bkav.android.mymusic.activities.MusicActivity;
import com.bkav.android.mymusic.adapters.SongAdapter;
import com.bkav.android.mymusic.models.Song;
import com.bkav.android.mymusic.providers.FavoriteSongsProvider;
import com.bkav.android.mymusic.services.MediaPlaybackService;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Objects;

public class BaseSongListFragment extends Fragment implements View.OnClickListener, SongAdapter.OnNewClickListener, SearchView.OnQueryTextListener {
    protected RelativeLayout mBottomAllSongRelativeLayout;
    protected SongAdapter mSongAdapter;
    protected RecyclerView mRecyclerView;
    protected ArrayList<Song> mSongList;
    protected TextView mTitleBottomAllSongTextView;
    protected TextView mArtistBottomAllSongTextView;
    protected ImageView mImageBottomAllSongImageView;
    protected ImageView mImagePauseBottomAllSongImageView;
    protected MediaPlaybackService mMediaPlaybackService;
    protected Song mSong;
    protected StorageUtil mStorage;
    protected PopupMenu mPopup;
    protected int mPositionPopup;
    protected ArrayList<Song> mAllSongList;
    protected ArrayList<Song> mFavoriteSongList;

    @Override
    public void onResume() {
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_song, container, false);
        init(view);
        setHasOptionsMenu(true);
        mSongAdapter = new SongAdapter(getContext());
        final int orientation = getResources().getConfiguration().orientation;
        if (getMediaPlayerService() != null) {
            mMediaPlaybackService = getMediaPlayerService();
            mSongAdapter.setService(mMediaPlaybackService);
            if (mMediaPlaybackService.getActiveAudio() != null && orientation == Configuration.ORIENTATION_PORTRAIT) {
                setDataBottom();
                setVisible(true);
            } else {
                setVisible(false);
            }
        }
        Objects.requireNonNull(getMusicActivity()).listenServiceConnectedForAllSong(new MusicActivity.OnServiceConnectedListenerForAllSong() {
            @Override
            public void onConnect() {
                mMediaPlaybackService = getMediaPlayerService();
                mSongAdapter.setService(mMediaPlaybackService);
                if (mMediaPlaybackService.getActiveAudio() != null && orientation == Configuration.ORIENTATION_PORTRAIT) {
                    setDataBottom();
                    setVisible(true);
                } else {
                    setVisible(false);
                }
            }
        });
        mStorage = new StorageUtil(getContext());
        mSongAdapter.setOnClick(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mSongAdapter);
        mImagePauseBottomAllSongImageView.setOnClickListener(this);
        mBottomAllSongRelativeLayout.setOnClickListener(this);
        return view;
    }

    private void init(View view) {
        mImageBottomAllSongImageView = view.findViewById(R.id.image_bottom_all_song);
        mTitleBottomAllSongTextView = view.findViewById(R.id.title_bottom_all_song);
        mArtistBottomAllSongTextView = view.findViewById(R.id.artist_bottom_all_song);
        mImagePauseBottomAllSongImageView = view.findViewById(R.id.pause_bottom_all_song);
        mBottomAllSongRelativeLayout = view.findViewById(R.id.layout_bottom_all_song);
        mTitleBottomAllSongTextView.setSelected(true);
        mRecyclerView = view.findViewById(R.id.recycle_list_song);
    }

    @Override
    public void onNewClick(ArrayList<Song> songList, int position) {
        mSongList = songList;
        mSong = mSongList.get(position);
        mStorage.storeAllSongList(mSongList);
        mStorage.storeAudioIndex(position);
        mStorage.storeAudioID(mSong.getID());
        mMediaPlaybackService.playSong(songList.get(position));
        if (Objects.requireNonNull(getMusicActivity()).getStateUI()) {
            setDataBottom();
            setVisible(true);

        } else {
            MediaPlaybackFragment mediaPlaybackFragment = (MediaPlaybackFragment) getMusicActivity().getSupportFragmentManager().findFragmentById(R.id.frame_layout_land_media);
            if (mediaPlaybackFragment != null) {
                mediaPlaybackFragment.setTitleMedia(songList.get(position));
            }
        }
        mSongAdapter.notifyDataSetChanged();
        FavoriteSongsProvider favoriteSongsProvider = new FavoriteSongsProvider(getContext());
        favoriteSongsProvider.insertFavoriteSongToDB(mSong.getID());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_item_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setIconified(false);
        searchView.setQueryHint("Search here");
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        mSongAdapter.getFilter().filter(s);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * set data for layout bottom allSongFragment when click recycler view
     */
    public void setDataBottom() {
        mStorage = new StorageUtil(getContext());
        byte[] art = ImageSong.getByteImageSong(mStorage.loadAllSongList().get(mStorage.loadAudioIndex()).getPath());
        Glide.with(Objects.requireNonNull(getContext())).asBitmap()
                .error(R.mipmap.ic_music_not_picture)
                .load(art)
                .into(mImageBottomAllSongImageView);

        mTitleBottomAllSongTextView.setText(mStorage.loadAllSongList().get(mStorage.loadAudioIndex()).getTitle());
        mArtistBottomAllSongTextView.setText(mStorage.loadAllSongList().get(mStorage.loadAudioIndex()).getArtist());
        if (mMediaPlaybackService.isPlayingState() == MediaPlaybackStatus.PAUSED) {
            mImagePauseBottomAllSongImageView.setImageResource(R.mipmap.ic_media_play_light);
        } else {
            mImagePauseBottomAllSongImageView.setImageResource(R.mipmap.ic_media_pause_light);
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * set data for layout bottom allSongFragment when click from media fragment
     *
     * @param song                playing song
     * @param mediaPlaybackStatus state of player
     */
    public void setDataBottomFromMedia(Song song, MediaPlaybackStatus mediaPlaybackStatus) {
        mSong = song;
        byte[] art = ImageSong.getByteImageSong(song.getPath());
        Glide.with(Objects.requireNonNull(getContext())).asBitmap()
                .error(R.mipmap.ic_music_not_picture)
                .load(art)
                .into(mImageBottomAllSongImageView);
        mTitleBottomAllSongTextView.setText(song.getTitle());
        mArtistBottomAllSongTextView.setText(song.getArtist());
        if (mediaPlaybackStatus == MediaPlaybackStatus.PLAYING) {
            mImagePauseBottomAllSongImageView.setImageResource(R.mipmap.ic_media_pause_light);
        } else {
            mImagePauseBottomAllSongImageView.setImageResource(R.mipmap.ic_media_play_light);
        }
        mSongAdapter.notifyDataSetChanged();
    }

    /**
     * show layout bottom allSongFragment when click item recyclerView
     */
    public void setVisible(boolean b) {
        if (b) {
            mBottomAllSongRelativeLayout.setVisibility(View.VISIBLE);
        } else {
            mBottomAllSongRelativeLayout.setVisibility(View.GONE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.pause_bottom_all_song:
                if (MediaPlaybackStatus.PLAYING == mMediaPlaybackService.isPlayingState()) {
                    mMediaPlaybackService.pauseMedia();
                    mMediaPlaybackService.updateMetaDataNotify(MediaPlaybackStatus.PAUSED);
                    mImagePauseBottomAllSongImageView.setImageResource(R.mipmap.ic_media_play_light);
                    mSongAdapter.notifyDataSetChanged();
                } else if (MediaPlaybackStatus.PAUSED == mMediaPlaybackService.isPlayingState()) {
                    if (mMediaPlaybackService.getMediaPlayer().getCurrentPosition() == 0) {
                        mMediaPlaybackService.playSong(mStorage.loadAllSongList().get(mStorage.loadAudioIndex()));
                    } else {
                        mMediaPlaybackService.playMedia();
                    }
                    mMediaPlaybackService.updateMetaDataNotify(MediaPlaybackStatus.PLAYING);
                    mImagePauseBottomAllSongImageView.setImageResource(R.mipmap.ic_media_pause_light);
                    mSongAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.layout_bottom_all_song:
                showMediaFragment(mMediaPlaybackService.isPlayingState());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
    }

    /**
     * click bottom layout all song show media fragment
     *
     * @param mediaPlaybackStatus state of player
     */
    public void showMediaFragment(MediaPlaybackStatus mediaPlaybackStatus) {
        FragmentManager mFragmentManager = Objects.requireNonNull(getMusicActivity()).getSupportFragmentManager();
//        getMusicActivity().mMediaPlaybackFragment = MediaPlaybackFragment.getInstancesMedia(/*mMediaPlaybackService.getActiveAudio(),*/ mediaPlaybackStatus);
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_media, getMusicActivity().mMediaPlaybackFragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    /**
     * update allSongFragment when click notification
     */
    public void update() {
        if (Objects.requireNonNull(getMusicActivity()).getStateUI()) {
            mTitleBottomAllSongTextView.setText(getMediaPlayerService().getActiveAudio().getTitle());
            mArtistBottomAllSongTextView.setText(getMediaPlayerService().getActiveAudio().getArtist());
            byte[] art = ImageSong.getByteImageSong(getMediaPlayerService().getActiveAudio().getPath());
            Glide.with(Objects.requireNonNull(getContext())).asBitmap()
                    .error(R.mipmap.ic_music_not_picture)
                    .load(art)
                    .into(mImageBottomAllSongImageView);
            if (mMediaPlaybackService.isPlayingState() == MediaPlaybackStatus.PLAYING) {
                mImagePauseBottomAllSongImageView.setImageResource(R.mipmap.ic_media_pause_light);
            } else if (mMediaPlaybackService.isPlayingState() == MediaPlaybackStatus.PAUSED) {
                mImagePauseBottomAllSongImageView.setImageResource(R.mipmap.ic_media_play_light);
            }
            //giu bai hat dang phat tren man hinh
//            mRecyclerView.smoothScrollToPosition(getMediaPlayerService().getAudioIndex());
        }
        mSongAdapter.notifyDataSetChanged();
    }

    /**
     * get service
     */
    public MediaPlaybackService getMediaPlayerService() {
        return (Objects.requireNonNull(getMusicActivity())).getPlayerService();
    }

    /**
     * get activity from MusicActivity
     *
     * @return MusicActivity if getActivity() instanceof MusicActivity
     */
    protected MusicActivity getMusicActivity() {
        if (getActivity() instanceof MusicActivity) {
            return (MusicActivity) getActivity();
        }
        return null;
    }

}
