package cuenen.raymond.java.ppawegkant.processing;

import cuenen.raymond.java.ppawegkant.sending.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;

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
        logger = LoggerFactory.getLogger(VLogProcessor.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message process(String filename, InputStream dataStream, String systemId) throws IOException {
        logger.debug("Verwerking van bestand {} voor {}", filename, systemId);
        List<String> vlogContent = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(dataStream));
        String line;
        while ((line = reader.readLine()) != null) {
            vlogContent.add(line);
        }
        String json = createObject(filename, systemId, vlogContent);
        return newMessage(ADDRESS, json.getBytes());
    }

    /**
     * Creeër een JSON-string.
     * 
     * @param filename de bestandsnaam
     * @param vri de systeem identificatie
     * @param vlogAscii de bestandsinhoud als lijst van regels
     * @return het JSON-bericht
     */
    private String createObject(String filename, String vri, List<String> vlogAscii) {
        StringBuilder obj = new StringBuilder();
        long timestamp = toTimestamp(filename);
        obj.append("{\"interface\":\"").append(ADDRESS).append("\",");
        obj.append("\"timestamp\":").append(timestamp).append(',');
        obj.append("\"vri\":\"").append(vri).append("\",");
        obj.append("\"vlog_naam\":\"").append(filename).append("\",");
        obj.append("\"vlog_ascii\":[");
        for (int i = 0; i < vlogAscii.size(); i++) {
            if (i > 0) {
                obj.append(',');
            }
            obj.append('"').append(vlogAscii.get(i)).append('"');
        }
        obj.append("]}");
        return obj.toString();
    }
}
