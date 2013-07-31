package cuenen.raymond.java.ppawegkant.watching;

import cuenen.raymond.java.ppawegkant.application.MainApplication;
import cuenen.raymond.java.ppawegkant.icon.ApplicationIcon;
import cuenen.raymond.java.ppawegkant.configuration.SystemData;
import cuenen.raymond.java.ppawegkant.sending.Message;
import cuenen.raymond.java.ppawegkant.processing.DataProcessor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deze class is verantwoordelijk voor het in de gaten
 * houden van een directory.
 * 
 * @author R. Cuenen
 */
public class DirectoryWatcher implements Runnable, FilenameFilter, FileHandler {

    private static final String FILE_NAME_MATCHER = ".*_[0-9]{8}_[0-9]{6}\\..*";
    private final Logger logger = LoggerFactory.getLogger(DirectoryWatcher.class);
    private final Collection<File> fileSet = new HashSet<File>();
    private final AtomicBoolean watching = new AtomicBoolean(false);
    private final SystemData systemData;
    private Thread watchThread;

    /**
     * Creeër een nieuwe {@link DirectoryWatcher}.
     * 
     * @param systemData de benodigde directory en informatie
     */
    public DirectoryWatcher(SystemData systemData) {
        this.systemData = systemData;
    }

    /**
     * Begin met pollen van de geconfigureerde directory.
     */
    public void start() {
        if (watchThread == null) {
            watchThread = new Thread(this, systemData.getIdentification() + "-Watch-Thread");
            watchThread.start();
            logger.info("Begin pollen voor {} in {}", systemData.getIdentification(), systemData.getDirectory());
        }
    }

    /**
     * Stop met pollen van de geconfigureerde directory.
     */
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

    /**
     * {@inheritDoc}
     */
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
                            logger.debug("Nieuw bestand gevonden: {}", file);
                            fileSet.add(file);
                            FileWatcher.watchFile(file, this);
                        }
                    }
                }
            }
            waitFor(125L);
        } while (watching.get());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(File dir, String name) {
        return name.matches(FILE_NAME_MATCHER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleFile(File file) {
        logger.info("Bestand verwerken: {}", file);
        InputStream dataStream = null;
        try {
            dataStream = new FileInputStream(file);
            DataProcessor processor = systemData.getType().getDataProcessor();
            Message message = processor.process(file.getName(), dataStream, systemData);
            if (message != null) {
                MainApplication.getApplication().getMessageSender().addMessage(message);
            }
            dataStream.close();
            dataStream = null;
            if (!file.delete()) {
                throw new IOException("Kan bestand niet verwijderen");
            }
            synchronized (fileSet) {
                fileSet.remove(file);
            }
        } catch (IOException ex) {
            logger.error("Fout tijdens het verwerken van bestand " + file, ex);
            ApplicationIcon.notifyState(2);
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

    /**
     * Wacht voor de aangegeven aantal milliseconden.
     * Er wordt met zekerheid {@code time} milliseconden gewacht.
     * 
     * @param time aantal milliseconden om te wachten
     */
    private void waitFor(long time) {
        long toWait = time;
        do {
            long current = System.nanoTime();
            try {
                Thread.sleep(toWait);
            } catch (InterruptedException ex) {
                // Ignore
            }
            toWait -= (System.nanoTime() - current);
        } while (toWait > 0L);
    }
}

