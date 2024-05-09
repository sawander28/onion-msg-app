package info.guardianproject.arti;

import android.content.Context;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import java.io.File;

/**
 * Provides a SOCKS5 proxy and DNS resolver for using Arti to access the Tor network.
 *
 * <p>
 *   Here's an examples:
 *   <code><pre>
 *     ArtiProxy p = ArtiProxy.Builder(context).build();
 *     p.start();
 *     ...
 *     p.stop();
 *   </pre></code>
 * </p>
 *
 * <p>
 *   ArtiProxy is not designed to be thread safe. If you need high level access to Arti consider
 *   using ArtiService instead.
 * </p>
 */
public class ArtiProxy {

    private static final int DEFAULT_SOCKS_PORT = 9150;
    private static final int DEFAULT_DNS_PORT = 9151;

    // private final Handler mainHandler = new Handler(Looper.getMainLooper());
    // private final Handler localHandler;

    private final int socksPort;
    private final int dnsPort;
    private final List<String> bridgeLines;
    private final String cacheDir;
    private final String stateDir;
    private final ArtiLogListener logCallback;

    private ArtiProxy(ArtiProxyBuilder builder) {

        socksPort = builder.socksPort;
        dnsPort = builder.dnsPort;
        bridgeLines = Collections.unmodifiableList(builder.bridgeLines);
        cacheDir = builder.cacheDir.getAbsolutePath();
        stateDir = builder.stateDir.getAbsolutePath();
        logCallback = builder.logListener;

        // HandlerThread localThread = new HandlerThread(ArtiProxy.class.getSimpleName() + ".thread");
        // localThread.start();
        // localHandler = new Handler(localThread.getLooper());
    }

    public void start() {
        // localHandler.post(() -> {
            ArtiJNI.startArtiProxyJNI(
                    cacheDir,
                    stateDir,
                    null,
                    bridgeLines.isEmpty() ? null : bridgeLines.get(0),  // TODO support multiple
                    socksPort,
                    dnsPort,
                    logLine -> logCallback.log(logLine)
            );
        // });
    }

    public void stop() {
        // TODO: implement stop
    }

    public static ArtiProxyBuilder Builder(Context context) {
        if (context == null) {
            // fail early and make sure devs get a proper error massage
            // we might want to add appropriate annotations too
            throw new NullPointerException(
                    "Can not initialize ArtiProxy.Builder(Context context): context must not be null.");
        }

        return new ArtiProxyBuilder(context.getApplicationContext());
    }

    public int getSocksPort() {
        return socksPort;
    }

    public int getDnsPort() {
        return dnsPort;
    }

    public String getCacheDir() {
        return cacheDir;
    }

    public String getStateDir() {
        return stateDir;
    }

    public ArtiLogListener getLogCallback() {
        return logCallback;
    }

    public static class ArtiProxyBuilder {

        private int socksPort = DEFAULT_SOCKS_PORT;
        private int dnsPort = DEFAULT_DNS_PORT;
        private List<String> bridgeLines = new ArrayList<>();
        private File cacheDir = null;
        private File stateDir = null;
        private ArtiLogListener logListener;

        private final Context context;

        private ArtiProxyBuilder(Context context) {
            this.context = context.getApplicationContext();
        }

        /**
         * Change the local SOCKS5 port Arti will provide for accessing the Tor network.
         * (defaults to 9150)
         */
        public ArtiProxyBuilder setSocksPort(int socksPort) {
            this.socksPort = socksPort;
            return this;
        }

        /**
         * Change the local dns resolver port Arti will provide for retrieving domain
         * names over the the Tor network. (defaults to 9151)
         */
        public ArtiProxyBuilder setDnsPort(int dnsPort) {
            this.dnsPort = dnsPort;
            return this;
        }

        public ArtiProxyBuilder setBridgeLines(List<String> bridgeLines) {
            this.bridgeLines = bridgeLines;
            return this;
        }

        /**
         * Change the directory where Arti stores its cached data.
         */
        public void setCacheDir(File cacheDir) {
            this.cacheDir = cacheDir;
        }

        /**
         * Change the directory where Arti stores its persistent data.
         */
        public void setStateDir(File stateDir) {
            this.stateDir = stateDir;
        }

        /**
         * Register a listener for receiving the log output of Arti.
         */
        public ArtiProxyBuilder setLogListener(ArtiLogListener logListener) {
            this.logListener = logListener;
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

            if (logListener == null) {
                // Create empty callback is nothing was registered, so so the arti jni
                // implementation always has an object to call and never has to worry that it's not
                // initialized. This implementation just ignores all logLines sent from arti.
                logListener = logLine -> {
                };
            }

            return new ArtiProxy(this);
        }
    }
}
