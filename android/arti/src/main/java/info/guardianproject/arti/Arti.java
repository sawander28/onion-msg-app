// SPDX-FileCopyrightText: 2022 Michael Pöhn <michael@poehn.at>
// SPDX-License-Identifier: MIT

package info.guardianproject.arti;

import android.content.Context;
import android.util.Log;

import androidx.webkit.ProxyConfig;
import androidx.webkit.ProxyController;

import java.io.File;

public class Arti {

    public final static int SOCKS_PORT = 9150;

    /**
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
    }**/

    /**
     * One shot call. If it works, Arti will be started in proxy mode like staring `arti proxy` in
     * a shell. If it fails there will be no error messages and no way to recover.
     *
     * default socks5 proxy: localhost:9150
     */
    public static void startSocksProxy(final File cacheDir, final File stateDir) {
        String artiResult = ArtiJNI.startArtiProxyJNI(cacheDir.getAbsolutePath(), stateDir.getAbsolutePath());
        Log.d("arti-android", "arti result: " + artiResult);
    }

    public static int startSocksProxy(Context context) {
        File artiCacheDir = new File(context.getCacheDir().getAbsolutePath() + "/arti_cache");
        artiCacheDir.mkdirs();

        File artiStateDir = new File(context.getFilesDir().getAbsolutePath() + "/arti_state");
        artiStateDir.mkdirs();

        startSocksProxy(artiCacheDir, artiStateDir);

        return SOCKS_PORT;
    }

    public static void wrapWebView () {
        String proxyHost = "socks://127.0.0.1:9150";

        ProxyConfig proxyConfig = new ProxyConfig.Builder()
                .addProxyRule(proxyHost) // proxy for tor
                .addDirect().build();
        ProxyController.getInstance().setProxyOverride(proxyConfig, command -> {
            //do nothing
        }, () -> {

        });
    }

    public static void init (Context context) {
    //    initLogging();
        startSocksProxy(context);
        wrapWebView();
    }
}
