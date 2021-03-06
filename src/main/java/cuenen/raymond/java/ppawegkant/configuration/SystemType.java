package cuenen.raymond.java.ppawegkant.configuration;

import cuenen.raymond.java.ppawegkant.processing.DataProcessor;
import cuenen.raymond.java.ppawegkant.processing.MTMProcessor;
import cuenen.raymond.java.ppawegkant.processing.MVProcessor;
import cuenen.raymond.java.ppawegkant.processing.RTProcessor;
import cuenen.raymond.java.ppawegkant.processing.VLogProcessor;

/**
 * Dit is de enumeratie van de beschikbare systemen;
 * TDI-MV, TDI-RT, VRI en MTM.
 * 
 * @author R. Cuenen
 */
public enum SystemType {

    TDI_MV(new MVProcessor()),
    TDI_RT(new RTProcessor()),
    VRI(new VLogProcessor()),
    MTM(new MTMProcessor());
    private final DataProcessor dataProcessor;

    /**
     * Creeër het {@link SystemType} enum-object.
     * 
     * @param dataProcessor de bijbehorende {@link DataProcessor}
     */
    private SystemType(DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    /**
     * Geeft de bijbehorende {@link DataProcessor}.
     * 
     * @return de {@link DataProcessor}
     */
    public DataProcessor getDataProcessor() {
        return dataProcessor;
    }
}
