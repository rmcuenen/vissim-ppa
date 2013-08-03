package cuenen.raymond.java.ppawegkant.test;

import java.util.List;
import java.util.Map;
import org.codehaus.jackson.map.ObjectMapper;
import cuenen.raymond.java.ppawegkant.sending.Message;
import java.net.URL;
import java.io.InputStream;
import java.io.File;
import cuenen.raymond.java.ppawegkant.processing.DataProcessor;
import cuenen.raymond.java.ppawegkant.configuration.SystemType;
import cuenen.raymond.java.test.TestUtil;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import org.apache.log4j.BasicConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit test voor het controleren van de verwerking
 * van de verschillende bestanden.
 * 
 * @author R. Cuenen
 */
public class ProcessorTest {
    
    private final ObjectMapper mapper = new ObjectMapper();
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        BasicConfigurator.configure();
        DataProcessor.setBaseURL("http://localhost:8080/");
    }
    
    @Test
    public void testVLogProcess() throws Exception {
        System.out.println("testVLogProcess");
        Message result = testProcessor("AMS.987", "kr987_20121113_150500.vlg", SystemType.VRI);
        assertEquals(new URL("http://localhost:8080/ppawegkant/data/vri/vlog"),
                result.getAddress());
        System.out.println(result.toString().replace(",", ",\n"));
        ByteArrayInputStream stream = new ByteArrayInputStream(result.toByteArray());
        Map<String, Object> map = mapper.readValue(stream, Map.class);
        assertEquals("data/vri/vlog", map.get("interface"));
        assertEquals(1352815500000L, map.get("timestamp"));
        assertEquals("AMS.987", map.get("vri"));
        assertEquals("kr987_20121113_150500.vlg", map.get("vlog_naam"));
        List<String> vlog = (List<String>) map.get("vlog_ascii");
        assertEquals(395, vlog.size());
        assertEquals("010012111315050000", vlog.get(0));
    }
    
    @Test
    public void testMVProcess() throws Exception {
        System.out.println("testMVProcess");
        Message result = testProcessor("RWSNWN.00104", "000104_20121113_150500.mvf", SystemType.TDI_MV);
        assertEquals(new URL("http://localhost:8080/ppawegkant/data/tdi/mv"),
                result.getAddress());
        System.out.println(result.toString().replace(",", ",\n"));
        ByteArrayInputStream stream = new ByteArrayInputStream(result.toByteArray());
        Map<String, Object> map = mapper.readValue(stream, Map.class);
        assertEquals("data/tdi/mv", map.get("interface"));
        assertEquals(1352815500000L, map.get("timestamp"));
        assertEquals("RWSNWN.00104", map.get("tdi"));
        assertEquals("000104_20121113_150500.mvf", map.get("mv_naam"));
        String mv = (String) map.get("mv_bin");
        assertTrue(mv.startsWith("CwAAAwENCwfcDwU"));
    }
    
    @Test
    public void testRTProcess() throws Exception {
        System.out.println("testRTProcess");
        Message result = testProcessor("RWSNWN.00104", "TDI_parms_S105_20121113_150500.txt", SystemType.TDI_RT);
        assertEquals(new URL("http://localhost:8080/ppawegkant/data/tdi/regeltoestand"),
                result.getAddress());
        System.out.println(result.toString().replace(",", ",\n"));
        ByteArrayInputStream stream = new ByteArrayInputStream(result.toByteArray());
        Map<String, Object> map = mapper.readValue(stream, Map.class);
        assertEquals("data/tdi/regeltoestand", map.get("interface"));
        assertEquals(1352815500000L, map.get("timestamp"));
        assertEquals("RWSNWN.00104", map.get("tdi"));
        assertEquals("UIT", map.get("regeltoestand"));
        assertEquals(81, map.get("v_rwso_kmh"));
        assertEquals(78, map.get("v_rwsa_kmh"));
        assertEquals(1620, map.get("i_rwso_vth"));
        assertEquals(2160, map.get("i_rwsa_vth"));
        assertEquals(0, map.get("i_doseer_vth"));
        List storing = (List) map.get("storing");
        assertTrue(storing.isEmpty());
    }
    
    @Test
    public void testMTMProcess() throws Exception {
        System.out.println("testMTMProcess");
        Message result = testProcessor("RWSNWN.12345", "PPA_MTM_8001_20121113_150500.txt", SystemType.MTM);
        assertEquals(new URL("http://localhost:8080/ppawegkant/data/hwn/tenuki"),
                result.getAddress());
        System.out.println(result.toString().replace(",", ",\n"));
        ByteArrayInputStream stream = new ByteArrayInputStream(result.toByteArray());
        Map<String, Object> map = mapper.readValue(stream, Map.class);
        assertEquals("data/hwn/tenuki", map.get("interface"));
        assertEquals(1352815500000L, map.get("timestamp"));
        List<Map<String, Object>> r = (List<Map<String, Object>>) map.get("raaien");
        assertEquals(28, r.size());
        map = r.get(0);
        assertEquals("010hrr0219rA", map.get("raaiId"));
        assertEquals(3000, map.get("intensiteit_vth"));
        assertEquals(80, map.get("snelheid_kmh"));
        assertEquals(3, map.get("betrokken_rijstroken"));
        r = (List<Map<String, Object>>) map.get("rijstroken");
        assertEquals(3, r.size());
        map = r.get(0);
        assertEquals(1, map.get("rijstrooknr"));
        assertEquals("AAN", map.get("status"));
        assertEquals("10D00A036C37D0070007", map.get("bpscode"));
    }
    
    private Message testProcessor(String id, String filename, SystemType type) throws Exception {
        File file = TestUtil.createFile(filename);
        InputStream dataStream = new FileInputStream(file);
        DataProcessor processor = type.getDataProcessor();
        Message result = processor.process(filename, dataStream, id);
        dataStream.close();
        return result;
    }
}
