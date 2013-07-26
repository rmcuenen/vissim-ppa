package cuenen.raymond.java.ppawegkant.file;

import cuenen.raymond.java.ppawegkant.configuration.SystemData;
import cuenen.raymond.java.ppawegkant.post.Message;
import cuenen.raymond.java.ppawegkant.processing.DataProcessor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author R. Cuenen
 */
public class VLogProcessor extends DataProcessor {

    private static final String ADDRESS = "data/vri/vlog";

    public VLogProcessor() {
        super();
    }

    @Override
    public Message process(String filename, InputStream dataStream, SystemData context) throws IOException {
        // Log debug message
        List<String> vlogContent = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(dataStream));
        String line;
        while ((line = reader.readLine()) != null) {
            vlogContent.add(line);
        }
        String json = createObject(filename, context.getIdentification(), vlogContent);
        return newMessage(ADDRESS, json.getBytes());
    }

    private String createObject(String filename, String vri, List<String> vlogAscii) {
        StringBuilder obj = new StringBuilder();
        long timestamp = toTimestamp(filename);
        obj.append("{\"interface\":\"").append(ADDRESS).append("\",");
        obj.append("{\"timestamp\":").append(timestamp).append(',');
        obj.append("{\"vri\":\"").append(vri).append("\",");
        obj.append("{\"vlog_naam\":\"").append(filename).append("\",");
        obj.append("{\"vlog_ascii\":[");
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
