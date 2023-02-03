// SPDX-FileCopyrightText: 2022 Michael Pöhn <michael@poehn.at>
// SPDX-License-Identifier: MIT

package info.guardianproject.arti.sample;

import android.app.Application;

import info.guardianproject.arti.Arti;
import info.guardianproject.arti.ArtiSocksProxy;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Arti.initLogging();
        ArtiSocksProxy.start(this);
    }
}
