/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.nearby.messages.samples.nearbybackgroundbeacons;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * The main fragment for this sample. Exposes functionality to subscribe to broadcasts from nearby
 * beacons using Bluetooth Low Energy (BLE).
 * <p/>
 * Subscribing for messages broadcast by nearby beacons takes place in the background, and it is
 * triggered by calling
 * {@link com.google.android.gms.nearby.messages.Messages#subscribe(GoogleApiClient, PendingIntent,
 * SubscribeOptions)}.  Message subscription is long lived, and it can be cancelled by calling
 * {@link com.google.android.gms.nearby.messages.Messages#unsubscribe(GoogleApiClient,
 * PendingIntent)}.
 * <p/>
 * By default, background subscription discovers all messages published by this application and
 * other applications in the same Google Developers Console project. When
 * attaching messages to a beacon, you must be authenticated as a certain Developer Console
 * project; if you then use the API key associated with that project in this app, you should
 * find all the messages you attached.
 * <p/>
 * A real-world app would most likely persist received messages in a database and implement a
 * {@link android.content.ContentProvider}. This sample focuses on patterns for using
 * {@link com.google.android.gms.nearby.messages.Messages Nearby.Messages}, and to keep things
 * simple, stores received messages in {@link SharedPreferences}.
 * <p/>
 * Using {@link com.google.android.gms.nearby.messages.Messages Nearby.Messages} requires
 * user opt in, and attempts to subscribe to messages fails without the necessary permission.
 * When that happens, we present an opt-in dialog to the user, who can then authorize use of Nearby.
 * A permission-related error can occur because the user either never opted into using Nearby, or
 * disabled Nearby from Google Settings after opting in.
 */
