package com.bkav.android.mymusic.adapter;

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
import com.bkav.android.mymusic.model.Song;

import java.util.List;

public class CustomRecycleAdapter extends RecyclerView.Adapter<CustomRecycleAdapter.SongHolder> implements OnNewClickListener {
    private OnNewClickListener mOnNewClickListener;
    private Context mContext;
    private List<Song> mSongList;
    private MediaPlayer mediaPlayer;
    private Boolean flag = true;

    public CustomRecycleAdapter(Context mContext, List<Song> mSongList) {
        this.mContext = mContext;
        this.mSongList = mSongList;
    }

    @NonNull
    @Override
    public CustomRecycleAdapter.SongHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.one_row_song, parent, false);
        return new SongHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SongHolder holder, final int position) {
        final Song song = mSongList.get(position);
        holder.txtStt.setText(mSongList.get(position).getmStt() + "");
        holder.txtNameSong.setText(mSongList.get(position).getmNameSong());
        holder.txtTimeSong.setText(mSongList.get(position).getmTimeSong());
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag) {
                    mediaPlayer = MediaPlayer.create(mContext, song.getmSong());
                    flag = false;
                }
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    mediaPlayer = MediaPlayer.create(mContext, song.getmSong());
                    mediaPlayer.start();
                } else {
                    mediaPlayer = MediaPlayer.create(mContext, song.getmSong());
                    mediaPlayer.start();
                }
                if (mOnNewClickListener != null) {
                    mOnNewClickListener.onNewClick(song);
                    notifyDataSetChanged();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mSongList.size();
    }

    @Override
    public void onNewClick(Song song) {

    }

    public static class SongHolder extends RecyclerView.ViewHolder {
        public TextView txtStt;
        public TextView txtNameSong;
        public TextView txtTimeSong;
        public ImageView ivShowPopup;
        public RelativeLayout relativeLayout;

        public SongHolder(@NonNull View itemView) {
            super(itemView);
            txtStt = (TextView) itemView.findViewById(R.id.txtSTT);
            txtNameSong = (TextView) itemView.findViewById(R.id.txtNameSong);
            txtTimeSong = (TextView) itemView.findViewById(R.id.txtTimeSong);
            ivShowPopup = (ImageView) itemView.findViewById(R.id.ivShowPopup);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.layoutRelative);

        }
    }
}
