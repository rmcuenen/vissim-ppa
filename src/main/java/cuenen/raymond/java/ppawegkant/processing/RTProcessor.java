package cuenen.raymond.java.ppawegkant.processing;

import cuenen.raymond.java.ppawegkant.configuration.SystemData;
import cuenen.raymond.java.ppawegkant.sending.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.LoggerFactory;

/**
 * {@link DataProcessor} implementatie voor de afhandeling
 * van tekstbestanden met regeltoestand TDI.
 * 
 * @author R. Cuenen
 */
public class RTProcessor extends DataProcessor {

    private static final String ADDRESS = "data/tdi/regeltoestand";
    private static final Map<String, String> FIELD_MAP = new HashMap<String, String>();

    static {
        FIELD_MAP.put("EG_PPA_wijziging", "");
        FIELD_MAP.put("EG_PPA_status_TDI", "");
        FIELD_MAP.put("EG_PPA_V_RWso", "v_rwso_kmh");
        FIELD_MAP.put("EG_PPA_V_RWsa", "v_rwsa_kmh");
        FIELD_MAP.put("EG_PPA_I_RWso", "i_rwso_vth");
        FIELD_MAP.put("EG_PPA_I_RWsa", "i_rwsa_vth");
        FIELD_MAP.put("EG_PPA_stroken_RWso", "");
        FIELD_MAP.put("EG_PPA_stroken_RWsa", "");
        FIELD_MAP.put("EG_PPA_doseerint_2", "i_doseer_vth");
        FIELD_MAP.put("EG_PPA_storing", "storing");
    }

    /**
     * Creeër een nieuwe {@link RTProcessor}.
     */
    public RTProcessor() {
        logger = LoggerFactory.getLogger(RTProcessor.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message process(String filename, InputStream dataStream, SystemData context) throws IOException {
        logger.debug("Verwerking van bestand {} voor {}", filename, context.getIdentification());
        long timestamp = toTimestamp(filename);
        Map<String, String> content = new HashMap<String, String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(dataStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] fields = line.split("=");
            if (fields.length == 2) {
                String field = FIELD_MAP.get(fields[0].trim());
                content.put(field, fields[1].trim());
            }
        }
        String json = createObject(timestamp, context.getIdentification(), content);
        return newMessage(ADDRESS, json.getBytes());
    }

    /**
     * Creeër een JSON-string.
     * 
     * @param timestamp het tijdstip
     * @param tdi de systeem identificatie
     * @param content de bestandsinhoud
     * @return het JSON-bericht
     */
    private String createObject(long timestamp, String tdi, Map<String, String> content) {
        StringBuilder obj = new StringBuilder();
        obj.append("{\"interface\":\"").append(ADDRESS).append("\",");
        obj.append("\"timestamp\":").append(timestamp).append(',');
        obj.append("\"tdi\":\"").append(tdi).append("\",");
        String state = content.get("regeltoestand");
        if (state == null) {
            obj.append("\"regeltoestand\":").append(state).append(',');
        } else {
            obj.append("\"regeltoestand\":\"").append(state).append("\",");
        }
        obj.append("\"v_rwso_kmh\":").append(content.get("v_rwso_kmh")).append(',');
        obj.append("\"v_rwsa_kmh\":").append(content.get("v_rwsa_kmh")).append(',');
        obj.append("\"i_rwso_vth\":").append(content.get("i_rwso_vth")).append(',');
        obj.append("\"i_rwsa_vth\":").append(content.get("i_rwsa_vth")).append(',');
        obj.append("\"i_doseer_vth\":").append(content.get("i_doseer_vth")).append(',');
        obj.append("\"storing\":[]}");
        return obj.toString();
    }
}