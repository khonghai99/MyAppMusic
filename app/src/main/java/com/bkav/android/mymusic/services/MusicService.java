package com.bkav.android.mymusic.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.bkav.android.mymusic.StorageUtil;
import com.bkav.android.mymusic.models.Song;

import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private final IBinder musicBind = new MusicBinder();
    //media player
    private MediaPlayer player;
    //song list
    private ArrayList<Song> songs;
    //current position
    private int songPosn;
    private ArrayList<Song> mAudioList;
    private Song mActiveAudio;

    public void onCreate() {

        //create the service
        super.onCreate();

        //initialize position
        songPosn = 0;

        //create player
        player = new MediaPlayer();
        initMusicPlayer();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    public void initMusicPlayer() {
        //set player properties

        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    public void playSong() {
        //play a song
        player.reset();
        StorageUtil storage = new StorageUtil(getApplicationContext());
        mAudioList = storage.loadAudio();
        mActiveAudio = mAudioList.get(storage.loadAudioIndex());

        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            // Set the data source to the mediaFile location
            player.setDataSource(mActiveAudio.getPath());
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        player.prepareAsync();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    public void setSong(int songIndex) {
        songPosn = songIndex;
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (!player.isPlaying()) {
            player.start();
        }
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
}
