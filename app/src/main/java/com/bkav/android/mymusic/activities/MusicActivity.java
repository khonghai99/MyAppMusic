package com.bkav.android.mymusic.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bkav.android.mymusic.PlaybackStatus;
import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.fragments.AllSongsFragment;
import com.bkav.android.mymusic.fragments.MediaPlaybackFragment;
import com.bkav.android.mymusic.models.Song;

import java.util.ArrayList;


public class MusicActivity extends AppCompatActivity {

    //sends broadcast intents to the MediaPlayerService

    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 1;
    private static final String SERVICE_STATE = "ServiceState";
    private static final String AUDIO_INDEX = "audioIndex";
    public Fragment mAllSongsFragment, mMediaPlaybackFragment;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransactionOne, mFragmentTransactionTwo;

    private ArrayList<Song> mAudioList;
    private int mCurrentPosition;
    private boolean mIsVertical = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        Toolbar toolbar = findViewById(R.id.toolbar);

        mAudioList = new ArrayList<>();

        //set toolbar
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.titleToolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        addFragment();

        //check permission
        if (ContextCompat.checkSelfPermission(MusicActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            ActivityCompat.requestPermissions(MusicActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        //    savedInstanceState.putBoolean(SERVICE_STATE, mServiceBound);
        savedInstanceState.putInt(AUDIO_INDEX, mCurrentPosition);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //    mServiceBound = savedInstanceState.getBoolean(SERVICE_STATE);
        mCurrentPosition = savedInstanceState.getInt(AUDIO_INDEX);
    }

    /**
     * get state UI (vertical or landscape)
     *
     * @return mIsVertical ís state UI
     */
    public boolean getStateUI() {
        return mIsVertical;
    }

    /**
     * initialization fragment
     */
    public void initFragment() {
        mAllSongsFragment = new AllSongsFragment();
        mMediaPlaybackFragment = new MediaPlaybackFragment();
    }

    /**
     * add fragment to activity
     */
    private void addFragment() {
        initFragment();
        //add fragment to frameLayout
        mFragmentManager = getSupportFragmentManager();
        mFragmentTransactionOne = mFragmentManager.beginTransaction();
        int orientation = getResources().getConfiguration().orientation;

        mIsVertical = orientation != Configuration.ORIENTATION_LANDSCAPE;

        if (mIsVertical) {
            mFragmentTransactionOne.replace(R.id.frameLayoutAllSong, mAllSongsFragment);
            mFragmentTransactionOne.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            mFragmentTransactionOne.commit();
        } else {
            mFragmentTransactionOne.replace(R.id.frameLayoutOne, mAllSongsFragment);
            mFragmentTransactionOne.commit();

            mFragmentTransactionTwo = mFragmentManager.beginTransaction();
            mFragmentTransactionTwo.replace(R.id.frameLayoutTwo, mMediaPlaybackFragment);
            mFragmentTransactionTwo.commit();
        }
    }

    /**
     * update fragment when click change from notification
     *
     * @param index          playing song position
     * @param playbackStatus state player
     */
    public void updateFragment(int index, PlaybackStatus playbackStatus) {
        ((AllSongsFragment) mAllSongsFragment).update(index, playbackStatus);
        if (mMediaPlaybackFragment.getView() != null) {
            ((MediaPlaybackFragment) mMediaPlaybackFragment).update(playbackStatus);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(MusicActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    addFragment();
                }
            } else {
                finish();
            }
        }
    }
//     @Override
//    public void onSongItemClick(SongListAdapter.SongViewHolder holder, Song song) {
//        int pos = holder.getAdapterPosition();
//        if (isFavorite) {
//            mediaPlaybackService.setSongList(SongData.getFavorAllSongs(mActivity));
//            Log.d(TAG, "onSongItemClick: " + SongData.getFavorAllSongs(mActivity).size());
//        } else mediaPlaybackService.setSongList(SongData.getAllSongs(mActivity));
//        mediaPlaybackService.play(song);
//        mediaPlaybackService.startForegroundService(pos, true);
//        mBaseSongsFragment.setStateMusic(song.getPos(), song.getId(), true);
//        mBaseSongsFragment.setFavorite(isFavorite);
//        Log.d(TAG, "onSongItemClick: " + mediaPlaybackService.getCurrentSongId());
//        mBaseSongsFragment.updateUI();
//    }
//    public void updateUI() {
//        mSongData.setPlaying(isPlaying);
//        if (mediaPlaybackService != null){
//            mSongCurrentId = mediaPlaybackService.getCurrentSongId();
//        }
//        mSongData.setSongCurrentId(mSongCurrentId);
////        mAdapter.setCurrentPos(mSongCurrentPosition);
//        mRecyclerView.scrollToPosition(mSongCurrentPosition);
//        mAdapter.notifyDataSetChanged();
//        SongData songData = new SongData(getActivity().getApplicationContext());
//        Song song = songData.getSongId(mSongCurrentId);
//        Log.d(TAG, "updateUI: "+song.getId());
//        if (isPortrait) updatePlaySongLayout(song);
//    }
//
//    public void updatePlaySongLayout(Song mSong) {
//        this.mSong = mSong;
//        mRelativeLayout.setVisibility(View.VISIBLE);
//        mSongName.setText(mSong.getTitle());
//        mSongArtist.setText(mSong.getArtistName());
//        if (isPlaying) {
//            mSongPlayBtn.setImageResource(R.drawable.ic_media_pause_light);
//        } else mSongPlayBtn.setImageResource(R.drawable.ic_media_play_light);
//        Bitmap albumArt = SongData.getAlbumArt(mSong.getData());
//        if (albumArt != null) {
//            mSongImage.setImageBitmap(albumArt);
//        } else {
//            mSongImage.setImageResource(R.drawable.art_song_default);
//        }
//    }
}