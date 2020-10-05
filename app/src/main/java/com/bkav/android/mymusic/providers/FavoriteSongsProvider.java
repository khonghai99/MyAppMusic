package com.bkav.android.mymusic.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.bkav.android.mymusic.providers.MusicDBHelper.TABLE_NAME_MUSIC;

public class FavoriteSongsProvider extends ContentProvider {
    // Represent one row in music table.
    public static final int MUSIC_ID = 2;
    // The authority of music content provider.
    public static final String AUTHORITY = "com.bkav.android.mymusic.providers";
    public static final int IS_NUMBER_FAVORITE = 2;
    public static final int IS_NUMBER_NOT_FAVORITE = 1;
    public static final int NUMBER_COUNT_DEFAULT = 0;
    static final String SINGLE_MUSIC_MIME_TYPE =
            "vnd.android.cursor.item/vnd.com.bkav.android.mymusic.providers";
    static final String MULTIPLE_MUSICS_MIME_TYPE =
            "vnd.android.cursor.dir/vnd.com.bkav.android.mymusic.providers";
    private static final Integer DEFAULT_INSERT_COUNT_OF_PLAY = 1;
    // Represent music table.
    private static final int MUSICS = 1;
    private static final String MUSIC_PATH = "music";
    public static final String URL = "content://" + AUTHORITY + "/" + MUSIC_PATH;
    public static final Uri CONTENT_URI = Uri.parse(URL);
    // Base uri for this content provider.
    private static String TAG_CONTENT_PROVIDER = "CONTENT_PROVIDER";
    // Declare UriMatcher object.
    private static UriMatcher mUriMatcher;

