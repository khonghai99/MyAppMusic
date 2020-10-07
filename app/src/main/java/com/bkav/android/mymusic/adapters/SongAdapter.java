package com.bkav.android.mymusic.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bkav.android.mymusic.MediaPlaybackStatus;
import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.StorageUtil;
import com.bkav.android.mymusic.models.Song;
import com.bkav.android.mymusic.services.MediaPlaybackService;

import java.util.ArrayList;

import es.claucookie.miniequalizerlibrary.EqualizerView;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongHolder> implements Filterable {
    private Context mContext;
    private ArrayList<Song> mSongList;
    private MediaPlaybackService mMediaPlaybackService;
    private OnNewClickListener mOnNewClickListener;
    private OnClickPopupListener mOnClickPopupListener;
    private ArrayList<Song> mSongListFilter;
    private CustomFilter filter;

    public SongAdapter(Context context) {
        this.mContext = context;
    }

    public void updateSongList(ArrayList<Song> songs) {
        this.mSongList = songs;
        this.mSongListFilter = songs;
        notifyDataSetChanged();
    }

    public void setService(MediaPlaybackService mediaPlaybackService) {
        this.mMediaPlaybackService = mediaPlaybackService;
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
        StorageUtil mStorageUtil = new StorageUtil(mContext.getApplicationContext());
        if (song != null) {
            if (song.getID() == mStorageUtil.loadAudioID()) {
                if (mMediaPlaybackService == null) {
                    holder.isClickSong(MediaPlaybackStatus.PAUSED);
                } else {
                    holder.isClickSong(mMediaPlaybackService.isPlayingState());
                }
            } else {
                holder.isSongDefault();
            }
            holder.mIdTextView.setText(String.valueOf(position + 1));
            holder.toBind(song);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnNewClickListener.onNewClick(mSongList, position);
                }
            });
            holder.mPopupMenuImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnClickPopupListener.onClickPopup(view,position);
                }
            });
//            holder.mRelativeLayoutOneRow.setAnimation(AnimationUtils.loadAnimation(mContext,R.anim.fade_scale_animation));
        }
    }

    @Override
    public int getItemCount() {
        return mSongList != null ? mSongList.size() : 0;
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new CustomFilter();
        }
        return filter;
    }

    public void setOnClick(OnNewClickListener onNewClickListener) {
        this.mOnNewClickListener = onNewClickListener;
    }

    public void setOnClickPopup(OnClickPopupListener onClickPopupListener) {
        this.mOnClickPopupListener = onClickPopupListener;
    }

    public interface OnNewClickListener {
        void onNewClick(ArrayList<Song> songList, int position);
    }

    public interface OnClickPopupListener {
        void onClickPopup(View view,int position);
    }

    public static class SongHolder extends RecyclerView.ViewHolder {
        private EqualizerView mEqualizer;
        private TextView mIdTextView;
        private TextView mTitleTextView;
        private TextView mDurationTextView;
        private ImageView mPopupMenuImageView;
//        private RelativeLayout mRelativeLayoutOneRow;

        public SongHolder(@NonNull final View itemView) {
            super(itemView);
            mIdTextView = itemView.findViewById(R.id.id);
            mTitleTextView = itemView.findViewById(R.id.title_one_row);
            mDurationTextView = itemView.findViewById(R.id.duration_one_row);
            mEqualizer = (EqualizerView) itemView.findViewById(R.id.equalizer_view);
            mPopupMenuImageView = itemView.findViewById(R.id.popup_one_row);
//            mRelativeLayoutOneRow = itemView.findViewById(R.id.layout_relative_one_row);

        }

        /**
         * set data for one row recyclerView
         *
         * @param song object Song
         */
        public void toBind(Song song) {
            mTitleTextView.setText(song.getTitle());
            mDurationTextView.setText(song.getDuration());

        }

        /**
         * set item recycler on play song
         */
        public void isClickSong(MediaPlaybackStatus status) {
            mIdTextView.setVisibility(View.INVISIBLE);
            mEqualizer.setVisibility(View.VISIBLE);
            if (status.equals(MediaPlaybackStatus.PLAYING)) {
                mEqualizer.animateBars();
            } else {
                mEqualizer.stopBars();
            }
            mTitleTextView.setTypeface(Typeface.DEFAULT_BOLD);
        }

        /**
         * set item recycler default
         */
        public void isSongDefault() {
            mIdTextView.setVisibility(View.VISIBLE);
            mEqualizer.setVisibility(View.INVISIBLE);
            mEqualizer.animateBars();
            mTitleTextView.setTypeface(Typeface.DEFAULT);
        }

    }

    public class CustomFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint != null && constraint.length() > 0) {
                constraint = constraint.toString().toUpperCase();
                ArrayList<Song> filterSong = new ArrayList<>();
                for (int i = 0; i < mSongListFilter.size(); i++) {
                    if (mSongListFilter.get(i).getTitle().toUpperCase().contains(constraint)) {
                        Song song = new Song(mSongListFilter.get(i).getID(), mSongListFilter.get(i).getTitle(), mSongListFilter.get(i).getArtist(), mSongListFilter.get(i).getDurationReal(), mSongListFilter.get(i).getPath());
                        filterSong.add(song);
                    }
                }
                results.count = filterSong.size();
                results.values = filterSong;
            } else {
                results.count = mSongListFilter.size();
                results.values = mSongListFilter;
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mSongList = (ArrayList<Song>) results.values;
            notifyDataSetChanged();
        }
    }
}
