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

public class CustomRecycleAdapter extends RecyclerView.Adapter<CustomRecycleAdapter.SongHolder> {
    public int type = 0;
    private OnNewClickListener mOnNewClickListener;
    private Context mContext;
    private List<Song> mSongList;
    private MediaPlayer mediaPlayer;
    private Boolean flag = true;

    public CustomRecycleAdapter(Context mContext, List<Song> mSongList, OnNewClickListener mOnNewClickListener) {
        this.mContext = mContext;
        this.mSongList = mSongList;
        this.mOnNewClickListener = mOnNewClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (mSongList.get(position).getPlay()) {
            type = 1;
        } else type = 0;

        return type;
    }

    @NonNull
    @Override
    public CustomRecycleAdapter.SongHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(mContext).inflate(R.layout.one_row_song_playing, parent, false);
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.one_row_song, parent, false);
        }
        SongHolder song = new SongHolder(view);
        song.setType(viewType);
        return song;
    }

    @Override
    public void onBindViewHolder(@NonNull final SongHolder holder, final int position) {
        final Song song = mSongList.get(position);
        if (type == 0) {
            holder.tvStt.setText(mSongList.get(position).getmStt() + "");
        }
        holder.tvNameSong.setText(mSongList.get(position).getmNameSong());
        holder.tvTimeSong.setText(mSongList.get(position).getmTimeSong());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnNewClickListener.onNewClick(song, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mSongList.size();
    }

    public static class SongHolder extends RecyclerView.ViewHolder {
        public TextView tvStt;
        public TextView tvNameSong;
        public TextView tvTimeSong;
        private ImageView ivSong;
        private int type = 0;

        public SongHolder(@NonNull final View itemView) {
            super(itemView);
            if (type == 0) {
                tvStt = (TextView) itemView.findViewById(R.id.tvSTT);
            } else {
                ivSong = (ImageView) itemView.findViewById(R.id.imgSTT);
            }

            tvNameSong = (TextView) itemView.findViewById(R.id.tvNameSong);
            tvTimeSong = (TextView) itemView.findViewById(R.id.tvTimeSong);

        }

        public void setType(int type) {
            this.type = type;
        }
    }
}
