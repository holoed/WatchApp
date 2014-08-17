package com.gattaca.watchapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.media.MediaRouteSelector;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.media.MediaRouter;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.images.WebImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends Activity {

    private HashMap<String, MediaInfo> movies;
    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private String APPLICATION_ID;
    private List<MediaRouter.RouteInfo> routes = new ArrayList<MediaRouter.RouteInfo>();

    private MainActivity mContext = this;
    private GoogleApiClient mApiClient;
    private RemoteMediaPlayer mRemoteMediaPlayer;
    private BroadcastReceiver onNotice= new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // intent can contain anydata
            Log.d("sohail", "onReceive called");

            byte[] data = intent.getByteArrayExtra("message");
            String spokenText = new String(data);

            if (!movies.containsKey(spokenText))
                return;

            final MediaInfo selectedMovie = movies.get(spokenText);

            CastDevice device = CastDevice.getFromBundle(routes.get(0).getExtras());

            Log.d("sohail", "acquiring a connection to Google Play services for " + device);
            Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions.builder(device, new Cast.Listener() {});
            mApiClient = new GoogleApiClient.Builder(mContext)
                    .addApi(Cast.API, apiOptionsBuilder.build())
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            Cast.CastApi.launchApplication(mApiClient, APPLICATION_ID).setResultCallback(
                                    new ResultCallback<Cast.ApplicationConnectionResult>() {
                                        @Override
                                        public void onResult(Cast.ApplicationConnectionResult applicationConnectionResult) {
                                            mRemoteMediaPlayer = new RemoteMediaPlayer();

//                                            MediaInfo mediaInfo = new MediaInfo.Builder("http://192.168.0.8/Movies/Memento.mp4")
//                                                    .setContentType("video/mp4")
//                                                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
//                                                    .build();

                                            mRemoteMediaPlayer.load(mApiClient, selectedMovie, true)
                                                    .setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                                                        @Override
                                                        public void onResult(RemoteMediaPlayer.MediaChannelResult mediaChannelResult) {

                                                        }
                                                    });

                                        }
                                    });
                        }

                        @Override
                        public void onConnectionSuspended(int i) {

                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                        }
                    })
                    .build();
            mApiClient.connect();

        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter iff= new IntentFilter("cast-a-movie");
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new AsyncTask<Void, Void, HashMap<String, MediaInfo>>(){
            @Override
            protected HashMap<String, MediaInfo> doInBackground(Void... voids) {
                return buildMedia("http://192.168.0.8:8000/Catalog");
            }

            @Override
            protected void onPostExecute(HashMap<String, MediaInfo> mediaInfos) {
                super.onPostExecute(mediaInfos);
                movies = mediaInfos;
            }
        }.execute();

        DiscoverCastDevices();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
    }

    private void DiscoverCastDevices() {
        APPLICATION_ID = "8B83D059";
        MediaRouter router =
        mMediaRouter = MediaRouter.getInstance(this);
        mMediaRouteSelector = new MediaRouteSelector.Builder().addControlCategory(
                CastMediaControlIntent.categoryForCast(APPLICATION_ID)).build();

        MediaRouter.Callback callback = new MediaRouter.Callback() {
            @Override
            public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route) {
                super.onRouteAdded(router, route);
                routes.add(route);
            }
        };

        mMediaRouter.addCallback(mMediaRouteSelector, callback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
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

    private JSONObject parseUrl(String urlString) {
        InputStream is = null;
        try {
            java.net.URL url = new java.net.URL(urlString);
            URLConnection urlConnection = url.openConnection();
            is = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream(), "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String json = sb.toString();
            return new JSONObject(json);
        } catch (Exception e) {
            Log.d("", "Failed to parse the json for media list", e);
            return null;
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private static String TAG_CATEGORIES = "categories";
    private static String TAG_NAME = "name";
    private static String TAG_STUDIO = "studio";
    private static String TAG_SOURCES = "sources";
    private static String TAG_SUBTITLE = "subtitle";
    private static String TAG_THUMB = "image-480x270"; // "thumb";
    private static String TAG_IMG_780_1200 = "image-780x1200";
    private static String TAG_TITLE = "title";
    private static String TAG_MEDIA = "videos";

    private HashMap<String, MediaInfo> buildMedia(String url) {

        try {
            HashMap<String, MediaInfo> mediaList = new HashMap<String, MediaInfo>();
            JSONObject jsonObj = parseUrl(url);
            JSONArray categories = jsonObj.getJSONArray(TAG_CATEGORIES);
            if (null != categories) {
                for (int i = 0; i < categories.length(); i++) {
                    JSONObject category = categories.getJSONObject(i);
                    category.getString(TAG_NAME);
                    JSONArray videos = category.getJSONArray(TAG_MEDIA);
                    if (null != videos) {
                        for (int j = 0; j < videos.length(); j++) {
                            JSONObject video = videos.getJSONObject(j);
                            String subTitle = video.getString(TAG_SUBTITLE);
                            JSONArray videoUrls = video.getJSONArray(TAG_SOURCES);
                            if (null == videoUrls || videoUrls.length() == 0) {
                                continue;
                            }
                            String videoUrl = videoUrls.getString(0);
                            String imageurl = video.getString(TAG_THUMB);
                            String bigImageurl = video.getString(TAG_IMG_780_1200);
                            String title = video.getString(TAG_TITLE);
                            String studio = video.getString(TAG_STUDIO);
                            mediaList.put(title.toLowerCase(), buildMediaInfo(title, studio, subTitle, videoUrl, imageurl,
                                    bigImageurl));
                        }
                    }
                }
            }
            return mediaList;
        }
        catch (Exception ex) {
            return new HashMap<String, MediaInfo>();
        }
    }

    private static MediaInfo buildMediaInfo(String title,
                                            String subTitle, String studio, String url, String imgUrl, String bigImageUrl) {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, subTitle);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, title);
        movieMetadata.putString(MediaMetadata.KEY_STUDIO, studio);
        movieMetadata.addImage(new WebImage(Uri.parse(imgUrl)));
        movieMetadata.addImage(new WebImage(Uri.parse(bigImageUrl)));

        return new MediaInfo.Builder(url)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("video/mp4")
                .setMetadata(movieMetadata)
                .build();
    }
}
