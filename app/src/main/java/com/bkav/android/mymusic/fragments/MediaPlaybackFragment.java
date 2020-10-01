package com.bkav.android.mymusic.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.bkav.android.mymusic.ImageSong;
import com.bkav.android.mymusic.MediaPlaybackStatus;
import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.StorageUtil;
import com.bkav.android.mymusic.activities.MusicActivity;
import com.bkav.android.mymusic.models.Song;
import com.bkav.android.mymusic.services.MediaPlaybackService;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;

public class MediaPlaybackFragment extends Fragment implements View.OnClickListener {
    private static final String KEY_SONG = "com.bkav.android.mymusic.fragments.SONG";
    private static final int NO_REPEAT = 0;
    private static final int REPEAT_ALL_LIST = 1;
    private static final int REPEAT_ONE_SONG = 2;
    private static final String KEY_PLAYBACK = "com.bkav.android.mymusic.fragments.PLAY_BACK";
    private StorageUtil mStorageUtil;
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
    private ImageView mRepeatImageView;
    private ImageView mShuffleImageView;
    private TextView mStartTimeTextView;
    private TextView mEndTimeTextView;
    private UpdateSeekBarThread mUpdateSeekBarThread;
    private ArrayList<Song> mSongList;
    private MediaPlaybackService mMediaPlaybackService;
    private boolean mStateShuffle;
    private int mStateRepeat;

    public static MediaPlaybackFragment getInstancesMedia(Song song, MediaPlaybackStatus mediaPlaybackStatus) {
        MediaPlaybackFragment fragment = new MediaPlaybackFragment();
        Bundle bundle = new Bundle();
        Gson gson = new Gson();
        String jsonSong = gson.toJson(song);
        bundle.putString(KEY_SONG, jsonSong);
        bundle.putSerializable(KEY_PLAYBACK, mediaPlaybackStatus);
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * get service
     */
    public MediaPlaybackService getMediaPlayerService() {
        return (Objects.requireNonNull(getMusicActivity())).getPlayerService();
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
        View view = inflater.inflate(R.layout.fragment_media_playback, container, false);
        init(view);
        runSeekBar(mSeekBar);
        setOnClick();
        if (getArguments() != null) {
            setUIMedia(getArguments());
        }
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mMediaPlaybackService = getMediaPlayerService();
        Objects.requireNonNull(getMusicActivity()).listenServiceConnectedForMedia(new MusicActivity.OnServiceConnectedListenerForMedia() {
            @Override
            public void onConnect() {
                mMediaPlaybackService = getMediaPlayerService();

            }
        });

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStorageUtil = new StorageUtil(getContext());
        mSongList = mStorageUtil.loadAllSongList();

        getNewStateRepeatAndShuffle();

        mUpdateSeekBarThread = new UpdateSeekBarThread();
        mUpdateSeekBarThread.start();

    }

    private void runSeekBar(SeekBar seekBar) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    mMediaPlaybackService.getMediaPlayer().seekTo(i);
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
        mListMusicImageView.setOnClickListener(this);
        mRepeatImageView.setOnClickListener(this);
        mShuffleImageView.setOnClickListener(this);
        mTitleTopMediaTextView.setSelected(true);
    }

    private void init(View view) {
        mImageTopMediaImageView = view.findViewById(R.id.image_top_media);
        mPopupTopMediaImageView = view.findViewById(R.id.popup_top_media);
        mBackgroundMediaImageView = view.findViewById(R.id.image_background_media);

        //top media
        mArtistTopMediaTextView = view.findViewById(R.id.artist_top_media);
        mTitleTopMediaTextView = view.findViewById(R.id.title_top_media);
        mListMusicImageView = view.findViewById(R.id.back_list_music);

        //bottom media
        mSeekBar = view.findViewById(R.id.seek_bar);
        mLikeImageView = view.findViewById(R.id.like);
        mDisLikeImageView = view.findViewById(R.id.dislike);
        mNextImageView = view.findViewById(R.id.next);
        mPreviousImageView = view.findViewById(R.id.previous);
        mPauseImageView = view.findViewById(R.id.pause);
        mStartTimeTextView = view.findViewById(R.id.start_time_seek_bar);
        mEndTimeTextView = view.findViewById(R.id.end_time_seek_bar);
        mRepeatImageView = view.findViewById(R.id.repeat);
        mShuffleImageView = view.findViewById(R.id.shuffle);
    }

    public void setUIMedia(Bundle bundle) {
        String jsonSong = bundle.getString(KEY_SONG);
        MediaPlaybackStatus mediaPlaybackStatus = (MediaPlaybackStatus) bundle.getSerializable(KEY_PLAYBACK);
        Gson gson = new Gson();
        Type type = new TypeToken<Song>() {
        }.getType();
        Song song = gson.fromJson(jsonSong, type);

        setTitleMedia(song);
        if (mediaPlaybackStatus == MediaPlaybackStatus.PAUSED) {
            mPauseImageView.setImageResource(R.drawable.ic_button_pause);
        } else if (mediaPlaybackStatus == MediaPlaybackStatus.PLAYING) {
            mPauseImageView.setImageResource(R.drawable.ic_button_playing);
        }

    }

    private void getNewStateRepeatAndShuffle() {
        mStateRepeat = mStorageUtil.loadStateRepeat();
        mStateShuffle = mStorageUtil.loadStateShuffle();
    }

