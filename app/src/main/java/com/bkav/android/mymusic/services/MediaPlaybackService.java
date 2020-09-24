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
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.bkav.android.mymusic.ImageSong;
import com.bkav.android.mymusic.PlaybackStatus;
import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.StorageUtil;
import com.bkav.android.mymusic.models.Song;

import java.io.IOException;
import java.util.ArrayList;

public class MediaPlaybackService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener,
        AudioManager.OnAudioFocusChangeListener {

    public static final String BROADCAST_PLAY_NEW_AUDIO = "com.bkav.musictest.PlayNewAudio";
    public static final String ACTION_PLAY = "com.bkav.musictest.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.bkav.musictest.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.bkav.musictest.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.bkav.musictest.ACTION_NEXT";
    public static final String ACTION_STOP = "com.bkav.musictest.ACTION_STOP";
    private static final String AUDIO_PLAYER = "com.bkav.android.mymusic.services.AUDIO_PLAYER";
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";

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

    //action notify
    private OnNotificationListener mOnNotificationListener;

    //MediaSession
    private MediaSessionManager mMediaSessionManager;
    private MediaSession mMediaSession;
    private MediaController.TransportControls mTransportControls;

    //List of available Audio files
    private ArrayList<Song> mAudioList;
    private int mAudioIndex = -1;
    private Song mActiveAudio; //Đối tượng đang phát

    //Xử lý các cuộc gọi đến
    private boolean mOngoingCall = false;
    private PhoneStateListener mPhoneStateListener;
    private TelephonyManager mTelephonyManager;
    private MediaPlayer mMediaPlayer;
    private NotificationManager mNotifyManager;

    //Used to pause/resume MediaPlayer
    private int mResumePosition;

    private AudioManager mAudioManager;

    //Becoming noisy
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY Tạm dừng khi có cuộc gọi
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);
            mOnNotificationListener.onUpdate(mAudioIndex, PlaybackStatus.PAUSED);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_DETACH);
            }
        }
    };
    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {
            //Get the new media index form SharedPreferences đã lưu tại playAudio của Main
            mAudioIndex = new StorageUtil(getApplicationContext()).loadAudioIndex();
            if (mAudioIndex != -1 && mAudioIndex < mAudioList.size()) {
                //index is in a valid range
                mActiveAudio = mAudioList.get(mAudioIndex);
            } else {
                stopSelf();

            }

            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Audio
            stopMedia();
            // mMediaPlayer.reset();
            initMediaPlayer();
            //  updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
        }
    };

    public Song getActiveAudio() {
        return mActiveAudio;
    }

    public void playMedia() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    public PlaybackStatus isPlayingState() {
        if (mMediaPlayer.isPlaying()) {
            return PlaybackStatus.PLAYING;
        } else {
            return PlaybackStatus.PAUSED;
        }
    }

    public int getAudioIndex() {

        return mAudioIndex;
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
        }
    }

    private void resumeMedia() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.seekTo(mResumePosition);
            mMediaPlayer.start();
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();

        //Set up MediaPlayer event listeners
        //được gọi khi bài hát chạy xong
        mMediaPlayer.setOnCompletionListener(this);

        //được gọi khi nguồn phương tiện sẵn sàng để phát lại
        mMediaPlayer.setOnPreparedListener(this);

        //được gọi khi một hoạt động tìm kiếm đã hoàn thành.
        mMediaPlayer.setOnSeekCompleteListener(this);

        //Reset so that the MediaPlayer is not pointing to another data source
        mMediaPlayer.reset();

        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            // Set the data source to the mediaFile location
            mMediaPlayer.setDataSource(mActiveAudio.getPath());
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        mMediaPlayer.prepareAsync();
    }

    //Hệ thống gọi phương thức này khi một hoạt động, yêu cầu dịch vụ được bắt đầu
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            //Load data from SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            mAudioList = storage.loadAudio();
            mAudioIndex = storage.loadAudioIndex();

            if (mAudioIndex != -1 && mAudioIndex < mAudioList.size()) {
                //index is in a valid range
                mActiveAudio = mAudioList.get(mAudioIndex);
            } else {
                stopSelf();
            }
        } catch (NullPointerException e) {
            stopSelf();
        }

        //Nhận tiêu điểm
        if (!requestAudioFocus()) {
            //Không nhận được tiêu điểm
            stopSelf();
        }
        //Xử lý hành động  từ MediaSession.TransportControls
        handleIncomingActions(intent);
        return START_NOT_STICKY;
    }

    //set image when customContentView
    private void setImageNotify(RemoteViews remoteViews, int id) {
        byte[] art = ImageSong.getByteImageSong(mActiveAudio.getPath());
        if (art != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            remoteViews.setImageViewBitmap(id, bitmap);

        } else {
            remoteViews.setImageViewResource(id, R.mipmap.ic_music_not_picture);
        }
    }

    //set text when customContentView
    private void setTextNotify(RemoteViews remoteViews, int idTitle, int idArtist) {
        remoteViews.setTextViewText(idTitle, mActiveAudio.getTitle());
        remoteViews.setTextViewText(idArtist, mActiveAudio.getArtist());
    }

    private void buildNotification(PlaybackStatus playbackStatus) {

        int notificationAction = R.drawable.ic_button_playing;//needs to be initialized
        PendingIntent playPauseAction = null;

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = R.drawable.ic_button_playing;
            //create the pause action
            playPauseAction = playbackAction(NUMBER_ACTION_PAUSE);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = R.drawable.ic_button_pause;
            //create the play action
            playPauseAction = playbackAction(NUMBER_ACTION_PLAY);
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_launcher_background); //replace with your own image

        RemoteViews smallNotify = new RemoteViews(getPackageName(), R.layout.small_notification);
        smallNotify.setOnClickPendingIntent(R.id.ivSmallPrevious, playbackAction(NUMBER_ACTION_PREVIOUS));
        smallNotify.setOnClickPendingIntent(R.id.ivSmallNext, playbackAction(NUMBER_ACTION_NEXT));
        smallNotify.setOnClickPendingIntent(R.id.ivSmallPause, playPauseAction);
        setImageNotify(smallNotify, R.id.ivSmallPicture);
        smallNotify.setOnClickPendingIntent(R.id.ivSmallPause, playPauseAction);
        smallNotify.setImageViewResource(R.id.ivSmallPause, notificationAction);
        setImageNotify(smallNotify, R.id.ivSmallPicture);

        RemoteViews bigNotify = new RemoteViews(getPackageName(), R.layout.big_notification);
        bigNotify.setOnClickPendingIntent(R.id.ivBigPrevious, playbackAction(3));
        bigNotify.setOnClickPendingIntent(R.id.ivBigNext, playbackAction(2));
        bigNotify.setOnClickPendingIntent(R.id.ivBigPause, playPauseAction);
        bigNotify.setImageViewResource(R.id.ivBigPause, notificationAction);
        setImageNotify(bigNotify, R.id.ivBigPicture);
        setTextNotify(bigNotify, R.id.tvBigTitle, R.id.tvBigArtist);

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
                    .setContentText(mActiveAudio.getArtist())
                    .setCustomContentView(smallNotify)
                    .setCustomBigContentView(bigNotify)
                    .setContentTitle(mActiveAudio.getTitle());
            startForeground(NOTIFICATION_ID, notificationBuilder.build());
        }
    }

    private void registerPlayNewAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(BROADCAST_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, filter);
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

    private boolean requestAudioFocus() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        //Yêu cầu tiêu điểm âm thanh để phát lại
        int result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        //AUDIOFOCUS_REQUEST_GRANTED: Yêu cầu thay đổi tiêu điểm thành công.
        //Focus thành công
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        //Lấy tiêu điểm không thành công
    }

    //Bỏ tiêu điểm âm thanh khi phát xong
    private void removeAudioFocus() {
        mAudioManager.abandonAudioFocus(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        //Được gọi khi quá trình phát lại nguồn phương tiện đã hoàn tất.
        skipToNext();
        //updateMetaData();
        buildNotification(PlaybackStatus.PLAYING);
        mOnNotificationListener.onUpdate(mAudioIndex, PlaybackStatus.PLAYING);
        new StorageUtil(getApplicationContext()).storeAudioIndex(mAudioIndex);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        //Được gọi khi nguồn phương tiện đã sẵn sàng để phát lại.
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        //Được gọi cho biết đã hoàn thành một hoạt động tìm kiếm.
    }

    private void registerBecomingNoisyReceiver() {
        //đăng ký sau khi nhận được tiêu điểm âm thanh
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }


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
        mNotifyManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        // Quản lý các cuộc gọi đến trong khi phát lại.
        // Tạm dừng MediaPlayer khi có cuộc gọi đến,
        // Tiếp tục khi cúp máy.
        callStateListener();

        //ACTION_AUDIO_BECOMING_NOISY -- thay đổi đầu ra âm thanh khi rút tai nghe -- BroadcastReceiver
        registerBecomingNoisyReceiver();

        //Listen for new Audio to play -- BroadcastReceiver
        registerPlayNewAudio();
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
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAudio);

        //Xóa danh sách đã lưu trong cache
        new StorageUtil(getApplicationContext()).clearCachedAudioPlaylist();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initMediaSession() throws RemoteException {
        if (mMediaSessionManager != null) return; //mediaSessionManager exists

        mMediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        // Create a new MediaSession
        mMediaSession = new MediaSession(getApplicationContext(), AUDIO_PLAYER);
        //Get MediaSessions transport controls
        mTransportControls = mMediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mMediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mMediaSession.setFlags(MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Attach Callback to receive MediaSession updates
        mMediaSession.setCallback(new MediaSession.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();
                resumeMedia();
                mOnNotificationListener.onUpdate(mAudioIndex, PlaybackStatus.PLAYING);
                buildNotification(PlaybackStatus.PLAYING);
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
                mOnNotificationListener.onUpdate(mAudioIndex, PlaybackStatus.PAUSED);
                buildNotification(PlaybackStatus.PAUSED);
                stopForeground(STOP_FOREGROUND_DETACH);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                skipToNext();
                //updateMetaData();
                mOnNotificationListener.onUpdate(mAudioIndex, PlaybackStatus.PLAYING);
                new StorageUtil(getApplicationContext()).storeAudioIndex(mAudioIndex);
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevious();
                //updateMetaData();
                mOnNotificationListener.onUpdate(mAudioIndex, PlaybackStatus.PLAYING);
                new StorageUtil(getApplicationContext()).storeAudioIndex(mAudioIndex);
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                //Stop the service
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
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
            mOnNotificationListener.onUpdate(mAudioIndex, PlaybackStatus.PLAYING);
            new StorageUtil(getApplicationContext()).storeAudioIndex(mAudioIndex);
            buildNotification(PlaybackStatus.PLAYING);

        } else if (playbackAction.getAction().equalsIgnoreCase(ACTION_PREVIOUS)) {
            skipToPrevious();
            mOnNotificationListener.onUpdate(mAudioIndex, PlaybackStatus.PLAYING);
            new StorageUtil(getApplicationContext()).storeAudioIndex(mAudioIndex);
            buildNotification(PlaybackStatus.PLAYING);

        } else if (playbackAction.getAction().equalsIgnoreCase(ACTION_PAUSE)) {
            pauseMedia();
            mOnNotificationListener.onUpdate(mAudioIndex, PlaybackStatus.PAUSED);
            buildNotification(PlaybackStatus.PAUSED);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_DETACH);
            }

        } else if (playbackAction.getAction().equalsIgnoreCase(ACTION_PLAY)) {
            resumeMedia();
            mOnNotificationListener.onUpdate(mAudioIndex, PlaybackStatus.PLAYING);
            buildNotification(PlaybackStatus.PLAYING);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void updateMetaDataNotify(PlaybackStatus playbackStatus) {
        buildNotification(playbackStatus);
    }

    /**
     * Next song
     */
    public void skipToNext() {
        if (mAudioIndex == mAudioList.size() - 1) {
            //if last in playlist
            mAudioIndex = 0;
            mActiveAudio = mAudioList.get(mAudioIndex);
        } else {
            //get next in playlist
            mActiveAudio = mAudioList.get(++mAudioIndex);
        }

        //Update stored index
        new StorageUtil(getApplicationContext()).storeAudioIndex(mAudioIndex);
        stopMedia();
        //reset mediaPlayer
        mMediaPlayer.reset();
        initMediaPlayer();
    }

    /**
     * previous song
     */
    public void skipToPrevious() {
        if (mMediaPlayer.getCurrentPosition() <= TIME_LIMIT) {
            if (mAudioIndex == 0) {
                //if first in playlist
                //set index to the last of audioList
                mAudioIndex = mAudioList.size() - 1;
                mActiveAudio = mAudioList.get(mAudioIndex);
            } else {
                //get previous in playlist
                mActiveAudio = mAudioList.get(--mAudioIndex);
            }
        }
        if (mAudioIndex == 0) {
            //if first in playlist
            //set index to the last of audioList
            mAudioIndex = mAudioList.size() - 1;
            mActiveAudio = mAudioList.get(mAudioIndex);
        } else {
            //get previous in playlist
            mActiveAudio = mAudioList.get(--mAudioIndex);
        }

        //Update stored index
        new StorageUtil(getApplicationContext()).storeAudioIndex(mAudioIndex);
        stopMedia();
        //reset mediaPlayer
        mMediaPlayer.reset();
        initMediaPlayer();
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


    /**
     * set interface to listener
     *
     * @param listener
     */
    public void setOnNotificationListener(OnNotificationListener listener) {
        this.mOnNotificationListener = listener;
    }

    public interface OnNotificationListener {
        void onUpdate(int position, PlaybackStatus playbackStatus);
    }

    public class LocalBinder extends Binder {
        public MediaPlaybackService getService() {
            return MediaPlaybackService.this;
        }
    }
}
