package com.bkav.android.mymusic.services;

import android.annotation.SuppressLint;
import android.app.Notification;
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

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

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
    public static final String CHANNEL_ID = "channel1";
    public static final String ACTION_PLAY = "com.bkav.musictest.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.bkav.musictest.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.bkav.musictest.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.bkav.musictest.ACTION_NEXT";
    public static final String ACTION_STOP = "com.bkav.musictest.ACTION_STOP";
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    //AudioPlayer notification ID
    private static final int NOTIFICATION_ID = 101;
    // Binder given to clients
    private final IBinder iBinder = new LocalBinder();
    //MediaSession
    private MediaSessionManager mediaSessionManager;
    private MediaSession mediaSession;
    private MediaController.TransportControls transportControls;
    //List of available Audio files
    private ArrayList<Song> audioList;
    private int audioIndex = -1;
    private Song activeAudio; //Đối tượng đang phát
    //Xử lý các cuộc gọi đến
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private MediaPlayer mediaPlayer;
    private NotificationManager mNotifyManager;
    //path to the audio file
    private String mediaFile;

    //Used to pause/resume MediaPlayer
    private int resumePosition;

    private AudioManager audioManager;

    //Becoming noisy
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY Tạm dừng khi có cuộc gọi
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };
    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {

            //Get the new media index form SharedPreferences đã lưu tại playAudio của Main
            audioIndex = new StorageUtil(getApplicationContext()).loadAudioIndex();
            if (audioIndex != -1 && audioIndex < audioList.size()) {
                //index is in a valid range
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }

            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Audio
            stopMedia();
            mediaPlayer.reset();
            initMediaPlayer();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
        }
    };

    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    private void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    private void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();

        //Set up MediaPlayer event listeners

        //được gọi khi đạt đến cuối nguồn phương tiện trong khi phát lại.
        mediaPlayer.setOnCompletionListener(this);

        //được gọi khi một lỗi đã xảy ra trong một hoạt động không đồng bộ
        mediaPlayer.setOnErrorListener(this);

        //được gọi khi nguồn phương tiện sẵn sàng để phát lại
        mediaPlayer.setOnPreparedListener(this);

        //được gọi khi trạng thái của bộ đệm của luồng mạng đã thay đổi.
        mediaPlayer.setOnBufferingUpdateListener(this);

        //được gọi khi một hoạt động tìm kiếm đã hoàn thành.
        mediaPlayer.setOnSeekCompleteListener(this);

        //được gọi khi có thông tin / cảnh báo.
        mediaPlayer.setOnInfoListener(this);

        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer.reset();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            // Set the data source to the mediaFile location
            mediaPlayer.setDataSource(activeAudio.getmPath());
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        mediaPlayer.prepareAsync();
    }

    //Hệ thống gọi phương thức này khi một hoạt động, yêu cầu dịch vụ được bắt đầu
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        try {
            //Load data from SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            audioList = storage.loadAudio();
            audioIndex = storage.loadAudioIndex();

            if (audioIndex != -1 && audioIndex < audioList.size()) {
                //index is in a valid range
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }
        } catch (NullPointerException e) {
            stopSelf();
        }

        //Nhận tiêu điểm
        if (requestAudioFocus() == false) {
            //Không nhận được tiêu điểm
            stopSelf();
        }

        if (mediaSessionManager == null) {
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void buildNotification(PlaybackStatus playbackStatus) {

        int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
        PendingIntent play_pauseAction = null;

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            //create the pause action
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            //create the play action
            play_pauseAction = playbackAction(0);
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_launcher_background); //replace with your own image

        mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID,
                    "Mascot Notification", NotificationManager
                    .IMPORTANCE_LOW);

            mNotifyManager.createNotificationChannel(notificationChannel);

            Notification notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setShowWhen(false)
                    // Set the Notification style
                    .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                    .addAction(notificationAction, "pause", play_pauseAction)
                    .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2))
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            // Attach our MediaSession token
                            //          .setMediaSession(MediaSessionCompat.Token.fromToken(mediaSession.getSessionToken()))
//                        // Show our playback controls in the compact notification view.
                            .setShowActionsInCompactView(0, 1, 2))
                    // Set the large and small icons
                    .setLargeIcon(largeIcon)
                    .setSmallIcon(android.R.drawable.stat_sys_headset)
                    // Set Notification content information
                    .setContentText(activeAudio.getmArtist())
                    .setContentTitle(activeAudio.getmTitle())
                    //.setContentInfo(activeAudio.getTitle())
                    // Add playback actions

                    .build();
            mNotifyManager.notify(NOTIFICATION_ID, notificationBuilder);
        }


    }


    private void register_playNewAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MusicActivity.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, filter);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onAudioFocusChange(int i) {
        //Được gọi khi tiêu điểm âm thanh của hệ thống được cập nhật.
        switch (i) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mediaPlayer == null) initMediaPlayer();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        //Yêu cầu tiêu điểm âm thanh để phát lại
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        //AUDIOFOCUS_REQUEST_GRANTED: Yêu cầu thay đổi tiêu điểm thành công.
        //Focus thành công
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        //Lấy tiêu điểm không thành công
    }

    //Bỏ tiêu điểm âm thanh khi phát xong
    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        //Được gọi khi quá trình phát lại nguồn phương tiện đã hoàn tất.
        stopMedia();
        //stop the service
        stopSelf();
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
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //nếu có ít nhất một cuộc gọi hoặc điện thoại đang đổ chuông
                    //tạm dừng MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
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
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();
        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
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
        if (mediaSessionManager != null) return; //mediaSessionManager exists

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        // Create a new MediaSession
        mediaSession = new MediaSession(getApplicationContext(), "AudioPlayer");
        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        //Set mediaSession's MetaData
        updateMetaData();

        // Attach Callback to receive MediaSession updates
        mediaSession.setCallback(new MediaSession.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();
                resumeMedia();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
                buildNotification(PlaybackStatus.PAUSED);
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

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updateMetaData() {
        Bitmap albumArt = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_no_image); //replace with medias albumArt
        // Update the current metadata
        mediaSession.setMetadata(new MediaMetadata.Builder()
                .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, activeAudio.getmArtist())
                .putString(MediaMetadata.METADATA_KEY_TITLE, activeAudio.getmTitle())
                .build());
    }

    private void skipToNext() {
        if (audioIndex == audioList.size() - 1) {
            //if last in playlist
            audioIndex = 0;
            activeAudio = audioList.get(audioIndex);
        } else {
            //get next in playlist
            activeAudio = audioList.get(++audioIndex);
        }

        //Update stored index
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
    }

    private void skipToPrevious() {
        if (audioIndex == 0) {
            //if first in playlist
            //set index to the last of audioList
            audioIndex = audioList.size() - 1;
            activeAudio = audioList.get(audioIndex);
        } else {
            //get previous in playlist
            activeAudio = audioList.get(--audioIndex);
        }

        //Update stored index
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
    }

    // Create a NotificationChannel
    public void createNotificationChannel() {
        mNotifyManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID,
                    "Mascot Notification", NotificationManager
                    .IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Notification from Mascot");
            mNotifyManager.createNotificationChannel(notificationChannel);
        }
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
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }

    public class LocalBinder extends Binder {
        public MediaPlaybackService getService() {
            return MediaPlaybackService.this;
        }
    }
}
