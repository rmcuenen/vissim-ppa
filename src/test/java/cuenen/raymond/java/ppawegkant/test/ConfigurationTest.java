package cuenen.raymond.java.ppawegkant.test;

import java.io.File;
import cuenen.raymond.java.ppawegkant.application.MainApplication;
import cuenen.raymond.java.ppawegkant.configuration.Configuration;
import cuenen.raymond.java.ppawegkant.configuration.SystemData;
import java.io.StringReader;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.util.Collection;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit test voor het controleren van de xsd van 
 * de configuratie xml.
 * 
 * @author R. Cuenen
 */
public class ConfigurationTest {

    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";
    private static JAXBContext JAXB_CONTEXT;
    private Unmarshaller unmarshaller;

    @BeforeClass
    public static void setUpClass() throws Exception {
        JAXB_CONTEXT = JAXBContext.newInstance(Configuration.class);
    }

    @Before
    public void setUp() throws Exception {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(MainApplication.class.getResource("/schema/vissim-ppa.xsd"));
        unmarshaller = JAXB_CONTEXT.createUnmarshaller();
        unmarshaller.setSchema(schema);
    }

    @Test
    public void testValidConfiguration() throws Exception {
        System.out.print("testValidConfiguration");
        StringBuilder xml = new StringBuilder(XML_HEADER);
        xml.append("<configuratie>");
        xml.append("<ppawegkant>http://192.168.50.102:8008</ppawegkant>");
        xml.append("<data>");
        xml.append("<tdi-mv id=\"RWSNWN.00104\">C:\\data\\mv\\000104</tdi-mv>");
        xml.append("<tdi-rt id=\"RWSNWN.00104\">C:\\data\\regeltoestand\\000104</tdi-rt>");
        xml.append("<vri id=\"AMS.987\">C:\\data\\vlog\\kr987</vri>");
        xml.append("<mtm id=\"RWSNWN.12345\">C:\\data\\mtm\\8001</mtm>");
        xml.append("</data></configuratie>");
        Configuration configuration = (Configuration) unmarshaller.unmarshal(new StringReader(xml.toString()));
        assertEquals("http://192.168.50.102:8008", configuration.getPpaBus());
        Collection<SystemData> data = configuration.getData();
        assertEquals(4, data.size());
        for (SystemData sd : data) {
            switch (sd.getType()) {
                case TDI_MV:
                    assertEquals("RWSNWN.00104", sd.getIdentification());
                    assertEquals(new File("C:\\data\\mv\\000104"), sd.getDirectory());
                    break;
                case TDI_RT:
                    assertEquals("RWSNWN.00104", sd.getIdentification());
                    assertEquals(new File("C:\\data\\regeltoestand\\000104"), sd.getDirectory());
                    break;
                case VRI:
                    assertEquals("AMS.987", sd.getIdentification());
                    assertEquals(new File("C:\\data\\vlog\\kr987"), sd.getDirectory());
                    break;
                case MTM:
                    assertEquals("RWSNWN.12345", sd.getIdentification());
                    assertEquals(new File("C:\\data\\mtm\\8001"), sd.getDirectory());
                    break;
                default:
                    fail("Unexpected type: " + sd.getType());
            }
        }
        System.out.println(" - OK");
    }

    @Test
    public void testEmptyPpaBus() {
        System.out.println("testEmptyPpaBus");
        StringBuilder xml = new StringBuilder(XML_HEADER);
        xml.append("<configuratie>");
        xml.append("<ppawegkant/>");
        xml.append("<data>");
        xml.append("<tdi-mv id=\"RWSNWN.00104\">C:\\data\\mv\\000104</tdi-mv>");
        xml.append("</data></configuratie>");
        try {
            unmarshaller.unmarshal(new StringReader(xml.toString()));
            fail("Expected JAXBException to be thrown");
        } catch (JAXBException ex) {
            Throwable t = ex.getLinkedException();
            System.out.println(t == null ? ex : t.getLocalizedMessage());
        }
    }

    @Test
    public void testNoPpaBus() {
        System.out.println("testNoPpaBus");
        StringBuilder xml = new StringBuilder(XML_HEADER);
        xml.append("<configuratie>");
        xml.append("<data>");
        xml.append("<vri id=\"AMS.987\">C:\\data\\vlog\\kr987</tdi-mv>");
        xml.append("</data></configuratie>");
        try {
            unmarshaller.unmarshal(new StringReader(xml.toString()));
            fail("Expected JAXBException to be thrown");
        } catch (JAXBException ex) {
            Throwable t = ex.getLinkedException();
            System.out.println(t == null ? ex : t.getLocalizedMessage());
        }
    }

