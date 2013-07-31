package cuenen.raymond.java.ppawegkant.test;

import cuenen.raymond.java.ppawegkant.sending.Message;
import java.net.URL;
import java.io.InputStream;
import java.io.File;
import cuenen.raymond.java.ppawegkant.processing.DataProcessor;
import cuenen.raymond.java.ppawegkant.configuration.SystemType;
import cuenen.raymond.java.test.TestUtil;
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
        String msg = new String(result.getMessage());
        System.out.println(msg.replace(",", ",\n"));
        // Convert to JSON?   
    }

    @Test
    public void testMVProcess() throws Exception {
        System.out.println("testMVProcess");
        Message result = testProcessor("RWSNWN.00104", "000104_20121113_150500.mvf", SystemType.TDI_MV);
        assertEquals(new URL("http://localhost:8080/ppawegkant/data/tdi/mv"),
                result.getAddress());
        String msg = new String(result.getMessage());
        System.out.println(msg.replace(",", ",\n"));
        // Convert to JSON?
    }

    @Test
    public void testRTProcess() throws Exception {
        System.out.println("testRTProcess");
        Message result = testProcessor("RWSNWN.00104", "TDI_parms_S105_20121113_150500.txt", SystemType.TDI_RT);
        assertEquals(new URL("http://localhost:8080/ppawegkant/data/tdi/regeltoestand"),
                result.getAddress());
        String msg = new String(result.getMessage());
        System.out.println(msg.replace(",", ",\n"));
        // Convert to JSON?
    }

    @Test
    public void testMTMProcess() throws Exception {
        System.out.println("testMTMProcess");
        Message result = testProcessor("RWSNWN.12345", "PPA_MTM_8001_20121113_150500.txt", SystemType.MTM);
        assertEquals(new URL("http://localhost:8080/ppawegkant/data/hwn/tenuki"),
                result.getAddress());
        String msg = new String(result.getMessage());
        System.out.println(msg.replace(",", ",\n"));
        // Convert to JSON?
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
