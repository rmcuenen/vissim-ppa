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

    private final SystemData systemData;
    private final AtomicBoolean watching = new AtomicBoolean(false);
    private Thread watchThread;

    public DirectoryWatcher(SystemData systemData) {
        this.systemData = systemData;
    }

    public void start() {
        if (watchThread == null) {
            watchThread = new Thread(this, systemData.getIdentification() + "-Watch-Thread");
            watchThread.start();
        }
    }

    public void stop() {
        watching.set(false);
        if (watchThread != null) {
            watchThread.interrupt();
            try {
                watchThread.join(3000L);
            } catch (InterruptedException ex) {
                // Log warning message
            }
            watchThread = null;
        }
    }

    @Override
    public void run() {
        long interval = 1000L;
        String value = MainApplication.getProperty("poll_interval");
        if (value != null) {
            try {
                interval = Long.parseLong(value);
            } catch (NumberFormatException ex) {
                // Log warning message
            }
        }
        File directory = systemData.getDirectory();
        watching.set(true);
        do {
            File[] list = directory.listFiles(this);
            if (list != null) {
                for (File file : list) {
                    handleFile(file);
                }
            }
            sleep(interval);
        } while (watching.get());
    }

    @Override
    public boolean accept(File dir, String name) {
        throw new UnsupportedOperationException("Not supported yet.");
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
        long diff = 0L;
        do {
            long lastModified = file.lastModified();
            sleep(100L);
            diff = file.lastModified() - lastModified;
        } while (diff > 0L);
        try {
            InputStream dataStream = new FileInputStream(file);
            DataProcessor processor = systemData.getType().getDataProcessor();
            Message message = processor.process(file.getName(), dataStream, systemData);
            if (message != null) {
                MainApplication.sendMessage(message);
            }
            dataStream.close();
            file.delete();
        } catch (IOException ex) {
            // Log error message
        }
    }
}