    // Initialize uriMatcher, add matched uri.
    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // Match uri to music table.
        mUriMatcher.addURI(AUTHORITY, MUSIC_PATH, MUSICS);
        // Match uri to music table row.
        mUriMatcher.addURI(AUTHORITY, MUSIC_PATH + "/#", MUSIC_ID);
    }

    private Context mContext;
    private ContentValues mContentValues;
    private MusicDBHelper mMusicDBHelper;

    public FavoriteSongsProvider(Context mContext) {
        this.mContext = mContext;
    }

    public FavoriteSongsProvider() {
    }

    @Override
    public boolean onCreate() {
        Log.d(TAG_CONTENT_PROVIDER, "Music content provider onCreate method is called.");
        mMusicDBHelper = new MusicDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor ret;
        // Get music db object.
        SQLiteDatabase db = mMusicDBHelper.getWritableDatabase();

        switch (mUriMatcher.match(uri)) {
            case MUSICS:
                // Return all rows that match query condition in music table.
                ret = db.query(TABLE_NAME_MUSIC, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case MUSIC_ID:
                // Update rows with request row id.
                String musicId = uri.getPathSegments().get(1);
                String whereClause = " _id = ? ";
                String[] whereArgsArr = {musicId};
                ret = db.query(TABLE_NAME_MUSIC, projection, whereClause, whereArgsArr, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        Log.d(TAG_CONTENT_PROVIDER, "Music content provider query method is called.");
        return ret;
    }

    //Return the MIME type corresponding to a content URI
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case MUSICS:
                return MULTIPLE_MUSICS_MIME_TYPE;
            case MUSIC_ID:
                return SINGLE_MUSIC_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        // Return newly inserted uri object..
        Uri ret = null;
        SQLiteDatabase db = mMusicDBHelper.getWritableDatabase();
        // Get uri match code.
        int matchCode = mUriMatcher.match(uri);

        // Both match code means insert data into music table.
        if (matchCode == MUSICS || matchCode == MUSIC_ID) {
            // Insert user data into SQLite database music table and get newly added music id..
            long newMusicId = db.insert(TABLE_NAME_MUSIC, null, values);

            // Create new music uri. Uri string format : "content://<authority>/path/id".
            String newMusicUriStr = "content://" + AUTHORITY + "/" + MUSIC_PATH + "/" + newMusicId;
            ret = Uri.parse(newMusicUriStr);
            getContext().getContentResolver().notifyChange(ret, null);

        }

        Log.d(TAG_CONTENT_PROVIDER, "Music content provider insert method is called.");
        return ret;
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[]
            selectionArgs) {
        // Return deleted rows count.
        int ret;
        SQLiteDatabase db = mMusicDBHelper.getWritableDatabase();
        switch (mUriMatcher.match(uri)) {
            case MUSICS:

                // Delete all rows in music table.
                ret = db.delete(TABLE_NAME_MUSIC,
                        selection,
                        selectionArgs);
                break;
            case MUSIC_ID:
                // Delete row with request row id.
                String musicId = uri.getPathSegments().get(1);
                String whereClause = " _id = ? ";
                String[] whereArgsArr = {musicId};
                ret = db.delete(TABLE_NAME_MUSIC, whereClause, whereArgsArr);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        Log.d(TAG_CONTENT_PROVIDER, "Music content provider delete method is called.");
        return ret;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String
            selection, @Nullable String[] selectionArgs) {
        // Return updated rows count.
        int ret;
        //get music db object
        SQLiteDatabase db = mMusicDBHelper.getWritableDatabase();
        switch (mUriMatcher.match(uri)) {
            case MUSICS:
                // Update all rows in music table.
                ret = db.update(TABLE_NAME_MUSIC, values, selection, selectionArgs);
                break;
            case MUSIC_ID:
                // Update rows with request row id.
                String musicId = uri.getPathSegments().get(1);
                String whereClause = " _id = ? ";
                String[] whereArgsArr = {musicId};
                ret = db.update(TABLE_NAME_MUSIC, values, whereClause, whereArgsArr);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        Log.d(TAG_CONTENT_PROVIDER, "Music content provider update method is called.");
        return ret;
    }

    public void insertDefaultFavoriteSong(int id) {
        mContentValues = new ContentValues();
        mContentValues.put(MusicDBHelper.ID_PROVIDER, id);
        mContentValues.put(MusicDBHelper.COUNT_OF_PLAY, DEFAULT_INSERT_COUNT_OF_PLAY);
        mContext.getContentResolver().insert(FavoriteSongsProvider.CONTENT_URI, mContentValues);
    }

    public void updateCount(int id, int count) {

        mContentValues = new ContentValues();
        mContentValues.put(MusicDBHelper.COUNT_OF_PLAY, count);
        mContext.getContentResolver().update(FavoriteSongsProvider.CONTENT_URI, mContentValues,
                MusicDBHelper.ID_PROVIDER + "=" + id, null);
    }

    public void updateFavorite(int id, int favorite) {
        Cursor cursor = mContext.getContentResolver().query(FavoriteSongsProvider.CONTENT_URI,
                new String[]{MusicDBHelper.ID_PROVIDER}, MusicDBHelper.ID_PROVIDER + " = " + id,
                null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                mContentValues = new ContentValues();
                mContentValues.put(MusicDBHelper.IS_FAVORITE, favorite);
                mContext.getContentResolver().update(FavoriteSongsProvider.CONTENT_URI, mContentValues,
                        MusicDBHelper.ID_PROVIDER + "=" + id, null);
            } else {
                mContentValues = new ContentValues();
                mContentValues.put(MusicDBHelper.ID_PROVIDER, id);
                mContentValues.put(MusicDBHelper.COUNT_OF_PLAY, DEFAULT_INSERT_COUNT_OF_PLAY);
                mContentValues.put(MusicDBHelper.IS_FAVORITE, favorite);
                mContext.getContentResolver().insert(FavoriteSongsProvider.CONTENT_URI, mContentValues);
            }
        }
    }

    public void insertFavoriteSongToDB(int id) {
        Cursor cursor = mContext.getContentResolver().query(FavoriteSongsProvider.CONTENT_URI,
                new String[]{MusicDBHelper.COUNT_OF_PLAY, MusicDBHelper.ID_PROVIDER},
                MusicDBHelper.ID_PROVIDER + " = " + id, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                int count = cursor.getInt(cursor.getColumnIndex(MusicDBHelper.COUNT_OF_PLAY));
                updateCount(id, ++count);
                if (count >= 3) {
                    updateFavorite(id, IS_NUMBER_FAVORITE);
                }
            } else {
                insertDefaultFavoriteSong(id);
            }
        }
    }

}
