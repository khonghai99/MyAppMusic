package com.bkav.android.mymusic.fragments;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bkav.android.mymusic.ImageSong;
import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.models.Song;

public class MediaPlaybackFragment extends Fragment {
    private ImageView ivSongTopMedia;
    private TextView tvTitleSongTopMedia;
    private TextView tvArtistTopMedia;
    private ImageView ivPopupTopMedia;
    private ImageView ivBackgroundMedia;

    public static MediaPlaybackFragment getInstancesMedia(Song song) {
        MediaPlaybackFragment fragment = new MediaPlaybackFragment();
        Bundle bundle = new Bundle();
        bundle.putString("path", song.getmPath());
        bundle.putString("title", song.getmTitle());
        bundle.putString("artist", song.getmArtist());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media_playback, container, false);
        ivSongTopMedia = view.findViewById(R.id.ivSongTopMedia);
        ivPopupTopMedia = view.findViewById(R.id.ivPopupTopMedia);
        tvArtistTopMedia = view.findViewById(R.id.tvArtistSongTopMedia);
        tvTitleSongTopMedia = view.findViewById(R.id.tvTitleSongTopMedia);
        ivBackgroundMedia = view.findViewById(R.id.ivBackgroundMedia);
        if (getArguments() != null) {
            setTopMedia(getArguments());
        }
        return view;
    }

    public void setTopMedia(Bundle bundle) {
        String path = bundle.getString("path");
        byte[] art = ImageSong.getByteImageSong(path);
        if (art != null) {
            ivSongTopMedia.setImageBitmap(BitmapFactory.decodeByteArray(art, 0, art.length));
            ivBackgroundMedia.setImageBitmap(BitmapFactory.decodeByteArray(art, 0, art.length));
        } else {
            ivSongTopMedia.setImageResource(R.drawable.ic_no_image);
            ivBackgroundMedia.setImageResource(R.drawable.ic_no_image);
        }
        tvTitleSongTopMedia.setText(bundle.getString("title"));
        tvArtistTopMedia.setText(bundle.getString("artist"));
    }

    public void setTitle(Song song) {
        String path = song.getmPath();
        byte[] art = ImageSong.getByteImageSong(path);
        if (art != null) {
            ivSongTopMedia.setImageBitmap(BitmapFactory.decodeByteArray(art, 0, art.length));
            ivBackgroundMedia.setImageBitmap(BitmapFactory.decodeByteArray(art, 0, art.length));
        } else {
            ivSongTopMedia.setImageResource(R.drawable.ic_no_image);
            ivBackgroundMedia.setImageResource(R.drawable.ic_no_image);
        }
        tvArtistTopMedia.setText(song.getmArtist());
        tvTitleSongTopMedia.setText(song.getmTitle());
    }
}
