package com.bkav.android.mymusic;

import android.media.MediaMetadataRetriever;

public class ImageSong {
    public static byte[] getByteImageSong(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        byte[] art = retriever.getEmbeddedPicture();
        return art;
    }
}
