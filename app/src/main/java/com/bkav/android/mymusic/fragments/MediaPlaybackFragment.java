package com.bkav.android.mymusic.fragments;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
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

public class MediaPlaybackFragment extends Fragment implements View.OnClickListener {
    private final String LOG_INFO = "appMusic";
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


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.i(LOG_INFO, "fragment media attach");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(LOG_INFO, "fragment media create view");
        View view = inflater.inflate(R.layout.fragment_media_playback, container, false);
        ivSongTopMedia = view.findViewById(R.id.ivSongTopMedia);
        ivPopupTopMedia = view.findViewById(R.id.ivPopupTopMedia);
        tvArtistTopMedia = view.findViewById(R.id.tvArtistSongTopMedia);
        tvTitleSongTopMedia = view.findViewById(R.id.tvTitleSongTopMedia);
        ivBackgroundMedia = view.findViewById(R.id.ivBackgroundMedia);
        ivBackgroundMedia.setOnClickListener(this);
        if (getArguments() != null) {
            setTopMedia(getArguments());
        }
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(LOG_INFO, "fragment media pause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(LOG_INFO, "fragment media stop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(LOG_INFO, "fragment media destroy view");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_INFO, "fragment media destroy");
    }

    public void setTopMedia(Bundle bundle) {
        String path = bundle.getString("path");
        byte[] art = ImageSong.getByteImageSong(path);
        if (art != null) {
            ivSongTopMedia.setImageBitmap(BitmapFactory.decodeByteArray(art, 0, art.length));
            ivBackgroundMedia.setImageBitmap(BitmapFactory.decodeByteArray(art, 0, art.length));
        } else {
            ivSongTopMedia.setImageResource(R.drawable.no_image_music);
            ivBackgroundMedia.setImageResource(R.drawable.no_image_music);
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
            ivSongTopMedia.setImageResource(R.drawable.no_image_music);
            ivBackgroundMedia.setImageResource(R.drawable.no_image_music);
        }
        tvArtistTopMedia.setText(song.getmArtist());
        tvTitleSongTopMedia.setText(song.getmTitle());
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.ivBackgroundMedia:
                break;
        }
    }
}
