package cuenen.raymond.java.ppawegkant.file;

import cuenen.raymond.java.ppawegkant.configuration.SystemData;
import cuenen.raymond.java.ppawegkant.post.Message;
import cuenen.raymond.java.ppawegkant.processing.DataProcessor;
import cuenen.raymond.java.ppawegkant.processing.DataProcessor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

/**
 *
 * @author R. Cuenen
 */
public class MVProcessor extends DataProcessor {

    private static final String ADDRESS = "data/tdi/mv";
    private final BASE64Encoder encoder = new BASE64Encoder();

    public MVProcessor() {
        logger = LoggerFactory.getLogger(MVProcessor.class);
    }

    @Override
    public Message process(String filename, InputStream dataStream, SystemData context) throws IOException {
        logger.debug("Verwerking van bestand {} voor {}", filename, context.getIdentification());
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] tmp = new byte[1024];
        int read;
        while ((read = dataStream.read(tmp)) != -1) {
            buffer.write(tmp, 0, read);
        }
        String mvBin = encoder.encode(buffer.toByteArray());
        String json = createObject(filename, context.getIdentification(), mvBin);
        return newMessage(ADDRESS, json.getBytes());
    }

    private String createObject(String filename, String tdi, String mvBin) {
        StringBuilder obj = new StringBuilder();
        long timestamp = toTimestamp(filename);
        obj.append("{\"interface\":\"").append(ADDRESS).append("\",");
        obj.append("{\"timestamp\":").append(timestamp).append(',');
        obj.append("{\"tdi\":\"").append(tdi).append("\",");
        obj.append("{\"mv_naam\":\"").append(filename).append("\",");
        obj.append("{\"mv_bin\":\"").append(mvBin).append("\"}");
        return obj.toString();
    }
}
