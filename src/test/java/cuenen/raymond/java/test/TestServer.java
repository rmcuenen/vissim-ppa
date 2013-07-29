package cuenen.raymond.java.test;

import java.io.BufferedReader;
import java.io.Console;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author R. Cuenen
 */
public class TestServer extends Thread {

    public static interface TestHandler {

        void onFailure(String origin, IOException ex);

        void onSuccess(String origin, List<String> request);
    }

    public static class DefaultHandler implements TestHandler {

        @Override
        public void onFailure(String origin, IOException ex) {
            System.out.println(origin + " - ERROR: " + ex.toString());
        }

        @Override
        public void onSuccess(String origin, List<String> request) {
            System.out.println(origin + ":");
            for (String line : request) {
                System.out.println(line);
            }
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof DefaultHandler;
        }
    }
    private static final String RESPONSE = "HTTP/1.1 200 OK\r\nDate: %s\r\n\r\n";
    private final List<TestHandler> testHandlers = new ArrayList<TestHandler>();
    private final ServerSocket server;
    private String currentClient;

    public TestServer(int port) throws IOException {
        server = new ServerSocket(port);
    }

    public void addTestHandler(TestHandler handler) {
        if (!testHandlers.contains(handler)) {
            testHandlers.add(handler);
        }
    }

    public void removeTestHandler(TestHandler handler) {
        testHandlers.remove(handler);
    }

    public void sutdown() {
        try {
            server.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void run() {
        do {
            try {
                currentClient = null;
                Socket client = server.accept();
                currentClient = client.getInetAddress().getHostName();
                handle(client);
            } catch (IOException ex) {
                notifyHandlers(ex);
            }
        } while (!server.isClosed());
    }

    private void handle(Socket socket) throws IOException {
        InputStream input = socket.getInputStream();
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        List<String> inputLines = new ArrayList<String>();
        String line;
        while ((line = reader.readLine()) != null) {
            inputLines.add(line);
            if (line.isEmpty()) {
                if (usingPost(inputLines)) {
                    readPostData(inputLines, reader);
                }
                break;
            }
        }
        output.writeBytes(String.format(RESPONSE, new Date().toString()));
        output.flush();
        output.close();
        socket.close();
        notifyHandlers(inputLines);
    }

    private boolean usingPost(List<String> inputs) {
        return inputs.get(0).toUpperCase().startsWith("POST");
    }

    private void readPostData(List<String> inputs, BufferedReader in) throws IOException {
        int contentLength = contentLength(inputs);
        char[] postData = new char[contentLength];
        in.read(postData, 0, contentLength);
        inputs.add(new String(postData, 0, contentLength));
    }

    private int contentLength(List<String> inputs) {
        for (String input : inputs) {
            if (input.isEmpty()) {
                break;
            }
            if (input.toUpperCase().startsWith("CONTENT-LENGTH")) {
                return (getLength(input));
            }
        }
        return 0;
    }

    private int getLength(String length) {
        StringTokenizer tok = new StringTokenizer(length);
        tok.nextToken();
        return (Integer.parseInt(tok.nextToken()));
    }

    private void notifyHandlers(Object o) {
        IOException ex = o instanceof IOException ? (IOException) o : null;
        for (TestHandler handler : testHandlers) {
            if (ex == null) {
                handler.onSuccess(currentClient, (List<String>) o);
            } else {
                handler.onFailure(currentClient, ex);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 8008;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception ex) {
        }
        TestServer server = new TestServer(port);
        server.addTestHandler(new DefaultHandler());
        server.start();
        Console c = System.console();
        BufferedReader reader;
        if (c == null) {
            reader = new BufferedReader(new InputStreamReader(System.in));
        } else {
            reader = new BufferedReader(c.reader());
        }
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.equalsIgnoreCase("stop")) {
                server.sutdown();
                break;
            }
        }
    }
}
