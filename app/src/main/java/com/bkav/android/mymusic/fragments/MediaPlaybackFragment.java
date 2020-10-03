package com.bkav.android.mymusic.fragments;

import android.annotation.SuppressLint;
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
import com.bkav.android.mymusic.models.Song;
import com.bkav.android.mymusic.providers.FavoriteSongsProvider;
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
    private ArrayList<Song> mSongList;
    private MediaPlaybackService mMediaPlaybackService;
    private boolean mStateShuffle;
    private int mStateRepeat;

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
        StorageUtil storageUtil = new StorageUtil(getContext());
        return storageUtil.loadAllSongList().get(storageUtil.loadAudioIndex());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media_playback, container, false);
        init(view);
        runSeekBar(mSeekBar);
        setOnClick();

        if (getMediaPlayerService() != null) {
            mMediaPlaybackService = getMediaPlayerService();
            update();
        }
        Objects.requireNonNull(getMusicActivity()).listenServiceConnectedForMedia(new MusicActivity.OnServiceConnectedListenerForMedia() {
            @Override
            public void onConnect() {
                mMediaPlaybackService = getMediaPlayerService();
                update();
            }
        });

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStorageUtil = new StorageUtil(getContext());
        mSongList = mStorageUtil.loadAllSongList();

        getNewStateRepeatAndShuffle();
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



    private void getNewStateRepeatAndShuffle() {
        mStateRepeat = mStorageUtil.loadStateRepeat();
        mStateShuffle = mStorageUtil.loadStateShuffle();
    }

    public void setTitleMedia(Song song) {
        Song songActive = mStorageUtil.loadAllSongList().get(mStorageUtil.loadAudioIndex());
        String path = songActive.getPath();
        byte[] art = ImageSong.getByteImageSong(path);
        Glide.with(Objects.requireNonNull(getContext())).asBitmap()
                .error(R.mipmap.ic_music_not_picture)
                .load(art)
                .into(mImageTopMediaImageView);
        Glide.with(getContext()).asBitmap()
                .error(R.mipmap.ic_music_not_picture)
                .load(art)
                .into(mBackgroundMediaImageView);
        mArtistTopMediaTextView.setText(songActive.getArtist());
        mTitleTopMediaTextView.setText(songActive.getTitle());
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
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.image_background_media:
            case R.id.like:
                FavoriteSongsProvider favoriteSongsProvider = new FavoriteSongsProvider(getContext());

                break;
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
                    StorageUtil storageUtil = new StorageUtil(getContext());
                    if (mMediaPlaybackService.getMediaPlayer().getCurrentPosition() == 0) {
                        mMediaPlaybackService.playSong(storageUtil.loadAllSongList().get(storageUtil.loadAudioIndex()));
                    } else {
                        mMediaPlaybackService.playMedia();
                    }
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
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
            mEndTimeTextView.setText(mMediaPlaybackService.getActiveAudio().getDuration());
            mSeekBar.setMax(mMediaPlaybackService.getMediaPlayer().getDuration());
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSeekBar.setProgress(mMediaPlaybackService.getMediaPlayer().getCurrentPosition());
                    mStartTimeTextView.setText(simpleDateFormat.format(mMediaPlaybackService.getMediaPlayer().getCurrentPosition()));
                    handler.postDelayed(this,100);
                }
            },100);
        }
    }
}
