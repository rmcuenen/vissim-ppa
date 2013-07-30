package cuenen.raymond.java.ppawegkant.test;

import cuenen.raymond.java.ppawegkant.sending.Message;
import org.junit.After;
import org.junit.Before;
import java.lang.reflect.Field;
import java.net.URL;
import cuenen.raymond.java.ppawegkant.application.MainApplication;
import java.io.InputStream;
import java.io.File;
import cuenen.raymond.java.ppawegkant.configuration.SystemData;
import cuenen.raymond.java.ppawegkant.processing.DataProcessor;
import cuenen.raymond.java.ppawegkant.processing.SystemType;
import cuenen.raymond.java.test.TestUtil;
import java.io.FileInputStream;
import org.apache.log4j.BasicConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * JUnit test voor het controleren van de verwerking
 * van de verschillende bestanden.
 * 
 * @author R. Cuenen
 */
public class ProcessorTest {

    private SystemData context;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BasicConfigurator.configure();
        Field baseUrl = MainApplication.class.getDeclaredField("baseURL");
        baseUrl.setAccessible(true);
        baseUrl.set(MainApplication.getApplication(), "http://localhost:8080/");
    }

    @Before
    public void setUp() {
        context = mock(SystemData.class);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(context);
    }

    @Test
    public void testVLogProcess() throws Exception {
        System.out.println("testVLogProcess");
        Message result = testProcessor("AMS.987", "kr987_20121113_150500.vlg", SystemType.VRI);
        assertEquals(new URL("http://localhost:8080/ppawegkant/data/vri/vlog"),
                result.getAddress());
        String msg = new String(result.getMessage());
        System.out.println(msg.replace(",", ",\n"));
        verify(context, times(2)).getIdentification();
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
        verify(context, times(2)).getIdentification();
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
        verify(context, times(2)).getIdentification();
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
        verify(context).getIdentification();
        // Convert to JSON?
    }

    private Message testProcessor(String id, String filename, SystemType type) throws Exception {
        when(context.getIdentification()).thenReturn(id);
        File file = TestUtil.createFile(filename);
        InputStream dataStream = new FileInputStream(file);
        DataProcessor processor = type.getDataProcessor();
        Message result = processor.process(filename, dataStream, context);
        dataStream.close();
        return result;
    }
}
