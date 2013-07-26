package cuenen.raymond.java.ppawegkant.processing;

import cuenen.raymond.java.ppawegkant.MainApplication;
import cuenen.raymond.java.ppawegkant.configuration.SystemData;
import cuenen.raymond.java.ppawegkant.post.Message;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author R. Cuenen
 */
public abstract class DataProcessor {

    private static final String CONTENT_TYPE = "application/json";
    private static final String ADDRESS_PREFIX = "ppawegkant/";
    private static final String DATE_TIME_MATCHER = "_[0-9]{6}_[0-9]{6}\\.";
    private static final DateFormat DATE_TIME_FORMATTER = new SimpleDateFormat("_yyyyMMdd_HHmmss.");

    protected DataProcessor() {
        // Abstract super constructor
    }

    public abstract Message process(String filename,
            InputStream dataStream, SystemData context) throws IOException;

    protected Message newMessage(String address, byte[] message) {
        try {
            Message msg = new Message();
            URL url = new URL(MainApplication.getURL()
                    + ADDRESS_PREFIX + address);
            msg.setAddress(url);
            msg.setContentType(CONTENT_TYPE);
            msg.setMessage(message);
            return msg;
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Configuratie fout?");
        }
    }

    protected long toTimestamp(String filname) {
        try {
            Pattern p = Pattern.compile(DATE_TIME_MATCHER);
            Matcher m = p.matcher(filname);
            Date date = DATE_TIME_FORMATTER.parse(m.group());
            return date.getTime();
        } catch (ParseException ex) {
            // Log error message
        }
        return 0L;
    }
}