public class MainActivityFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = MainActivityFragment.class.getSimpleName();

    // Constants for persisting values to Bundle.
    private static final String KEY_SUB_STATE = "sub-state";
    private static final String KEY_RESOLVING_ERROR = "resolving-error";

    // Enum to track subscription state.
    private enum SubState {
        NOT_SUBSCRIBING,
        ATTEMPTING_TO_SUBSCRIBE,
        SUBSCRIBING,
        ATTEMPTING_TO_UNSUBSCRIBE
    }

    // Views.
    private ProgressBar mSubscriptionProgressBar;
    private ImageButton mSubscriptionImageButton;

    /**
     * Adapter for working with messages from nearby beacons.
     */
    private ArrayAdapter<String> mNearbyMessagesArrayAdapter;

    /**
     * Backing data structure for {@code mNearbyMessagesArrayAdapter}.
     */
    private List<String> mNearbyMessagesList = new ArrayList<>();

    /**
     * Entry point to Google Play Services.
     */
    private GoogleApiClient mGoogleApiClient;

    // Fields for tracking subscription state.
    private SubState mSubState = SubState.NOT_SUBSCRIBING;

    /**
     * Tracks if we are currently resolving an error related to Nearby permissions. Used to avoid
     * duplicate Nearby permission dialogs if the user initiates both subscription and publication
     * actions without having opted into Nearby.
     */
    private boolean mResolvingError = false;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        if (savedInstanceState != null) {
            mSubState = (SubState) savedInstanceState.getSerializable(
                    KEY_SUB_STATE);
            mResolvingError = savedInstanceState.getBoolean((KEY_RESOLVING_ERROR));
        }
        mNearbyMessagesList = Utils.getCachedMessages(getActivity());

        final ListView nearbyMessagesListView = (ListView) view.findViewById(
                R.id.nearby_messages_list_view);
        mNearbyMessagesArrayAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(),
                android.R.layout.simple_list_item_1,
                mNearbyMessagesList);
        nearbyMessagesListView.setAdapter(mNearbyMessagesArrayAdapter);

        mSubscriptionImageButton = (ImageButton) view.findViewById(R.id.subscription_image_button);
        mSubscriptionImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                switch (mSubState) {
                    case NOT_SUBSCRIBING:
                    case ATTEMPTING_TO_UNSUBSCRIBE:
                        mSubState = SubState.ATTEMPTING_TO_SUBSCRIBE;
                        subscribe();
                        break;
                    case SUBSCRIBING:
                    case ATTEMPTING_TO_SUBSCRIBE:
                        mSubState = SubState.ATTEMPTING_TO_UNSUBSCRIBE;
                        unsubscribe();
                        break;
                }
                updateUI();
            }
        });
        mSubscriptionProgressBar = (ProgressBar) view.findViewById(R.id.subscription_progress_bar);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateUI();
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getSharedPreferences(getActivity().getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getActivity().getSharedPreferences(getActivity().getApplicationContext().getPackageName(),
                Context.MODE_PRIVATE)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_SUB_STATE, mSubState);
        outState.putBoolean(KEY_RESOLVING_ERROR, mResolvingError);
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "GoogleApiClient connected");
        executePendingSubscriptionTask();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // For simplicity, we don't handle connection failure thoroughly in this sample. Refer to
        // the following Google Play services doc for more details:
        // http://developer.android.com/google/auth/api-client.html
        Log.w(TAG, "connection to GoogleApiClient failed");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.w(TAG, "GoogleApiClient connection suspended: "
                + connectionSuspendedCauseToString(cause));
    }

    private static String connectionSuspendedCauseToString(int cause) {
        switch (cause) {
            case CAUSE_NETWORK_LOST:
                return "CAUSE_NETWORK_LOST";
            case CAUSE_SERVICE_DISCONNECTED:
                return "CAUSE_SERVICE_DISCONNECTED";
            default:
                return "CAUSE_UNKNOWN: " + cause;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (TextUtils.equals(key, Constants.KEY_CACHED_MESSAGES)) {
            List<String> messages = new ArrayList<>(Utils.getCachedMessages(getActivity()));
            mNearbyMessagesList.clear();
            for (String message : messages) {
                mNearbyMessagesList.add(message);
            }
            mNearbyMessagesArrayAdapter.notifyDataSetChanged();
        }
    }

    private void subscribe() {
        Log.i(TAG, "attempting to subscribe");

        // Clean start every time we start subscribing.
        Utils.clearCachedMessages(getActivity());
        mNearbyMessagesArrayAdapter.clear();

        if (!mGoogleApiClient.isConnected()) {
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
            return;
        }

        SubscribeOptions options = new SubscribeOptions.Builder()
                // Finds messages attached to BLE beacons. See
                // https://developers.google.com/beacons/
                .setStrategy(Strategy.BLE_ONLY)
                .build();

        Nearby.Messages.subscribe(mGoogleApiClient, getPendingIntent(), options)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "subscribed successfully");
                            mSubState = SubState.SUBSCRIBING;
                            // Start background service for handling the notification.
                            getActivity().startService(getBackgroundSubscribeServiceIntent());
                        } else {
                            Log.i(TAG, "could not subscribe");
                            handleUnsuccessfulNearbyResult(status);
                        }
                    }
                });
    }

    private void unsubscribe() {
        Log.i(TAG, "attempting to unsubscribe from background updates");
        if (!mGoogleApiClient.isConnected()) {
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
            return;
        }

        Nearby.Messages.unsubscribe(mGoogleApiClient, getPendingIntent())
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "unsubscribed successfully");
                            mSubState = SubState.NOT_SUBSCRIBING;
                            BackgroundSubscribeIntentService.cancelNotification(getActivity());
                        } else {
                            Log.i(TAG, "could not unsubscribe");
                            handleUnsuccessfulNearbyResult(status);
                        }
                    }
                });
    }

    private PendingIntent getPendingIntent() {
        return PendingIntent.getService(getActivity(), 0,
                getBackgroundSubscribeServiceIntent(), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    protected void finishedResolvingNearbyPermissionError() {
        mResolvingError = false;
    }


    private Intent getBackgroundSubscribeServiceIntent() {
        return new Intent(getActivity(), BackgroundSubscribeIntentService.class);
    }

    private void updateUI() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (mSubState) {
                    case ATTEMPTING_TO_SUBSCRIBE:
                    case SUBSCRIBING:
                        mSubscriptionProgressBar.setVisibility(View.VISIBLE);
                        mSubscriptionImageButton.setImageResource(R.drawable.ic_cancel);
                        break;
                    default:
                        mSubscriptionProgressBar.setVisibility(View.INVISIBLE);
                        mSubscriptionImageButton.setImageResource(R.drawable.ic_nearby);
                }
            }
        });
    }

    /**
     * Invokes a pending task based on the subscription and publication states.
     */
    void executePendingSubscriptionTask() {
        if (mSubState == SubState.ATTEMPTING_TO_SUBSCRIBE) {
            subscribe();
        } else if (mSubState == SubState.ATTEMPTING_TO_UNSUBSCRIBE) {
            unsubscribe();
        }
    }


    /**
     * Resets the state of pending subscription and publication tasks.
     */
    void resetToDefaultState() {
        mSubState = SubState.NOT_SUBSCRIBING;
        updateUI();
    }

    /**
     * Handles errors generated when performing a subscription or publication action. Uses
     * {@link Status#startResolutionForResult} to display an opt-in dialog to handle the case
     * where a device is not opted into using Nearby.
     */
    private void handleUnsuccessfulNearbyResult(Status status) {
        Log.i(TAG, "processing error, status = " + status);
        if (status.hasResolution()) {

            // This is to avoid showing the dialog twice.
            if (!mResolvingError) {
                try {
                    status.startResolutionForResult(getActivity(), Constants.REQUEST_RESOLVE_ERROR);
                    mResolvingError = true;
                } catch (IntentSender.SendIntentException unlikely) {
                    Log.e(TAG, "Exception when starting resolution", unlikely);
                }
            }
        } else if (!status.isSuccess()) {
            Log.e(TAG, "Could not resolve error. Status: " + status);
            resetToDefaultState();
        }
    }
}