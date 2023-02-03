// SPDX-FileCopyrightText: 2022 Michael Pöhn <michael@poehn.at>
// SPDX-License-Identifier: MIT

package info.guardianproject.arti;

class ArtiJNI {

    static {
        System.loadLibrary("arti_mobile_ex");
    }

    static native String startArtiProxyJNI(String cacheDir, String stateDir);

    static native void initLogging();
}
