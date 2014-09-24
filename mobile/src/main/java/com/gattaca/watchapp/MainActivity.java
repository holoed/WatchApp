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
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;

import java.util.HashMap;

import rx.functions.Action1;


public class MainActivity extends Activity {

    private static final String LOG_TAG = "WatchAppMainActivity";
    private final MoviesCatalogService _moviesCatalogService;
    private final ChromeCastService _chromeCastService;
    private final SearchEngine _searchEngine;
    private HashMap<String, MediaInfo> _movies;

    public MainActivity() {
        _moviesCatalogService = new MoviesCatalogServiceImpl();
        _chromeCastService = new ChromeCastServiceImpl(this);
        _searchEngine = new SearchEngineImpl();
    }

    private BroadcastReceiver onNotice = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // intent can contain anydata
            Log.d(LOG_TAG, "onReceive called");

            byte[] data = intent.getByteArrayExtra("message");
            String spokenText = new String(data);

            String[] foundTitles = _searchEngine.Search(spokenText);

            if (foundTitles.length <= 0)
                return;

            final MediaInfo selectedMovie = _movies.get(foundTitles[0]);

            _chromeCastService.PlayMovie(selectedMovie);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Activity host = this;
        final ListView view = (ListView)this.findViewById(R.id.movies_list);

        _moviesCatalogService.BuildMediaInfos()
                .take(1)
                .subscribe(new Action1<HashMap<String, MediaInfo>>() {
                    @Override
                    public void call(HashMap<String, MediaInfo> moviesInfos) {
                        Log.d(LOG_TAG, "Received media info list");
                        _movies = moviesInfos;
                        String[] titles = new String[_movies.size()];
                        int index = 0;
                        for (MediaInfo info : moviesInfos.values()) {
                            titles[index++] = info.getMetadata().getString(MediaMetadata.KEY_TITLE);
                        }
                        _searchEngine.Initialize(titles);
                        ArrayAdapter<String> test = new ArrayAdapter<String>(host, android.R.layout.simple_list_item_1, titles);
                        view.setAdapter(test);
                    }
                });

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
