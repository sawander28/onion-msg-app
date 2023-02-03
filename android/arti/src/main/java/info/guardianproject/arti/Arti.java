// SPDX-FileCopyrightText: 2022 Michael Pöhn <michael@poehn.at>
// SPDX-License-Identifier: MIT

package info.guardianproject.arti;

import android.util.Log;

public class Arti {
    private static Boolean logInitialized = false;

    public static void initLogging() {
        // make sure tracing subscriber is only ever called once
        // otherwise (according to docs) the app will crash without error message.
        synchronized (logInitialized) {
            if (!logInitialized) {
                logInitialized = true;
                Log.d("arti-android", "Arti.initLogging() called for the first time, initializing");
                ArtiJNI.initLogging();
            } else {
                Log.d("arti-android", "Arti.initLogging() called, while logging was already initialized");
            }
        }
    }
}
