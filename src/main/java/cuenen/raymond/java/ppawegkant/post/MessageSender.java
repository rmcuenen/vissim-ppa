package cuenen.raymond.java.ppawegkant.post;

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
 *
 * @author R. Cuenen
 */
public class MessageSender implements Runnable {

    private static final String POST_METHOD = "POST";
    private static final String CONTENT_TYPE_KEY = "Content-Type";
    private static final String CONTENT_LENGTH_KEY = "Content-Length";
    private final Logger logger = LoggerFactory.getLogger(MessageSender.class);
    private final BlockingQueue<Message> messageQueue = new ArrayBlockingQueue<Message>(1024);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread queueThread;

    public MessageSender() {
        initialize();
    }

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

    private void initialize() {
        queueThread = new Thread(this, "Message-Queue-Thread");
        queueThread.start();
        logger.info("Begin verzenden van berichten");
    }

    private void sendMessage(Message message) {
        URL url = message.getAddress();
        byte[] msg = message.getMessage();
        HttpURLConnection conn = null;
        try {
            logger.debug("Poging een bericht te posten op {}", url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(POST_METHOD);
            conn.setRequestProperty(CONTENT_TYPE_KEY, message.getContentType());
            conn.setRequestProperty(CONTENT_LENGTH_KEY, Integer.toString(msg.length));
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            OutputStream output = conn.getOutputStream();
            output.write(msg);
            conn.connect();
            output.flush();
            int response = conn.getResponseCode();
            if (200 != response) {
                throw new IOException("Got response code " + response);
            }
        } catch (IOException ex) {
            onFailure(message, ex);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void onFailure(Message message, IOException ex) {
        if (message.resendOnFailure()) {
            logger.warn("Verzenden mislukt, opnieuw...", ex);
            sendMessage(message);
        } else {
            logger.error("Fout tijdens het posten van een bericht", ex);
        }
    }
}
