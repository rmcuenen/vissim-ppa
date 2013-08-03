package cuenen.raymond.java.ppawegkant.processing;

import cuenen.raymond.java.ppawegkant.sending.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import org.codehaus.jackson.JsonGenerator;

/**
 * {@link DataProcessor} implementatie voor de afhandeling
 * van tekstbestanden met MTM data.
 * 
 * @author R. Cuenen
 */
public class MTMProcessor extends DataProcessor {

    /**
     * Dit is een enum inner-class om het gedeelte
     * van de WOL en BOL in de BPS-code te bepalen.
     */
    private static enum Orientation {

        N(0),
        L(1),
        M(2),
        R(3);
        private final int base;

        /**
         * Creeër het {@link Orientation} enum-object.
         * 
         * @param base het basisgetal van de oriëntatielijn
         */
        private Orientation(int base) {
            this.base = base;
        }

        /*
         * Geeft de waarde van de Weg Oriëntatie Lijn.
         *
         * @return de WOL waarde
         */
        public int getWOL() {
            return base % 2 == 0 ? base : base + 4;
        }

        /**
         * Geeeft de waarde van de Baan Oriëntatie Lijn.
         *
         * @return de BOL waarde (van de eerste rijstrook)
         */
        public int getBOL() {
            return base + 4;
        }
    }
    private static final String ADDRESS = "data/hwn/tenuki";
    private static final String BPS_CODE = "10D%03X%06XD00%d00%02X";

    /**
     * Creeër een nieuwe {@link MTMProcessor}.
     */
    public MTMProcessor() {
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
        jsonGenerator.writeArrayFieldStart("raaien");
        BufferedReader reader = new BufferedReader(new InputStreamReader(dataStream));
        String line;
        while ((line = reader.readLine()) != null) {
            createRaai(jsonGenerator, line);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
        jsonGenerator.close();
        return message;
    }

    /**
     * Converteer een regel naar een Raai.
     * 
     * @param jsonGenerator het {@link JsonGenerator} object
     * @param line een regel uit het bestand
     */
    private void createRaai(JsonGenerator jsonGenerator, String line) throws IOException {
        Scanner s = new Scanner(line);
        try {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("raaiId", s.next());
            double v = s.nextDouble();
            if (v == (int) v) {
                jsonGenerator.writeNumberField("intensiteit_vth", (int) v);
            } else {
                jsonGenerator.writeNumberField("intensiteit_vth", v);
            }
            v = s.nextDouble();
            if (v == (int) v) {
                jsonGenerator.writeNumberField("snelheid_kmh", (int) v);
            } else {
                jsonGenerator.writeNumberField("snelheid_kmh", v);
            }
            int laneCount = s.nextInt();
            jsonGenerator.writeNumberField("betrokken_rijstroken", laneCount);
            createLanes(jsonGenerator, s.nextLine(), laneCount);
            jsonGenerator.writeEndObject();
        } catch (IOException ex) { //NOSONAR
            throw ex;
        } catch (Exception ex) {
            logger.warn("Ongeldinge MTM data-regel: {}", line);
            throw new IOException("Invalid input", ex);
        }
    }

    /**
     * Genereer Rijstrook JSON-objecten voor de gegeven BPS.
     *
     * @param jsonGenerator het {@link JsonGenerator} object
     * @param bps de plaatsbepalingsstring uit het bestand
     * @param count het aantal rijstroken
     */
    private void createLanes(JsonGenerator jsonGenerator, String bps, int count) throws IOException {
        Scanner s = new Scanner(bps);
        /* RW */
        s.next();
        /* Wegnummer */
        int num = s.nextInt();
        /* Afstand */
        double dist = s.nextDouble();
        int distance = toDistance(dist);
        /* HR */
        s.next();
        /* WOL */
        Orientation ol = Orientation.valueOf(s.next());
        jsonGenerator.writeArrayFieldStart("rijstroken");
        for (int i = 0; i < count; i++) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeNumberField("rijstrooknr", i + 1);
            jsonGenerator.writeStringField("status", "AAN");
            int lane = ol.getBOL() + i * 4;
            String code = String.format(BPS_CODE, num, distance, ol.getWOL(), lane);
            jsonGenerator.writeStringField("bpscode", code);
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();
    }

    /**
     * Converteer de afstand, gegeven als een double,
     * naar de 3-byte gecodeerde afstand voor de BPS-code.
     *
     * @param dist de afstand uit het bestand
     * @return de integer-waarde voor de BPS-code
     **/
    private int toDistance(double dist) {
        double totalDist = dist * 10;
        int bord = (int) totalDist;
        int afstand = (int) Math.round((totalDist - bord) * 100);
        String bin = String.format("%s%10s", Integer.toBinaryString(bord),
                Integer.toBinaryString(afstand)).replace(' ', '0');
        return Integer.parseInt(bin, 2);
    }
}
