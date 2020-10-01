package com.bkav.android.mymusic.providers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

public class MusicDBHelper extends SQLiteOpenHelper {
    public static final String ID = "_id";
    public static final String ID_PROVIDER = "is_provider";
    public static final String IS_FAVORITE = "is_favorite";
    public static final String COUNT_OF_PLAY = "count_of_play";

    // Database Name
    public static final String DATABASE_NAME_MUSIC = "Music";
    public static final String TABLE_NAME_MUSIC = "FavoriteMusic";
    private static final String LOG_TAG = "music upgrade";

    // Database Version
    private static final int DATABASE_VERSION = 1;


    public MusicDBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME_MUSIC, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String script = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_MUSIC
                + "(" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ID_PROVIDER + " INTEGER, " +
                IS_FAVORITE + " INTEGER, " +
                COUNT_OF_PLAY + " INTEGER DEFAULT 0" +
                ")";
        // Execute Script.
        sqLiteDatabase.execSQL(script);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.w(LOG_TAG, "Upgrading database from version " + i + " to "
                + i1 + ", which will destroy all old data");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME_MUSIC);
        onCreate(sqLiteDatabase);
    }
}
