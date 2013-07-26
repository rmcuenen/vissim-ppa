package cuenen.raymond.java.ppawegkant;

import cuenen.raymond.java.ppawegkant.configuration.Configuration;
import cuenen.raymond.java.ppawegkant.configuration.SystemData;
import cuenen.raymond.java.ppawegkant.file.DirectoryWatcher;
import cuenen.raymond.java.ppawegkant.post.Message;
import cuenen.raymond.java.ppawegkant.post.MessageSender;
import java.io.File;
import java.io.FileReader;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import javax.swing.JOptionPane;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            MainApplication.logger.info("Stoppen van de applicatie");
            for (DirectoryWatcher watcher : MainApplication.direcoryWatchers) {
                watcher.stop();
            }
            if (MainApplication.messageSender != null) {
                MainApplication.messageSender.shutdown();
            }
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
    private static final String LOG_FILE = "log4j.properties";
    private static final String XSD = "/schema/vissim-ppa.xsd";
    private static final Collection<DirectoryWatcher> direcoryWatchers = new HashSet<DirectoryWatcher>();
    private static final Properties settings = new Properties();
    private static MessageSender messageSender;
    private static Logger logger;
    private static String baseURL;

    public static void main(String[] args) {
        ShutdownHandler handler = new ShutdownHandler();
        Thread.setDefaultUncaughtExceptionHandler(handler);
        Runtime.getRuntime().addShutdownHook(handler);
        try {
            Configuration configuration = readConfiguration();
            baseURL = configuration.getPpaBus();
            if (!baseURL.endsWith("/")) {
                baseURL += "/";
            }
            logger.debug("Gevonden PPA-bus URL: {}", baseURL);
            for (SystemData data : configuration.getData()) {
                logger.debug("Nieuwe directory bewaker voor {} op {}",
                        data.getIdentification(), data.getDirectory());
                direcoryWatchers.add(new DirectoryWatcher(data));
            }
        } catch (Exception ex) {
            onError("Fout tijdens het initialiseren van de applicaie", ex);
        }
        logger.info("Starten van de applicatie");
        messageSender = new MessageSender();
        for (DirectoryWatcher watcher : direcoryWatchers) {
            watcher.start();
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
        initializeLogging(cfgDir);
        File settingsFile = new File(cfgDir, SETTINGS_FILE);
        logger.debug("Zoeken naar applicatie settings: {}", settingsFile);
        if (settingsFile.exists()) {
            settings.load(new FileReader(settingsFile));
        }
        JAXBContext ctx = JAXBContext.newInstance(Configuration.class);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(MainApplication.class.getResource(XSD));
        Unmarshaller um = ctx.createUnmarshaller();
        um.setSchema(schema);
        File configFile = new File(cfgDir, CONFIGURATION_FILE);
        logger.debug("Zoeken naar applicatie configuratie: {}", configFile);
        return (Configuration) um.unmarshal(new FileReader(configFile));
    }

    private static void onError(String message, Throwable e) {
        logger.error(message, e);
        StringBuilder msg = new StringBuilder("<html>");
        msg.append("<b>").append(message).append("</b>");
        String info = e.getLocalizedMessage();
        if (info != null && !info.isEmpty()) {
            msg.append("<br>").append(info);
        }
        msg.append("</html>");
        JOptionPane.showMessageDialog(null, msg,
                e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    private static void initializeLogging(String cfgDir) {
        File logFile = new File(cfgDir, LOG_FILE);
        PropertyConfigurator.configure(logFile.getPath());
        if (System.console() == null) {
            org.apache.log4j.Logger.getRootLogger().removeAppender("stdout");

        }
        logger = LoggerFactory.getLogger(MainApplication.class);
    }

    private MainApplication() {
        // Do not instantiate this class.
    }
}
