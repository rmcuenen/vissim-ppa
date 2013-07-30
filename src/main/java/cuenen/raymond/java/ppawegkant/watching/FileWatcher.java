package cuenen.raymond.java.ppawegkant.watching;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deze class controleert of een bestand nog
 * verandert alvorens de verwerking te starten.
 * 
 * @author R. Cuenen
 */
public final class FileWatcher extends Thread {

    private final Logger logger = LoggerFactory.getLogger(FileWatcher.class);
    private final FileHandler fileHandler;
    private final File file;

    /**
     * Start een nieuwe {@link FileWatcher}.
     * 
     * @param file het te controleren bestand
     * @param fileHandler de {@link FileHandler} callback
     */
    public static void watchFile(File file, FileHandler fileHandler) {
        FileWatcher watcher = new FileWatcher(fileHandler, file);
        watcher.start();
    }

    /**
     * Creeër een nieuwe {@link FileWatcher}-thread.
     * 
     * @param fileHandler de {@link FileHandler} callback
     * @param file het te controleren bestand
     */
    private FileWatcher(FileHandler fileHandler, File file) {
        super(file.getName() + "-FileWatcher-Thread");
        this.fileHandler = fileHandler;
        this.file = file;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        logger.debug("Controleren van bestand {}", file);
        boolean isLocked = false;
        do {
            RandomAccessFile fos = null;
            FileLock lock = null;
            try {
                fos = new RandomAccessFile(file, "rw");
                lock = fos.getChannel().lock();
            } catch (Exception ex) {
                isLocked = true;
                pause();
            } finally {
                try {
                    if (lock != null) {
                        lock.release();
                    }
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    // Ignore
                }
            }
        } while (isLocked);
        fileHandler.handleFile(file);
    }

    /**
     * Wacht 250 milliseconden.
     */
    private void pause() {
        logger.debug("Wachten op bestand {}", file);
        try {
            Thread.sleep(250L);
        } catch (InterruptedException ex) {
            // Ignore
        }
    }
}
