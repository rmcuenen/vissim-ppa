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
            for (DirectoryWatcher watcher : MainApplication.direcoryWatchers) {
                watcher.stop();
            }
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
    private static final String XSD = "/schema/vissim-ppa.xsd";
    private static final Collection<DirectoryWatcher> direcoryWatchers = new HashSet<DirectoryWatcher>();
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
            if (!baseURL.endsWith("/")) {
                baseURL += "/";
            }
            Collection<String> systems = new HashSet<String>();
            Collection<File> directories = new HashSet<File>();
            for (SystemData data : configuration.getData()) {
                String id = data.getIdentification();
                File dir = data.getDirectory();
                if (systems.contains(id)) {
                    throw new Exception(id
                            + " komt meerdere keren voor");
                } else if (directories.contains(dir)) {
                    throw new Exception(dir
                            + " is meerdere keren geconfigureerd");
                }
                direcoryWatchers.add(new DirectoryWatcher(data));
                systems.add(id);
                directories.add(dir);
            }
        } catch (Exception ex) {
            onError("Fout tijdens het initialiseren van de applicaie", ex);
        }
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
        File settingsFile = new File(cfgDir, SETTINGS_FILE);
        if (settingsFile.exists()) {
            settings.load(new FileReader(settingsFile));
        }
        JAXBContext ctx = JAXBContext.newInstance(Configuration.class);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(MainApplication.class.getResource(XSD));
        Unmarshaller um = ctx.createUnmarshaller();
        um.setSchema(schema);
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
        msg.append("</html>");
        JOptionPane.showMessageDialog(null, msg,
                e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    private MainApplication() {
        // Do not instantiate this class.
    }
}
