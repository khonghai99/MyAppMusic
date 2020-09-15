package com.bkav.android.mymusic.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bkav.android.mymusic.PlaybackStatus;
import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.StorageUtil;
import com.bkav.android.mymusic.models.Song;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import es.claucookie.miniequalizerlibrary.EqualizerView;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongHolder> {
    private static final int VIEW_TYPE_TRUE = 1;
    private static final int VIEW_TYPE_FALSE = 0;
    private StorageUtil mStorage;
    private PlaybackStatus playbackStatus;
    private int mCurrentSong;
    private OnNewClickListener mOnNewClickListener;
    private Context mContext;
    private ArrayList<Song> mSongList;

    public SongAdapter(Context mContext, ArrayList<Song> mSongList, OnNewClickListener mOnNewClickListener) {
        this.mContext = mContext;
        this.mSongList = mSongList;
        this.mOnNewClickListener = mOnNewClickListener;
        this.mStorage = new StorageUtil(mContext);
        this.mCurrentSong = this.mStorage.loadAudioIndex();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mCurrentSong) return VIEW_TYPE_TRUE;
        return VIEW_TYPE_FALSE;
    }

    @NonNull
    @Override
    public SongAdapter.SongHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            view = LayoutInflater.from(mContext).inflate(R.layout.one_row_song, parent, false);
        } else
            view = LayoutInflater.from(mContext).inflate(R.layout.one_row_song_playing, parent, false);
        return new SongHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SongHolder holder, final int position) {
        Log.i("main1", "bind");
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
            holder.equalizer.animateBars();

        }
    }

    @Override
    public int getItemCount() {
        return mSongList != null ? mSongList.size() : 0;
    }

    /**
     * sets the current song when clicked
     *
     * @param mCurrentSong the position when clicked
     */
    public void setCurrentSong(int mCurrentSong) {
        this.mCurrentSong = mCurrentSong;
    }

    public void setPlaybackStatus(PlaybackStatus playbackStatus) {
        this.playbackStatus = playbackStatus;
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
         * call timeUnitToFullTime to convert
         *
         * @param millisecond milliseconds to transfer
         * @return "m:s"
         */

        public static String millisecondToFullTime(long millisecond) {
            return timeUnitToFullTime(millisecond, TimeUnit.MILLISECONDS);
        }

        /**
         * convert time to string ("m:s")
         *
         * @param time     duration of song
         * @param timeUnit Object TimeUnit
         * @return time formatted
         */
        public static String timeUnitToFullTime(long time, TimeUnit timeUnit) {
            long day = timeUnit.toDays(time);
            long hour = timeUnit.toHours(time) % 24;
            long minute = timeUnit.toMinutes(time) % 60;
            long second = timeUnit.toSeconds(time) % 60;
            if (day > 0) {
                return String.format("%dday %02d:%02d:%02d", day, hour, minute, second);
            } else if (hour > 0) {
                return String.format("%d:%02d:%02d", hour, minute, second);
            } else if (minute > 0) {
                return String.format("%d:%02d", minute, second);
            } else {
                return String.format("%d:%02d", minute, second);
            }
        }

        /**
         * set data for one row recyclerView
         *
         * @param song object Song
         */
        public void toBind(Song song) {
            tvTitleSong.setText(song.getTitle());
            tvTitleSong.setSelected(true);
            tvDuration.setText(millisecondToFullTime(Long.parseLong(song.getDuration())));
        }
    }
}
