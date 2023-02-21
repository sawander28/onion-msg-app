package info.guardianproject.arti;

class ArtiJNI {

    static {
        System.loadLibrary("arti_mobile_ex");
    }

    static native String startArtiProxyJNI(String cacheDir, String stateDir, Integer socksPort, Integer dnsPort);
}
