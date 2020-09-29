package com.bkav.android.mymusic.providers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class MusicDBHelper extends SQLiteOpenHelper {
    public static final String ID = "_id";
    public static final String ID_PROVIDER = "is_provider";
    public static final String IS_FAVORITE = "is_favorite";
    public static final String COUNT_OF_PLAY = "count_of_play";
    public static final String TITLE = "song_title";
    public static final String ARTIST = "song_artist";
    public static final String DURATION = "song_duration";
    public static final String DATA = "song_data";

    // Database Name
    public static final String DATABASE_NAME = "Music";
    private static final String LOG_TAG = "music upgrade";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    public MusicDBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String script = "CREATE TABLE if not exists " + DATABASE_NAME + " (" +
                ID + " integer PRIMARY KEY autoincrement," +
                ID_PROVIDER + "," +
                IS_FAVORITE + "," +
                COUNT_OF_PLAY + "," +
                TITLE + "," +
                ARTIST + "," +
                DURATION + "," +
                DATA + ");" ;

        // Execute Script.
        sqLiteDatabase.execSQL(script);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.w(LOG_TAG, "Upgrading database from version " + i + " to "
                + i1 + ", which will destroy all old data");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
        onCreate(sqLiteDatabase);
    }
}
