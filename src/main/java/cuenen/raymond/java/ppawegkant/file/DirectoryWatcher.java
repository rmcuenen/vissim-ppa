package cuenen.raymond.java.ppawegkant.file;

import cuenen.raymond.java.ppawegkant.MainApplication;
import cuenen.raymond.java.ppawegkant.configuration.SystemData;
import cuenen.raymond.java.ppawegkant.post.Message;
import cuenen.raymond.java.ppawegkant.processing.DataProcessor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author R. Cuenen
 */
public class DirectoryWatcher implements Runnable, FilenameFilter {

    private static final String FILE_NAME_MATCHER = ".*_[0-9]{6}_[0-9]{6}\\..*";
    private final SystemData systemData;
    private final AtomicBoolean watching = new AtomicBoolean(false);
    private long pollInterval = 1000L;
    private Thread watchThread;

    public DirectoryWatcher(SystemData systemData) {
        this.systemData = systemData;
        String value = MainApplication.getProperty("poll_interval");
        if (value != null) {
            try {
                pollInterval = Long.parseLong(value);
            } catch (NumberFormatException ex) {
                // Log warning message
            }
        }
    }

    public void start() {
        if (watchThread == null) {
            watchThread = new Thread(this, systemData.getIdentification() + "-Watch-Thread");
            watchThread.start();
            // Log debug message
        }
    }

    public void stop() {
        watching.set(false);
        if (watchThread != null) {
            watchThread.interrupt();
            try {
                watchThread.join(3000L);
                // Log debug messsage
            } catch (InterruptedException ex) {
                // Log warning message
            }
            watchThread = null;
        }
    }

    @Override
    public void run() {
        File directory = systemData.getDirectory();
        watching.set(true);
        do {
            File[] list = directory.listFiles(this);
            if (list != null) {
                for (File file : list) {
                    handleFile(file);
                }
            }
            sleep(pollInterval);
        } while (watching.get());
    }

    @Override
    public boolean accept(File dir, String name) {
        return name.matches(FILE_NAME_MATCHER);
    }

    private void sleep(long time) {
        do {
            long current = System.nanoTime();
            try {
                Thread.sleep(time);
            } catch (InterruptedException ex) {
                // Ignore
            }
            time -= (System.nanoTime() - current);
        } while (time > 0L);
    }

    private void handleFile(File file) {
        // Log debug message
        long diff = 0L;
        do {
            long lastModified = file.lastModified();
            sleep(100L);
            diff = file.lastModified() - lastModified;
        } while (diff > 0L);
        InputStream dataStream = null;
        try {
            dataStream = new FileInputStream(file);
            DataProcessor processor = systemData.getType().getDataProcessor();
            Message message = processor.process(file.getName(), dataStream, systemData);
            if (message != null) {
                MainApplication.sendMessage(message);
            }
            file.delete();
        } catch (IOException ex) {
            // Log error message
        } finally {
            if (dataStream != null) {
                try {
                    dataStream.close();
                } catch (IOException ex) {
                    // Ignore
                }
            }
        }
    }
}
