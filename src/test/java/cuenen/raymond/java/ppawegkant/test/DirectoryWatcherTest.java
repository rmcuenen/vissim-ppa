package cuenen.raymond.java.ppawegkant.test;

import cuenen.raymond.java.ppawegkant.post.Message;
import cuenen.raymond.java.ppawegkant.post.MessageSender;
import cuenen.raymond.java.ppawegkant.MainApplication;
import java.lang.reflect.Field;
import cuenen.raymond.java.ppawegkant.configuration.SystemData;
import cuenen.raymond.java.ppawegkant.file.DirectoryWatcher;
import cuenen.raymond.java.ppawegkant.processing.SystemType;
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
        Field baseUrl = MainApplication.class.getDeclaredField("baseURL");
        baseUrl.setAccessible(true);
        baseUrl.set(MainApplication.getApplication(), "http://localhost:8080/");
    }

    @Test
    public void testDirectoryWatcher() throws Exception {
        File dir = TestUtil.createDir("test" + File.separator + "data");
        SystemData sd = mock(SystemData.class);
        MessageSender ms = mock(MessageSender.class);
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                waitingThread.interrupt();
                return null;
            }
        }).when(ms).addMessage(any(Message.class));
        Field messageSender = MainApplication.class.getDeclaredField("messageSender");
        messageSender.setAccessible(true);
        messageSender.set(MainApplication.getApplication(), ms);
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
        verify(sd, times(4)).getIdentification();
        verify(sd).getType();
        verify(ms).addMessage(any(Message.class));
        verify(sd, times(3)).getDirectory();
        verifyNoMoreInteractions(sd, ms);
    }
}
