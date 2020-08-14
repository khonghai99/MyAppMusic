package com.bkav.android.mymusic.fragment;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.adapter.CustomRecycleAdapter;
import com.bkav.android.mymusic.model.Song;

import java.util.ArrayList;

public class AllSongsFragment extends Fragment {
    private ArrayList<Song> mListSong;
    private CustomRecycleAdapter mAdapter;
    private RecyclerView mRecyclerView;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_song, container, false);
        mRecyclerView = view.findViewById(R.id.listSong);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        addData();
        mAdapter = new CustomRecycleAdapter(getContext(), mListSong);
        mRecyclerView.setAdapter(mAdapter);
        return view;
    }

    public void addData() {
        mListSong = new ArrayList<>();
        mListSong.add(new Song(1, "Chieu hom ay", "5:39",
                "Jaykii", R.drawable.chieu_hom_ay, R.raw.chieu_hom_ay));
        mListSong.add(new Song(2, "Co chang trai viet len cay",
                "5:34", "Jaykii", R.drawable.co_chang_trai_viet_len_cay,
                R.raw.co_chang_trai_viet_len_cay));
        mListSong.add(new Song(3, "Dung cho anh nua", "5:34",
                "Jaykii", R.drawable.dung_cho_anh_nua, R.raw.dung_cho_anh_nua));

    }

}
