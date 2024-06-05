package info.guardianproject.artiservice;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import info.guardianproject.arti.ArtiProxy;

/**
 * This is a bound service for running an Arti proxy for accessing the Tor network.
 * <p><code><pre>
 *     TODO: add usage code sample
 * </pre></code></p>
 *
 * <p>
 * Internally this service is driven by a state machine with following states and transitions:
 * </p>
 *
 * <pre>
 *                +-------------+
 *                |             |
 *                |   Stopped   |
 *                |             |
 *                +-------------+
 *                  |        ^
 *                  v        |
 *   startBackgroundProxy()  |
 *                  |        |
 *                  |      stopProxy()
 *                  |        ^
 *                  v        |
 *           +-----------------------+
 *           |  RunningInBackground  |
 *           +-----------------------+
 * </pre>
 */
public class ArtiService extends Service {

    private ArtiProxy artiProxy;

    // private volatile boolean started;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Handler serviceHandler;

    public ArtiService() {
        // initialize handlers and the single background thread of this service
        HandlerThread serviceThread = new HandlerThread(ArtiService.class.getSimpleName() + ".thread");
        serviceThread.start();
        serviceHandler = new Handler(serviceThread.getLooper());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ArtiServiceBinder();
    }

    public class ArtiServiceBinder extends Binder {
        ArtiService getService() {
            return ArtiService.this;
        }
    }

    public interface ArtiServiceListener {
        void serviceConnected(ArtiServiceConnection serviceConnection);

        void serviceDisconnected(ArtiServiceConnection connection);

        void log(@NonNull String logLine);

        void proxyStarted();

        void proxyStopped();
    }

    private final ArtiServiceListeners listeners = new ArtiServiceListeners();

    /**
     * internal abstraction for holding multiple listeners from multiple service connections
     */
    class ArtiServiceListeners {
        private final List<ArtiServiceListener> listenerList = Collections.synchronizedList(new ArrayList<>());

        public void add(ArtiServiceListener listener) {
            listenerList.add(listener);
        }

        public void remove(ArtiServiceListener listener) {
            listenerList.remove(listener);
        }

        public void proxyStarted() {
            synchronized (listenerList) {
                for (ArtiServiceListener listener : listenerList) {
                    mainHandler.post(listener::proxyStarted);
                }
            }
        }

        public void proxyStopped() {
            synchronized (listenerList) {
                for (ArtiServiceListener listener : listenerList) {
                    mainHandler.post(listener::proxyStopped);
                }
            }
        }

        public void log(final String logLine) {
            synchronized (listenerList) {
                for (ArtiServiceListener listener : listenerList) {
                    mainHandler.post(() -> listener.log(logLine));
                }
            }
        }

        public void serviceConnected(ArtiServiceConnection connection) {
            synchronized (listenerList) {
                for (ArtiServiceListener listener : listenerList) {
                    mainHandler.post(() -> listener.serviceConnected(connection));
                }
            }
        }

        public void serviceDisconnected(ArtiServiceConnection connection) {
            synchronized (listenerList) {
                for (ArtiServiceListener listener : listenerList) {
                    mainHandler.post(() -> listener.serviceDisconnected(connection));
                }
            }
        }
    }


    public static class ArtiServiceConnection implements ServiceConnection {

        private ArtiService service;
        private final ArtiServiceListener listener;

        public ArtiServiceConnection(ArtiServiceListener listener) {
            this.listener = listener;
        }

        public void bindArtiService(Context context) {
            context.bindService(new Intent(context, ArtiService.class), this, Context.BIND_AUTO_CREATE);
        }

        public void unbindArtiService(Context context) {
            try {
                context.unbindService(this);
            } catch (IllegalArgumentException ignored) {
            }
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = ((ArtiServiceBinder) binder).getService();
            service.listeners.add(listener);
            service.listeners.serviceConnected(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            this.service.listeners.remove(listener);
            this.service.listeners.serviceDisconnected(this);
            this.service = null;
        }

        public void startProxy() {
            if (service != null) {
                service.serviceHandler.post(() -> service.stateMachine.startProxy());
            } else {
                Log.e("###", "service not connected, can't call startProxy");
            }

        }

        public void stopProxy() {
            if (service != null) {
                service.serviceHandler.post(() -> service.stateMachine.stopProxy());
            } else {
                Log.e("###", "service not connected, can't call stopProxy");
            }
        }
    }

    private final ServiceStateMachine stateMachine = new ServiceStateMachine();

    class ServiceStateMachine {
        private ServiceState state = new StoppedState(this);

        public void startProxy() {
            synchronized (this) {
                state.startProxy();
            }
        }

        public void stopProxy() {
            synchronized (this) {
                state.stopProxy();
            }
        }
    }

    /**
     * implementations of this interface are the states this service can be in
     * methods in this are the state transitions of this state machine
     */
    interface ServiceState {
        void startProxy();
        void stopProxy();
    }

    abstract class AbstractServiceState implements ServiceState {

        AbstractServiceState(ServiceStateMachine stateMachine) {
        }

        @Override
        public void startProxy() {
            throw new RuntimeException(String.format("%s doesn't implement startArtiProxy()", this.getClass().getSimpleName()));
        }

        @Override
        public void stopProxy() {
            throw new RuntimeException(String.format("%s doesn't implement stopArtiProxy()", this.getClass().getSimpleName()));
        }
    }

    class StoppedState extends AbstractServiceState {
        StoppedState(ServiceStateMachine stateMachine) {
            super(stateMachine);
            listeners.proxyStopped();
        }

        @Override
        public void startProxy() {
            stateMachine.state = new RunningInBackgroundState(stateMachine);
        }

        @Override
        public void stopProxy() {
            // no-op, arti not running, nothing to do.
        }
    }

    class RunningInBackgroundState extends AbstractServiceState {
        RunningInBackgroundState(ServiceStateMachine stateMachine) {
            super(stateMachine);

            if (artiProxy == null) {
                artiProxy = ArtiProxy.Builder(ArtiService.this).setLogListener(listeners::log).build();
            }
            artiProxy.start();

            listeners.proxyStarted();
        }

        @Override
        public void startProxy() {
            // no-op, is already running, nothing to do
        }

        @Override
        public void stopProxy() {
            stateMachine.state = new StoppedState(stateMachine);
        }
    }
}