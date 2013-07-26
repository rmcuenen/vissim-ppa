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

/**
 *
 * @author R. Cuenen
 */
public abstract class DataProcessor {

    private static final String CONTENT_TYPE = "application/json";
    private static final DateFormat DATE_TIME_FORMATTER = new SimpleDateFormat("yyyyMMdd_HHmmss");

    protected DataProcessor() {
        // Abstract super constructor
    }

    public abstract Message process(String filename,
            InputStream dataStream, SystemData context) throws IOException;

    protected Message newMessage(String address) throws MalformedURLException {
        Message msg = new Message();
        URL url = new URL(MainApplication.getURL() + address);
        msg.setAddress(url);
        msg.setContentType(CONTENT_TYPE);
        return msg;
    }

    protected long toTimestamp(String timestamp) throws ParseException {
        Date date = DATE_TIME_FORMATTER.parse(timestamp);
        return date.getTime();
    }
}