    @Test
    public void testEmptyData() throws Exception {
        System.out.print("testEmptyData");
        StringBuilder xml = new StringBuilder(XML_HEADER);
        xml.append("<configuratie>");
        xml.append("<ppawegkant>http://192.168.50.102:8008</ppawegkant>");
        xml.append("<data/></configuratie>");
        Configuration configuration = (Configuration) unmarshaller.unmarshal(new StringReader(xml.toString()));
        assertEquals("http://192.168.50.102:8008", configuration.getPpaBus());
        Collection<SystemData> data = configuration.getData();
        assertTrue(data.isEmpty());
        System.out.println(" - OK");
    }

    @Test
    public void testNoData() {
        System.out.println("testNoData");
        StringBuilder xml = new StringBuilder(XML_HEADER);
        xml.append("<configuratie>");
        xml.append("<ppawegkant>http://192.168.50.102:8008</ppawegkant>");
        xml.append("</configuratie>");
        try {
            unmarshaller.unmarshal(new StringReader(xml.toString()));
            fail("Expected JAXBException to be thrown");
        } catch (JAXBException ex) {
            Throwable t = ex.getLinkedException();
            System.out.println(t == null ? ex : t.getLocalizedMessage());
        }
    }

    @Test
    public void testNoSystemId() {
        System.out.println("testNoSystemId");
        StringBuilder xml = new StringBuilder(XML_HEADER);
        xml.append("<configuratie>");
        xml.append("<ppawegkant>http://192.168.50.102:8008</ppawegkant>");
        xml.append("<data>");
        xml.append("<tdi-rt>C:\\data\\regeltoestand\\000104</tdi-rt>");
        xml.append("</data></configuratie>");
        try {
            unmarshaller.unmarshal(new StringReader(xml.toString()));
            fail("Expected JAXBException to be thrown");
        } catch (JAXBException ex) {
            Throwable t = ex.getLinkedException();
            System.out.println(t == null ? ex : t.getLocalizedMessage());
        }
    }

    @Test
    public void testNoSystemDirectory() throws Exception {
        System.out.println("testNoSystemDirectory");
        StringBuilder xml = new StringBuilder(XML_HEADER);
        xml.append("<configuratie>");
        xml.append("<ppawegkant>http://192.168.50.102:8008</ppawegkant>");
        xml.append("<data>");
        xml.append("<tdi-rt id=\"RWSNWN.00104\"/>");
        xml.append("</data></configuratie>");
        try {
            unmarshaller.unmarshal(new StringReader(xml.toString()));
            fail("Expected JAXBException to be thrown");
        } catch (JAXBException ex) {
            Throwable t = ex.getLinkedException();
            System.out.println(t == null ? ex : t.getLocalizedMessage());
        }
    }

    @Test
    public void testDuplicateSystems() throws Exception {
        System.out.print("testDuplicateSystems");
        StringBuilder xml = new StringBuilder(XML_HEADER);
        xml.append("<configuratie>");
        xml.append("<ppawegkant>http://192.168.50.102:8008</ppawegkant>");
        xml.append("<data>");
        xml.append("<tdi-mv id=\"RWSNWN.00104\">C:\\data\\mv\\000104</tdi-mv>");
        xml.append("<tdi-mv id=\"RWSNWN.00104\">C:\\data\\mv\\000104</tdi-mv>");
        xml.append("</data></configuratie>");
        Configuration configuration = (Configuration) unmarshaller.unmarshal(new StringReader(xml.toString()));
        assertEquals("http://192.168.50.102:8008", configuration.getPpaBus());
        Collection<SystemData> data = configuration.getData();
        assertEquals(1, data.size());
        for (SystemData sd : data) {
            assertEquals("RWSNWN.00104", sd.getIdentification());
            assertEquals(new File("C:\\data\\mv\\000104"), sd.getDirectory());
        }
        System.out.println(" - OK");
    }

    @Test
    public void testIncorrectOrder() {
        System.out.println("testIncorrectOrder");
        StringBuilder xml = new StringBuilder(XML_HEADER);
        xml.append("<configuratie>");
        xml.append("<data>");
        xml.append("<mtm id=\"RWSNWN.12345\">C:\\data\\mtm\\8001</mtm>");
        xml.append("</data>");
        xml.append("<ppawegkant>http://192.168.50.102:8008</ppawegkant>");
        xml.append("</configuratie>");
        try {
            unmarshaller.unmarshal(new StringReader(xml.toString()));
            fail("Expected JAXBException to be thrown");
        } catch (JAXBException ex) {
            Throwable t = ex.getLinkedException();
            System.out.println(t == null ? ex : t.getLocalizedMessage());
        }
    }
}
