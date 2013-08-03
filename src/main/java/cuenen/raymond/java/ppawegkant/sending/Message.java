package cuenen.raymond.java.ppawegkant.sending;

import java.io.ByteArrayOutputStream;
import java.net.URL;

/**
 * Deze class stelt een JSON bericht voor
 * wat op de PPA-bus gezet moet worden.
 * 
 * @author R. Cuenen
 */
public class Message extends ByteArrayOutputStream {

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
    private int nofRetries = RETRY_COUNT;

    /**
     * Creeër een nieuw bericht.
     */
    public Message() {
        // Empty initialization
    }

    /**
     * Stel het afleveradres in.
     * 
     * @param address het adres van het bericht
     */
    public void setAddress(URL address) {
        this.address = address;
    }

    /**
     * Bekijk het afleveradres.
     * 
     * @return het adres van het bericht
     */
    public URL getAddress() {
        return address;
    }

    /**
     * Set het berichttype in.
     * Bijvoorbeeld "text/html" of "application/json".
     * 
     * @param contentType het berichttype
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Bekijkt het berichttype.
     * 
     * @return het berichttype
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Bepaal of het bericht nog een keer verstuurd
     * moet worden na een foute poging.
     * 
     * @return {@code true} wanneer het bericht nog
     * een keer verstuurd moet worden, anders {@code false}
     */
    public boolean resendOnFailure() {
        return --nofRetries > 0;
    }
}
