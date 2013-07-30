package cuenen.raymond.java.ppawegkant.configuration;

import cuenen.raymond.java.ppawegkant.processing.SystemType;
import java.io.File;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * Deze class stelt de configuratie van een directory voor.
 * Alle benodigde conversie informatie bevind zich in deze class.
 * 
 * @author R. Cuenen
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class SystemData {

    /**
     * Class voor het TDI-MV type.
     */
    public static class TDIMV extends SystemData {

        /**
         * Creeër een TDI-MV type.
         */
        public TDIMV() {
            // JAXB initialization
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SystemType getType() {
            return SystemType.TDI_MV;
        }
    }

    /**
     * Class voor het TDI-RT type.
     */
    public static class TDIRT extends SystemData {

        /**
         * Creeër een TDI-RT type.
         */
        public TDIRT() {
            // JAXB initialization
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SystemType getType() {
            return SystemType.TDI_RT;
        }
    }

    /**
     * Class voor het VRI type.
     */
    public static class VRI extends SystemData {

        /**
         * Creeër een VRI type.
         */
        public VRI() {
            // JAXB initialization
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SystemType getType() {
            return SystemType.VRI;
        }
    }

    /**
     * Class voor het MTM type.
     */
    public static class MTM extends SystemData {

        /**
         * Creeër een MTM type.
         */
        public MTM() {
            // JAXB initialization
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SystemType getType() {
            return SystemType.MTM;
        }
    }
    @XmlValue
    private File directory;
    @XmlAttribute(name = "id", required = true)
    private String identification;

    /**
     * Creeër een nieuwe {@link SystemData}.
     */
    public SystemData() {
        // JAXB initialization
    }

    /**
     * Bepaal het systeemtype.
     * 
     * @return het type systeem
     */
    public abstract SystemType getType();

    /**
     * Geeft de geconfigureerde directory.
     * 
     * @return de directory
     */
    public File getDirectory() {
        return directory;
    }

    /**
     * Configureer de directory.
     * 
     * @param directory de directory
     */
    public void setDirectory(File directory) {
        this.directory = directory;
    }

    /**
     * Geeft de systeem identificatie.
     * 
     * @return de identificatie
     */
    public String getIdentification() {
        return identification;
    }

    /**
     * Configureer de systeem identificatie.
     * 
     * @param identification de identificatie
     */
    public void setIdentification(String identification) {
        this.identification = identification;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SystemData) {
            SystemData that = (SystemData) obj;
            return (this.directory == null ? that.directory == null
                    : this.directory.equals(that.directory))
                    && (this.identification == null ? that.identification == null
                    : this.identification.equals(that.identification));
        }
        return super.equals(obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (this.directory != null ? this.directory.hashCode() : 0);
        hash = 67 * hash + (this.identification != null ? this.identification.hashCode() : 0);
        return hash;
    }
}
