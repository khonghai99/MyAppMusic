package com.bkav.android.mymusic.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.StorageUtil;
import com.bkav.android.mymusic.models.Song;

import java.util.ArrayList;

import es.claucookie.miniequalizerlibrary.EqualizerView;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongHolder> {
    private Context mContext;
    private int mCurrentSong;
    private ArrayList<Song> mSongList;
    private OnNewClickListener mOnNewClickListener;
    private int mSongID;
    private StorageUtil storageUtil;

    public SongAdapter(Context context, ArrayList<Song> mSongList, OnNewClickListener onNewClickListener) {
        this.mContext = context;
        this.mSongList = mSongList;
        this.mOnNewClickListener = onNewClickListener;

    }

    public void updateSongList(ArrayList<Song> songs,int position) {
        this.mSongList = songs;
        this.mCurrentSong = position;
        notifyDataSetChanged();
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
        storageUtil = new StorageUtil(mContext.getApplicationContext());
        if (song != null) {
           Log.d("HaiKH", "onBindViewHolder: "+song.getId()+"||"+storageUtil.loadAudioId());
            if (song.getId() == storageUtil.loadAudioId()){
                Log.d("HaiKH", "onBindViewHolder: 123");
                holder.isClick();
            }else{holder.isNotClick();

            }
            holder.tvID.setText(String.valueOf(position + 1));
            holder.toBind(song);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnNewClickListener.onNewClick(mSongList, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mSongList != null ? mSongList.size() : 0;
    }

    /**
     * sets the current song when clicked
     *
     * @param mSongID the id song when clicked
     */
    public void setSongID(int mSongID) {
        this.mSongID = mSongID;
    }

    public void clickListener(OnNewClickListener onNewClickListener) {
        this.mOnNewClickListener = onNewClickListener;
    }

    public interface OnNewClickListener {
        void onNewClick(ArrayList<Song> songList, int position);
    }

    public static class SongHolder extends RecyclerView.ViewHolder {
        private EqualizerView equalizer;
        private TextView tvID;
        private TextView tvTitleSong;
        private TextView tvDuration;

        public SongHolder(@NonNull final View itemView) {
            super(itemView);
            tvID = itemView.findViewById(R.id.tvID);
            tvTitleSong = itemView.findViewById(R.id.tvTitleSongOneRow);
            tvDuration = itemView.findViewById(R.id.tvDurationSongOneRow);
            equalizer = (EqualizerView) itemView.findViewById(R.id.equalizer_view);

        }

        /**
         * set data for one row recyclerView
         *
         * @param song object Song
         */
        public void toBind(Song song) {
            tvTitleSong.setText(song.getTitle());
            tvDuration.setText(song.getDuration());
        }

        public void isClick() {
            tvID.setVisibility(View.INVISIBLE);
            equalizer.setVisibility(View.VISIBLE);
            equalizer.animateBars();
            tvTitleSong.setTypeface(Typeface.DEFAULT_BOLD);
        }
        public void isNotClick() {
            tvID.setVisibility(View.VISIBLE);
            equalizer.setVisibility(View.INVISIBLE);
            equalizer.animateBars();
            tvTitleSong.setTypeface(Typeface.DEFAULT);
        }

    }
}
