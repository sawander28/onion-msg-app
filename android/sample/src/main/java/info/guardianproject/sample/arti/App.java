// SPDX-FileCopyrightText: 2022 Michael Pöhn <michael@poehn.at>
// SPDX-License-Identifier: MIT

package info.guardianproject.sample.arti;

import android.app.Application;
import android.util.Log;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import IPtProxy.IPtProxy;

import info.guardianproject.arti.ArtiProxy;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("###", String.format("obfs2 port: %d", IPtProxy.obfs2Port()));
        Log.d("###", String.format("obfs3 port: %d", IPtProxy.obfs3Port()));
        Log.d("###", String.format("obfs4 port: %d", IPtProxy.obfs4Port()));
        Log.d("###", String.format("lyrebird version: %s", IPtProxy.lyrebirdVersion()));
        Log.d("###", String.format("snowflake port: %d", IPtProxy.snowflakePort()));
        Log.d("###", String.format("snowflake version: %s", IPtProxy.snowflakeVersion()));

        IPtProxy.setStateLocation(new File(getCacheDir(), "pt_state").getAbsolutePath());
        // run obfs4/lyrebird client
        IPtProxy.startLyrebird("DEBUG", false, false, null);

        // run snowflake client
        // TODO: fix this, once we've updated to the latest iptproxy version
        // final String stunServers = "stun:stun.l.google.com:19302,stun:stun.antisip.com:3478,stun:stun.bluesip.net:3478,stun:stun.dus.net:3478,stun:stun.epygi.com:3478,stun:stun.sonetel.com:3478,stun:stun.sonetel.net:3478,stun:stun.stunprotocol.org:3478,stun:stun.uls.co.za:3478,stun:stun.voipgate.com:3478,stun:stun.voys.nl:3478";
        // final String target = "https://snowflake-broker.torproject.net.global.prod.fastly.net/";
        // final String front = "github.githubassets.com";
        // final String ampCache = "https://cdn.ampproject.org/";
        // IPtProxy.startSnowflake(stunServers, target, front, ampCache, null, true, false, false, 1);

        // initialize and start ArtiProxy
        List<String> bridgeLines = Arrays.asList(
            // NOTICE: you'll need to provide bridge lines to make this work!
        );
        ArtiProxy artiProxy = ArtiProxy.Builder(this)
                // .setUnmanagedSnowflakeClientPort((int) IPtProxy.snowflakePort())
                .setBridgeLines(bridgeLines)
                // .setLogListener((log) -> {Log.e("arti", log);})
                .build();
        artiProxy.start();

    }
}
