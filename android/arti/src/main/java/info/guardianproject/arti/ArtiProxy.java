package info.guardianproject.arti;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.File;

/**
 * Create a Socks5 proxy + DNS resolver for using Arti.
 * <p>
 * Here are some usage examples:
 * <p>
 * ```
 * ArtiProxy p = ArtiProxy.Builder(context).build();
 * p.start();
 * ...
 * p.stop();
 * ```
 */
public class ArtiProxy {

    private static final int DEFAULT_SOCKS_PORT = 9150;
    private static final int DEFAULT_DNS_PORT = 9151;
    private final ArtiProxyBuilder args;

    Handler mainHandler = new Handler(Looper.getMainLooper());

    private ArtiProxy(ArtiProxyBuilder builder) {
        this.args = builder;
    }

    public void start() {
        ArtiJNI.startArtiProxyJNI(
                args.cacheDir.getAbsolutePath(),
                args.stateDir.getAbsolutePath(),
                null,
                null,
                args.socksPort,
                args.dnsPort,
                logLine -> {
                    // Wrap this into additional listener which makes sure callback gets called on
                    // main thread.
                    mainHandler.post(() -> args.logCallback.log(logLine));
                }
        );
    }

    public static ArtiProxyBuilder Builder(Context context) {
        if (context == null) {
            // fail early and make sure devs get a proper error massage
            // we might want to add appropriate annotations too
            throw new NullPointerException(
                    "Can not initialize ArtiProxy.Builder(Context context): context must not be null.");
        }

        return new ArtiProxyBuilder(context);
    }

    public void stop() {
        // TODO: implement stop
    }

    public static class ArtiProxyBuilder {
        private int socksPort = DEFAULT_SOCKS_PORT;
        private int dnsPort = DEFAULT_DNS_PORT;
        private File cacheDir = null;
        private File stateDir = null;
        private ArtiLoggingCallback logCallback;

        private final Context context;

        private ArtiProxyBuilder(Context context) {
            this.context = context.getApplicationContext();
        }

        public ArtiProxyBuilder setSocksPort(int socksPort) {
            this.socksPort = socksPort;
            return this;
        }

        public ArtiProxyBuilder setDnsPort(int dnsPort) {
            this.dnsPort = dnsPort;
            return this;
        }

        public ArtiProxyBuilder setLogCallback(ArtiLoggingCallback logCallback) {
            this.logCallback = logCallback;
            return this;
        }

        public ArtiProxy build() {
            if (cacheDir == null) {
                cacheDir = new File(context.getCacheDir().getAbsolutePath() + "/arti_cache");
            }
            cacheDir.mkdirs();

            if (stateDir == null) {
                stateDir = new File(context.getFilesDir().getAbsolutePath() + "/arti_state");
            }
            stateDir.mkdirs();

            if (logCallback == null) {
                // Create empty callback is nothing was registered, so so the arti jni
                // implementation always has an object to call and never has to worry that it's not
                // initialized. This implementation just ignores all logLines sent from arti.
                logCallback = logLine -> {
                };
            }

            return new ArtiProxy(this);
        }
    }
}
