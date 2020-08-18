package com.bkav.android.mymusic.activities;

import android.os.Bundle;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bkav.android.mymusic.OnNewClickListener;
import com.bkav.android.mymusic.OnShowMediaListener;
import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.fragments.AllSongsFragment;
import com.bkav.android.mymusic.fragments.MediaPlaybackFragment;
import com.bkav.android.mymusic.models.Song;


public class MusicActivity extends AppCompatActivity implements OnNewClickListener, OnShowMediaListener {
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Music");
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        AllSongsFragment allSongsFragment = new AllSongsFragment();
        fragmentTransaction.replace(R.id.fragmentLayout, allSongsFragment);
        fragmentTransaction.commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onNewClick(Song song, int position) {
        AllSongsFragment allSongsFragment = (AllSongsFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentLayout);
        allSongsFragment.setDataBottom(song, position);
    }

    @Override
    public void showMediaFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        MediaPlaybackFragment mediaPlaybackFragment = new MediaPlaybackFragment();
        fragmentTransaction.replace(R.id.fragmentLayout, mediaPlaybackFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}