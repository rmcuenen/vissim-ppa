package cuenen.raymond.java.ppawegkant.processing;

import cuenen.raymond.java.ppawegkant.file.RTProcessor;
import cuenen.raymond.java.ppawegkant.file.MVProcessor;
import cuenen.raymond.java.ppawegkant.file.VLogProcessor;
import cuenen.raymond.java.ppawegkant.file.MTMProcessor;

/**
 *
 * @author R. Cuenen
 */
public enum SystemType {

    TDI_MV(new MVProcessor()),
    TDI_RT(new RTProcessor()),
    VRI(new VLogProcessor()),
    MTM(new MTMProcessor());
    private final DataProcessor dataProcessor;

    private SystemType(DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    public DataProcessor getDataProcessor() {
        return dataProcessor;
    }
}
