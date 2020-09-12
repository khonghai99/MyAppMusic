package com.bkav.android.mymusic.fragments;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bkav.android.mymusic.ImageSong;
import com.bkav.android.mymusic.PlaybackStatus;
import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.SongLoader;
import com.bkav.android.mymusic.activities.MusicActivity;
import com.bkav.android.mymusic.adapters.SongAdapter;
import com.bkav.android.mymusic.models.Song;
import com.bkav.android.mymusic.services.MediaPlaybackService;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AllSongsFragment extends Fragment implements View.OnClickListener {

    public RelativeLayout mBottomAllSongRelativeLayout;
    private PlaybackStatus playbackStatus;
    private ArrayList<Song> mSongList;
    private SongAdapter mSongAdapter;
    private OnShowMediaListener mOnShowMediaListener;
    private RecyclerView mRecyclerView;
    private TextView mTitleBottomAllSongTextView;
    private TextView mArtistBottomAllSongTextView;
    private ImageView mImageBottomAllSongImageView;
    private ImageView mImagePauseBottomAllSongImageView;
    private Song mSong;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mOnShowMediaListener = (OnShowMediaListener) context;
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
        mRecyclerView = view.findViewById(R.id.listSong);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);

        mImagePauseBottomAllSongImageView.setOnClickListener(this);
        mBottomAllSongRelativeLayout.setOnClickListener(this);
        new LoadData().execute("");
        return view;
    }

    //get service from activity
    private MediaPlaybackService mediaPlaybackService() {
        return getMusicActivity().getMediaPlayerService();
    }

    //get activity
    private MusicActivity getMusicActivity() {
        if (getActivity() instanceof MusicActivity) {
            return (MusicActivity) getActivity();
        }
        return null;
    }

    private PlaybackStatus getPlaybackStatus() {
        return mediaPlaybackService().isPlaying();
    }

    public void setDataBottom(ArrayList<Song> songs, int position, PlaybackStatus playbackStatus) {
        this.playbackStatus = playbackStatus;
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

    public void setDataBottomFromMedia(Song song, PlaybackStatus playbackStatus) {
        this.playbackStatus = playbackStatus;
        mSong = song;
        byte[] art = ImageSong.getByteImageSong(song.getPath());
        if (art != null) {
            mImageBottomAllSongImageView.setImageBitmap(BitmapFactory.decodeByteArray(art, 0, art.length));
        } else {
            mImageBottomAllSongImageView.setImageResource(R.drawable.ic_music_not_picture);
        }
        mTitleBottomAllSongTextView.setText(song.getTitle());
        mArtistBottomAllSongTextView.setText(song.getArtist());
        if (playbackStatus == PlaybackStatus.PLAYING) {
            mImagePauseBottomAllSongImageView.setImageResource(R.drawable.ic_media_pause_light);
        } else {
            mImagePauseBottomAllSongImageView.setImageResource(R.drawable.ic_media_play_light);
        }
    }

    public void setVisible(int current) {
        mBottomAllSongRelativeLayout.setVisibility(View.VISIBLE);
        mSongAdapter.setCurrentSong(current);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivPauseBottomAllSong:
                if (PlaybackStatus.PLAYING == getPlaybackStatus()) {
                    mediaPlaybackService().pauseMedia();
                    mediaPlaybackService().updateMetaDataNotify(PlaybackStatus.PAUSED);
                    mImagePauseBottomAllSongImageView.setImageResource(R.drawable.ic_media_play_light);
                } else if (PlaybackStatus.PAUSED == getPlaybackStatus()) {
                    mediaPlaybackService().playMedia();
                    mediaPlaybackService().updateMetaDataNotify(PlaybackStatus.PLAYING);
                    mImagePauseBottomAllSongImageView.setImageResource(R.drawable.ic_media_pause_light);
                }

                break;

            case R.id.layoutBottomAllSong:
                mOnShowMediaListener.showMediaFragment(mSong, getPlaybackStatus());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
    }

    public void update(int index, boolean isClickPlay) {
        if (!isClickPlay) {
            mTitleBottomAllSongTextView.setText(mSongList.get(index).getTitle());
            mArtistBottomAllSongTextView.setText(mSongList.get(index).getArtist());
            byte[] art = ImageSong.getByteImageSong(mSongList.get(index).getPath());
            Glide.with(getContext()).asBitmap()
                    .error(R.drawable.ic_music_not_picture)
                    .load(art)
                    .into(mImageBottomAllSongImageView);
        }
        else {
            //change button
        }
    }

    public interface OnShowMediaListener {
        void showMediaFragment(Song song, PlaybackStatus playbackStatus);
    }

    public class LoadData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            if (getActivity() != null) {
                mSongAdapter = new SongAdapter(getContext(), new SongLoader().getAllSongDevice(getActivity()), (MusicActivity) getActivity());
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String s) {
            if (getActivity() != null) {
                mRecyclerView.setAdapter(mSongAdapter);
            }
        }
    }
}
