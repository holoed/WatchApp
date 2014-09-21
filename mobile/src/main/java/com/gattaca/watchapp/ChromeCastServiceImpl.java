package com.gattaca.watchapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;

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

/**
 * Created by epentangelo on 9/21/14.
 */
public class ChromeCastServiceImpl implements ChromeCastService {

    private static final String LOG_TAG = "ChromeCastService";
    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private String APPLICATION_ID;
    private GoogleApiClient mApiClient;
    private RemoteMediaPlayer mRemoteMediaPlayer;

    private List<MediaRouter.RouteInfo> routes = new ArrayList<MediaRouter.RouteInfo>();
    private Activity _context;

    public ChromeCastServiceImpl(Activity context) {

        _context = context;
    }

    @Override
    public void DiscoverCastDevices() {
        APPLICATION_ID = "8B83D059";
        mMediaRouter = MediaRouter.getInstance(_context);
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
    public void PlayMovie(final MediaInfo selectedMovie) {
        CastDevice device = CastDevice.getFromBundle(routes.get(0).getExtras());

        Log.d(LOG_TAG, "acquiring a connection to Google Play services for " + device);
        Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions.builder(device, new Cast.Listener() {});
        mApiClient = new GoogleApiClient.Builder(_context)
                .addApi(Cast.API, apiOptionsBuilder.build())
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Cast.CastApi.launchApplication(mApiClient, APPLICATION_ID).setResultCallback(
                                new ResultCallback<Cast.ApplicationConnectionResult>() {
                                    @Override
                                    public void onResult(Cast.ApplicationConnectionResult applicationConnectionResult) {
                                        mRemoteMediaPlayer = new RemoteMediaPlayer();


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
}
