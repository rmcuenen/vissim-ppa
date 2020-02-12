package cuenen.raymond.java.ppawegkant.processing;

import com.fasterxml.jackson.core.JsonGenerator;
import cuenen.raymond.java.ppawegkant.sending.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link DataProcessor} implementatie voor de afhandeling
 * van tekstbestanden met regeltoestand TDI.
 * 
 * @author R. Cuenen
 */
public class RTProcessor extends DataProcessor {

    private static final String ADDRESS = "data/tdi/regeltoestand";
    private static final Map<String, String> FIELD_MAP = new HashMap<String, String>();
    private static final String[] FIELDS = {"regeltoestand", "v_rwso_kmh", "v_rwsa_kmh",
        "i_rwso_vth", "i_rwsa_vth", "i_doseer_vth", "storing"};

    static {
        FIELD_MAP.put("EG_PPA_wijziging", "");
        FIELD_MAP.put("EG_PPA_status_TDI", FIELDS[0]);
        FIELD_MAP.put("EG_PPA_V_RWso", FIELDS[1]);
        FIELD_MAP.put("EG_PPA_V_RWsa", FIELDS[2]);
        FIELD_MAP.put("EG_PPA_I_RWso", FIELDS[3]);
        FIELD_MAP.put("EG_PPA_I_RWsa", FIELDS[4]);
        FIELD_MAP.put("EG_PPA_stroken_RWso", "");
        FIELD_MAP.put("EG_PPA_stroken_RWsa", "");
        FIELD_MAP.put("EG_PPA_doseerint_2", FIELDS[5]);
        FIELD_MAP.put("EG_PPA_storing", FIELDS[6]);
    }

    /**
     * Creeër een nieuwe {@link RTProcessor}.
     */
    public RTProcessor() {
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
        jsonGenerator.writeStringField(FIELDS[0], toStatus(content.get(FIELDS[0])));
        jsonGenerator.writeFieldName(FIELDS[1]);
        jsonGenerator.writeNumber(content.get(FIELDS[1]));
        jsonGenerator.writeFieldName(FIELDS[2]);
        jsonGenerator.writeNumber(content.get(FIELDS[2]));
        jsonGenerator.writeFieldName(FIELDS[3]);
        jsonGenerator.writeNumber(content.get(FIELDS[3]));
        jsonGenerator.writeFieldName(FIELDS[4]);
        jsonGenerator.writeNumber(content.get(FIELDS[4]));
        jsonGenerator.writeFieldName(FIELDS[5]);
        jsonGenerator.writeNumber(content.get(FIELDS[5]));
        jsonGenerator.writeArrayFieldStart(FIELDS[6]);
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
        jsonGenerator.close();
        return message;
    }

    /**
     * Converteerd een integer-string naar een regelstatus.
     * 
     * @param state de integer-string uit het bestand
     * @return de bijbehorende regeltoestand
     */
    private String toStatus(String state) {
        int status = -1;
        try {
            status = Integer.parseInt(state);
        } catch (Exception ex) {
            logger.warn("Onbekende status: {}", state);
        }
        switch (status) {
            case 1:
            case 2:
            case 4:
            case 11:
            case 12:
            case 14:
                return "AAN";
            case 3:
            case 13:
                return "PPA_AAN";
            case 31:
            case 32:
            case 34:
                return "UIT";
            case 33:
                return "PPA_UIT";
            default:
                return "ONBEKEND";
        }
    }
}
