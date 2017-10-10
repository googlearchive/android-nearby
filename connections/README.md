# Nearby Connections

[Nearby Connections](https://developers.google.com/nearby/connections/overview)
is a peer-to-peer networking API that allows apps to easily discover,
connect to, and exchange data with nearby devices in real-time, regardless
of network connectivity.

Where to Download
---------------
```groovy
dependencies {
  compile 'com.google.android.gms:play-services-nearby:14.0.0'
}
```

Permissions
-----------
The following permissions are required in your AndroidManfiest.xml
```xml
<!-- Required for Nearby Connections -->
  <uses-permission android:name="android.permission.BLUETOOTH" />
  <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
  <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
  <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
  <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

Advertising
-------------------
Devices can advertise themselves to other nearby devices discovering with the same
service id and strategy.
```java
Nearby.getConnectionsClient(this)
  .startAdvertising(
    /* endpointName= */ "Device A",
    /* serviceId= */ "com.example.package_name",
    mConnectionLifecycleCallback,
    new AdvertisingOptions(Strategy.P2P_CLUSTER));
```

Discovery
-------------------
A discovering device will be notified of nearby advertisers through their
EndpointDiscoveryCallback.
```java
Nearby.getConnectionsClient(this)
  .startDiscovery(
    /* serviceId= */ "com.example.package_name",
    mEndpointDiscoveryCallback,
    new DiscoveryOptions(Strategy.P2P_CLUSTER));
```

Connect
-------------------
When ready, the discoverer can request a connection to the advertiser.
```java
Nearby.getConnectionsClient(this)
  .requestConnection(
    /* endpointName= */ "Device B",
    advertiserEndpointId,
    mConnectionLifecycleCallback);
```

Both devices are then asked to confirm the connection. An authentication token
is provided that can be manually (typically visually) confirmed.
```java
private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
  new ConnectionLifecycleCallback() {
    @Override
    public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
      // Automatically accept the connection on both sides.
      Nearby.getConnectionsClient(this).acceptConnection(endpointId, mPayloadCallback);
    }

    @Override
    public void onConnectionResult(String endpointId, ConnectionResolution result) {
      switch (result.getStatus().getStatusCode()) {
        case ConnectionsStatusCodes.STATUS_OK:
          // We're connected! Can now start sending and receiving data.
          break;
        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
          // The connection was rejected by one or both sides.
          break;
        default:
          // The connection was broken before it was accepted.
          break;
      }
    }

    @Override
    public void onDisconnected(String endpointId) {
      // We've been disconnected from this endpoint. No more data can be
      // sent or received.
    }
  };
```

Transfer Payloads
-------------------
Once connected, devices can send payloads to each other.
```java
Nearby.getConnectionsClient(this).sendPayload(endpointId, payload);
```

The payload will appear on the other device via the PayloadCallback
passed in during acceptConnection.
```java
private final PayloadCallback mPayloadCallback =
  new PayloadCallback() {
    @Override
    public void onPayloadReceived(String endpointId, Payload payload) {
      // A new payload is being sent over.
    }

    @Override
    public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
      // Payload progress has updated.
    }
  };
```
