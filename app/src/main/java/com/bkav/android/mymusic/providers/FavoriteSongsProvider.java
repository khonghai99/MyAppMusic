package com.bkav.android.mymusic.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FavoriteSongsProvider extends ContentProvider {
    private static String TAG_CONTENT_PROVIDER = "CONTENT_PROVIDER";

    private MusicDBHelper mMusicDBHelper;

    // Represent music table.
    private static final int MUSICS = 1;

    // Represent one row in music table.
    public static final int MUSIC_ID = 2;

    // The authority of account content provider.
    public static final String AUTHORITY = "com.bkav.android.mymusic.providers";

    private static final String accountPath = "account";

    // Declare UriMatcher object.
    private static UriMatcher uriMatcher;

    // if content data mimetype is a table then use this prefix.
    private static final String mimeTypeDirPrefix = "vnd.android.cursor.dir/";

    // if content data mimetype is table rows then use this prefix.
    private static final String mimeTypeItemPrefix = "vnd.android.cursor.item/";

    // Base uri for this content provider.
    public static final String BASE_CONTENT_URI = "content://" + AUTHORITY + "/" + accountPath;

    // Initialize uriMatcher, add matched uri.
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // Match uri to music table.
        uriMatcher.addURI(AUTHORITY, accountPath, MUSICS);
        // Match uri to music table row.
        uriMatcher.addURI(AUTHORITY, accountPath+"/#", MUSIC_ID);
    }
    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
