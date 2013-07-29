package cuenen.raymond.java.ppawegkant.file;

import cuenen.raymond.java.ppawegkant.MainApplication;
import cuenen.raymond.java.ppawegkant.application.ApplicationIcon;
import cuenen.raymond.java.ppawegkant.configuration.SystemData;
import cuenen.raymond.java.ppawegkant.post.Message;
import cuenen.raymond.java.ppawegkant.processing.DataProcessor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author R. Cuenen
 */
public class DirectoryWatcher implements Runnable, FilenameFilter {

    private class FileHandler extends Thread {

        private final File file;

        private FileHandler(File file) {
            super(file.getName() + "-FileHandler-Thread");
            this.file = file;
        }

        @Override
        public void run() {
            logger.debug("Nieuw bestand gevonden: {}", file);
            boolean isLocked = false;
            do {
                RandomAccessFile fos = null;
                try {
                    fos = new RandomAccessFile(file, "rw");
                } catch (Exception ex) {
                    isLocked = true;
                    waitFor(250);
                } finally {
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        // Ignore
                    }
                }
            }  while (isLocked);
            handleFile(file);
        }
    }
    private static final String FILE_NAME_MATCHER = ".*_[0-9]{8}_[0-9]{6}\\..*";
    private final Logger logger = LoggerFactory.getLogger(DirectoryWatcher.class);
    private final Collection<File> fileSet = new HashSet<File>();
    private final AtomicBoolean watching = new AtomicBoolean(false);
    private final SystemData systemData;
    private Thread watchThread;

    public DirectoryWatcher(SystemData systemData) {
        this.systemData = systemData;
    }

    public void start() {
        if (watchThread == null) {
            watchThread = new Thread(this, systemData.getIdentification() + "-Watch-Thread");
            watchThread.start();
            logger.info("Begin pollen voor {} in {}", systemData.getIdentification(), systemData.getDirectory());
        }
    }

    public void stop() {
        watching.set(false);
        if (watchThread != null) {
            watchThread.interrupt();
            try {
                watchThread.join(3000L);
                logger.info("Het pollen van {} is gestopt", systemData.getDirectory());
            } catch (InterruptedException ex) {
                logger.warn("Fout tijdens het stoppen van " + watchThread.getName());
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
                    synchronized (fileSet) {
                        if (!fileSet.contains(file)) {
                            fileSet.add(file);
                            new FileHandler(file).start();
                        }
                    }
                }
            }
            waitFor(125L);
        } while (watching.get());
    }

    @Override
    public boolean accept(File dir, String name) {
        return name.matches(FILE_NAME_MATCHER);
    }

    private void waitFor(long time) {
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
        logger.info("Bestand verwerken: {}", file);
        InputStream dataStream = null;
        try {
            dataStream = new FileInputStream(file);
            DataProcessor processor = systemData.getType().getDataProcessor();
            Message message = processor.process(file.getName(), dataStream, systemData);
            if (message != null) {
                MainApplication.getApplication().getMessageSender().addMessage(message);
            }
            file.delete();
        } catch (IOException ex) {
            logger.error("Fout tijdens het verwerken van bestand " + file, ex);
            ApplicationIcon.notifyState(2);
        } finally {
            synchronized (fileSet) {
                fileSet.remove(file);
            }
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
