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
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.bkav.android.mymusic.fragments.AllSongsFragment;
import com.bkav.android.mymusic.fragments.BaseSongListFragment;
import com.bkav.android.mymusic.fragments.FavoriteSongsFragment;
import com.bkav.android.mymusic.fragments.MediaPlaybackFragment;
import com.bkav.android.mymusic.services.MediaPlaybackService;
import com.google.android.material.navigation.NavigationView;


public class MusicActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 1;
    private static final String SERVICE_STATE = "com.bkav.android.mymusic.activities.SERVICE_STATE";
    private static final String AUDIO_INDEX = "com.bkav.android.mymusic.activities.AUDIO_INDEX";
    public AllSongsFragment mAllSongsFragment;
    public MediaPlaybackFragment mMediaPlaybackFragment;
    public BaseSongListFragment mBaseSongListFragment;
    protected MediaPlaybackService mMediaService;
    private OnServiceConnectedListenerForAllSong mOnServiceConnectedListenerForAllSong;
    private OnServiceConnectedListenerForMedia mOnServiceConnectedListenerForMedia;
    private int mCurrentPosition;
    private boolean mIsVertical = false;
    private boolean mServiceBound = false;
    private Intent mPlayIntent;
    private DrawerLayout mDrawer;

    // Ràng buộc Client này với MusicPlayer
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            // Đã liên kết với LocalService, truyền IBinder
            MediaPlaybackService.LocalBinder binder = (MediaPlaybackService.LocalBinder) service;
            mMediaService = binder.getService();
            mServiceBound = true;
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
            Log.i("HaiKH", "onServiceConnected: " + mMediaService.getActiveAudio());
            if (mMediaService.getActiveAudio() == null) {

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }
    };

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
        setContentView(R.layout.activity_music);
        Toolbar toolbar = findViewById(R.id.toolbar);
        //set toolbar
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.titleToolbarAllSong);
        toolbar.setTitleTextColor(Color.WHITE);
        createFragment();
        //check permission
        if (ContextCompat.checkSelfPermission(MusicActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            ActivityCompat.requestPermissions(MusicActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
        }
        mDrawer = findViewById(R.id.drawer_layout);
        if (mIsVertical) {
            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, toolbar,
                    R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            mDrawer.addDrawerListener(toggle);
            toggle.syncState();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(SERVICE_STATE, mServiceBound);
        savedInstanceState.putInt(AUDIO_INDEX, mCurrentPosition);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaService != null) {
            unbindService(mServiceConnection);
        }

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mServiceBound = savedInstanceState.getBoolean(SERVICE_STATE);
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
        mBaseSongListFragment = new AllSongsFragment();
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
            mFragmentTransactionOne.replace(R.id.frame_layout_land_all_song, mBaseSongListFragment);
            mFragmentTransactionOne.commit();

            FragmentTransaction mFragmentTransactionTwo = mFragmentManager.beginTransaction();
            mFragmentTransactionTwo.replace(R.id.frame_layout_land_media, mMediaPlaybackFragment);
            mFragmentTransactionTwo.commit();
        }
    }

    /**
     * update fragment when click change from notification
     */
    public void updateFragment() {
        if (mIsVertical) {
            if (mMediaPlaybackFragment.getView() != null) {
                mMediaPlaybackFragment.update();
            }
            mBaseSongListFragment.update();
        } else {
            mBaseSongListFragment.update();
            mMediaPlaybackFragment.update();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(MusicActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    createFragment();
                }
            } else {
                finish();
            }
        }
    }

    public MediaPlaybackService getPlayerService() {
        return mMediaService;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_listen_now:
                Toast.makeText(this, "listen now", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_recents:
                Toast.makeText(this, "recent", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_music_library:
                getSupportActionBar().setTitle(R.string.titleToolbarAllSong);
                mBaseSongListFragment = new AllSongsFragment();
                Toast.makeText(this, "all song", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_music_favorite:
                getSupportActionBar().setTitle(R.string.titleToolbarFavoriteSong);
                mBaseSongListFragment = new FavoriteSongsFragment();
                Toast.makeText(this, "favorite song", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_setting:
                Toast.makeText(this, "setting", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_help_and_feedback:
                Toast.makeText(this, "help", Toast.LENGTH_SHORT).show();
                break;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_all_song, mBaseSongListFragment).commit();
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
    }

    public void listenServiceConnectedForAllSong(OnServiceConnectedListenerForAllSong onServiceConnectedListenerForAllSong) {
        this.mOnServiceConnectedListenerForAllSong = onServiceConnectedListenerForAllSong;
    }

    public void listenServiceConnectedForMedia(OnServiceConnectedListenerForMedia onServiceConnectedListener) {
        this.mOnServiceConnectedListenerForMedia = onServiceConnectedListener;
    }

    public interface OnServiceConnectedListenerForAllSong {
        void onConnect();
    }

    public interface OnServiceConnectedListenerForMedia {
        void onConnect();
    }

}