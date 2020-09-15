package com.bkav.android.mymusic.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
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
import com.bkav.android.mymusic.PlaybackStatus;
import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.activities.MusicActivity;
import com.bkav.android.mymusic.models.Song;
import com.bkav.android.mymusic.services.MediaPlaybackService;
import com.bumptech.glide.Glide;

public class MediaPlaybackFragment extends Fragment implements View.OnClickListener {
    private static final String KEY_PATH = "com.bkav.android.mymusic.fragments.PATH";
    private static final String KEY_TITLE = "com.bkav.android.mymusic.fragments.TITLE";
    private static final String KEY_ARTIST = "com.bkav.android.mymusic.fragments.ARTIST";
    private static final String KEY_PLAYBACK = "com.bkav.android.mymusic.fragments.PLAY_BACK";
    private final String LOG_INFO = "appMusic";
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
    private Song mSong;

    public static MediaPlaybackFragment getInstancesMedia(Song song, PlaybackStatus playbackStatus) {
        MediaPlaybackFragment fragment = new MediaPlaybackFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_PATH, song.getPath());
        bundle.putString(KEY_TITLE, song.getTitle());
        bundle.putString(KEY_ARTIST, song.getArtist());
        bundle.putSerializable(KEY_PLAYBACK, playbackStatus);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.i("HaiKH", "onSaveInstanceState: on");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        Log.i("HaiKH", "onViewStateRestored: on");
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            Log.i("TAG", "onAttach: on");
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnShowMediaListener");
        }
    }

    private MusicActivity getMusicActivity() {
        if (getActivity() instanceof MusicActivity) {
            return (MusicActivity) getActivity();
        }
        return null;
    }

    private MediaPlaybackService mediaPlaybackService() {
        return getMusicActivity().getMediaPlayerService();
    }

    private Song getSong() {
        return mediaPlaybackService().getActiveAudio();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i("HaiKH", "onCreateView: fragment media create view");
        View view = inflater.inflate(R.layout.fragment_media_playback, container, false);
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

        mLikeImageView.setOnClickListener(this);
        mDisLikeImageView.setOnClickListener(this);
        mNextImageView.setOnClickListener(this);
        mPreviousImageView.setOnClickListener(this);
        mPauseImageView.setOnClickListener(this);
        mBackgroundMediaImageView.setOnClickListener(this);
        mTitleTopMediaTextView.setSelected(true);
        if (getArguments() != null) {
            setTopMedia(getArguments());
        }
        return view;
    }

    public void setTopMedia(Bundle bundle) {
        String path = bundle.getString(KEY_PATH);
        PlaybackStatus playbackStatus = (PlaybackStatus) bundle.getSerializable(KEY_PLAYBACK);
        byte[] art = ImageSong.getByteImageSong(path);
        Glide.with(getContext()).asBitmap()
                .error(R.drawable.ic_music_not_picture)
                .load(art)
                .into(mImageTopMediaImageView);
        Glide.with(getContext()).asBitmap()
                .error(R.drawable.ic_music_not_picture)
                .load(art)
                .into(mBackgroundMediaImageView);
        mTitleTopMediaTextView.setText(bundle.getString(KEY_TITLE));
        mArtistTopMediaTextView.setText(bundle.getString(KEY_ARTIST));
        if (playbackStatus == PlaybackStatus.PAUSED) {
            mPauseImageView.setImageResource(R.drawable.ic_button_pause);
        } else if (playbackStatus == PlaybackStatus.PLAYING) {
            mPauseImageView.setImageResource(R.drawable.ic_button_playing);
        }
    }

    public void setTitle(Song song) {

        String path = song.getPath();
        byte[] art = ImageSong.getByteImageSong(path);
        Glide.with(getContext()).asBitmap()
                .error(R.drawable.ic_music_not_picture)
                .load(art)
                .into(mImageTopMediaImageView);
        Glide.with(getContext()).asBitmap()
                .error(R.drawable.ic_music_not_picture)
                .load(art)
                .into(mBackgroundMediaImageView);
        mArtistTopMediaTextView.setText(song.getArtist());
        mTitleTopMediaTextView.setText(song.getTitle());
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.ivBackgroundMedia:
                break;
            case R.id.ivLike:

            case R.id.ivDislike:

            case R.id.ivNext:
                mediaPlaybackService().skipToNext();
                mediaPlaybackService().updateMetaDataNotify(PlaybackStatus.PLAYING);
                setBottomAllSong(getSong(), PlaybackStatus.PLAYING);
                mPauseImageView.setImageResource(R.drawable.ic_button_playing);
                setTitle(getSong());
                break;
            case R.id.ivPrevious:
                mediaPlaybackService().skipToPrevious();
                mediaPlaybackService().updateMetaDataNotify(PlaybackStatus.PLAYING);
                setBottomAllSong(getSong(), PlaybackStatus.PLAYING);
                mPauseImageView.setImageResource(R.drawable.ic_button_playing);
                setTitle(getSong());
                break;
            case R.id.ivPause:
                Log.d("HaiKH", "onClick: pause click");
                if (mediaPlaybackService().isPlaying() == PlaybackStatus.PLAYING) {
                    mediaPlaybackService().pauseMedia();
                    mediaPlaybackService().updateMetaDataNotify(PlaybackStatus.PAUSED);
                    mPauseImageView.setImageResource(R.drawable.ic_button_pause);
                    setBottomAllSong(getSong(), PlaybackStatus.PAUSED);
                } else {
                    mediaPlaybackService().playMedia();
                    mediaPlaybackService().updateMetaDataNotify(PlaybackStatus.PLAYING);
                    mPauseImageView.setImageResource(R.drawable.ic_button_playing);
                    setBottomAllSong(getSong(), PlaybackStatus.PLAYING);
                }
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(LOG_INFO, "fragment media pause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(LOG_INFO, "fragment media stop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(LOG_INFO, "fragment media destroy view");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_INFO, "fragment media destroy");
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
        AllSongsFragment allSongsFragment = (AllSongsFragment) getMusicActivity().getSupportFragmentManager().findFragmentById(R.id.frameLayoutAllSong);
        allSongsFragment.setDataBottomFromMedia(song, playbackStatus);
    }
}
