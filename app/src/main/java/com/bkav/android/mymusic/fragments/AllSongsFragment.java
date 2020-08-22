package com.bkav.android.mymusic.fragments;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bkav.android.mymusic.ImageSong;
import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.SongLoader;
import com.bkav.android.mymusic.activities.MusicActivity;
import com.bkav.android.mymusic.adapters.SongAdapter;
import com.bkav.android.mymusic.models.Song;

import java.util.ArrayList;

public class AllSongsFragment extends Fragment {
    public RelativeLayout layoutBottomAllSong;
    private ArrayList<Song> mListSong;
    private SongAdapter mSongAdapter;
    private OnShowMediaListener mOnShowMediaListener;

    private RecyclerView mRecyclerView;


    private TextView tvTitleSongBottomAllSong;
    private TextView tvArtistBottomAllSong;

    private ImageView ivSongBottomAllSong;
    private ImageView ivPauseBottomAllSong;

    private Song mSong;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mOnShowMediaListener = (OnShowMediaListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnShowMediaListener");
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("main","Create fragment one");
        View view = inflater.inflate(R.layout.fragment_all_song, container, false);
        ivSongBottomAllSong = view.findViewById(R.id.ivSongBottomAllSong);
        tvTitleSongBottomAllSong = view.findViewById(R.id.tvTitleSongBottomAllSong);
        tvArtistBottomAllSong = view.findViewById(R.id.tvArtistBottomAllSong);
        ivPauseBottomAllSong = view.findViewById(R.id.ivPauseBottomAllSong);
        layoutBottomAllSong = view.findViewById(R.id.layoutBottomAllSong);

        mRecyclerView = view.findViewById(R.id.listSong);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);

        layoutBottomAllSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnShowMediaListener.showMediaFragment(mSong);
            }
        });
        new LoadData().execute("");
        return view;
    }

    public void setDataBottom(Song song, int position) {
        mSong = song;
        byte[] art = ImageSong.getByteImageSong(song.getmPath());
        if (art != null) {
            ivSongBottomAllSong.setImageBitmap(BitmapFactory.decodeByteArray(art, 0, art.length));
        } else {
            ivSongBottomAllSong.setImageResource(R.drawable.ic_no_image);
        }
        tvTitleSongBottomAllSong.setText(song.getmTitle());
        tvArtistBottomAllSong.setText(song.getmArtist());

    }

    public void setVisible() {
        layoutBottomAllSong.setVisibility(View.VISIBLE);

    }

    public interface OnShowMediaListener {
        void showMediaFragment(Song song);
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
