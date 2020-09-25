package com.bkav.android.mymusic.fragments;

import android.content.Context;
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
import androidx.fragment.app.Fragment;

import com.bkav.android.mymusic.ImageSong;
import com.bkav.android.mymusic.MediaPlaybackStatus;
import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.StorageUtil;
import com.bkav.android.mymusic.activities.MusicActivity;
import com.bkav.android.mymusic.adapters.SongAdapter;
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
    private SongAdapter mSongAdapter;
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
        return (getMusicActivity()).getPlayerService();
    }

    private MusicActivity getMusicActivity() {
        if (getActivity() instanceof MusicActivity) {
            return (MusicActivity) getActivity();
        }
        return null;
    }

    public void setAdapter(SongAdapter adapter) {
        this.mSongAdapter = adapter;
    }

    private Song getSong() {
        return getMediaPlayerService().getActiveAudio();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media_playback, container, false);
        init(view);
        Log.i("HaiKH", "onCreateView: media on");
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

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("HaiKH", "onCreate media: on");
        mStorageUtil = new StorageUtil(getContext());
        mSongList = mStorageUtil.loadAudio();

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
        mRepeatImageView = view.findViewById(R.id.ivRepeat);
        mShuffleImageView = view.findViewById(R.id.ivShuffle);
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
            case R.id.ivBackgroundMedia:
            case R.id.ivLike:
            case R.id.ivDislike:
                break;
            case R.id.ivRepeat:
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
            case R.id.ivShuffle:
                mStateShuffle = mStorageUtil.loadStateShuffle();
                if (mStateShuffle) {
                    mShuffleImageView.setImageResource(R.mipmap.ic_shuffle_white);
                    mStorageUtil.storeShuffle(false);
                } else {
                    mShuffleImageView.setImageResource(R.mipmap.ic_play_shuffle_orange);
                    mStorageUtil.storeShuffle(true);
                }
                break;
            case R.id.ivListMusic:
                getFragmentManager().popBackStack();
                break;
            case R.id.ivNext:
                mMediaPlaybackService.skipToNext();
                mMediaPlaybackService.updateMetaDataNotify(MediaPlaybackStatus.PLAYING);
                setBottomAllSong(getSong(), MediaPlaybackStatus.PLAYING);
                mPauseImageView.setImageResource(R.drawable.ic_button_playing);
                setTitleMedia(getSong());
                Log.i("HaiKH", "onClick: " + mSongAdapter);
//                mSongAdapter.updateSongList(mSongList, MediaPlaybackStatus.PLAYING);
                break;
            case R.id.ivPrevious:
                mMediaPlaybackService.skipToPrevious();
                mMediaPlaybackService.updateMetaDataNotify(MediaPlaybackStatus.PLAYING);
                setBottomAllSong(getSong(), MediaPlaybackStatus.PLAYING);
                mPauseImageView.setImageResource(R.drawable.ic_button_playing);
                setTitleMedia(getSong());
//                mSongAdapter.updateSongList(mSongList, MediaPlaybackStatus.PLAYING);
                break;
            case R.id.ivPause:
                if (mMediaPlaybackService.isPlayingState() == MediaPlaybackStatus.PLAYING) {
                    mMediaPlaybackService.pauseMedia();
                    mMediaPlaybackService.updateMetaDataNotify(MediaPlaybackStatus.PAUSED);
                    mPauseImageView.setImageResource(R.drawable.ic_button_pause);
                    setBottomAllSong(getSong(), MediaPlaybackStatus.PAUSED);
//                    mSongAdapter.updateSongList(mSongList, MediaPlaybackStatus.PAUSED);
                } else {
                    mMediaPlaybackService.playMedia();
                    mMediaPlaybackService.updateMetaDataNotify(MediaPlaybackStatus.PLAYING);
                    mPauseImageView.setImageResource(R.drawable.ic_button_playing);
                    setBottomAllSong(getSong(), MediaPlaybackStatus.PLAYING);
//                    mSongAdapter.updateSongList(mSongList, MediaPlaybackStatus.PLAYING);
                }
                break;
        }
    }

    public void update(MediaPlaybackStatus mediaPlaybackStatus) {
        setTitleMedia(getSong());
        if (mediaPlaybackStatus == MediaPlaybackStatus.PLAYING) {
            mPauseImageView.setImageResource(R.drawable.ic_button_playing);
        } else if (mediaPlaybackStatus == MediaPlaybackStatus.PAUSED) {
            mPauseImageView.setImageResource(R.drawable.ic_button_pause);
        }
    }

    public void setBottomAllSong(Song song, MediaPlaybackStatus mediaPlaybackStatus) {
        AllSongsFragment allSongsFragment = (AllSongsFragment) Objects.requireNonNull(getMusicActivity()).getSupportFragmentManager().findFragmentById(R.id.frameLayoutAllSong);
        if (allSongsFragment != null) {
            allSongsFragment.setDataBottomFromMedia(song, mediaPlaybackStatus);
        }
    }

    private void updateSeekBar() {
        if (mMediaPlaybackService != null) {
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
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
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
