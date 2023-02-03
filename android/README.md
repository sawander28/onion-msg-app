<!--
SPDX-FileCopyrightText: 2022 Michael Pöhn <michael@poehn.at>
SPDX-License-Identifier: MIT
-->

# arti-android

WIP: This project provides a wrapper for running
[arti](https://gitlab.torproject.org/tpo/core/arti.git/) on Android.


## using tor/arti in your Android app

This project is still in a very early development stage and not ready for use.

When it gets ready, we'll update this section to illustrate how to fetch
arti-android from a maven repository and configure your network
libraries/clients to use arti.


## license

This project is publicly available under the MIT license and
[reuse](https://api.reuse.software/info/codeberg.org/uniqx/arti-android)
compliant.


## project anatomy

- `/arti` - Android library providing a Java/Kotlin API for arti
- `/arti-native` - rust based JNI implementations for the arti project
- `/sample` - android app illustrating using arti on android


## build and run the code

1. build binaries

   ```
   # initialize vm for cross-complining rust code
   vagrant up
   # do a release build
   tools/vgrnt-release-build
   ```

   This will build the native binaries (.so files) containing JNI bindings and
   arti for all supported ABIs (armeabi-v7a, arm64-v8a, x86\_64m, x86) and move
   them to the correct location for the gradle build process to automatically
   pick them up.

2. run sample app in android studio

   Open this project in android studio and start the `sample` project as you
   would start any other app.
