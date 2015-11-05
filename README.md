## Android-nearby
Samples for Nearby APIs on Android

#Getting Started

These samples uses the Gradle build system. To build this project, use the "gradlew build" command. Or, use "Import Project" in Android Studio.

To use this sample, follow the following steps:

Create a project on Google Developer Console. Or, use an existing project.

Click on APIs & auth -> APIs, and enable Nearby Messages API.

Click on Credentials, then click on Create new key, and pick Android key. Then register your Android app's SHA1 certificate fingerprint and package name for your app.

Copy the API key generated, paste it in [an environment variable](https://www.google.com/#q=add+an+environment+variables) with the name `ANDROID_NEARBY_API_KEY`.

#Projects

[connections-quickstart](https://github.com/googlesamples/android-nearby/tree/master/connections-quickstart)

[NearbyDevices](https://github.com/googlesamples/android-nearby/tree/master/messages/NearbyDevices)
