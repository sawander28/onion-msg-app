// SPDX-FileCopyrightText: 2022 Michael Pöhn <michael@poehn.at>
// SPDX-License-Identifier: MIT

package info.guardianproject.sample.arti;

import android.app.Application;
import android.util.Log;

import java.io.File;
import IPtProxy.IPtProxy;

import info.guardianproject.arti.Arti;
import info.guardianproject.arti.ArtiProxy;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        /**
         * Arti.initLogging();
         * int socksPort = Arti.startSocksProxy(this);
         * Arti.wrapWebView();
         */

        File ptDir = new File(getCacheDir(), "pt_state");
        IPtProxy.setStateLocation(ptDir.getAbsolutePath());
        Log.d("###", String.format("obfs2 port: %d", IPtProxy.obfs2Port()));
        Log.d("###", String.format("obfs3 port: %d", IPtProxy.obfs3Port()));
        Log.d("###", String.format("obfs4 port: %d", IPtProxy.obfs4Port()));
        Log.d("###", String.format("obfs4 version: %s", IPtProxy.obfs4ProxyVersion()));

        Log.d("###", String.format("snowflake port: %d", IPtProxy.snowflakePort()));
        Log.d("###", String.format("snowflake version: %s", IPtProxy.snowflakeVersion()));

        //this does the three steps above
        // Arti.init(this);

        ArtiProxy artiProxy = ArtiProxy.Builder(this).build();
        artiProxy.start();
    }
}
