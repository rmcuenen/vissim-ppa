package cuenen.raymond.java.ppawegkant.processing;

import cuenen.raymond.java.ppawegkant.sending.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.slf4j.LoggerFactory;

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
        Scanner s = new Scanner(line);
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"raaiId\":\"").append(s.next()).append("\",");
            double v = s.nextDouble();
            sb.append("\"intensiteit_vth\":").append(v == (int) v ? (int) v : v).append(',');
            s.nextDouble();
            sb.append("\"snelheid_kmh\":").append(v == (int) v ? (int) v : v).append(',');
            int laneCount = s.nextInt();
            sb.append("\"betrokken_rijstroken\":").append(laneCount).append(',');
            String bps = s.nextLine();
            List<String> lanes = createLanes(bps, laneCount);
            sb.append("\"rijstroken\":[");
            for (int i = 0; i < lanes.size(); i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(lanes.get(i));
            }
            sb.append("]}");
            return sb.toString();
        } catch (Exception ex) {
            logger.warn("Ongeldinge MTM data-regel: {}", line);
        }
        return null;
    }

    /**
     * Genereer Rijstrook JSON-objecten voor de gegeven BPS.
     *
     * @param bps de plaatsbepalingsstring uit het bestand
     * @param count het aantal rijstroken
     * @return een lijst met JSON-strings
     */
    private List<String> createLanes(String bps, int count) {
        List<String> lanes = new ArrayList<String>();
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
        for (int i = 0; i < count; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"rijstrooknr\":").append(i + 1).append(',');
            sb.append("\"status\":\"AAN\",");
            int lane = ol.getBOL() + i * 4;
            String code = String.format(BPS_CODE, num, distance, ol.getWOL(), lane);
            sb.append("\"bpscode\":\"").append(code).append("\"}");
            lanes.add(sb.toString());
        }
        return lanes;
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