    public void setTitleMedia(Song song) {
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
        getNewStateRepeatAndShuffle();
        if (mStateRepeat == NO_REPEAT) {
            mRepeatImageView.setImageResource(R.mipmap.ic_repeat_white);
        } else if (mStateRepeat == REPEAT_ALL_LIST) {
            mRepeatImageView.setImageResource(R.mipmap.ic_repeat_dark_selected);
        } else if (mStateRepeat == REPEAT_ONE_SONG) {
            mRepeatImageView.setImageResource(R.mipmap.ic_repeat_one_song_dark);
        }
        if (mStateShuffle) {
            mShuffleImageView.setImageResource(R.mipmap.ic_play_shuffle_orange);
        } else {
            mShuffleImageView.setImageResource(R.mipmap.ic_shuffle_white);
        }
        updateSeekBar();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUpdateSeekBarThread.exit();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.image_background_media:
            case R.id.like:
            case R.id.dislike:
                break;
            case R.id.repeat:
                mStateRepeat = mStorageUtil.loadStateRepeat();
                switch (mStateRepeat) {
                    case NO_REPEAT:
                        mRepeatImageView.setImageResource(R.mipmap.ic_repeat_dark_selected);
                        mStorageUtil.storeRepeat(REPEAT_ALL_LIST);
                        break;
                    case REPEAT_ALL_LIST:
                        mRepeatImageView.setImageResource(R.mipmap.ic_repeat_one_song_dark);
                        mStorageUtil.storeRepeat(REPEAT_ONE_SONG);
                        break;
                    case REPEAT_ONE_SONG:
                        mRepeatImageView.setImageResource(R.mipmap.ic_repeat_white);
                        mStorageUtil.storeRepeat(NO_REPEAT);
                        break;
                }
                break;
            case R.id.shuffle:
                mStateShuffle = mStorageUtil.loadStateShuffle();
                if (mStateShuffle) {
                    mShuffleImageView.setImageResource(R.mipmap.ic_shuffle_white);
                    mStorageUtil.storeShuffle(false);
                } else {
                    mShuffleImageView.setImageResource(R.mipmap.ic_play_shuffle_orange);
                    mStorageUtil.storeShuffle(true);
                }
                break;
            case R.id.back_list_music:
                if (getFragmentManager() != null) {
                    getFragmentManager().popBackStack();
                }
                break;
            case R.id.next:
                mMediaPlaybackService.skipToNext();
                mMediaPlaybackService.updateMetaDataNotify(MediaPlaybackStatus.PLAYING);
                setBottomAllSong(getSong(), MediaPlaybackStatus.PLAYING);
                mPauseImageView.setImageResource(R.drawable.ic_button_playing);
                setTitleMedia(getSong());
                break;
            case R.id.previous:
                mMediaPlaybackService.skipToPrevious();
                mMediaPlaybackService.updateMetaDataNotify(MediaPlaybackStatus.PLAYING);
                setBottomAllSong(getSong(), MediaPlaybackStatus.PLAYING);
                mPauseImageView.setImageResource(R.drawable.ic_button_playing);
                setTitleMedia(getSong());
                break;
            case R.id.pause:
                if (mMediaPlaybackService.isPlayingState() == MediaPlaybackStatus.PLAYING) {
                    mMediaPlaybackService.pauseMedia();
                    mMediaPlaybackService.updateMetaDataNotify(MediaPlaybackStatus.PAUSED);
                    mPauseImageView.setImageResource(R.drawable.ic_button_pause);
                    setBottomAllSong(getSong(), MediaPlaybackStatus.PAUSED);
                } else {
                    mMediaPlaybackService.playMedia();
                    mMediaPlaybackService.updateMetaDataNotify(MediaPlaybackStatus.PLAYING);
                    mPauseImageView.setImageResource(R.drawable.ic_button_playing);
                    setBottomAllSong(getSong(), MediaPlaybackStatus.PLAYING);
                }
                break;
        }
    }

    public void update() {
        setTitleMedia(getSong());
        if (mMediaPlaybackService.isPlayingState() == MediaPlaybackStatus.PLAYING) {
            mPauseImageView.setImageResource(R.drawable.ic_button_playing);
        } else if (mMediaPlaybackService.isPlayingState() == MediaPlaybackStatus.PAUSED) {
            mPauseImageView.setImageResource(R.drawable.ic_button_pause);
        }
    }

    public void setBottomAllSong(Song song, MediaPlaybackStatus mediaPlaybackStatus) {
        AllSongsFragment allSongsFragment = (AllSongsFragment) Objects.requireNonNull(getMusicActivity()).getSupportFragmentManager().findFragmentById(R.id.frame_layout_all_song);
        if (allSongsFragment != null) {
            allSongsFragment.setDataBottomFromMedia(song, mediaPlaybackStatus);
        }
    }

    private void updateSeekBar() {
        if (mMediaPlaybackService != null) {
            mUpdateSeekBarThread.updateSeekBar();
        }

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
            @SuppressLint("SimpleDateFormat") final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
            mEndTimeTextView.setText(mMediaPlaybackService.getActiveAudio().getDuration());
            mSeekBar.setMax(mMediaPlaybackService.getMediaPlayer().getDuration());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (mMediaPlaybackService.getAudioIndex() >= 0) {
                        while (mMediaPlaybackService.getMediaPlayer() != null) {
                            try {
                                if (getActivity() != null && mSongList.size() > 0) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mSeekBar.setProgress(mMediaPlaybackService.getMediaPlayer().getCurrentPosition());
                                            mStartTimeTextView.setText(simpleDateFormat.format(mMediaPlaybackService.getMediaPlayer().getCurrentPosition()));
                                        }
                                    });
                                }
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }

        public void exit() {
            handler.getLooper().quit();
        }
    }
}
