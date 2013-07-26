package cuenen.raymond.java.ppawegkant.post;

import cuenen.raymond.java.ppawegkant.MainApplication;
import java.net.URL;

/**
 *
 * @author R. Cuenen
 */
public class Message {

    private URL address;
    private String contentType;
    private String message;
    private int nofRetries = 0;

    public Message() {
        String value = MainApplication.getProperty("nof_retries");
        if (value != null) {
            try {
                nofRetries = Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                // Log warning message
            }
        }
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

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public boolean resendOnFailure() {
        return --nofRetries > 0;
    }
}