package com.bkav.android.mymusic.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.bkav.android.mymusic.ImageSong;
import com.bkav.android.mymusic.MediaPlaybackStatus;
import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.StorageUtil;
import com.bkav.android.mymusic.models.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MediaPlaybackService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener,
        AudioManager.OnAudioFocusChangeListener {

    public static final String ACTION_PLAY = "com.bkav.musictest.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.bkav.musictest.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.bkav.musictest.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.bkav.musictest.ACTION_NEXT";
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";

    private static final int NO_REPEAT_CODE = 0;
    private static final int REPEAT_ALL_LIST_CODE = 1;
    private static final int REPEAT_ONE_SONG_CODE = 2;

    //AudioPlayer notification ID
    private static final int NOTIFICATION_ID = 101;

    //number action
    private static final int NUMBER_ACTION_PLAY = 0;
    private static final int NUMBER_ACTION_PAUSE = 1;
    private static final int NUMBER_ACTION_NEXT = 2;
    private static final int NUMBER_ACTION_PREVIOUS = 3;
    private static final int TIME_LIMIT = 3000;

    // Binder given to clients
    private final IBinder mIBinder = new LocalBinder();
    private int mStateRepeat;
    private boolean mStateShuffle;
    private StorageUtil mStorageUtil;
    //action notify
    private OnNotificationListener mOnNotificationListener;

    private Random random;

    //List of available Audio files
    private ArrayList<Song> mSongList;
    private int mSongIndex = -1;
    private Song mSongActive; //Đối tượng đang phát

    //Xử lý các cuộc gọi đến
    private boolean mOngoingCall = false;
    private PhoneStateListener mPhoneStateListener;
    private TelephonyManager mTelephonyManager;
    private MediaPlayer mMediaPlayer;
    private NotificationManager mNotifyManager;

    //Used to pause/resume MediaPlayer
    private int mResumePosition;

    private AudioManager mSongManager;

    //Becoming noisy
    private BroadcastReceiver mBecomingNoisyReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {

            //pause audio on ACTION_AUDIO_BECOMING_NOISY Tạm dừng khi có cuộc gọi
            pauseMedia();
            buildNotification(MediaPlaybackStatus.PAUSED);
            mOnNotificationListener.onUpdate();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_DETACH);
            }
        }
    };

    public ArrayList<Song> getSongList() {
        return mSongList;
    }


    public Song getActiveAudio() {
        return mSongActive;
    }

    public void playMedia() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    public MediaPlaybackStatus isPlayingState() {
        if (mMediaPlayer.isPlaying()) {
            return MediaPlaybackStatus.PLAYING;
        } else {
            return MediaPlaybackStatus.PAUSED;
        }
    }

    public int getAudioIndex() {

        return mSongIndex;
    }

    private void stopMedia() {
        if (mMediaPlayer == null) return;
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
    }

    public void pauseMedia() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mResumePosition = mMediaPlayer.getCurrentPosition();
            buildNotification(MediaPlaybackStatus.PLAYING);
            mOnNotificationListener.onUpdate();
        }
    }

    private void resumeMedia() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.seekTo(mResumePosition);
            mMediaPlayer.start();
            mOnNotificationListener.onUpdate();
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();

        //được gọi khi nguồn phương tiện sẵn sàng để phát lại
        mMediaPlayer.setOnPreparedListener(this);

        //Set up MediaPlayer event listeners
        //được gọi khi bài hát chạy xong
        mMediaPlayer.setOnCompletionListener(this);

        //được gọi khi một hoạt động tìm kiếm đã hoàn thành.
        mMediaPlayer.setOnSeekCompleteListener(this);

    }

    public void playSong(Song song) {
        //play a song
        mMediaPlayer.reset();
        mSongIndex = mStorageUtil.loadAudioIndex();
        mSongList = mStorageUtil.loadAudio();
        mSongActive = song;

        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            // Set the data source to the mediaFile location
            mMediaPlayer.setDataSource(mSongActive.getPath());
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        mMediaPlayer.prepareAsync();
        buildNotification(MediaPlaybackStatus.PLAYING);
    }


    //Hệ thống gọi phương thức này khi một hoạt động, yêu cầu dịch vụ được bắt đầu
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Xử lý hành động
        handleIncomingActions(intent);
        return START_NOT_STICKY;
    }

    //set image when customContentView
    private void setImageNotify(RemoteViews remoteViews, int id) {
        byte[] art = ImageSong.getByteImageSong(mSongActive.getPath());
        if (art != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            remoteViews.setImageViewBitmap(id, bitmap);
        } else {
            remoteViews.setImageViewResource(id, R.mipmap.ic_music_not_picture);
        }
    }

    //set text when customContentView
    private void setTextNotify(RemoteViews remoteViews, int idTitle, int idArtist) {
        remoteViews.setTextViewText(idTitle, mSongActive.getTitle());
        remoteViews.setTextViewText(idArtist, mSongActive.getArtist());
    }

    private void buildNotification(MediaPlaybackStatus mediaPlaybackStatus) {
        int notificationAction = R.drawable.ic_button_playing;//needs to be initialized
        PendingIntent playPauseAction = null;

        //Build a new notification according to the current state of the MediaPlayer
        if (mediaPlaybackStatus == MediaPlaybackStatus.PLAYING) {

            //create the pause action
            playPauseAction = playbackAction(NUMBER_ACTION_PAUSE);
        } else if (mediaPlaybackStatus == MediaPlaybackStatus.PAUSED) {
            notificationAction = R.drawable.ic_button_pause;

            //create the play action
            playPauseAction = playbackAction(NUMBER_ACTION_PLAY);
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_launcher_background); //replace with your own image

        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_small);
        notificationLayout.setOnClickPendingIntent(R.id.small_previous, playbackAction(NUMBER_ACTION_PREVIOUS));
        notificationLayout.setOnClickPendingIntent(R.id.small_next, playbackAction(NUMBER_ACTION_NEXT));
        notificationLayout.setOnClickPendingIntent(R.id.small_pause, playPauseAction);
        setImageNotify(notificationLayout, R.id.small_picture);
        notificationLayout.setOnClickPendingIntent(R.id.small_pause, playPauseAction);
        notificationLayout.setImageViewResource(R.id.small_pause, notificationAction);
        setImageNotify(notificationLayout, R.id.small_picture);

        RemoteViews notificationLayoutExpanded = new RemoteViews(getPackageName(), R.layout.notification_large);
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.big_previous, playbackAction(3));
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.big_next, playbackAction(2));
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.big_pause, playPauseAction);
        notificationLayoutExpanded.setImageViewResource(R.id.big_pause, notificationAction);
        setImageNotify(notificationLayoutExpanded, R.id.big_picture);
        setTextNotify(notificationLayoutExpanded, R.id.big_title, R.id.big_artist);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Create a NotificationChannel
            NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID,
                    "Notification", NotificationManager
                    .IMPORTANCE_LOW);
            notificationChannel.enableLights(true);
            notificationChannel.setSound(null, null);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(false);
            notificationChannel.setDescription("Notification");
            mNotifyManager.createNotificationChannel(notificationChannel);

            // Create a new Notification
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                    .setShowWhen(false)
                    // Set the Notification style
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    // Set the Notification color
                    .setColor(getResources().getColor(R.color.colorPrimary, null))
                    // Set the large and small icons
                    .setLargeIcon(largeIcon)
                    .setSmallIcon(R.mipmap.stat_notify_musicplayer)
                    // Set Notification content information
                    .setContentText(mSongActive.getArtist())
                    .setCustomContentView(notificationLayout)
                    .setCustomBigContentView(notificationLayoutExpanded)
                    .setContentTitle(mSongActive.getTitle());
            startForeground(NOTIFICATION_ID, notificationBuilder.build());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mIBinder;
    }

    @Override
    public void onAudioFocusChange(int i) {

        //Được gọi khi tiêu điểm âm thanh của hệ thống được cập nhật.
        switch (i) {
            case AudioManager.AUDIOFOCUS_GAIN:

                // resume playback
                if (mMediaPlayer == null) initMediaPlayer();
                else if (!mMediaPlayer.isPlaying()) mMediaPlayer.start();
                mMediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:

                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:

                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:

                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    //Bỏ tiêu điểm âm thanh khi phát xong
    private void removeAudioFocus() {
        mSongManager.abandonAudioFocus(this);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        songCompletion();
    }

    /**
     * goi khi bai hat tu dong chuyen tiep
     */
    private void songCompletion() {
        StorageUtil storageUtil = new StorageUtil(getApplicationContext());
        mStateRepeat = storageUtil.loadStateRepeat();
        mStateShuffle = storageUtil.loadStateShuffle();
        if (mStateShuffle) {
            mSongIndex = random.nextInt(mSongList.size());
        }
        switch (mStateRepeat) {
            case NO_REPEAT_CODE:
                if (mSongIndex == mSongList.size() - 1) {
                    mMediaPlayer.pause();
                    buildNotification(MediaPlaybackStatus.PAUSED);
                    mOnNotificationListener.onUpdate();
                } else {

                    //get next in playlist
                    mSongActive = mSongList.get(++mSongIndex);
                    playSongWhenComplete();
                }
                break;
            case REPEAT_ALL_LIST_CODE:
                if (mSongIndex == mSongList.size() - 1) {

                    //if last in playlist
                    mSongIndex = 0;
                    mSongActive = mSongList.get(mSongIndex);
                } else {

                    //get next in playlist
                    mSongActive = mSongList.get(++mSongIndex);
                }
                playSongWhenComplete();
                break;
            case REPEAT_ONE_SONG_CODE:
                mSongActive = mSongList.get(mSongIndex);
                playSongWhenComplete();
                break;
        }

    }

    /**
     * phat nhac sau khi songCompletion() goi den
     */
    public void playSongWhenComplete() {
        playMedia();
        buildNotification(MediaPlaybackStatus.PLAYING);
        mOnNotificationListener.onUpdate();

        //Update stored index
        mStorageUtil.storeAudioIndex(mSongIndex);
        playSong(mSongActive);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
    }

    /**
     * dang ki broadcast lang nghe rut tai nghe
     */
    private void registerBecomingNoisyReceiver() {
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mBecomingNoisyReceiver, intentFilter);
    }

    /**
     * duoc goi khi co dien thoai goi den
     */
    private void callStateListener() {
        // Get the telephony manager
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //nếu có ít nhất một cuộc gọi hoặc điện thoại đang đổ chuông
                    //tạm dừng MediaPlayer
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mMediaPlayer != null) {
                            pauseMedia();
                            mOngoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Nếu không có hoạt động nào của cuộc gọi thì tiếp tục phát
                        if (mMediaPlayer != null) {
                            if (mOngoingCall) {
                                mOngoingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        mTelephonyManager.listen(mPhoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Thực hiện các thủ tục thiết lập một lần
        initMediaPlayer();
        random = new Random();
        mNotifyManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        mStorageUtil = new StorageUtil(getApplicationContext());
        // Quản lý các cuộc gọi đến trong khi phát lại.
        // Tạm dừng MediaPlayer khi có cuộc gọi đến,
        // Tiếp tục khi cúp máy.
        callStateListener();

        //ACTION_AUDIO_BECOMING_NOISY -- thay đổi đầu ra âm thanh khi rút tai nghe -- BroadcastReceiver
        registerBecomingNoisyReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            stopMedia();
            mMediaPlayer.release();
        }
        removeAudioFocus();

        //Disable the PhoneStateListener
        if (mPhoneStateListener != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        removeNotification();

        //hủy đăng kí BroadcastReceivers
        unregisterReceiver(mBecomingNoisyReceiver);

        //Xóa danh sách đã lưu trong cache
        mStorageUtil.clearCachedAudioPlaylist();
    }

    /**
     * tìm ra hành động phát lại nào được kích hoạt
     * get action from pending intent to run
     *
     * @param playbackAction Intent action
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;
        if (playbackAction.getAction().equalsIgnoreCase(ACTION_NEXT)) {
            skipToNext();
            mStorageUtil.storeAudioIndex(mSongIndex);
            buildNotification(MediaPlaybackStatus.PLAYING);
        } else if (playbackAction.getAction().equalsIgnoreCase(ACTION_PREVIOUS)) {
            skipToPrevious();
            mStorageUtil.storeAudioIndex(mSongIndex);
            buildNotification(MediaPlaybackStatus.PLAYING);
        } else if (playbackAction.getAction().equalsIgnoreCase(ACTION_PAUSE)) {
            pauseMedia();
            buildNotification(MediaPlaybackStatus.PAUSED);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_DETACH);
            }
        } else if (playbackAction.getAction().equalsIgnoreCase(ACTION_PLAY)) {
            resumeMedia();
            buildNotification(MediaPlaybackStatus.PLAYING);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void updateMetaDataNotify(MediaPlaybackStatus mediaPlaybackStatus) {
        buildNotification(mediaPlaybackStatus);
    }

    /**
     * Next song
     */
    public void skipToNext() {

        StorageUtil storageUtil = new StorageUtil(getApplicationContext());
        mStateShuffle = storageUtil.loadStateShuffle();

        if (mStateShuffle) {
            mSongIndex = random.nextInt(mSongList.size());
            mSongActive = mSongList.get(mSongIndex);
        } else {
            if (mSongIndex == mSongList.size() - 1) {

                //if last in playlist
                mSongIndex = 0;
                mSongActive = mSongList.get(mSongIndex);
            } else {

                //get next in playlist
                mSongActive = mSongList.get(++mSongIndex);
            }
        }
        mOnNotificationListener.onUpdate();

        //Update stored index
        mStorageUtil.storeAudioIndex(mSongIndex);
        mStorageUtil.storeAudioID(mSongActive.getID());
        playSong(mSongActive);

    }

    /**
     * previous song
     */
    public void skipToPrevious() {
        StorageUtil storageUtil = new StorageUtil(getApplicationContext());
        mStateShuffle = storageUtil.loadStateShuffle();
        if (mMediaPlayer.getCurrentPosition() <= TIME_LIMIT) {
            if (mSongIndex == 0) {

                //if first in playlist
                //set index to the last of audioList
                mSongIndex = mSongList.size() - 1;
                mSongActive = mSongList.get(mSongIndex);
            } else if (mStateShuffle) {
                mSongIndex = random.nextInt(mSongList.size());
                mSongActive = mSongList.get(mSongIndex);
            } else {

                //get previous in playlist
                mSongActive = mSongList.get(--mSongIndex);
            }
        } else {

            mSongActive = mSongList.get(mSongIndex);
        }
        mOnNotificationListener.onUpdate();

        //Update stored index
        mStorageUtil.storeAudioIndex(mSongIndex);
        mStorageUtil.storeAudioID(mSongActive.getID());
        playSong(mSongActive);
    }

    /**
     * remove notification
     */
    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    /**
     * Takes action that will be triggered to launch
     *
     * @param actionNumber number order will handling
     * @return action to call control
     */
    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, MediaPlaybackService.class);
        switch (actionNumber) {
            case NUMBER_ACTION_PLAY:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case NUMBER_ACTION_PAUSE:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case NUMBER_ACTION_NEXT:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case NUMBER_ACTION_PREVIOUS:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    public void setOnNotificationListener(OnNotificationListener listener) {
        this.mOnNotificationListener = listener;
    }

    public interface OnNotificationListener {
        void onUpdate();
    }

    public class LocalBinder extends Binder {
        public MediaPlaybackService getService() {
            return MediaPlaybackService.this;
        }
    }
}
