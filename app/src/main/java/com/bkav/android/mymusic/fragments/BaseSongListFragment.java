package com.bkav.android.mymusic.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
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

import static com.bkav.android.mymusic.services.MediaPlaybackService.BROADCAST_PLAY_NEW_AUDIO;

public class BaseSongListFragment extends Fragment implements View.OnClickListener {
    public RelativeLayout mBottomAllSongRelativeLayout;
    protected SongAdapter mSongAdapter;
    protected RecyclerView mRecyclerView;
    private PlaybackStatus playbackStatus;
    private ArrayList<Song> mSongList;
    private TextView mTitleBottomAllSongTextView;
    private TextView mArtistBottomAllSongTextView;
    private ImageView mImageBottomAllSongImageView;
    private ImageView mImagePauseBottomAllSongImageView;
    private Song mSong;
    private boolean mServiceBound = false;
    protected MediaPlaybackService mPlayerService;
    private ArrayList<Song> mAudioList;
    // Ràng buộc Client này với MusicPlayer
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            // Đã liên kết với LocalService, truyền IBinder
            MediaPlaybackService.LocalBinder binder = (MediaPlaybackService.LocalBinder) service;
            mPlayerService = binder.getService();
            mServiceBound = true;

            mPlayerService.setOnNotificationListener(new MediaPlaybackService.OnNotificationListener() {
                @Override
                public void onUpdate(int position, PlaybackStatus playbackStatus) {
                    getMusicActivity().updateFragment(position, playbackStatus);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            Log.i("TAG", "onAttach: " + context);
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnShowMediaListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("main", "Create fragment one");
        View view = inflater.inflate(R.layout.fragment_all_song, container, false);
        mImageBottomAllSongImageView = view.findViewById(R.id.ivSongBottomAllSong);
        mTitleBottomAllSongTextView = view.findViewById(R.id.tvTitleSongBottomAllSong);
        mArtistBottomAllSongTextView = view.findViewById(R.id.tvArtistBottomAllSong);
        mImagePauseBottomAllSongImageView = view.findViewById(R.id.ivPauseBottomAllSong);
        mBottomAllSongRelativeLayout = view.findViewById(R.id.layoutBottomAllSong);
        mTitleBottomAllSongTextView.setSelected(true);
        mSongAdapter = new SongAdapter(getContext());
        mRecyclerView = view.findViewById(R.id.rcListSong);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mImagePauseBottomAllSongImageView.setOnClickListener(this);
        mBottomAllSongRelativeLayout.setOnClickListener(this);

        mSongAdapter.clickListener(new SongAdapter.OnNewClickListener() {
            @Override
            public void onNewClick(ArrayList<Song> songList, int position) {
                mSongAdapter.updateSongList(songList);
                if (getMusicActivity().getStateUI()) {
                    AllSongsFragment allSongsFragment = (AllSongsFragment) getMusicActivity().getSupportFragmentManager().findFragmentById(R.id.frameLayoutAllSong);
                    if (allSongsFragment != null) {
                        allSongsFragment.setDataBottom(songList, position);
                        allSongsFragment.setVisible(position);
                        mAudioList = songList;
                        playAudio(position);
                    }
                } else {
                    MediaPlaybackFragment mediaPlaybackFragment = (MediaPlaybackFragment) getMusicActivity().getSupportFragmentManager().findFragmentById(R.id.frameLayoutTwo);
                    mediaPlaybackFragment.setTitle(songList.get(position));
                    mAudioList = songList;
                    playAudio(position);
                }
            }
        });
        return view;
    }

    /**
     * run player and set storage
     *
     * @param audioIndex the position of the track
     */
    public void playAudio(int audioIndex) {

        //Check is service is active
        StorageUtil storage = new StorageUtil(getContext().getApplicationContext());

        //Lưu vị trí âm thanh mới to SharedPreferences
        storage.storeAudioIndex(audioIndex);
        if (!mServiceBound) {

            //Lưu danh sách âm thanh to SharedPreferences
            storage.storeAudio(mAudioList);
            Intent playerIntent = new Intent(getContext(), MediaPlaybackService.class);
            getActivity().startService(playerIntent);

            //kết nối với service
            getActivity().bindService(playerIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        } else {

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(BROADCAST_PLAY_NEW_AUDIO);
            getActivity().sendBroadcast(broadcastIntent);
        }
    }
    /**
     * get service
     *
     * @return mPlayerService is Servive
     */
    public MediaPlaybackService getMediaPlayerService() {
        Log.d("HaiKH", "getMediaPlayerService: on");
        return mPlayerService;
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

//    /**
//     * get service from activity
//     *
//     * @return service
//     */
//    private MediaPlaybackService mediaPlaybackService() {
//        return getMediaPlayerService();
//    }
//
//    /**
//     * get state of player from service
//     *
//     * @return state of player
//     */
//    private PlaybackStatus getPlaybackStatus() {
//        return mPlayerService.isPlaying();
//    }

    /**
     * set data for layout bottom allSongFragment when click recycler view
     *
     * @param songs    arrayList of object Song
     * @param position playing song position
     */
    public void setDataBottom(ArrayList<Song> songs, int position) {
        mSong = songs.get(position);
        mSongList = songs;
        byte[] art = ImageSong.getByteImageSong(mSong.getPath());


        Glide.with(getContext()).asBitmap()
                .error(R.drawable.ic_music_not_picture)
                .load(art)
                .into(mImageBottomAllSongImageView);

        mTitleBottomAllSongTextView.setText(mSong.getTitle());
        mArtistBottomAllSongTextView.setText(mSong.getArtist());
        mImagePauseBottomAllSongImageView.setImageResource(R.drawable.ic_media_pause_light);

    }

    /**
     * set data for layout bottom allSongFragment when click from media fragment
     *
     * @param song           playing song
     * @param playbackStatus state of player
     */
    public void setDataBottomFromMedia(Song song, PlaybackStatus playbackStatus) {
        this.playbackStatus = playbackStatus;
        mSong = song;
        byte[] art = ImageSong.getByteImageSong(song.getPath());
        Glide.with(getContext()).asBitmap()
                .error(R.drawable.ic_music_not_picture)
                .load(art)
                .into(mImageBottomAllSongImageView);
        mTitleBottomAllSongTextView.setText(song.getTitle());
        mArtistBottomAllSongTextView.setText(song.getArtist());
        if (playbackStatus == PlaybackStatus.PLAYING) {
            mImagePauseBottomAllSongImageView.setImageResource(R.drawable.ic_media_pause_light);
        } else {
            mImagePauseBottomAllSongImageView.setImageResource(R.drawable.ic_media_play_light);
        }
    }

    /**
     * show layout bottom allSongFragment when click item recyclerView
     *
     * @param current set current song for adapter
     */
    public void setVisible(int current) {
        Log.i("HaiKH", "onCreateView: " + getView());
        mBottomAllSongRelativeLayout.setVisibility(View.VISIBLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivPauseBottomAllSong:
                if (PlaybackStatus.PLAYING == mPlayerService.isPlaying()) {
                    mPlayerService.pauseMedia();
                    mPlayerService.updateMetaDataNotify(PlaybackStatus.PAUSED);
                    mImagePauseBottomAllSongImageView.setImageResource(R.drawable.ic_media_play_light);
                } else if (PlaybackStatus.PAUSED == mPlayerService.isPlaying()) {
                    mPlayerService.playMedia();
                    mPlayerService.updateMetaDataNotify(PlaybackStatus.PLAYING);
                    mImagePauseBottomAllSongImageView.setImageResource(R.drawable.ic_media_pause_light);
                }

                break;

            case R.id.layoutBottomAllSong:
                showMediaFragment(mPlayerService.isPlaying());
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
        FragmentManager mFragmentManager = getMusicActivity().getSupportFragmentManager();
        getMusicActivity().mMediaPlaybackFragment = MediaPlaybackFragment.getInstancesMedia(mPlayerService.getActiveAudio(), playbackStatus);
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
        mTitleBottomAllSongTextView.setText(mSongList.get(position).getTitle());
        mArtistBottomAllSongTextView.setText(mSongList.get(position).getArtist());
        byte[] art = ImageSong.getByteImageSong(mSongList.get(position).getPath());
        Glide.with(getContext()).asBitmap()
                .error(R.drawable.ic_music_not_picture)
                .load(art)
                .into(mImageBottomAllSongImageView);
        if (playbackStatus == PlaybackStatus.PLAYING) {
            mImagePauseBottomAllSongImageView.setImageResource(R.drawable.ic_media_pause_light);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            mImagePauseBottomAllSongImageView.setImageResource(R.drawable.ic_media_play_light);
        }

    }
}
