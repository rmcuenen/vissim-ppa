package cuenen.raymond.java.ppawegkant.processing;

import cuenen.raymond.java.ppawegkant.sending.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.slf4j.LoggerFactory;

/**
 * {@link DataProcessor} implementatie voor de afhandeling
 * van tekstbestanden met MTM data.
 * 
 * @author R. Cuenen
 */
public class MTMProcessor extends DataProcessor {

    private static final String ADDRESS = "data/hwn/tenuki";

    /**
     * Creeër een nieuwe {@link MTMProcessor}.
     */
    public MTMProcessor() {
        logger = LoggerFactory.getLogger(MTMProcessor.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message process(String filename, InputStream dataStream, String systemId) throws IOException {
        logger.debug("Verwerking van bestand {} voor {}", filename, systemId);
        long timestamp = toTimestamp(filename);
        List<String> raaien = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(dataStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String raai = createRaai(line);
            if (raai != null) {
                raaien.add(raai);
            }
        }
        String json = createObject(timestamp, raaien);
        return newMessage(ADDRESS, json.getBytes());
    }

    /**
     * Creeër een JSON-string.
     * 
     * @param timestamp het tijdstip
     * @param raaien een lijst met JSON-strings die raaien voorstellen
     * @return het JSON-bericht
     */
    private String createObject(long timestamp, List<String> raaien) {
        StringBuilder obj = new StringBuilder();
        obj.append("{\"interface\":\"").append(ADDRESS).append("\",");
        obj.append("\"timestamp\":").append(timestamp).append(',');
        obj.append("\"raaien\":[");
        for (int i = 0; i < raaien.size(); i++) {
            if (i > 0) {
                obj.append(',');
            }
            obj.append(raaien.get(i));
        }
        obj.append("]}");
        return obj.toString();
    }

    /**
     * Converteer een regel naar een Raai.
     * 
     * @param line een regel uit het bestand
     * @return de Raai als JSON-string
     */
    private String createRaai(String line) {
        StringTokenizer st = new StringTokenizer(line, " ");
        if (st.countTokens() == 3) {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"raaiId\":\"").append(st.nextToken()).append("\",");
            sb.append("\"intensiteit_vth\":").append(st.nextToken()).append(',');
            sb.append("\"snelheid_kmh\":").append(st.nextToken()).append(',');
            sb.append("\"betrokken_rijstroken\":0,");
            sb.append("\"rijstroken\":[]}");
            return sb.toString();
        }
        return null;
    }
}
