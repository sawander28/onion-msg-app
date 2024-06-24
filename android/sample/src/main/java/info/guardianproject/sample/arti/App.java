// SPDX-FileCopyrightText: 2022 Michael Pöhn <michael@poehn.at>
// SPDX-License-Identifier: MIT

package info.guardianproject.sample.arti;

import android.app.Application;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import IPtProxy.IPtProxy;

import info.guardianproject.arti.ArtiProxy;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

//        Log.d("###", String.format("obfs2 port: %d", IPtProxy.obfs2Port()));
//        Log.d("###", String.format("obfs3 port: %d", IPtProxy.obfs3Port()));
//        Log.d("###", String.format("obfs4 port: %d", IPtProxy.obfs4Port()));
//        Log.d("###", String.format("lyrebird version: %s", IPtProxy.lyrebirdVersion()));
//        Log.d("###", String.format("snowflake port: %d", IPtProxy.snowflakePort()));
//        Log.d("###", String.format("snowflake version: %s", IPtProxy.snowflakeVersion()));

        IPtProxy.setStateLocation(new File(getCacheDir(), "pt_state").getAbsolutePath());
        // run obfs4/lyrebird client
//        IPtProxy.startLyrebird("DEBUG", false, false, null);

        // run snowflake client (values copied from )
        // TODO: fix this, once we've updated to the latest iptproxy version
        final String stunServers = "stun:stun.l.google.com:19302,stun:stun.antisip.com:3478,stun:stun.bluesip.net:3478,stun:stun.dus.net:3478,stun:stun.epygi.com:3478,stun:stun.sonetel.com:3478,stun:stun.sonetel.net:3478,stun:stun.stunprotocol.org:3478,stun:stun.uls.co.za:3478,stun:stun.voipgate.com:3478,stun:stun.voys.nl:3478";
        final String target = "https://snowflake-broker.torproject.net.global.prod.fastly.net/";
        final String front = "github.githubassets.com";
        final String ampCache = "https://cdn.ampproject.org/";
        // IPtProxy.startSnowflake(stunServers, target, front, ampCache, null, null, null, false, false);
        IPtProxy.startSnowflake(
                stunServers, // String ice,
                target, //String url,
                front, // String fronts,
                null, // ampCache, // String ampCache,
                null, // String sqsQueueURL,
                null, // String sqsCredsStr,
                null, // String logFile,
                false, // boolean logToStateDir,
                false, // boolean keepLocalAddresses,
                false, // boolean unsafeLogging,
                1 // long maxPeers
        );

//        List<String> bridgeLines = Arrays.asList(
//                // NOTICE: you'll need to provide bridge lines to make this work!
//                "snowflake 192.0.2.3:80 2B280B23E1107BB62ABFC40DDCC8824814F80A72 fingerprint=2B280B23E1107BB62ABFC40DDCC8824814F80A72 url=https://snowflake-broker.torproject.net.global.prod.fastly.net/ front=github.githubassets.com ice=stun:stun.l.google.com:19302,stun:stun.antisip.com:3478,stun:stun.bluesip.net:3478,stun:stun.dus.net:3478,stun:stun.epygi.com:3478,stun:stun.sonetel.com:3478,stun:stun.uls.co.za:3478,stun:stun.voipgate.com:3478,stun:stun.voys.nl:3478 utls-imitate=hellorandomizedalpn",
//                "snowflake 192.0.2.4:80 8838024498816A039FCBBAB14E6F40A0843051FA fingerprint=8838024498816A039FCBBAB14E6F40A0843051FA url=https://snowflake-broker.torproject.net.global.prod.fastly.net/ front=github.githubassets.com ice=stun:stun.l.google.com:19302,stun:stun.antisip.com:3478,stun:stun.bluesip.net:3478,stun:stun.dus.net:3478,stun:stun.epygi.com:3478,stun:stun.sonetel.net:3478,stun:stun.uls.co.za:3478,stun:stun.voipgate.com:3478,stun:stun.voys.nl:3478 utls-imitate=hellorandomizedalpn"
//        );

        // initialize and start ArtiProxy

//        ArtiProxy artiProxy = ArtiProxy.Builder(this)
//                // .setUnmanagedSnowflakeClientPort((int) IPtProxy.snowflakePort())
//                .setBridgeLines(bridgeLines)
//                .setSnowflakePort((int) IPtProxy.snowflakePort())
//                .setLogListener((log) -> {Log.e("artilog", log);})
//                .build();
//        artiProxy.start();
    }

    public void connectTorDirect() {
        ArtiProxy artiProxy = ArtiProxy.Builder(this)
                // .setUnmanagedSnowflakeClientPort((int) IPtProxy.snowflakePort())
//                .setSnowflakePort((int) IPtProxy.snowflakePort())
                .setLogListener((log) -> {Log.e("artilog", log);})
                .build();
        artiProxy.start();
    }

    public void connectWithLyrebird(int port, List<String> bridgeLines) {
        IPtProxy.startLyrebird("DEBUG", false, false, null);

        ArtiProxy artiProxy = ArtiProxy.Builder(this)
                // .setUnmanagedSnowflakeClientPort((int) IPtProxy.snowflakePort())
                //.setObfs4Port()
                //.setBridgeLines()
                .setSnowflakePort((int) IPtProxy.snowflakePort())
                .setLogListener((log) -> {Log.e("artilog", log);})
                .build();
        artiProxy.start();
    }

}
