package com.bkav.android.mymusic.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.bkav.android.mymusic.SongLoader;
import com.bkav.android.mymusic.activities.MusicActivity;
import com.bkav.android.mymusic.adapters.SongAdapter;

public class AllSongsFragment extends BaseSongListFragment {



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        new LoadData().execute();
        return super.onCreateView(inflater, container, savedInstanceState);

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
