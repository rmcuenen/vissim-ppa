package cuenen.raymond.java.ppawegkant.processing;

import cuenen.raymond.java.ppawegkant.sending.Message;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.LoggerFactory;

/**
 * {@link DataProcessor} implementatie voor de afhandeling
 * van MV-bestanden.
 * 
 * @author R. Cuenen
 */
public class MVProcessor extends DataProcessor {

    private static final String ADDRESS = "data/tdi/mv";

    /**
     * Creeër een nieuwe {@link MVProcessor}.
     */
    public MVProcessor() {
        logger = LoggerFactory.getLogger(MVProcessor.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message process(String filename, InputStream dataStream, String systemId) throws IOException {
        logger.debug("Verwerking van bestand {} voor {}", filename, systemId);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] tmp = new byte[1024];
        int read;
        while ((read = dataStream.read(tmp)) != -1) {
            buffer.write(tmp, 0, read);
        }
        String mvBin = Base64.encodeBase64String(buffer.toByteArray());
        String json = createObject(filename, systemId, mvBin);
        return newMessage(ADDRESS, json.getBytes());
    }

    /**
     * Creeër een JSON-string.
     * 
     * @param filename de bestandsnaam
     * @param tdi de systeem identificatie
     * @param mvBin de Base64 encoded bestandsinhoud
     * @return het JSON-bericht
     */
    private String createObject(String filename, String tdi, String mvBin) {
        StringBuilder obj = new StringBuilder();
        long timestamp = toTimestamp(filename);
        obj.append("{\"interface\":\"").append(ADDRESS).append("\",");
        obj.append("\"timestamp\":").append(timestamp).append(',');
        obj.append("\"tdi\":\"").append(tdi).append("\",");
        obj.append("\"mv_naam\":\"").append(filename).append("\",");
        obj.append("\"mv_bin\":\"").append(mvBin).append("\"}");
        return obj.toString();
    }
}
