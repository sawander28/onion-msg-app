// SPDX-FileCopyrightText: 2022 Michael Pöhn <michael@poehn.at>
// SPDX-License-Identifier: MIT

package info.guardianproject.arti;

import android.content.Context;
import android.util.Log;

import java.io.File;

public class ArtiSocksProxy {

    /**
     * One shot call. If it works, Arti will be started in proxy mode like staring `arti proxy` in
     * a shell. If it fails there will be no error messages and no way to recover.
     *
     * default socks5 proxy: localhost:9150
     */
    public static void start(final File cacheDir, final File stateDir) {
        String artiResult = ArtiJNI.startArtiProxyJNI(cacheDir.getAbsolutePath(), stateDir.getAbsolutePath());
        Log.d("arti-android", "arti result: " + artiResult);
    }

    public static void start(Context context) {
        File artiCacheDir = new File(context.getCacheDir().getAbsolutePath() + "/arti_cache");
        artiCacheDir.mkdirs();

        File artiStateDir = new File(context.getFilesDir().getAbsolutePath() + "/arti_state");
        artiStateDir.mkdirs();

        ArtiSocksProxy.start(artiCacheDir, artiStateDir);
    }
}
