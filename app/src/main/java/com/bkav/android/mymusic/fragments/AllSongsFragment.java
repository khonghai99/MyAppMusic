package com.bkav.android.mymusic.fragments;

import android.content.Context;
import android.os.Bundle;
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

import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.activities.MusicActivity;
import com.bkav.android.mymusic.adapters.CustomRecycleAdapter;
import com.bkav.android.mymusic.models.Song;

import java.util.ArrayList;

public class AllSongsFragment extends Fragment {
    public RelativeLayout layoutBottomAllSong;
    private ArrayList<Song> mListSong;
    private CustomRecycleAdapter mAdapter;
    private OnShowMediaListener mOnShowMediaListener;
    private RecyclerView mRecyclerView;
    private ImageView ivSongBottomAllSong;
    private TextView tvNameSongBottomAllSong;
    private TextView tvNameAuthorBottomAllSong;
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
        View view = inflater.inflate(R.layout.fragment_all_song, container, false);
        ivSongBottomAllSong = view.findViewById(R.id.ivSongBottomAllSong);
        tvNameSongBottomAllSong = view.findViewById(R.id.tvNameSongBottomAllSong);
        tvNameAuthorBottomAllSong = view.findViewById(R.id.tvNameAuthorBottomAllSong);
        ivPauseBottomAllSong = view.findViewById(R.id.ivPauseBottomAllSong);
        layoutBottomAllSong = view.findViewById(R.id.layoutBottomAllSong);

        mRecyclerView = view.findViewById(R.id.listSong);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        addSong();
        layoutBottomAllSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnShowMediaListener.showMediaFragment(mSong);
            }
        });
        mAdapter = new CustomRecycleAdapter(getContext(), mListSong, (MusicActivity) getActivity());
        mRecyclerView.setAdapter(mAdapter);
        return view;
    }

    public void addSong() {
        mListSong = new ArrayList<>();
        mListSong.add(new Song(1, "Chieu hom ay", "5:39",
                "Jaykii", R.drawable.chieu_hom_ay, R.raw.chieu_hom_ay, false));
        mListSong.add(new Song(2, "Co chang trai viet len cay",
                "5:34", "Jaykii", R.drawable.co_chang_trai_viet_len_cay,
                R.raw.co_chang_trai_viet_len_cay, true));
        mListSong.add(new Song(3, "Dung cho anh nua", "5:34",
                "Jaykii", R.drawable.dung_cho_anh_nua, R.raw.dung_cho_anh_nua, false));

    }

    public void setDataBottom(Song song, int position) {
        ivSongBottomAllSong.setImageResource(song.getmImageSong());
        tvNameSongBottomAllSong.setText(song.getmNameSong());
        tvNameAuthorBottomAllSong.setText(song.getmAuthorSong());
        for (int i = 0; i < mListSong.size(); i++) {
            mListSong.get(i).setPlay(false);
        }
        mListSong.get(position).setPlay(true);
        mSong = song;
        mAdapter.notifyDataSetChanged();
    }

    public interface OnShowMediaListener {
        void showMediaFragment(Song song);
    }
}
