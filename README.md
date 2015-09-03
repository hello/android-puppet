android-puppet
==============

The Android portion of Sense-Pill-Phone integration test rig.

Building
========

You will need the following prerequisites.

- [Java](http://support.apple.com/kb/DL1572) (on Yosemite).
- [Android Studio](http://developer.android.com/sdk/index.html).
- The [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) (for lambda support).
- The correct SDK and build tools. These will be automatically installed by Android Studio and the Square SDK manager gradle plugin.

If you're building the app on a platform other than OS X, you will need to define `JAVA_HOME` in order for the project to find your installation of the JDK 8.

Once the above conditions are satisfied, you should be able to perform a gradle sync and build and run.
