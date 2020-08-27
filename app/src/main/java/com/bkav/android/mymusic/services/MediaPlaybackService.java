package com.bkav.android.mymusic.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.Nullable;

import com.bkav.android.mymusic.models.Song;

import java.util.ArrayList;

public class MediaPlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {
    private final IBinder musicBind = new MusicBinder();
    //media player
    private MediaPlayer player;
    //song list
    private ArrayList<Song> songs;

    //current position
    private int songPos;

    public void onCreate() {
        //create the service
        super.onCreate();

        //initialize position
        songPos = 0;

        //create player
        player = new MediaPlayer();

        initMusicPlayer();
    }

    public void initMusicPlayer() {

        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        //khi MediaPlayer được chuẩn bị
        player.setOnPreparedListener(this);

        //khi phát xong
        player.setOnCompletionListener(this);

        //khi sinh lỗi
        player.setOnErrorListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {

    }

    public void setList(ArrayList<Song> theSongs) {
        songs = theSongs;
    }

    public class MusicBinder extends Binder {
        public MediaPlaybackService getService() {
            return MediaPlaybackService.this;
        }
    }
    public void playSong(){
        player.reset();
    }
}
