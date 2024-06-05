package info.guardianproject.arti;


/**
 * Callback class for mapping log messages from Arti to JVM.
 */
public interface ArtiLogListener {
    void log(String logLine);
}
