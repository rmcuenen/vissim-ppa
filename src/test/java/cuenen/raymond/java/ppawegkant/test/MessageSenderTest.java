package cuenen.raymond.java.ppawegkant.test;

import cuenen.raymond.java.ppawegkant.sending.Message;
import cuenen.raymond.java.ppawegkant.sending.MessageSender;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import java.io.IOException;
import java.net.URL;
import cuenen.raymond.java.test.TestServer;
import org.apache.log4j.BasicConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit test voor het controleren van het posten
 * van een bericht.
 * 
 * @author R. Cuenen
 */
public class MessageSenderTest implements TestServer.TestHandler {

    private static class Request {

        String origin;
        Exception exception;
        List<String> requestLines;
    }
    private static final String MESSAGE = "{\"hello\":\"world\"}";
    private Request messageRequest;
    private Thread waitingThread;
    private MessageSender messageSender;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BasicConfigurator.configure();
        System.getProperties().put("message.retryCount", "3");
    }

    @Before
    public void setUp() {
        messageSender = new MessageSender();
    }

    @After
    public void tearDown() {
        messageSender.shutdown();
    }

    @Test
    public void testMessageCount() {
        System.out.print("testMessageCount");
        Message msg = new Message();
        int count = 0;
        do {
            count++;
        } while (msg.resendOnFailure());
        assertEquals(3, count);
        System.out.println(" - OK");
    }

    @Test
    public void testPostMessage() throws Exception {
        System.out.println("testPostMessage");
        Message msg = new Message();
        msg.setAddress(new URL("http://localhost:8008/ppawegkant/test/"));
        msg.setContentType("application/json");
        msg.setMessage(MESSAGE.getBytes());
        TestServer server = new TestServer(8008);
        server.addTestHandler(this);
        server.start();
        waitingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                do {
                    try {
                        Thread.sleep(5000L);
                    } catch (InterruptedException ex) {
                        break;
                    }
                } while (true);
            }
        });
        waitingThread.start();
        messageSender.addMessage(msg);
        waitingThread.join();
        server.sutdown();
        validateRequest();
    }

    private void validateRequest() throws Exception {
        assertNotNull(messageRequest);
        assertEquals("localhost", messageRequest.origin);
        if (messageRequest.exception != null) {
            fail("Unexpected exception " + messageRequest.exception);
        }
        assertFalse(messageRequest.requestLines.isEmpty());
        assertEquals("POST /ppawegkant/test/ HTTP/1.1", messageRequest.requestLines.get(0));
        for (int i = 2; i < messageRequest.requestLines.size(); i++) {
            String line = messageRequest.requestLines.get(i);
            if (line.startsWith("Content-Type")) {
                assertEquals("Content-Type: application/json", line);
            } else if (line.startsWith("Content-Length")) {
                assertEquals("Content-Length: 17", line);
            } else if (i == messageRequest.requestLines.size() - 1) {
                assertEquals(MESSAGE, line);
            }
        }
    }

    @Override
    public void onFailure(String origin, IOException ex) {
        messageRequest = new Request();
        messageRequest.origin = origin;
        messageRequest.exception = ex;
        waitingThread.interrupt();
    }

    @Override
    public void onSuccess(String origin, List<String> request) {
        messageRequest = new Request();
        messageRequest.origin = origin;
        messageRequest.requestLines = request;
        waitingThread.interrupt();
    }
}
