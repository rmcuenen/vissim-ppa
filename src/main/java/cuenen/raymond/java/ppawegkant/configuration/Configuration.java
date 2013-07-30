package cuenen.raymond.java.ppawegkant.configuration;

import java.util.Collection;
import java.util.HashSet;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Deze class stelt de configuratie voor.
 * Hierin staat o.a. de URL van de PPA-bus.
 * 
 * @author R. Cuenen
 */
@XmlRootElement(name = "configuratie")
@XmlAccessorType(XmlAccessType.FIELD)
public class Configuration {

    @XmlElement(name = "ppawegkant", required = true)
    private String ppaBus;
    @XmlElementWrapper(name = "data")
    @XmlElements({
        @XmlElement(name = "tdi-mv", type = SystemData.TDIMV.class),
        @XmlElement(name = "tdi-rt", type = SystemData.TDIRT.class),
        @XmlElement(name = "vri", type = SystemData.VRI.class),
        @XmlElement(name = "mtm", type = SystemData.MTM.class)
    })
    private final Collection<SystemData> data = new HashSet<SystemData>();

    /**
     * Creeër een nieuwe {@link Configuration}.
     */
    public Configuration() {
        // JAXB initialization
    }

    /**
     * Geeft de geconfigureerde URL van de PPA-bus.
     * 
     * @return de URL van de PPA-bus
     */
    public String getPpaBus() {
        return ppaBus;
    }

    /**
     * Confiureer de URL van de PPA-bus.
     * 
     * @param ppaBus de URL van de PPA-bus
     */
    public void setPpaBus(String ppaBus) {
        this.ppaBus = ppaBus;
    }

    /**
     * Bekijk de gecofigureerde systemen.
     * 
     * @return collectie van geconfigureerde systemen
     */
    public Collection<SystemData> getData() {
        return data;
    }
}
