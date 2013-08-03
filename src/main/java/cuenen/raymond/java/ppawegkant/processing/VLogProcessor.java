package cuenen.raymond.java.ppawegkant.processing;

import cuenen.raymond.java.ppawegkant.sending.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.codehaus.jackson.JsonGenerator;

/**
 * {@link DataProcessor} implementatie voor de afhandeling
 * van VLog_ASCII-bestanden.
 * 
 * @author R. Cuenen
 */
public class VLogProcessor extends DataProcessor {

    private static final String ADDRESS = "data/vri/vlog";

    /**
     * Creeër een nieuwe {@link VLogProcessor}.
     */
    public VLogProcessor() {
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
        jsonGenerator.writeStringField("vri", systemId);
        jsonGenerator.writeStringField("vlog_naam", filename);
        jsonGenerator.writeArrayFieldStart("vlog_ascii");
        BufferedReader reader = new BufferedReader(new InputStreamReader(dataStream));
        String line;
        while ((line = reader.readLine()) != null) {
            jsonGenerator.writeString(line);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
        jsonGenerator.close();
        return message;
    }
}
