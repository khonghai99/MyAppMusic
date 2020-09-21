package com.bkav.android.mymusic.fragments;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.bkav.android.mymusic.ImageSong;
import com.bkav.android.mymusic.PlaybackStatus;
import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.StorageUtil;
import com.bkav.android.mymusic.activities.MusicActivity;
import com.bkav.android.mymusic.models.Song;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

public class MediaPlaybackFragment extends BaseSongListFragment implements View.OnClickListener {
    private static final String KEY_SONG = "com.bkav.android.mymusic.fragments.SONG";
    private static final String KEY_PLAYBACK = "com.bkav.android.mymusic.fragments.PLAY_BACK";
    StorageUtil storageUtil;
    private ImageView mImageTopMediaImageView;
    private TextView mTitleTopMediaTextView;
    private TextView mArtistTopMediaTextView;
    private ImageView mPopupTopMediaImageView;
    private ImageView mListMusicImageView;
    private ImageView mBackgroundMediaImageView;
    private SeekBar mSeekBar;
    private ImageView mLikeImageView;
    private ImageView mDisLikeImageView;
    private ImageView mNextImageView;
    private ImageView mPreviousImageView;
    private ImageView mPauseImageView;
    private TextView mStartTimeTextView;
    private TextView mEndTimeTextView;
    private UpdateSeekBarThread mUpdateSeekBarThread;
    private ArrayList<Song> mSongList;
    private MediaPlayer mMediaPlayer;
    private Song mSong;

    public static MediaPlaybackFragment getInstancesMedia(Song song, PlaybackStatus playbackStatus) {
        MediaPlaybackFragment fragment = new MediaPlaybackFragment();
        Bundle bundle = new Bundle();
        Gson gson = new Gson();
        String jsonSong = gson.toJson(song);
        bundle.putString(KEY_SONG, jsonSong);
        bundle.putSerializable(KEY_PLAYBACK, playbackStatus);
        fragment.setArguments(bundle);
        return fragment;
    }

    private MusicActivity getMusicActivity() {
        if (getActivity() instanceof MusicActivity) {
            return (MusicActivity) getActivity();
        }
        return null;
    }

    private Song getSong() {
        return getMediaPlayerService().getActiveAudio();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i("HaiKH", "onCreateView: fragment media create view");
        View view = inflater.inflate(R.layout.fragment_media_playback, container, false);
        init(view);
        runSeekBar(mSeekBar);
        setOnClick();
        if (getArguments() != null) {
            setUIMedia(getArguments());
        }
        if (storageUtil != null) {
            setTitle(mSong);
        }
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i("HaiKH", "onCreate: on");
        super.onCreate(savedInstanceState);

        storageUtil = new StorageUtil(getContext());
        mSongList = storageUtil.loadAudio();
        mSong = storageUtil.loadAudio().get(storageUtil.loadAudioIndex());
        mUpdateSeekBarThread = new UpdateSeekBarThread();
        mUpdateSeekBarThread.start();

    }

    private void runSeekBar(SeekBar seekBar) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    getMediaPlayerService().getMediaPlayer().seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    private void setOnClick() {
        mLikeImageView.setOnClickListener(this);
        mDisLikeImageView.setOnClickListener(this);
        mNextImageView.setOnClickListener(this);
        mPreviousImageView.setOnClickListener(this);
        mPauseImageView.setOnClickListener(this);
        mBackgroundMediaImageView.setOnClickListener(this);
        mTitleTopMediaTextView.setSelected(true);
    }

    private void init(View view) {
        mImageTopMediaImageView = view.findViewById(R.id.ivSongTopMedia);
        mPopupTopMediaImageView = view.findViewById(R.id.ivPopupTopMedia);
        mBackgroundMediaImageView = view.findViewById(R.id.ivBackgroundMedia);

        //top media
        mArtistTopMediaTextView = view.findViewById(R.id.tvArtistSongTopMedia);
        mTitleTopMediaTextView = view.findViewById(R.id.tvTitleSongTopMedia);
        mListMusicImageView = view.findViewById(R.id.ivListMusic);

        //bottom media
        mSeekBar = view.findViewById(R.id.seekBarTimeSong);
        mLikeImageView = view.findViewById(R.id.ivLike);
        mDisLikeImageView = view.findViewById(R.id.ivDislike);
        mNextImageView = view.findViewById(R.id.ivNext);
        mPreviousImageView = view.findViewById(R.id.ivPrevious);
        mPauseImageView = view.findViewById(R.id.ivPause);
        mStartTimeTextView = view.findViewById(R.id.tvStartTimeSong);
        mEndTimeTextView = view.findViewById(R.id.tvEndTimeSong);
    }

    public void setUIMedia(Bundle bundle) {
        String jsonSong = bundle.getString(KEY_SONG);
        PlaybackStatus playbackStatus = (PlaybackStatus) bundle.getSerializable(KEY_PLAYBACK);
        Gson gson = new Gson();
        Type type = new TypeToken<Song>() {
        }.getType();
        Song song = gson.fromJson(jsonSong, type);

        setTitle(song);
        if (playbackStatus == PlaybackStatus.PAUSED) {
            mPauseImageView.setImageResource(R.drawable.ic_button_pause);
        } else if (playbackStatus == PlaybackStatus.PLAYING) {
            mPauseImageView.setImageResource(R.drawable.ic_button_playing);
        }
    }

