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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bkav.android.mymusic.Interfaces.OnNewClickListener;
import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.StorageUtil;
import com.bkav.android.mymusic.fragments.AllSongsFragment;
import com.bkav.android.mymusic.fragments.MediaPlaybackFragment;
import com.bkav.android.mymusic.models.Song;
import com.bkav.android.mymusic.services.MediaPlaybackService;

import java.util.ArrayList;


public class MusicActivity extends AppCompatActivity implements OnNewClickListener,
        AllSongsFragment.OnShowMediaListener {

    //sends broadcast intents to the MediaPlayerService
    public static final String BROADCAST_PLAY_NEW_AUDIO = "com.bkav.musictest.PlayNewAudio";
    private static final int MY_PERMISSION_REQUEST = 1;
    private static final String SERVICE_STATE = "ServiceState";
    private static final String AUDIO_INDEX = "audioIndex";

    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    private MediaPlaybackService mPlayer;
    private ArrayList<Song> mAudioList;
    private int mCurrentPosition;
    private boolean mIsVertical = false;
    private boolean mServiceBound = false;

    // Ràng buộc Client này với MusicPlayer
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            // Đã liên kết với LocalService, truyền IBinder và nhận phiên bản LocalService
            MediaPlaybackService.LocalBinder binder = (MediaPlaybackService.LocalBinder) service;
            mPlayer = binder.getService();
            mServiceBound = true;

//            Toast.makeText(MusicActivity.this, "Service Bound", Toast.LENGTH_SHORT)
//                    .show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        Toolbar toolbar = findViewById(R.id.toolbar);
        float density = getResources().getDisplayMetrics().density;
        Log.d("HaiKH", String.valueOf(density));
        Log.d("HaiKH", "onCreate: " + density);
        mAudioList = new ArrayList<>();

        //set toolbar
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.titleToolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        addFragment();

        //check permission
        if (ContextCompat.checkSelfPermission(MusicActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MusicActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WAKE_LOCK},
                    MY_PERMISSION_REQUEST);
        }
    }

    public MediaPlaybackService getMediaPlayerService() {
        return mPlayer;
    }

    private void playAudio(int audioIndex) {
        mCurrentPosition = audioIndex;
        //Check is service is active
        StorageUtil storage = new StorageUtil(getApplicationContext());
        if (!mServiceBound) {
            //Lưu danh sách âm thanh to SharedPreferences
            storage.storeAudio(mAudioList);
            storage.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(this, MediaPlaybackService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Lưu vị trí âm thanh mới to SharedPreferences
            storage.storeAudioIndex(audioIndex);

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(BROADCAST_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(SERVICE_STATE, mServiceBound);
        savedInstanceState.putInt(AUDIO_INDEX, mCurrentPosition);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mServiceBound = savedInstanceState.getBoolean(SERVICE_STATE);
        mCurrentPosition = savedInstanceState.getInt(AUDIO_INDEX);
    }

    private void addFragment() {
        //add fragment to frameLayout
        mFragmentManager = getSupportFragmentManager();
        int orientation = getResources().getConfiguration().orientation;

        mIsVertical = orientation != Configuration.ORIENTATION_LANDSCAPE;

        if (mIsVertical) {
            mFragmentTransaction = mFragmentManager.beginTransaction();
            AllSongsFragment allSongsFragment = new AllSongsFragment();
            mFragmentTransaction.replace(R.id.frameLayoutAllSong, allSongsFragment);
            mFragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            mFragmentTransaction.commit();
        } else {
            AllSongsFragment allSongFragment = new AllSongsFragment();
            mFragmentTransaction = mFragmentManager.beginTransaction();
            mFragmentTransaction.replace(R.id.frameLayoutOne, allSongFragment);
            mFragmentTransaction.commit();

            MediaPlaybackFragment mediaPlaybackFragment = new MediaPlaybackFragment();
            FragmentTransaction transactionTow = mFragmentManager.beginTransaction();
            transactionTow.replace(R.id.frameLayoutTwo, mediaPlaybackFragment);
            transactionTow.addToBackStack(null);
            transactionTow.commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //event click recyclerView
    @Override
    public void onNewClick(ArrayList<Song> songList, int position) {
        if (mIsVertical) {
            AllSongsFragment allSongsFragment = (AllSongsFragment) getSupportFragmentManager().findFragmentById(R.id.frameLayoutAllSong);
            if (allSongsFragment != null) {
                allSongsFragment.setDataBottom(songList, position);
                allSongsFragment.setVisible(position);
                this.mAudioList = songList;
                playAudio(position);
            }
        } else {
            MediaPlaybackFragment player = (MediaPlaybackFragment) getSupportFragmentManager().findFragmentById(R.id.frameLayoutTwo);
            player.setTitle(songList.get(position));
            this.mAudioList = songList;
            playAudio(position);
        }
    }

    @Override
    public void showMediaFragment(Song song) {
        MediaPlaybackFragment mediaPlaybackFragment;
        if (mIsVertical) {
            mediaPlaybackFragment = MediaPlaybackFragment.getInstancesMedia(song);
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frameLayoutMedia, mediaPlaybackFragment);
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else {
            mediaPlaybackFragment = new MediaPlaybackFragment();
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frameLayoutTwo, mediaPlaybackFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(MusicActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                    addFragment();
                }
            } else {
                Toast.makeText(this, "No permission granted", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

}