package com.bkav.android.mymusic.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

import com.bkav.android.mymusic.OnNewClickListener;
import com.bkav.android.mymusic.R;
import com.bkav.android.mymusic.fragments.AllSongsFragment;
import com.bkav.android.mymusic.fragments.MediaPlaybackFragment;
import com.bkav.android.mymusic.models.Song;


public class MusicActivity extends AppCompatActivity implements OnNewClickListener, AllSongsFragment.OnShowMediaListener {
    private static final int MY_PERMISSION_REQUEST = 1;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        Toolbar toolbar = findViewById(R.id.toolbar);

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