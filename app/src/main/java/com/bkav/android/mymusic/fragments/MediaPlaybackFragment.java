package com.bkav.android.mymusic.fragments;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.bkav.android.mymusic.providers.MusicDBHelper;
import com.bkav.android.mymusic.services.MediaPlaybackService;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;

public class MediaPlaybackFragment extends Fragment implements View.OnClickListener {
    private static final String KEY_SONG = "com.bkav.android.mymusic.fragments.SONG";
    private static final int NO_REPEAT = 0;
    private static final int REPEAT_ALL_LIST = 1;
    private static final int REPEAT_ONE_SONG = 2;
    private static final String KEY_PLAYBACK = "com.bkav.android.mymusic.fragments.PLAY_BACK";
    private OnReLoadList mOnReLoadList;
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
        return storageUtil.loadSongList().get(storageUtil.loadAudioIndex());
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
        mSongList = mStorageUtil.loadSongList();

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

    public void setUIMedia() {
        Song songActive = mStorageUtil.loadSongActive();
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
        if (checkFavorite(new StorageUtil(getContext()).loadAudioID())) {
            mLikeImageView.setImageResource(R.mipmap.ic_thumbs_up_selected);
        } else mLikeImageView.setImageResource(R.mipmap.ic_thumbs_up_default);
        updateSeekBar();
    }

    private boolean checkFavorite(int id) {
        String selection = MusicDBHelper.IS_FAVORITE + " = 2";
        Cursor cursor = getContext().getContentResolver().query(FavoriteSongsProvider.CONTENT_URI,
                new String[]{MusicDBHelper.ID_PROVIDER}, MusicDBHelper.ID_PROVIDER + " = " + id + " and " + selection,
                null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                return true;
            } else {
                return false;
            }
        } else return false;
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
                if (checkFavorite(new StorageUtil(getContext()).loadAudioID())) {
                    Toast.makeText(getContext(), "Added to favorite", Toast.LENGTH_SHORT).show();
                    FavoriteSongsProvider favoriteSongsProvider = new FavoriteSongsProvider(getContext());
                    favoriteSongsProvider.updateFavorite(new StorageUtil(getContext()).loadAudioID(), FavoriteSongsProvider.IS_NUMBER_NOT_FAVORITE);
                    mLikeImageView.setImageResource(R.mipmap.ic_thumbs_up_default);
                } else {
                    Toast.makeText(getContext(), "Removed from favorite", Toast.LENGTH_SHORT).show();
                    FavoriteSongsProvider favoriteSongsProvider = new FavoriteSongsProvider(getContext());
                    favoriteSongsProvider.updateFavorite(new StorageUtil(getContext()).loadAudioID(), FavoriteSongsProvider.IS_NUMBER_FAVORITE);
                    mLikeImageView.setImageResource(R.mipmap.ic_thumbs_up_selected);
                }
                mOnReLoadList.reLoad();
                break;
            case R.id.dislike:
                Toast.makeText(getContext(), "You click button dislike", Toast.LENGTH_SHORT).show();
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
                setBottomBaseSong(getSong(), MediaPlaybackStatus.PLAYING);
                mPauseImageView.setImageResource(R.drawable.ic_button_playing);
                setUIMedia();
                getMusicActivity().setAnimation();
                break;
            case R.id.previous:
                mMediaPlaybackService.skipToPrevious();
                mMediaPlaybackService.updateMetaDataNotify(MediaPlaybackStatus.PLAYING);
                setBottomBaseSong(getSong(), MediaPlaybackStatus.PLAYING);
                mPauseImageView.setImageResource(R.drawable.ic_button_playing);
                setUIMedia();
                getMusicActivity().setAnimation();
                break;
            case R.id.pause:
                if (mMediaPlaybackService.isPlayingState() == MediaPlaybackStatus.PLAYING) {
                    mMediaPlaybackService.pauseMedia();
                    mMediaPlaybackService.updateMetaDataNotify(MediaPlaybackStatus.PAUSED);
                    mPauseImageView.setImageResource(R.drawable.ic_button_pause);
                    setBottomBaseSong(getSong(), MediaPlaybackStatus.PAUSED);
                } else {
                    StorageUtil storageUtil = new StorageUtil(getContext());
                    if (mMediaPlaybackService.getMediaPlayer().getCurrentPosition() == 0) {
                        mMediaPlaybackService.playSong(storageUtil.loadSongList().get(storageUtil.loadAudioIndex()));
                    } else {
                        mMediaPlaybackService.playMedia();
                    }
                    mMediaPlaybackService.updateMetaDataNotify(MediaPlaybackStatus.PLAYING);
                    mPauseImageView.setImageResource(R.drawable.ic_button_playing);
                    setBottomBaseSong(getSong(), MediaPlaybackStatus.PLAYING);
                }
                break;
        }
    }

    public void update() {
        setUIMedia();
        if (mMediaPlaybackService.isPlayingState() == MediaPlaybackStatus.PLAYING) {
            mPauseImageView.setImageResource(R.drawable.ic_button_playing);
        } else if (mMediaPlaybackService.isPlayingState() == MediaPlaybackStatus.PAUSED) {
            mPauseImageView.setImageResource(R.drawable.ic_button_pause);
        }
    }

    public void setBottomBaseSong(Song song, MediaPlaybackStatus mediaPlaybackStatus) {
        BaseSongListFragment baseSongListFragment = (BaseSongListFragment) Objects.requireNonNull(getMusicActivity()).getSupportFragmentManager().findFragmentById(R.id.frame_layout_all_song);
        if (baseSongListFragment != null) {
            baseSongListFragment.setDataBottomFromMedia(song, mediaPlaybackStatus);
        }
    }

    private void updateSeekBar() {
        if (mMediaPlaybackService != null) {
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
            if (mMediaPlaybackService.getActiveAudio() == null) {
                mMediaPlaybackService.setSongActive(mStorageUtil.loadSongActive());
            }
            mEndTimeTextView.setText(mMediaPlaybackService.getActiveAudio().getDuration());
            mSeekBar.setMax(mMediaPlaybackService.getMediaPlayer().getDuration());
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSeekBar.setProgress(mMediaPlaybackService.getMediaPlayer().getCurrentPosition());
                    mStartTimeTextView.setText(simpleDateFormat.format(mMediaPlaybackService.getMediaPlayer().getCurrentPosition()));
                    handler.postDelayed(this, 100);
                }
            }, 100);
        }
    }

    public void actionReLoad(OnReLoadList onReLoadList) {
        this.mOnReLoadList = onReLoadList;
    }

    public interface OnReLoadList {
        void reLoad();
    }
}
