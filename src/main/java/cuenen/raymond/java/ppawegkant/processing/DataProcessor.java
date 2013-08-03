package cuenen.raymond.java.ppawegkant.processing;

import cuenen.raymond.java.ppawegkant.sending.Message;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.codehaus.jackson.JsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dit is een abstract class die de basis vormt voor het verwerken
 * en converteren van bestandsgegevens.
 * 
 * @author R. Cuenen
 */
public abstract class DataProcessor {

    private static final String CONTENT_TYPE = "application/json";
    private static final String ADDRESS_PREFIX = "ppawegkant/";
    private static final String DATE_TIME_MATCHER = "_[0-9]{8}_[0-9]{6}\\.";
    private static final DateFormat DATE_TIME_FORMATTER = new SimpleDateFormat("_yyyyMMdd_HHmmss.");
    private static String baseURL;
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final JsonFactory jsonFactory = new JsonFactory();

    /**
     * Configureer de basis URL voor het adres.
     *
     * @param baseURL het adres van de PPA-bus
     */
    public static void setBaseURL(String baseURL) {
        DataProcessor.baseURL = baseURL;
    }

    /**
     * Creeër de {@link DataProcessor}.
     */
    protected DataProcessor() {
        // Abstract super constructor
    }

    /**
     * Verwerk het gegeven bestand tot een bericht.
     * 
     * @param filename de bestandsnaam
     * @param dataStream de {@link InputStream} naar de bestandsinhoud
     * @param systemId de systeem identificatie
     * @return een nieuw te verzenden bericht
     * @throws IOException wanneer er een fout optreed tijdens de verwerking
     */
    public abstract Message process(String filename,
            InputStream dataStream, String systemId) throws IOException;

    /**
     * Creeër een {@link Message}.
     * 
     * @param address het (sub-)adres van het bericht
     * @return het representeerdende {@link Message}-object
     */
    protected Message newMessage(String address) {
        logger.debug("Creeër nieuw bericht voor {}", address);
        try {
            Message msg = new Message();
            URL url = new URL(baseURL + ADDRESS_PREFIX + address);
            msg.setAddress(url);
            msg.setContentType(CONTENT_TYPE);
            return msg;
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Configuratie fout?", ex);
        }
    }

    /**
     * Bepaal het tijdtip uit de bestandsnaam.
     * 
     * @param filename de bestandsnaam
     * @return het tijdstip
     */
    protected long toTimestamp(String filename) {
        try {
            Pattern p = Pattern.compile(DATE_TIME_MATCHER);
            Matcher m = p.matcher(filename);
            if (m.find()) {
                Date date = DATE_TIME_FORMATTER.parse(m.group());
                return date.getTime();
            }
        } catch (ParseException ex) {
            logger.error("Bestand {} voldoet niet aan het verwachte tijdsformaat",
                    filename);
        }
        return 0L;
    }
}
