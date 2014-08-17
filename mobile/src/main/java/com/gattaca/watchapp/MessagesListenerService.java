package com.gattaca.watchapp;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

public class MessagesListenerService extends WearableListenerService {

    private static final String LOG_TAG = "DataLayerSample";

    public MessagesListenerService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);

        String id = peer.getId();
        String name = peer.getDisplayName();

        Log.d(LOG_TAG, "Connected peer name & ID: " + name + "|" + id);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(LOG_TAG, "MessageEvent received: " + messageEvent.getData());
        //do work
        sendMessage(messageEvent.getData());
    }

    private void sendMessage(byte[] data) {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent("cast-a-movie");
        // You can also include some extra data.
        intent.putExtra("message", data);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}

