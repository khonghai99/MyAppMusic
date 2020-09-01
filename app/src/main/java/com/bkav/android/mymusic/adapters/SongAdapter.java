package com.bkav.android.mymusic.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bkav.android.mymusic.Interfaces.OnNewClickListener;
import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.models.Song;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongHolder> {
    private OnNewClickListener mOnNewClickListener;
    private Context mContext;
    private ArrayList<Song> mSongList;

    public SongAdapter(Context mContext, ArrayList<Song> mSongList, OnNewClickListener mOnNewClickListener) {
        this.mContext = mContext;
        this.mSongList = mSongList;
        this.mOnNewClickListener = mOnNewClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public SongAdapter.SongHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(mContext).inflate(R.layout.one_row_song, parent, false);
        return new SongHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SongHolder holder, final int position) {
        final Song song = mSongList.get(position);
        if (song != null) {
            holder.tvID.setText(String.valueOf(position + 1));
            holder.toBind(song);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnNewClickListener.onNewClick(mSongList, position);
                }
            });
            //get position
            holder.getLayoutPosition();
        }
    }

    @Override
    public int getItemCount() {
        return mSongList != null ? mSongList.size() : 0;
    }

    public static class SongHolder extends RecyclerView.ViewHolder {
        private ImageView ivPlaying;
        private TextView tvID;
        private TextView tvTitleSong;
        private TextView tvDuration;

        public SongHolder(@NonNull final View itemView) {
            super(itemView);
            tvID = itemView.findViewById(R.id.tvID);
            tvTitleSong = itemView.findViewById(R.id.tvTitleSongOneRow);
            ivPlaying = itemView.findViewById(R.id.ivPlaying);
            tvDuration = itemView.findViewById(R.id.tvDurationSongOneRow);

        }

        public void toBind(Song song) {
            tvTitleSong.setText(song.getmTitle());
            tvDuration.setText(song.getmDuration());
        }
    }
}
