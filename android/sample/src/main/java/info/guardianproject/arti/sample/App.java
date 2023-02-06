// SPDX-FileCopyrightText: 2022 Michael Pöhn <michael@poehn.at>
// SPDX-License-Identifier: MIT

package info.guardianproject.arti.sample;

import android.app.Application;

import info.guardianproject.arti.Arti;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        /**
         * Arti.initLogging();
         * int socksPort = Arti.startSocksProxy(this);
         * Arti.wrapWebView();
         */

        //this does the three steps above
        Arti.init(this);

    }
}
