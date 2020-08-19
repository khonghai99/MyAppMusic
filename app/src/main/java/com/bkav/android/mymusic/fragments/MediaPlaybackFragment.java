package com.bkav.android.mymusic.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.models.Song;

public class MediaPlaybackFragment extends Fragment {
    private ImageView ivSongTopMedia;
    private TextView tvNameSongTopMedia;
    private TextView tvNameAlbumTopMedia;
    private ImageView ivPopupTopMedia;

    public static MediaPlaybackFragment getInstancesMedia(Song song){
        MediaPlaybackFragment fragment = new MediaPlaybackFragment();
        Bundle bundle = new Bundle();
        bundle.putString("nameSong",song.getmNameSong());
        bundle.putString("authorSong",song.getmAuthorSong());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media_playback, container, false);
        ivSongTopMedia = view.findViewById(R.id.ivSongTopMedia);
        ivPopupTopMedia = view.findViewById(R.id.ivPopupTopMedia);
        tvNameAlbumTopMedia = view.findViewById(R.id.tvNameAlbumTopMedia);
        tvNameSongTopMedia = view.findViewById(R.id.tvNameSongTopMedia);
        setTopMedia(getArguments());
        return view;
    }


    public void setTopMedia(Bundle bundle) {
        tvNameSongTopMedia.setText(bundle.getString("nameSong"));
        tvNameAlbumTopMedia.setText(bundle.getString("authorSong"));
    }


}
