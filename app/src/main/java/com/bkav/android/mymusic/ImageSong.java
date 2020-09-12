package com.bkav.android.mymusic;

import android.media.MediaMetadataRetriever;

public class ImageSong {
    public static byte[] getByteImageSong(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
        } catch (IllegalArgumentException e) {
            retriever.setDataSource("");
        }
        return retriever.getEmbeddedPicture();
    }
}
