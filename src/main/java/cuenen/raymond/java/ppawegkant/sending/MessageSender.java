package cuenen.raymond.java.ppawegkant.sending;

import cuenen.raymond.java.ppawegkant.icon.ApplicationIcon;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deze class is verantwoordelijk voor het uitvoeren
 * van de HTTP POST actie.
 * 
 * @author R. Cuenen
 */
public final class MessageSender implements Runnable {

    private static final String POST_METHOD = "POST";
    private static final String CONTENT_TYPE_KEY = "Content-Type";
    private static final String CONTENT_LENGTH_KEY = "Content-Length";
    private static final MessageSender INSTANCE = new MessageSender();
    private final Logger logger = LoggerFactory.getLogger(MessageSender.class);
    private final BlockingQueue<Message> messageQueue = new ArrayBlockingQueue<Message>(1024);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread queueThread;

    /**
     * Geeft de {@link MessageSender} sigleton.
     * 
     * @return de {@link MessageSender}
     */
    public static synchronized MessageSender getInstance() {
        return INSTANCE;
    }

    /**
     * Creeër de {@link MessageSender}.
     */
    private MessageSender() {
        queueThread = new Thread(this, "Message-Queue-Thread");
        queueThread.start();
        logger.info("Begin verzenden van berichten");
    }

    /**
     * Voeg een bericht toe om te versturen.
     * 
     * @param message het bericht
     */
    public void addMessage(Message message) {
        boolean failed = true;
        do {
            try {
                messageQueue.put(message);
                failed = false;
            } catch (InterruptedException ex) {
                logger.debug("Toevoegen van een bericht was onderbroken");
            }
        } while (failed);
    }

    /**
     * Stop het verzenden van berichten.
     */
    public void shutdown() {
        running.set(false);
        if (queueThread != null) {
            queueThread.interrupt();
            try {
                queueThread.join(5000L);
                logger.info("Het verzenden van berichten is gestopt");
            } catch (InterruptedException ex) {
                logger.warn("Fout tijdens het stoppen van " + queueThread.getName());
            }
            queueThread = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        running.set(true);
        do {
            Message msg = null;
            try {
                msg = messageQueue.take();
            } catch (InterruptedException ex) {
                logger.debug("Ophalen van een bericht was onderbroken");
            }
            if (msg != null) {
                sendMessage(msg);
            }
        } while (running.get());
    }

    /**
     * Verzend een bericht.
     * 
     * @param message het bericht
     */
    private void sendMessage(Message message) {
        URL url = message.getAddress();
        HttpURLConnection conn = null;
        try {
            logger.debug("Poging een bericht te posten op {}", url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(POST_METHOD);
            conn.setRequestProperty(CONTENT_TYPE_KEY, message.getContentType());
            conn.setRequestProperty(CONTENT_LENGTH_KEY, Integer.toString(message.size()));
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            OutputStream output = conn.getOutputStream();
            output.write(message.toByteArray());
            output.flush();
            int response = conn.getResponseCode();
            if (200 != response) {
                throw new IOException("Got response code " + response);
            }
            ApplicationIcon.notifyState(0);
            logger.info("Bericht verzonden naar {}", url);
        } catch (IOException ex) {
            onFailure(message, ex);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Bepaal wat te doen met een gefaalde verzending.
     * 
     * @param message het bericht
     * @param ex de {@link IOException} die is opgetreden
     */
    private void onFailure(Message message, IOException ex) {
        if (message.resendOnFailure()) {
            logger.warn("Verzenden mislukt, opnieuw...", ex);
            ApplicationIcon.notifyState(1);
            sendMessage(message);
        } else {
            logger.error("Fout tijdens het posten van een bericht", ex);
            ApplicationIcon.notifyState(2);
        }
    }
}
