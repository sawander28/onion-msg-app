This is a shared project for exposing a standard API for Tor Arti to Android and iOS applications. 

****

This was based on the following projects:
- Trinity's https://gitlab.torproject.org/trinity-1686a/arti-mobile-example/
- Uniq's https://codeberg.org/uniqx/arti-android
- ahf's https://gitlab.torproject.org/ahf/arti-orbot-ios

***

For more explanations on what it is doing. You should read Arti documentation [for Android](https://gitlab.torproject.org/tpo/core/arti/-/blob/main/doc/Android.md) and [for iOS](https://gitlab.torproject.org/tpo/core/arti/-/blob/main/doc/iOS.md).

To build for Android:
- install Rust and Android Studio. Make sure you can run an Hello World with both.
- go in the common folder and run `make android`.
- take a coffee, or two.
- open the android folder in Android Studio or use gradle to build your app as usual.

To build for iOS:
- grab a Mac (you can't create an iOS app on a PC)
- install Rust and XCode. Make sure you can run a Hello World with both.
- go in the common folder and run `make ios`.
- take a coffee, or two.
- open the ios folder in XCode and compile your app as usual.
