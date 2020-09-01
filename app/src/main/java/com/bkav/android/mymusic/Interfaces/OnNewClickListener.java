package com.bkav.android.mymusic.Interfaces;

import com.bkav.android.mymusic.models.Song;

import java.util.ArrayList;

public interface OnNewClickListener {
    void onNewClick(ArrayList<Song> songList, int position);
}
