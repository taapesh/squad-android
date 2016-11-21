package com.squad.a2pm.squad_android.activity;

import android.os.Bundle;
import android.util.Log;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.squad.a2pm.squad_android.model.Squad;
import com.squad.a2pm.squad_android.model.User;
import com.squad.a2pm.squad_android.util.Constants;
import com.squad.a2pm.squad_android.util.JsonUtil;

import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SquadActivity extends BaseActivity {
    protected User user;
    protected Squad squad;
    protected PubNub pubnub;
    protected String channelName = "squad";
    private List<String> pubSubChannel;
    private SubscribeCallback subCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: Check for user/squad and then initPubnub and channels after success
        initPubnub();
        initChannels();
        subscribe();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectAndCleanup();
    }

    @Override
    public void onResume() {
        super.onResume();
        // TODO: Check for user/squad before subscribing
        this.subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.unsubscribe();
    }

    private void initPubnub() {
        PNConfiguration config = new PNConfiguration();
        config.setSubscribeKey(Constants.SUBSCRIBE_KEY);
        config.setPublishKey(Constants.PUBLISH_KEY);
        config.setSecure(true);
        this.pubnub = new PubNub(config);
    }

    public void subscribe() {
        this.pubnub.subscribe().channels(this.pubSubChannel).execute();
    }

    public void unsubscribe() {
        if (this.pubnub != null) {
            this.pubnub.unsubscribe().channels(this.pubSubChannel).execute();
        }
    }

    private void initChannels() {
        // TODO: Replace channel name with dynamic squad ID
        this.pubSubChannel = Collections.singletonList(this.channelName);

        this.subCallback = new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
                /*
                switch (status.getCategory()) {
                    // for common cases to handle, see: https://www.pubnub.com/docs/java/pubnub-java-sdk-v4
                     case PNStatusCategory.PNConnectedCategory:
                     case PNStatusCategory.PNUnexpectedDisconnectCategory:
                     case PNStatusCategory.PNReconnectedCategory:
                     case PNStatusCategory.PNDecryptionErrorCategory:
                }
                */

                // no status handling for simplicity
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                try {
                    Log.v("PubNub", "message(" + JsonUtil.asJson(message) + ")");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
                // no presence handling for simplicity
            }
        };

        this.pubnub.addListener(this.subCallback);
    }

    public void publish(final Map<String, String> data) {
        PNCallback<PNPublishResult> pubCallback = new PNCallback<PNPublishResult>() {
            @Override
            public void onResponse(PNPublishResult result, PNStatus status) {
                try {
                    if (!status.isError()) {
                        Log.d("PubNub", "publish(" + JsonUtil.asJson(result) + ")");
                    } else {
                        Log.d("PubNub", "publishErr(" + JsonUtil.asJson(status) + ")");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        this.pubnub.publish()
                .channel(this.channelName)
                .message(data)
                .async(pubCallback);
    }

    private void disconnectAndCleanup() {
        if (this.pubnub != null) {
            this.pubnub.unsubscribe().channels(this.pubSubChannel).execute();
            this.pubnub.removeListener(this.subCallback);
            this.pubnub = null;
        }
    }
}
