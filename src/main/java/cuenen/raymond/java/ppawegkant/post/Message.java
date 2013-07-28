package cuenen.raymond.java.ppawegkant.post;

import java.net.URL;

/**
 *
 * @author R. Cuenen
 */
public class Message {

    private static final int RETRY_COUNT;

    static {
        String value = System.getProperty("message.retryCount", "0");
        int count = 0;
        try {
            count = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            // Ignore
        }
        RETRY_COUNT = count;
    }
    private URL address;
    private String contentType;
    private byte[] message;
    private int nofRetries = RETRY_COUNT;

    public Message() {
        // Empty initialization
    }

    public void setAddress(URL address) {
        this.address = address;
    }

    public URL getAddress() {
        return address;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public void setMessage(byte[] message) {
        this.message = new byte[message.length];
        System.arraycopy(message, 0, this.message, 0, message.length);
    }

    public byte[] getMessage() {
        return message;
    }

    public boolean resendOnFailure() {
        return --nofRetries > 0;
    }
}
