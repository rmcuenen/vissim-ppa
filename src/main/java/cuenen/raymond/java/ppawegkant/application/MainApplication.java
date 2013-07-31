package cuenen.raymond.java.ppawegkant.application;

import cuenen.raymond.java.ppawegkant.watching.DirectoryWatcher;
import cuenen.raymond.java.ppawegkant.icon.ApplicationIcon;
import cuenen.raymond.java.ppawegkant.configuration.Configuration;
import cuenen.raymond.java.ppawegkant.configuration.SystemData;
import cuenen.raymond.java.ppawegkant.processing.DataProcessor;
import cuenen.raymond.java.ppawegkant.sending.MessageSender;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashSet;
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
 * Dit is de class die de initialisatie en het opstarten
 * van de applicatie verzorgt.
 * 
 * @author R. Cuenen
 */
public final class MainApplication {

    private static final String CONF_DIR_KEY = "ppawegkant.conf";
    private static final String DEFAULT_CONF_DIR = "conf";
    private static final String CONFIGURATION_FILE = "configuration.xml";
    private static final String LOG_FILE = "log4j.properties";
    private static final String XSD = "/schema/vissim-ppa.xsd";
    private static final Collection<DirectoryWatcher> WATCHERS = new HashSet<DirectoryWatcher>();
    private static Logger logger;

    /**
     * Het startpunt van de applicatie.
     * 
     * @param args command-line argumenten
     */
    public static void main(String[] args) {
        ShutdownHandler handler = new ShutdownHandler();
        Thread.setDefaultUncaughtExceptionHandler(handler);
        Runtime.getRuntime().addShutdownHook(handler);
        try {
            Configuration configuration = readConfiguration();
            String ppaBus = configuration.getPpaBus();
            if (!ppaBus.endsWith("/")) {
                ppaBus += "/";
            }
            logger.debug("Gevonden PPA-bus URL: {}", ppaBus);
            DataProcessor.setBaseURL(ppaBus);
            Collection<File> directories = new HashSet<File>();
            for (SystemData data : configuration.getData()) {
                File directory = data.getDirectory();
                if (!directory.exists() || !directory.isDirectory()) {
                    throw new IllegalArgumentException(directory + " bestaat niet");
                } else if (directories.contains(directory)) {
                    throw new IllegalArgumentException(directory + " is al geconfigureerd");
                }
                logger.debug("Nieuwe directory bewaker voor {} op {}",
                        data.getIdentification(), directory);
                WATCHERS.add(new DirectoryWatcher(data));
            }
        } catch (Exception ex) {
            onError("Fout tijdens het initialiseren van de applicaie", ex);
        }
        logger.info("Starten van de applicatie");
        ApplicationIcon.notifyState(-1);
        startup();
    }

    /**
     * Open een dialoogvenster met de opgetreden fout.
     * Hierna sluit de applicatie zich af.
     * 
     * @param message het foutbericht
     * @param e de {@link Throwable} wat is opgetreden
     */
    public static void onError(String message, Throwable e) {
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

    /**
     * Stop de applicatie.
     */
    public static void shutdown() {
        logger.info("Stoppen van de applicatie");
        for (DirectoryWatcher watcher : WATCHERS) {
            watcher.stop();
        }
        MessageSender.getInstance().shutdown();
    }

    /**
     * Lees de configuratie uit de configuratoie-bestanden.
     * 
     * @return een {@link Configuration} object
     * @throws Exception wanneer er iets mis is met de configuratie
     */
    private static Configuration readConfiguration() throws Exception { //NOSONAR
        String cfgDir = System.getProperty(CONF_DIR_KEY, DEFAULT_CONF_DIR);
        initializeLogging(cfgDir);
        JAXBContext ctx = JAXBContext.newInstance(Configuration.class);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(MainApplication.class.getResource(XSD));
        Unmarshaller um = ctx.createUnmarshaller();
        um.setSchema(schema);
        File configFile = new File(cfgDir, CONFIGURATION_FILE);
        logger.debug("Zoeken naar applicatie configuratie: {}", configFile);
        return (Configuration) um.unmarshal(new FileReader(configFile));
    }

    /**
     * Initialiseer de logging.
     * 
     * @param cfgDir de directory met configratie-bestanden
     */
    private static void initializeLogging(String cfgDir) {
        File logFile = new File(cfgDir, LOG_FILE);
        PropertyConfigurator.configure(logFile.getPath());
        if (System.console() == null) {
            org.apache.log4j.Logger.getRootLogger().removeAppender("stdout");

        }
        logger = LoggerFactory.getLogger(MainApplication.class);
    }

    /**
     * Start de applicatie.
     */
    private static void startup() {
        for (DirectoryWatcher watcher : WATCHERS) {
            watcher.start();
        }
    }

    /**
     * Creeër de applicatie.
     */
    private MainApplication() {
        // Singleton.
    }
}
