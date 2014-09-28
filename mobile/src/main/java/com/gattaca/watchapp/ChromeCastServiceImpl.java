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
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by epentangelo on 9/21/14.
 */
public class ChromeCastServiceImpl implements ChromeCastService {

    private static final String LOG_TAG = "ChromeCastService";
    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private String APPLICATION_ID;
    private GoogleApiClient mApiClient;
    private RemoteMediaPlayer _remoteMediaPlayer;

    private Map<String, MediaRouter.RouteInfo> routes = new HashMap<String, MediaRouter.RouteInfo>();
    private Activity _context;
    private String _remoteMediaPlayerCommand;
    private MediaInfo _selectedMovie;

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
                routes.put(route.getName(),route);
            }
        };

        mMediaRouter.addCallback(mMediaRouteSelector, callback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
    }

    @Override
    public void PlayMovie(final MediaInfo selectedMovie) {

        if (mApiClient != null) {
            try {
                Cast.CastApi.joinApplication(mApiClient, APPLICATION_ID).setResultCallback(new ResultCallback<Cast.ApplicationConnectionResult>() {
                    @Override
                    public void onResult(Cast.ApplicationConnectionResult applicationConnectionResult) {
                        _remoteMediaPlayerCommand = "Load";
                        _selectedMovie = selectedMovie;
                        if (isConnected()) {
                            try {
                                Cast.CastApi.requestStatus(mApiClient);
                                _remoteMediaPlayer.requestStatus(mApiClient);
                            } catch (Exception ex){
                                Log.d(LOG_TAG, "Failed to request status for load");
                            }
                        }
                    }
                });
            }
            catch (Exception ex){
                Log.d(LOG_TAG, ex.getMessage());
            }

        }
        else {
            ConnectToChromeCastDeviceAndPlay(selectedMovie);
        }
    }

    @Override
    public void Pause() {
        _remoteMediaPlayerCommand = "Pause";
        if (isConnected()) {
            try {
                Cast.CastApi.requestStatus(mApiClient);
                _remoteMediaPlayer.requestStatus(mApiClient);
            } catch (Exception ex){
              Log.d(LOG_TAG, "Failed to request status for pause");
            }
        }
    }

    @Override
    public void Play() {
        _remoteMediaPlayerCommand = "Play";
        if (isConnected()) {
            try {
                Cast.CastApi.requestStatus(mApiClient);
                _remoteMediaPlayer.requestStatus(mApiClient);
            } catch (Exception ex){
                Log.d(LOG_TAG, "Failed to request status for play");
            }
        }
    }

    @Override
    public void Stop() {
        _remoteMediaPlayerCommand = "Stop";
        if (isConnected()) {
            try {
                Cast.CastApi.requestStatus(mApiClient);
                _remoteMediaPlayer.requestStatus(mApiClient);
            } catch (Exception ex){
                Log.d(LOG_TAG, "Failed to request status for stop");
            }
        }
    }

    @Override
    public void Exit() {
        _remoteMediaPlayerCommand = "Exit";
        if (isConnected()) {
            try {
                Cast.CastApi.leaveApplication(mApiClient);
                mApiClient.disconnect();
                mApiClient = null;
            } catch (Exception ex){
                Log.d(LOG_TAG, "Failed to request status for exit");
            }
        }
    }

    private void ConnectToChromeCastDeviceAndPlay(final MediaInfo selectedMovie) {
        CastDevice device = CastDevice.getFromBundle(routes.get("Bedroom").getExtras());

        Log.d(LOG_TAG, "acquiring a connection to Google Play services for " + device);
        Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions.builder(device, new Cast.Listener() {});
        mApiClient = new GoogleApiClient.Builder(_context)
                .addApi(Cast.API, apiOptionsBuilder.build())
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        LaunchCastApplication(selectedMovie);
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

    private void LaunchCastApplication(final MediaInfo selectedMovie) {
        Cast.CastApi.launchApplication(mApiClient, APPLICATION_ID).setResultCallback(
                new ResultCallback<Cast.ApplicationConnectionResult>() {
                    @Override
                    public void onResult(Cast.ApplicationConnectionResult applicationConnectionResult) {
                        try {
                            attachMediaChannel();
                            LoadMovie(selectedMovie);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void LoadMovie(MediaInfo selectedMovie) {
        _remoteMediaPlayer.load(mApiClient, selectedMovie, true)
                .setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                    @Override
                    public void onResult(RemoteMediaPlayer.MediaChannelResult mediaChannelResult) {
                        Status status = mediaChannelResult.getStatus();
                        if (status.isSuccess()) {
                            Log.d(LOG_TAG, "Success!");
                        }
                    }
                });
    }


    private void attachMediaChannel() throws Exception {
        checkConnectivity();
        if (null == _remoteMediaPlayer) {
            _remoteMediaPlayer = new RemoteMediaPlayer();

            _remoteMediaPlayer.setOnStatusUpdatedListener(
                    new RemoteMediaPlayer.OnStatusUpdatedListener() {

                        @Override
                        public void onStatusUpdated() {
                            Log.d(LOG_TAG, "RemoteMediaPlayer::onStatusUpdated() is reached");

                            try {

                                if (Objects.equals(_remoteMediaPlayerCommand, "Load") && _selectedMovie != null) {
                                    _remoteMediaPlayer.load(mApiClient, _selectedMovie, true);
                                    _selectedMovie = null;
                                    _remoteMediaPlayerCommand = null;
                                }

                                MediaStatus mediaStatus = _remoteMediaPlayer.getMediaStatus();

                                if (mediaStatus != null) {
                                    if (mediaStatus.getPlayerState() == MediaStatus.PLAYER_STATE_PLAYING) {
                                        if (Objects.equals(_remoteMediaPlayerCommand, "Pause")) {
                                            _remoteMediaPlayer.pause(mApiClient);
                                            _remoteMediaPlayerCommand = null;
                                        }
                                    }

                                    if (mediaStatus.getPlayerState() == MediaStatus.PLAYER_STATE_PAUSED) {
                                        if (Objects.equals(_remoteMediaPlayerCommand, "Play")) {
                                            _remoteMediaPlayer.play(mApiClient);
                                            _remoteMediaPlayerCommand = null;
                                        }
                                    }

                                    if (mediaStatus.getPlayerState() == MediaStatus.PLAYER_STATE_PLAYING ||
                                            mediaStatus.getPlayerState() == MediaStatus.PLAYER_STATE_PAUSED) {
                                        if (Objects.equals(_remoteMediaPlayerCommand, "Stop")) {
                                            _remoteMediaPlayer.stop(mApiClient);
                                            _remoteMediaPlayerCommand = null;
                                        }
                                    }
                                }
                            }
                            catch (Exception ex) {
                                Log.d(LOG_TAG, ex.getMessage());
                            }
                        }
                    }
            );

            _remoteMediaPlayer.setOnMetadataUpdatedListener(
                    new RemoteMediaPlayer.OnMetadataUpdatedListener() {
                        @Override
                        public void onMetadataUpdated() {
                            Log.d(LOG_TAG,  "RemoteMediaPlayer::onMetadataUpdated() is reached");

                        }
                    }
            );

        }
        try {
            Log.d(LOG_TAG, "Registering MediaChannel namespace");
            Cast.CastApi.setMessageReceivedCallbacks(mApiClient, _remoteMediaPlayer.getNamespace(),
                    _remoteMediaPlayer);
        } catch (Exception e) {
            Log.d(LOG_TAG, "Failed to set up media channel", e);
        }
    }

    public boolean isConnected() {
        return (null != mApiClient) && mApiClient.isConnected();
    }

    public void checkConnectivity() throws Exception {
        if (!isConnected()) {
            throw new Exception("Not connected");
        }
    }


}
