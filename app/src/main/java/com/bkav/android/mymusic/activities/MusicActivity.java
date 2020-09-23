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
import android.view.Menu;
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

import com.bkav.android.mymusic.PlaybackStatus;
import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.fragments.AllSongsFragment;
import com.bkav.android.mymusic.fragments.MediaPlaybackFragment;
import com.bkav.android.mymusic.services.MediaPlaybackService;
import com.google.android.material.navigation.NavigationView;

import java.util.Objects;


public class MusicActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 1;
    private static final String SERVICE_STATE = "com.bkav.android.mymusic.activities.SERVICE_STATE";
    private static final String AUDIO_INDEX = "com.bkav.android.mymusic.activities.AUDIO_INDEX";
    public AllSongsFragment mAllSongsFragment;
    public MediaPlaybackFragment mMediaPlaybackFragment;
    protected MediaPlaybackService mMediaService;
    private int mCurrentPosition;
    private boolean mIsVertical = false;
    private boolean mServiceBound = false;
    private Intent playIntent;
    private boolean musicBound = false;
    private DrawerLayout drawer;

    // Ràng buộc Client này với MusicPlayer
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            // Đã liên kết với LocalService, truyền IBinder
            MediaPlaybackService.LocalBinder binder = (MediaPlaybackService.LocalBinder) service;
            mMediaService = binder.getService();
            mServiceBound = true;

            mMediaService.setOnNotificationListener(new MediaPlaybackService.OnNotificationListener() {
                @Override
                public void onUpdate(int position, PlaybackStatus playbackStatus) {
                    updateFragment(position, playbackStatus);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }
    };

    // FIXED:
    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MediaPlaybackService.class);
        }
        bindService(playIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        Log.i("HaiKH", "onStart: "+mServiceConnection);
        startService(playIntent);
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
        toolbar.setTitle(R.string.titleToolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        createFragment();

        //check permission
        if (ContextCompat.checkSelfPermission(MusicActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            ActivityCompat.requestPermissions(MusicActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
        }
        drawer = findViewById(R.id.drawer_layout);
        if (mIsVertical) {
            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                    R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
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
        mAllSongsFragment = new AllSongsFragment();
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
            mFragmentTransactionOne.replace(R.id.frameLayoutAllSong, mAllSongsFragment);
            mFragmentTransactionOne.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            mFragmentTransactionOne.commit();
        } else {
            mFragmentTransactionOne.replace(R.id.frameLayoutLandAllSong, mAllSongsFragment);
            mFragmentTransactionOne.commit();

            FragmentTransaction mFragmentTransactionTwo = mFragmentManager.beginTransaction();
            mFragmentTransactionTwo.replace(R.id.frameLayoutLandMedia, mMediaPlaybackFragment);
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
        if (mAllSongsFragment.getView() != null) {
            mAllSongsFragment.update(index, playbackStatus);
        }

        if (mMediaPlaybackFragment.getView() != null) {
            mMediaPlaybackFragment.update(playbackStatus);
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

    public boolean getServiceBound() {
        return mServiceBound;
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_listen_now:
                Toast.makeText(this, "listen now", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_recents:
                Toast.makeText(this, "recents", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_music_library:
                Toast.makeText(this, "music library", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_setting:
                Toast.makeText(this, "setting", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_help_and_feedback:
                Toast.makeText(this, "help", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}