package cuenen.raymond.java.ppawegkant.processing;

import com.fasterxml.jackson.core.JsonGenerator;
import cuenen.raymond.java.ppawegkant.sending.Message;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message process(String filename, InputStream dataStream, String systemId) throws IOException {
        logger.debug("Verwerking van bestand {} voor {}", filename, systemId);
        Message message = newMessage(ADDRESS);
        JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(message);
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("interface", ADDRESS);
        jsonGenerator.writeNumberField("timestamp", toTimestamp(filename));
        jsonGenerator.writeStringField("tdi", systemId);
        jsonGenerator.writeStringField("mv_naam", filename);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] tmp = new byte[1024];
        int read;
        while ((read = dataStream.read(tmp)) != -1) {
            buffer.write(tmp, 0, read);
        }
        jsonGenerator.writeBinaryField("mv_bin", buffer.toByteArray());
        jsonGenerator.writeEndObject();
        jsonGenerator.close();
        return message;
    }
}
