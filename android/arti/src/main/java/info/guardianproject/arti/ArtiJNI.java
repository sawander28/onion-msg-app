package info.guardianproject.arti;

class ArtiJNI {

    static {
        System.loadLibrary("arti_mobile_ex");
    }

    static native String startArtiProxyJNI(
            String cacheDir,
            String stateDir,
            int obfs4Port,
            int snowflakePort,
            String obfs4proxyPath,
            String bridgeLine,
            int socksPort,
            int dnsPort,
            ArtiLogListener loggingCallback);
}
