package com.bkav.android.mymusic.services;

import android.annotation.SuppressLint;
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
import android.media.MediaMetadata;
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
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.bkav.android.mymusic.ImageSong;
import com.bkav.android.mymusic.Interfaces.ListenerNotify;
import com.bkav.android.mymusic.PlaybackStatus;
import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.StorageUtil;
import com.bkav.android.mymusic.activities.MusicActivity;
import com.bkav.android.mymusic.models.Song;

import java.io.IOException;
import java.util.ArrayList;

public class MediaPlaybackService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener {

    public static final String ACTION_PLAY = "com.bkav.musictest.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.bkav.musictest.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.bkav.musictest.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.bkav.musictest.ACTION_NEXT";
    public static final String ACTION_STOP = "com.bkav.musictest.ACTION_STOP";
    private static final String AUDIO_PLAYER = "com.bkav.android.mymusic.services.AUDIO_PLAYER";
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    //AudioPlayer notification ID
    private static final int NOTIFICATION_ID = 101;
    // Binder given to clients
    private final IBinder mIBinder = new LocalBinder();
    private ListenerNotify mListenerNotify;
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_DETACH);
            }
        }
    };
    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("HaiKH", "onReceive: on");
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
            mMediaPlayer.reset();
            initMediaPlayer();
            updateMetaData();
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

    public PlaybackStatus isPlaying() {
        if (mMediaPlayer.isPlaying()) {
            return PlaybackStatus.PLAYING;
        } else {
            return PlaybackStatus.PAUSED;
        }
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

    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();

        //Set up MediaPlayer event listeners

        //được gọi khi đạt đến cuối nguồn phương tiện trong khi phát lại.
        mMediaPlayer.setOnCompletionListener(this);

        //được gọi khi một lỗi đã xảy ra trong một hoạt động không đồng bộ
        mMediaPlayer.setOnErrorListener(this);

        //được gọi khi nguồn phương tiện sẵn sàng để phát lại
        mMediaPlayer.setOnPreparedListener(this);

        //được gọi khi trạng thái của bộ đệm của luồng mạng đã thay đổi.
        mMediaPlayer.setOnBufferingUpdateListener(this);

        //được gọi khi một hoạt động tìm kiếm đã hoàn thành.
        mMediaPlayer.setOnSeekCompleteListener(this);

        //được gọi khi có thông tin / cảnh báo.
        mMediaPlayer.setOnInfoListener(this);

        //Reset so that the MediaPlayer is not pointing to another data source
        mMediaPlayer.reset();

        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            // Set the data source to the mediaFile location
            mMediaPlayer.setDataSource(mActiveAudio.getmPath());
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
        Log.d("HaiKH", "onStartCommand: on");
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

        if (mMediaSessionManager == null) {
            try {
                initMediaSession();
                initMediaPlayer();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }

            buildNotification(PlaybackStatus.PLAYING);
        }

        //Handle Intent action from MediaSession.TransportControls //Xử lý hành động có ý định từ MediaSession.TransportControls
        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    //set image when customContentView
    private void setImageNotify(RemoteViews remoteViews, int id) {
        byte[] art = ImageSong.getByteImageSong(mActiveAudio.getmPath());
        if (art != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            remoteViews.setImageViewBitmap(id, bitmap);

        } else {
            remoteViews.setImageViewResource(id, R.drawable.ic_music_not_picture);
        }
    }

    //set image when customContentView
    private void setTextNotify(RemoteViews remoteViews, int idTitle, int idArtist) {

        remoteViews.setTextViewText(idTitle, mActiveAudio.getmTitle());
        remoteViews.setTextViewText(idArtist, mActiveAudio.getmArtist());
    }

    private void buildNotification(PlaybackStatus playbackStatus) {
        mNotifyManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        int notificationAction = R.drawable.ic_button_playing;//needs to be initialized
        PendingIntent play_pauseAction = null;

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = R.drawable.ic_button_playing;
            //create the pause action
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = R.drawable.ic_button_pause;
            //create the play action
            play_pauseAction = playbackAction(0);
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_launcher_background); //replace with your own image

        RemoteViews smallNotify = new RemoteViews(getPackageName(), R.layout.small_notification);
        smallNotify.setOnClickPendingIntent(R.id.ivSmallPrevious, playbackAction(3));
        smallNotify.setOnClickPendingIntent(R.id.ivSmallNext, playbackAction(2));
        smallNotify.setOnClickPendingIntent(R.id.ivSmallPause, play_pauseAction);
        setImageNotify(smallNotify, R.id.ivSmallPicture);
        smallNotify.setOnClickPendingIntent(R.id.ivSmallPause, play_pauseAction);
        smallNotify.setImageViewResource(R.id.ivSmallPause, notificationAction);
        setImageNotify(smallNotify, R.id.ivSmallPicture);

        RemoteViews bigNotify = new RemoteViews(getPackageName(), R.layout.big_notification);
        bigNotify.setOnClickPendingIntent(R.id.ivBigPrevious, playbackAction(3));
        bigNotify.setOnClickPendingIntent(R.id.ivBigNext, playbackAction(2));
        bigNotify.setOnClickPendingIntent(R.id.ivBigPause, play_pauseAction);
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
                    .setSmallIcon(android.R.drawable.stat_sys_headset)
                    // Set Notification content information
                    .setContentText(mActiveAudio.getmArtist())
                    .setCustomContentView(smallNotify)
                    .setCustomBigContentView(bigNotify)
                    .setContentTitle(mActiveAudio.getmTitle());
            startForeground(NOTIFICATION_ID, notificationBuilder.build());
        }
    }

    private void register_playNewAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MusicActivity.BROADCAST_PLAY_NEW_AUDIO);
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

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        //Được gọi khi quá trình phát lại nguồn phương tiện đã hoàn tất.
        skipToNext();
        updateMetaData();
        buildNotification(PlaybackStatus.PLAYING);
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        //Được gọi khi có lỗi trong quá trình hoạt động không đồng bộ.
        switch (i) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + i1);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + i1);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + i1);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        //Được mời để giao tiếp một số thông tin.
        return false;
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

    //
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
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mMediaPlayer != null) {
                            pauseMedia();
                            mOngoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
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
        Log.i("Main1", "service create");
        // Thực hiện các thủ tục thiết lập một lần
        //
        // Quản lý các cuộc gọi đến trong khi phát lại.
        // Tạm dừng MediaPlayer khi có cuộc gọi đến,
        // Tiếp tục khi cúp máy.
        callStateListener();
        //ACTION_AUDIO_BECOMING_NOISY -- thay đổi đầu ra âm thanh khi có cuộc gọi đến -- BroadcastReceiver
        registerBecomingNoisyReceiver();
        //Listen for new Audio to play -- BroadcastReceiver
        register_playNewAudio();
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

        //Set mediaSession's MetaData
        updateMetaData();

        // Attach Callback to receive MediaSession updates
        mMediaSession.setCallback(new MediaSession.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();
                resumeMedia();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
                buildNotification(PlaybackStatus.PAUSED);
                stopForeground(STOP_FOREGROUND_DETACH);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                skipToNext();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevious();
                updateMetaData();
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void updateMetaDataNotify(PlaybackStatus playbackStatus) {
        updateMetaData();
        buildNotification(playbackStatus);
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updateMetaData() {
        Bitmap albumArt = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_music_not_picture); //replace with medias albumArt
        // Update the current metadata
        mMediaSession.setMetadata(new MediaMetadata.Builder()
                .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, mActiveAudio.getmArtist())
                .putString(MediaMetadata.METADATA_KEY_TITLE, mActiveAudio.getmTitle())
                .build());
    }

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
        Log.d("HaiKH", "skipToNext: " + mAudioIndex);
        stopMedia();
        //reset mediaPlayer
        mMediaPlayer.reset();
        initMediaPlayer();
    }

    public void skipToPrevious() {
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
        Log.d("HaiKH", "skipToNext: " + mAudioIndex);

        stopMedia();
        //reset mediaPlayer
        mMediaPlayer.reset();
        initMediaPlayer();
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, MediaPlaybackService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    //Hàm này tìm ra hành động phát lại nào được kích hoạt
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            mTransportControls.play();

            mListenerNotify.clickPlay();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            mTransportControls.pause();

            mListenerNotify.clickPause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            mTransportControls.skipToNext();

            mListenerNotify.clickNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            mTransportControls.skipToPrevious();

            mListenerNotify.clickPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            mTransportControls.stop();
        }
    }
    // nạp listener
    public void setOnListener(ListenerNotify listener){
        this.mListenerNotify = listener;
    }


    public class LocalBinder extends Binder {
        public MediaPlaybackService getService() {
            return MediaPlaybackService.this;
        }
    }
}
