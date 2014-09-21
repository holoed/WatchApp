package com.gattaca.watchapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.cast.MediaInfo;

import java.util.HashMap;


public class MainActivity extends Activity {

    private static final String LOG_TAG = "WatchAppMainActivity";
    private final MoviesCatalogService _moviesCatalogService;
    private final ChromeCastService _chromeCastService;

    public MainActivity() {
        _moviesCatalogService = new MoviesCatalogServiceImpl();
        _chromeCastService = new ChromeCastServiceImpl(this);
    }

    private BroadcastReceiver onNotice = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // intent can contain anydata
            Log.d(LOG_TAG, "onReceive called");

            byte[] data = intent.getByteArrayExtra("message");
            String spokenText = new String(data).toLowerCase().replaceAll("\\s+", "");

            HashMap<String, MediaInfo> movies = _moviesCatalogService.getMovies();
            if (!movies.containsKey(spokenText))
                return;

            final MediaInfo selectedMovie = movies.get(spokenText);

            _chromeCastService.PlayMovie(selectedMovie);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _moviesCatalogService.BuildMediaInfos();

        _chromeCastService.DiscoverCastDevices();

        IntentFilter iff= new IntentFilter("cast-a-movie");
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
