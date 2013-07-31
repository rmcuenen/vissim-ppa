package cuenen.raymond.java.ppawegkant.test;

import java.util.concurrent.BlockingQueue;
import cuenen.raymond.java.ppawegkant.sending.Message;
import cuenen.raymond.java.ppawegkant.sending.MessageSender;
import java.lang.reflect.Field;
import cuenen.raymond.java.ppawegkant.configuration.SystemData;
import cuenen.raymond.java.ppawegkant.watching.DirectoryWatcher;
import cuenen.raymond.java.ppawegkant.configuration.SystemType;
import cuenen.raymond.java.ppawegkant.processing.DataProcessor;
import cuenen.raymond.java.test.TestUtil;
import java.io.File;
import org.apache.log4j.BasicConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * JUnit test voor het controleren van het pollen
 * van een directory.
 * 
 * @author R. Cuenen
 */
public class DirectoryWatcherTest {

    private static final String FILENAME = "kr987_20121113_150500.vlg";
    private Thread waitingThread;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BasicConfigurator.configure();
        DataProcessor.setBaseURL("http://localhost:8080/");
    }

    @Test
    public void testDirectoryWatcher() throws Exception {
        File dir = TestUtil.createDir("test" + File.separator + "data");
        SystemData sd = mock(SystemData.class);
        BlockingQueue<Message> mq = mock(BlockingQueue.class);
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                waitingThread.interrupt();
                return null;
            }
        }).when(mq).put(any(Message.class));
        Field mqField = MessageSender.class.getDeclaredField("messageQueue");
        mqField.setAccessible(true);
        mqField.set(MessageSender.getInstance(), mq);
        when(sd.getIdentification()).thenReturn("TestVRI");
        when(sd.getDirectory()).thenReturn(dir);
        when(sd.getType()).thenReturn(SystemType.VRI);
        DirectoryWatcher watcher = new DirectoryWatcher(sd);
        watcher.start();
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
        File testFile = TestUtil.createFile(dir, "test.properties");
        File targetFile = TestUtil.createFile(dir, FILENAME);
        waitingThread.join();
        watcher.stop();
        Thread.sleep(500L);
        assertTrue(testFile.exists());
        assertFalse(targetFile.exists());
        verify(sd, times(3)).getIdentification();
        verify(sd).getType();
        verify(mq).put(any(Message.class));
        verify(sd, times(3)).getDirectory();
        verifyNoMoreInteractions(sd, mq);
    }
}
