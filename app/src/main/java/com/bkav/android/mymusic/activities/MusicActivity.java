package com.bkav.android.mymusic.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bkav.android.mymusic.Interfaces.OnNewClickListener;
import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.fragments.AllSongsFragment;
import com.bkav.android.mymusic.fragments.MediaPlaybackFragment;
import com.bkav.android.mymusic.models.Song;
import com.bkav.android.mymusic.services.MediaPlaybackService;
import com.bkav.android.mymusic.services.MediaPlaybackService.MusicBinder;

import java.util.ArrayList;


public class MusicActivity extends AppCompatActivity implements OnNewClickListener, AllSongsFragment.OnShowMediaListener {
    private static final int MY_PERMISSION_REQUEST = 1;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private MediaPlaybackService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;
    private ArrayList<Song> songList;
    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicBinder binder = (MusicBinder) iBinder;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MediaPlaybackService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        Toolbar toolbar = findViewById(R.id.toolbar);

        songList = new ArrayList<Song>();
        //set toolbar
        setSupportActionBar(toolbar);
        toolbar.setTitle("Music");

        //check permission
        if (ContextCompat.checkSelfPermission(MusicActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MusicActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
        } else {
            //add fragment to frameLayout
            AddFragmentOne();
        }


    }

    private void AddFragmentOne() {
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        AllSongsFragment allSongsFragment = new AllSongsFragment();
        fragmentTransaction.replace(R.id.fragmentLayoutOne, allSongsFragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //event click recyclerView
    @Override
    public void onNewClick(Song song, int position) {
        AllSongsFragment allSongsFragment = (AllSongsFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentLayoutOne);
        if (allSongsFragment != null) {
            allSongsFragment.setDataBottom(song, position);
            allSongsFragment.setVisible();
        }

    }

    @Override
    public void showMediaFragment(Song song) {
        Fragment fragment = MediaPlaybackFragment.getInstancesMedia(song);
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentLayoutOne, fragment);

        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(MusicActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No permission granted", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}