    public void setTitle(Song song) {
        String path = song.getPath();
        byte[] art = ImageSong.getByteImageSong(path);
        Glide.with(Objects.requireNonNull(getContext())).asBitmap()
                .error(R.mipmap.ic_music_not_picture)
                .load(art)
                .into(mImageTopMediaImageView);
        Glide.with(getContext()).asBitmap()
                .error(R.mipmap.ic_music_not_picture)
                .load(art)
                .into(mBackgroundMediaImageView);
        mArtistTopMediaTextView.setText(song.getArtist());
        mTitleTopMediaTextView.setText(song.getTitle());
        updateSeekBar();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.ivBackgroundMedia:
            case R.id.ivLike:
            case R.id.ivDislike:
                break;
            case R.id.ivNext:
                getMediaPlayerService().skipToNext();
                getMediaPlayerService().updateMetaDataNotify(PlaybackStatus.PLAYING);
                setBottomAllSong(getSong(), PlaybackStatus.PLAYING);
                mPauseImageView.setImageResource(R.drawable.ic_button_playing);
                setTitle(getSong());
                break;
            case R.id.ivPrevious:
                getMediaPlayerService().skipToPrevious();
                getMediaPlayerService().updateMetaDataNotify(PlaybackStatus.PLAYING);
                setBottomAllSong(getSong(), PlaybackStatus.PLAYING);
                mPauseImageView.setImageResource(R.drawable.ic_button_playing);
                setTitle(getSong());
                break;
            case R.id.ivPause:
                Log.d("HaiKH", "onClick: pause click");
                if (getMediaPlayerService().isPlayingState() == PlaybackStatus.PLAYING) {
                    getMediaPlayerService().pauseMedia();
                    getMediaPlayerService().updateMetaDataNotify(PlaybackStatus.PAUSED);
                    mPauseImageView.setImageResource(R.drawable.ic_button_pause);
                    setBottomAllSong(getSong(), PlaybackStatus.PAUSED);
                } else {
                    getMediaPlayerService().playMedia();
                    getMediaPlayerService().updateMetaDataNotify(PlaybackStatus.PLAYING);
                    mPauseImageView.setImageResource(R.drawable.ic_button_playing);
                    setBottomAllSong(getSong(), PlaybackStatus.PLAYING);
                }
                break;
        }
    }

    public void update(PlaybackStatus playbackStatus) {
        setTitle(getSong());
        if (playbackStatus == PlaybackStatus.PLAYING) {
            mPauseImageView.setImageResource(R.drawable.ic_button_playing);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            mPauseImageView.setImageResource(R.drawable.ic_button_pause);
        }
    }

    public void setBottomAllSong(Song song, PlaybackStatus playbackStatus) {
        AllSongsFragment allSongsFragment = (AllSongsFragment) Objects.requireNonNull(getMusicActivity()).getSupportFragmentManager().findFragmentById(R.id.frameLayoutAllSong);
        if (allSongsFragment != null) {
            allSongsFragment.setDataBottomFromMedia(song, playbackStatus);
        }
    }

    private void updateSeekBar() {
        if (getMediaPlayerService() != null) {
            mUpdateSeekBarThread.updateSeekBar();
        }
    }

    private String formattedTime(long time) {
        int minutes = (int) (time / 1000 / 60);
        int seconds = (int) ((time / 1000) % 60);
        if (seconds < 10) {
            String seconds2 = "0" + seconds;
            return minutes + ":" + seconds2;
        }
        return minutes + ":" + seconds;
    }


    public class UpdateSeekBarThread extends Thread {
        private Handler handler;

        @Override
        public void run() {
            super.run();
            Looper.prepare();
            handler = new Handler();
            Looper.loop();
        }

        public void updateSeekBar() {
            if (getMediaPlayerService() != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (getMediaPlayerService().getAudioIndex() >= 0) {
                            while (getMediaPlayerService().getMediaPlayer() != null) {
                                try {
                                    long current = -1;
                                    try {
                                        current = getMediaPlayerService().getCurrentPosition();
                                    } catch (IllegalStateException e) {
                                        e.printStackTrace();
                                    }
                                    if (getActivity() != null && mSongList.size() > 0) {
                                        final long finalCurrent = current;
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mSeekBar.setMax((int) (getMediaPlayerService().getActiveAudio().getTimeEnd()));
                                                mSeekBar.setProgress((int) (finalCurrent));
                                                mStartTimeTextView.setText(formattedTime(finalCurrent));
                                                mEndTimeTextView.setText(getMediaPlayerService().getActiveAudio().getDuration());
                                            }
                                        });
                                    }
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
            }
        }

        public void exit() {
            handler.getLooper().quit();
        }
    }
}
