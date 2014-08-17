package com.gattaca.watchapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

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

                                            MediaInfo mediaInfo = new MediaInfo.Builder("http://192.168.0.8/Movies/Memento.mp4")
                                                    .setContentType("video/mp4")
                                                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                                                    .build();

                                            mRemoteMediaPlayer.load(mApiClient, mediaInfo, true)
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
}
