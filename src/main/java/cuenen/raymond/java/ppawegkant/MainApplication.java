package cuenen.raymond.java.ppawegkant;

import cuenen.raymond.java.ppawegkant.configuration.Configuration;
import cuenen.raymond.java.ppawegkant.configuration.SystemData;
import cuenen.raymond.java.ppawegkant.post.Message;
import cuenen.raymond.java.ppawegkant.post.MessageSender;
import java.io.File;
import java.io.FileReader;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Properties;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

/**
 * 
 * @author R. Cuenen
 */
public class MainApplication {

    private static class ShutdownHandler extends Thread
            implements UncaughtExceptionHandler {

        private ShutdownHandler() {
            // Private constructor
        }

        @Override
        public void run() {
            MainApplication.messageSender.shutdown();
        }

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            onError("Er is een onherstelbare fout opgetreden", e);
        }
    }
    private static final String CONF_DIR_KEY = "ppawegkant.conf";
    private static final String DEFAULT_CONF_DIR = "conf";
    private static final String CONFIGURATION_FILE = "configuration.xml";
    private static final String SETTINGS_FILE = "settings.properties";
    private static final Properties settings = new Properties();
    private static final MessageSender messageSender = new MessageSender();
    private static String baseURL;

    public static void main(String[] args) {
        ShutdownHandler handler = new ShutdownHandler();
        Thread.setDefaultUncaughtExceptionHandler(handler);
        Runtime.getRuntime().addShutdownHook(handler);
        try {
            Configuration configuration = readConfiguration();
            baseURL = configuration.getPpaBus();
            for (SystemData data : configuration.getData()) {
                
            }
        } catch (Exception ex) {
            onError("Fout tijdens het initialiseren van de applicaie", ex);
        }
    }

    public static void sendMessage(Message message) {
        messageSender.addMessage(message);
    }

    public static String getURL() {
        return baseURL;
    }

    public static String getProperty(String key) {
        return settings.getProperty(key);
    }

    private static Configuration readConfiguration() throws Exception {
        String cfgDir = System.getProperty(CONF_DIR_KEY, DEFAULT_CONF_DIR);
        settings.load(new FileReader(new File(cfgDir, SETTINGS_FILE)));
        JAXBContext ctx = JAXBContext.newInstance(Configuration.class);
        Unmarshaller um = ctx.createUnmarshaller();
        return (Configuration) um.unmarshal(
                new FileReader(new File(cfgDir, CONFIGURATION_FILE)));
    }

    private static void onError(String message, Throwable e) {
        StringBuilder msg = new StringBuilder("<html>");
        msg.append("<b>").append(message).append("</b>");
        String info = e.getLocalizedMessage();
        if (info != null && !info.isEmpty()) {
            msg.append("<br>").append(info);
        }
        JOptionPane.showMessageDialog(null, msg,
                e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    private MainApplication() {
        // Do not instantiate this class.
    }
}
