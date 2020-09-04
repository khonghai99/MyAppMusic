package com.bkav.android.mymusic.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
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
    public static final String Broadcast_PLAY_NEW_AUDIO = "com.bkav.musictest.PlayNewAudio";
    private static final int MY_PERMISSION_REQUEST = 1;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private MediaPlaybackService player;
    private ArrayList<Song> songList;
    private int mCurrentPosition;
    private boolean isVertical = false;
    private boolean serviceBound = false;

    // Ràng buộc Client này với MusicPlayer
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            // Đã liên kết với LocalService, truyền IBinder và nhận phiên bản LocalService
            MediaPlaybackService.LocalBinder binder = (MediaPlaybackService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

            Toast.makeText(MusicActivity.this, "Service Bound", Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        Toolbar toolbar = findViewById(R.id.toolbar);

        songList = new ArrayList<Song>();

        //set toolbar
        setSupportActionBar(toolbar);
        toolbar.setTitle("Music");
        AddFragment();

        //check permission
        if (ContextCompat.checkSelfPermission(MusicActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MusicActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
        }
    }

    private void playAudio(int audioIndex) {
        mCurrentPosition = audioIndex;
        //Check is service is active
        StorageUtil storage = new StorageUtil(getApplicationContext());
        if (!serviceBound) {
            //Lưu danh sách âm thanh to SharedPreferences
            storage.storeAudio(songList);
            storage.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(this, MediaPlaybackService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Lưu vị trí âm thanh mới to SharedPreferences
            storage.storeAudioIndex(audioIndex);

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    private void AddFragment() {
        //add fragment to frameLayout
        fragmentManager = getSupportFragmentManager();
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            isVertical = false;
        } else {
            // In portrait
            isVertical = true;
        }
        if (isVertical) {
            fragmentTransaction = fragmentManager.beginTransaction();
            AllSongsFragment allSongsFragment = new AllSongsFragment();
            fragmentTransaction.replace(R.id.frameLayoutAllSong, allSongsFragment);
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.commit();
        } else {
            AllSongsFragment allSongFragment = new AllSongsFragment();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frameLayoutOne, allSongFragment);
            fragmentTransaction.commit();

            MediaPlaybackFragment mediaPlaybackFragment = new MediaPlaybackFragment();
            FragmentTransaction transactionTow = fragmentManager.beginTransaction();
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
        if (isVertical) {
            AllSongsFragment allSongsFragment = (AllSongsFragment) getSupportFragmentManager().findFragmentById(R.id.frameLayoutAllSong);
            if (allSongsFragment != null) {
                allSongsFragment.setDataBottom(songList, position);
                allSongsFragment.setVisible();
                this.songList = songList;
                playAudio(position);
            }
        } else {
            MediaPlaybackFragment player = (MediaPlaybackFragment) getSupportFragmentManager().findFragmentById(R.id.frameLayoutTwo);
            player.setTitle(songList.get(position));
            this.songList = songList;
            playAudio(position);
        }


    }

    @Override
    public void showMediaFragment(Song song) {
        MediaPlaybackFragment mediaPlaybackFragment;
        if (isVertical) {
            mediaPlaybackFragment = MediaPlaybackFragment.getInstancesMedia(song);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frameLayoutMedia, mediaPlaybackFragment);
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else {
            mediaPlaybackFragment = new MediaPlaybackFragment();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
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
                }
            } else {
                Toast.makeText(this, "No permission granted", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

}