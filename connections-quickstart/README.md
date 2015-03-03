# Nearby Connections Quickstart
Demonstrates basic usage of the Nearby Connections API to establish a connection between two
devices and send messages between them.

## Build
This sample uses the Gradle build system.  To build this project, use the `gradlew build` command
or import the project into Android Studio.

## Run
To run this sample you will need two (or more) physical Android devices with Google Play Services
installed.  You will need to connect both devices to the same WiFi network.

Run the application on both devices.  On on device click **Advertise** and on the other click
**Discover**.  On the discovering device select an endpoint from the 'Endpoint(s) Found' dialog, and
on the advertising device click 'Connect' when prompted to accept or reject the connection.  Now
the two devices are connected via Nearby Connections.  You can use the 'Message' field at the top
of the screen to enter a text message, and click **Send** to send the message to the other device.

## Next Steps
Visit [https://developers.google.com/games/services/android/nearby]
(https://developers.google.com/games/services/android/nearby) for more information on the Nearby
Connections API.
