package com.bkav.android.mymusic.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.StorageUtil;
import com.bkav.android.mymusic.fragments.AllSongsFragment;
import com.bkav.android.mymusic.fragments.BaseSongListFragment;
import com.bkav.android.mymusic.fragments.FavoriteSongsFragment;
import com.bkav.android.mymusic.fragments.MediaPlaybackFragment;
import com.bkav.android.mymusic.services.MediaPlaybackService;
import com.google.android.material.navigation.NavigationView;

import java.util.Objects;


public class MusicActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        MediaPlaybackFragment.OnReLoadList {

    // permission request
    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 1;
    private static final String STATE_FAVORITE = "com.bkav.android.mymusic.activities.STATE_FAVORITE";

    public MediaPlaybackFragment mMediaPlaybackFragment;
    public BaseSongListFragment mBaseSongListFragment;
    protected MediaPlaybackService mMediaService;
    private OnServiceConnectedListenerForAllSong mOnServiceConnectedListenerForAllSong;
    private OnServiceConnectedListenerForMedia mOnServiceConnectedListenerForMedia;
    private boolean mIsVertical = false;
    private Intent mPlayIntent;
    private DrawerLayout mDrawer;
    private boolean mStateFavorite;

    // Binding this Client to the Service
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            // Bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlaybackService.LocalBinder binder = (MediaPlaybackService.LocalBinder) service;
            mMediaService = binder.getService();
            // Connect service for fragment all song
            mOnServiceConnectedListenerForAllSong.onConnect();
            if (mMediaPlaybackFragment.getView() != null) {
                mOnServiceConnectedListenerForMedia.onConnect();
            }
            if (mMediaService.getActiveAudio() != null) {
                updateFragment();
            }
            mMediaService.setOnNotificationListener(new MediaPlaybackService.OnNotificationListener() {
                @Override
                public void onUpdate() {
                    updateFragment();
                }
            });
            if (mMediaService.getActiveAudio() == null) {
                StorageUtil storageUtil = new StorageUtil(getApplicationContext());
                if (storageUtil.loadAudioIndex() != -1) {
                    mMediaService.setSongActive(storageUtil.loadSongActive());
                    mMediaService.setSongList(storageUtil.loadSongList());
                    mMediaService.setSongIndex(storageUtil.loadAudioIndex());
                    mBaseSongListFragment.setDataBottom();
                    mBaseSongListFragment.setVisible(true);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    /**
     * set animation for item of recycler view: show in screen
     */
    public void setAnimation() {
        if (new StorageUtil(getApplicationContext()).loadAudioIndex() != -1) {
            mBaseSongListFragment.setSmoothScrollToPosition(new StorageUtil(getApplicationContext()).loadAudioIndex());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mPlayIntent == null) {
            mPlayIntent = new Intent(this, MediaPlaybackService.class);
        }
        startService(mPlayIntent);
        bindService(mPlayIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mStateFavorite = savedInstanceState.getBoolean(STATE_FAVORITE);
        }
        setContentView(R.layout.activity_music);
        Toolbar toolbar = findViewById(R.id.toolbar);
        //set toolbar
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.title_toolbar_all_song);
        toolbar.setTitleTextColor(Color.WHITE);
        createFragment();
        //check permission
        if (ContextCompat.checkSelfPermission(MusicActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            ActivityCompat.requestPermissions(MusicActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
        }
        NavigationView mNavigationView = findViewById(R.id.nav_view);
        mDrawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle mToggle = new ActionBarDrawerToggle(this, mDrawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(mToggle);
        mNavigationView.setNavigationItemSelectedListener(this);
        mToggle.syncState();

        mMediaPlaybackFragment.actionReLoad(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaService != null) {
            unbindService(mServiceConnection);
        }
    }

    /**
     * get state UI (vertical or landscape)
     *
     * @return mIsVertical ís state UI
     */
    public boolean getStateUI() {
        return mIsVertical;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_FAVORITE, mStateFavorite);
    }

    /**
     * initialization fragment
     */
    public void initFragment() {
        if (mStateFavorite) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.title_toolbar_favorite_song);
            mBaseSongListFragment = new FavoriteSongsFragment();
        } else mBaseSongListFragment = new AllSongsFragment();
        mMediaPlaybackFragment = new MediaPlaybackFragment();
    }

    /**
     * add fragment to activity
     */
    private void createFragment() {
        initFragment();
        //add fragment to frameLayout
        FragmentManager mFragmentManager = getSupportFragmentManager();
        FragmentTransaction mFragmentTransactionOne = mFragmentManager.beginTransaction();
        int orientation = getResources().getConfiguration().orientation;
        mIsVertical = orientation != Configuration.ORIENTATION_LANDSCAPE;
        if (mIsVertical) {
            mFragmentTransactionOne.replace(R.id.frame_layout_all_song, mBaseSongListFragment);
            mFragmentTransactionOne.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            mFragmentTransactionOne.commit();
        } else {
            FragmentTransaction mFragmentTransactionTwo = mFragmentManager.beginTransaction();
            mFragmentTransactionTwo.replace(R.id.frame_layout_all_song, mBaseSongListFragment).
                    replace(R.id.frame_layout_land_media, mMediaPlaybackFragment);
            mFragmentTransactionTwo.commit();
        }
    }

    /**
     * update fragment when click change from notification
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void updateFragment() {
        if (mIsVertical) {
            if (mMediaPlaybackFragment.getView() != null) {
                mMediaPlaybackFragment.update();
            }
            if (mBaseSongListFragment.getView() != null) {
                mBaseSongListFragment.update();
            }
        } else if (mBaseSongListFragment != null && mMediaPlaybackFragment.getView() != null) {
            mBaseSongListFragment.update();
            mMediaPlaybackFragment.update();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(MusicActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    createFragment();
                }
            } else {
                finish();
            }
        }
    }

    /**
     * call to get service
     *
     * @return service
     */
    public MediaPlaybackService getPlayerService() {
        return mMediaService;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_music_library:
                mStateFavorite = false;
                Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.title_toolbar_all_song);
                mBaseSongListFragment = new AllSongsFragment();
                Toast.makeText(this, R.string.title_toolbar_all_song, Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_music_favorite:
                mStateFavorite = true;
                Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.title_toolbar_favorite_song);
                mBaseSongListFragment = new FavoriteSongsFragment();
                Toast.makeText(this, R.string.title_toolbar_favorite_song, Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_setting:
                Toast.makeText(this, "setting", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_help_and_feedback:
                Toast.makeText(this, "help", Toast.LENGTH_SHORT).show();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + menuItem.getItemId());
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_all_song,
                mBaseSongListFragment).commit();
        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        Objects.requireNonNull(getSupportActionBar()).show();
        mBaseSongListFragment.setSmoothScrollToPosition(new StorageUtil(getApplicationContext()).loadAudioIndex());
    }

    /**
     * listener service for all song fragment
     *
     * @param onServiceConnectedListenerForAllSong is listener
     */
    public void listenServiceConnectedForAllSong(OnServiceConnectedListenerForAllSong onServiceConnectedListenerForAllSong) {
        this.mOnServiceConnectedListenerForAllSong = onServiceConnectedListenerForAllSong;
    }

    /**
     * listener service for media playback fragment
     *
     * @param onServiceConnectedListener is listener
     */
    public void listenServiceConnectedForMedia(OnServiceConnectedListenerForMedia onServiceConnectedListener) {
        this.mOnServiceConnectedListenerForMedia = onServiceConnectedListener;
    }

    @Override
    public void reLoadListFragment() {
        mBaseSongListFragment.updateAdapter();
    }

    public interface OnServiceConnectedListenerForAllSong {
        void onConnect();
    }

    public interface OnServiceConnectedListenerForMedia {
        void onConnect();
    }


}