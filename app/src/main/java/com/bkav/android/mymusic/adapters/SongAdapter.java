package com.bkav.android.mymusic.adapters;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bkav.android.mymusic.OnNewClickListener;
import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.models.Song;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongHolder> {
    public int type = 0;
    private OnNewClickListener mOnNewClickListener;
    private Context mContext;
    private List<Song> mSongList;
    private MediaPlayer mediaPlayer;

    public SongAdapter(Context mContext, List<Song> mSongList, OnNewClickListener mOnNewClickListener) {
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
            holder.tvNameSong.setText(mSongList.get(position).getmTitle());
            holder.tvTimeSong.setText(mSongList.get(position).getmDuration());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnNewClickListener.onNewClick(song, position);
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return mSongList != null ? mSongList.size() : 0;
    }

    public static class SongHolder extends RecyclerView.ViewHolder {
        public TextView tvID;
        public TextView tvNameSong;
        public TextView tvTimeSong;
        private ImageView ivSong;
        private RelativeLayout layoutBottomAllSong;

        public SongHolder(@NonNull final View itemView) {
            super(itemView);
            tvID = itemView.findViewById(R.id.tvID);
            tvNameSong = itemView.findViewById(R.id.tvTitleSongOneRow);
            tvTimeSong = itemView.findViewById(R.id.tvDurationSongOneRow);
            layoutBottomAllSong = itemView.findViewById(R.id.layoutBottomAllSong);

        }


    }
}
