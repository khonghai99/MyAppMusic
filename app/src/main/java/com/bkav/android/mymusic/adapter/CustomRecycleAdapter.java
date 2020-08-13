package com.bkav.android.mymusic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.model.Song;

import java.util.List;

public class CustomRecycleAdapter extends RecyclerView.Adapter<CustomRecycleAdapter.SongHolder> {
    private List<Song> mSongList;

    public CustomRecycleAdapter(List<Song> mSongList) {
        this.mSongList = mSongList;
    }

    @NonNull
    @Override
    public CustomRecycleAdapter.SongHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.one_row_song, parent, false);
        return new SongHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongHolder holder, int position) {
        holder.txtStt.setText(mSongList.get(position).getmStt() + "");
        holder.txtNameSong.setText(mSongList.get(position).getmNameSong());
        holder.txtTimeSong.setText(mSongList.get(position).getmTimeSong());

    }

    @Override
    public int getItemCount() {
        return mSongList.size();
    }

    public static class SongHolder extends RecyclerView.ViewHolder {
        public TextView txtStt;
        public TextView txtNameSong;
        public TextView txtTimeSong;

        public SongHolder(@NonNull View itemView) {
            super(itemView);
            txtStt = (TextView) itemView.findViewById(R.id.txtSTT);
            txtNameSong = (TextView) itemView.findViewById(R.id.txtNameSong);
            txtTimeSong = (TextView) itemView.findViewById(R.id.txtTimeSong);

        }
    }
}
