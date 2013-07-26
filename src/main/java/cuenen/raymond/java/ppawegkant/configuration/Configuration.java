package cuenen.raymond.java.ppawegkant.configuration;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author R. Cuenen
 */
@XmlRootElement(name = "configuratie")
@XmlAccessorType(XmlAccessType.FIELD)
public class Configuration {

    @XmlElement(name = "ppawegkant", required = true)
    private String ppaBus;
    @XmlElements({
        @XmlElement(name = "tdi-mv", type = SystemData.TDIMV.class),
        @XmlElement(name = "tdi-rt", type = SystemData.TDIRT.class),
        @XmlElement(name = "vri", type = SystemData.VRI.class),
        @XmlElement(name = "mtm", type = SystemData.MTM.class)
    })
    private final List<SystemData> data = new ArrayList<SystemData>();

    public Configuration() {
        // JAXB initialization
    }

    public String getPpaBus() {
        return ppaBus;
    }

    public void setPpaBus(String ppaBus) {
        this.ppaBus = ppaBus;
    }

    public List<SystemData> getData() {
        return data;
    }
}