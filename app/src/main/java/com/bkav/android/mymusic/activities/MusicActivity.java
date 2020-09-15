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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bkav.android.mymusic.PlaybackStatus;
import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.StorageUtil;
import com.bkav.android.mymusic.adapters.SongAdapter;
import com.bkav.android.mymusic.fragments.AllSongsFragment;
import com.bkav.android.mymusic.fragments.MediaPlaybackFragment;
import com.bkav.android.mymusic.models.Song;
import com.bkav.android.mymusic.services.MediaPlaybackService;

import java.util.ArrayList;


public class MusicActivity extends AppCompatActivity implements SongAdapter.OnNewClickListener {

    //sends broadcast intents to the MediaPlayerService
    public static final String BROADCAST_PLAY_NEW_AUDIO = "com.bkav.musictest.PlayNewAudio";
    private static final int MY_PERMISSION_REQUEST = 1;// FIXME: 15/09/2020 xin 1 quyền cụ thể thì em đặt tên cho rõ nghĩa hơn cho tường minh
    private static final String SERVICE_STATE = "ServiceState";
    private static final String AUDIO_INDEX = "audioIndex";
    public Fragment mAllSongsFragment, mMediaPlaybackFragment;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    private MediaPlaybackService mPlayerService;
    private ArrayList<Song> mAudioList;
    private int mCurrentPosition;
    private boolean mIsVertical = false;
    private boolean mServiceBound = false;
    // Ràng buộc Client này với MusicPlayer
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            // Đã liên kết với LocalService, truyền IBinder và nhận phiên bản LocalService
            MediaPlaybackService.LocalBinder binder = (MediaPlaybackService.LocalBinder) service;
            mPlayerService = binder.getService();
            mServiceBound = true;

            mPlayerService.setOnNotificationListener(new MediaPlaybackService.OnNotificationListener() {
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

    public void showMediaPlaybackFragment() {
        FragmentManager mFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayoutMedia, mMediaPlaybackFragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

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
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
        }
    }

    /**
     * get service
     *
     * @return mPlayerService is Servive
     */
    public MediaPlaybackService getMediaPlayerService() {
        Log.d("HaiKH", "getMediaPlayerService: on");
        return mPlayerService;
    }

    /**
     * run player and set storage
     *
     * @param audioIndex the position of the track
     */
    private void playAudio(int audioIndex) {
        mCurrentPosition = audioIndex;

        //Check is service is active
        StorageUtil storage = new StorageUtil(getApplicationContext());

        //Lưu vị trí âm thanh mới to SharedPreferences
        storage.storeAudioIndex(audioIndex);
        if (!mServiceBound) {
            //Lưu danh sách âm thanh to SharedPreferences
            storage.storeAudio(mAudioList);
            Intent playerIntent = new Intent(this, MediaPlaybackService.class);
            startService(playerIntent);
            bindService(playerIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        } else {
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
        int orientation = getResources().getConfiguration().orientation;

        mIsVertical = orientation != Configuration.ORIENTATION_LANDSCAPE;

        if (mIsVertical) {
            mFragmentTransaction = mFragmentManager.beginTransaction();
            mFragmentTransaction.replace(R.id.frameLayoutAllSong, mAllSongsFragment);
            mFragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            mFragmentTransaction.commit();
        } else {
            mFragmentTransaction = mFragmentManager.beginTransaction();
            mFragmentTransaction.replace(R.id.frameLayoutOne, mAllSongsFragment);
            mFragmentTransaction.commit();

            FragmentTransaction transactionTow = mFragmentManager.beginTransaction();
            transactionTow.replace(R.id.frameLayoutTwo, mMediaPlaybackFragment);
            transactionTow.commit();
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

    /**
     * get event click recyclerView and play audio
     *
     * @param songList ArrayList of object Song
     * @param position playing song position
     */
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
            MediaPlaybackFragment mediaPlaybackFragment = (MediaPlaybackFragment) getSupportFragmentManager().findFragmentById(R.id.frameLayoutTwo);
            mediaPlaybackFragment.setTitle(songList.get(position));
            this.mAudioList = songList;
            playAudio(position);
        }
    }

    /**
     * grant audio access
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
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