package com.bkav.android.mymusic.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bkav.android.mymusic.ImageSong;
import com.bkav.android.mymusic.PlaybackStatus;
import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.StorageUtil;
import com.bkav.android.mymusic.activities.MusicActivity;
import com.bkav.android.mymusic.adapters.SongAdapter;
import com.bkav.android.mymusic.models.Song;
import com.bkav.android.mymusic.services.MediaPlaybackService;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Objects;

import static com.bkav.android.mymusic.services.MediaPlaybackService.BROADCAST_PLAY_NEW_AUDIO;

public class BaseSongListFragment extends Fragment implements View.OnClickListener, SongAdapter.OnNewClickListener {
    public RelativeLayout mBottomAllSongRelativeLayout;
    protected SongAdapter mSongAdapter;
    protected RecyclerView mRecyclerView;
    private ArrayList<Song> mSongList;
    private TextView mTitleBottomAllSongTextView;
    private TextView mArtistBottomAllSongTextView;
    private ImageView mImageBottomAllSongImageView;
    private ImageView mImagePauseBottomAllSongImageView;

    private Song mSong;
    private ArrayList<Song> mAudioList;
    private StorageUtil storage;

    @Override
    public void onResume() {
        super.onResume();
        if (getMediaPlayerService()!=null)
            setVisible();
        Log.i("HaiKH", "onResume: all song on");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("HaiKH", "onCreateView: all song on");
        View view = inflater.inflate(R.layout.fragment_all_song, container, false);
        init(view);
        storage = new StorageUtil(Objects.requireNonNull(getContext()).getApplicationContext());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mImagePauseBottomAllSongImageView.setOnClickListener(this);
        mBottomAllSongRelativeLayout.setOnClickListener(this);

        return view;
    }

    private void init(View view) {
        mImageBottomAllSongImageView = view.findViewById(R.id.ivSongBottomAllSong);
        mTitleBottomAllSongTextView = view.findViewById(R.id.tvTitleSongBottomAllSong);
        mArtistBottomAllSongTextView = view.findViewById(R.id.tvArtistBottomAllSong);
        mImagePauseBottomAllSongImageView = view.findViewById(R.id.ivPauseBottomAllSong);
        mBottomAllSongRelativeLayout = view.findViewById(R.id.layoutBottomAllSong);
        mTitleBottomAllSongTextView.setSelected(true);
        mRecyclerView = view.findViewById(R.id.rcListSong);
    }


    /**
     * run player and set storage
     *
     * @param audioIndex the position of the track
     */
    public void playAudio(int audioIndex) {
        //Lưu vị trí âm thanh mới to SharedPreferences
        storage.storeAudioIndex(audioIndex);
        //Check is service is active
        if (!Objects.requireNonNull(getMusicActivity()).getServiceBound()) {
            //Lưu danh sách âm thanh to SharedPreferences
            storage.storeAudio(mAudioList);

        } else {
            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(BROADCAST_PLAY_NEW_AUDIO);
            Objects.requireNonNull(getActivity()).sendBroadcast(broadcastIntent);
        }
    }



    @Override
    public void onNewClick(ArrayList<Song> songList, int position) {
        mSongAdapter.updateSongList(songList);
        mSongList = storage.loadAudio();
        mSong = storage.loadAudio().get(position);
        if (Objects.requireNonNull(getMusicActivity()).getStateUI()) {
            setDataBottom();
            setVisible();

        } else {
            MediaPlaybackFragment mediaPlaybackFragment = (MediaPlaybackFragment) getMusicActivity().getSupportFragmentManager().findFragmentById(R.id.frameLayoutLandMedia);
            if (mediaPlaybackFragment != null) {
                mediaPlaybackFragment.setTitle(songList.get(position));
            }
        }
        mAudioList = songList;
        playAudio(position);
    }

