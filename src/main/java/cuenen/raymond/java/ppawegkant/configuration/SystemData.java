package cuenen.raymond.java.ppawegkant.configuration;

import cuenen.raymond.java.ppawegkant.processing.SystemType;
import java.io.File;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 *
 * @author R. Cuenen
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class SystemData {

    public static class TDIMV extends SystemData {

        public TDIMV() {
            // JAXB initialization
        }

        @Override
        public SystemType getType() {
            return SystemType.TDI_MV;
        }
    }

    public static class TDIRT extends SystemData {

        public TDIRT() {
            // JAXB initialization
        }

        @Override
        public SystemType getType() {
            return SystemType.TDI_RT;
        }
    }

    public static class VRI extends SystemData {

        public VRI() {
            // JAXB initialization
        }

        @Override
        public SystemType getType() {
            return SystemType.VRI;
        }
    }

    public static class MTM extends SystemData {

        public MTM() {
            // JAXB initialization
        }

        @Override
        public SystemType getType() {
            return SystemType.MTM;
        }
    }
    @XmlValue
    private File directory;
    @XmlAttribute(name = "id", required = true)
    private String identification;

    public SystemData() {
        // JAXB initialization
    }

    public abstract SystemType getType();

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }
}
