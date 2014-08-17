package com.gattaca.watchapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableStatusCodes;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getName();
    public static final String PATH = "/start/Foo";

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        googleApiClient.connect();

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                final Button btn = (Button)stub.findViewById(R.id.button);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fireMessage();
                    }
                });
            }

            private void fireMessage() {
                // Send the RPC
                PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient);
                nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult result) {
                        for (int i = 0; i < result.getNodes().size(); i++) {
                            Node node = result.getNodes().get(i);
                            String nName = node.getDisplayName();
                            String nId = node.getId();
                            Log.d(TAG, "Node name and ID: " + nName + " | " + nId);

                            Wearable.MessageApi.addListener(googleApiClient, new MessageApi.MessageListener() {
                                @Override
                                public void onMessageReceived(MessageEvent messageEvent) {
                                    Log.d(TAG, "Message received: " + messageEvent);
                                }
                            });

                            PendingResult<MessageApi.SendMessageResult> messageResult = Wearable.MessageApi.sendMessage(googleApiClient, node.getId(),
                                    PATH, null);
                            messageResult.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                                @Override
                                public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                    Status status = sendMessageResult.getStatus();
                                    Log.d(TAG, "Status: " + status.toString());
                                    if (status.getStatusCode() != WearableStatusCodes.SUCCESS) {
                                        //alertButton.setProgress(-1);
                                        //label.setText("Tap to retry. Alert not sent :(");
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
    }
}