    /**
     * set data for layout bottom allSongFragment when click recycler view
     */
    public void setDataBottom() {
        byte[] art = ImageSong.getByteImageSong(mSong.getPath());
        Glide.with(Objects.requireNonNull(getContext())).asBitmap()
                .error(R.mipmap.ic_music_not_picture)
                .load(art)
                .into(mImageBottomAllSongImageView);

        mTitleBottomAllSongTextView.setText(mSong.getTitle());
        mArtistBottomAllSongTextView.setText(mSong.getArtist());
        mImagePauseBottomAllSongImageView.setImageResource(R.mipmap.ic_media_pause_light);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * set data for layout bottom allSongFragment when click from media fragment
     *
     * @param song           playing song
     * @param playbackStatus state of player
     */
    public void setDataBottomFromMedia(Song song, PlaybackStatus playbackStatus) {
        mSong = song;
        byte[] art = ImageSong.getByteImageSong(song.getPath());
        Glide.with(Objects.requireNonNull(getContext())).asBitmap()
                .error(R.mipmap.ic_music_not_picture)
                .load(art)
                .into(mImageBottomAllSongImageView);
        mTitleBottomAllSongTextView.setText(song.getTitle());
        mArtistBottomAllSongTextView.setText(song.getArtist());
        if (playbackStatus == PlaybackStatus.PLAYING) {
            mImagePauseBottomAllSongImageView.setImageResource(R.mipmap.ic_media_pause_light);
        } else {
            mImagePauseBottomAllSongImageView.setImageResource(R.mipmap.ic_media_play_light);
        }
    }

    /**
     * show layout bottom allSongFragment when click item recyclerView
     */
    public void setVisible() {
        mBottomAllSongRelativeLayout.setVisibility(View.VISIBLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivPauseBottomAllSong:
                if (PlaybackStatus.PLAYING == getMediaPlayerService().isPlayingState()) {
                    getMediaPlayerService().pauseMedia();
                    getMediaPlayerService().updateMetaDataNotify(PlaybackStatus.PAUSED);
                    mImagePauseBottomAllSongImageView.setImageResource(R.mipmap.ic_media_play_light);
                } else if (PlaybackStatus.PAUSED == getMediaPlayerService().isPlayingState()) {
                    getMediaPlayerService().playMedia();
                    getMediaPlayerService().updateMetaDataNotify(PlaybackStatus.PLAYING);
                    mImagePauseBottomAllSongImageView.setImageResource(R.mipmap.ic_media_pause_light);
                }
                break;
            case R.id.layoutBottomAllSong:
                showMediaFragment(getMediaPlayerService().isPlayingState());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
    }

    /**
     * click bottom layout all song show media fragment
     *
     * @param playbackStatus state of player
     */
    public void showMediaFragment(PlaybackStatus playbackStatus) {
        FragmentManager mFragmentManager = Objects.requireNonNull(getMusicActivity()).getSupportFragmentManager();
        getMusicActivity().mMediaPlaybackFragment = MediaPlaybackFragment.getInstancesMedia(getMediaPlayerService().getActiveAudio(), playbackStatus);
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayoutMedia, getMusicActivity().mMediaPlaybackFragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    /**
     * update allSongFragment when click notification
     *
     * @param position       playing song position
     * @param playbackStatus state of player
     */
    public void update(int position, PlaybackStatus playbackStatus) {
        if (Objects.requireNonNull(getMusicActivity()).getStateUI()) {
            mTitleBottomAllSongTextView.setText(mSongList.get(position).getTitle());
            mArtistBottomAllSongTextView.setText(mSongList.get(position).getArtist());
            byte[] art = ImageSong.getByteImageSong(mSongList.get(position).getPath());
            Glide.with(Objects.requireNonNull(getContext())).asBitmap()
                    .error(R.mipmap.ic_music_not_picture)
                    .load(art)
                    .into(mImageBottomAllSongImageView);
            if (playbackStatus == PlaybackStatus.PLAYING) {
                mImagePauseBottomAllSongImageView.setImageResource(R.mipmap.ic_media_pause_light);
            } else if (playbackStatus == PlaybackStatus.PAUSED) {
                mImagePauseBottomAllSongImageView.setImageResource(R.mipmap.ic_media_play_light);
            }
        }
    }

    /**
     * get service
     */
    public MediaPlaybackService getMediaPlayerService() {
        return (getMusicActivity()).getPlayerService();
    }

    /**
     * get activity from MusicActivity
     *
     * @return MusicActivity if getActivity() instanceof MusicActivity
     */
    private MusicActivity getMusicActivity() {
        if (getActivity() instanceof MusicActivity) {
            return (MusicActivity) getActivity();
        }
        return null;
    }
}
