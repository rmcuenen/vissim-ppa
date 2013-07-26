package cuenen.raymond.java.ppawegkant.processing;

/**
 *
 * @author R. Cuenen
 */
public enum SystemType {

    TDI_MV(null),
    TDI_RT(null),
    VRI(null),
    MTM(null);
    private final DataProcessor dataProcessor;

    private SystemType(DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    public DataProcessor getDataProcessor() {
        return dataProcessor;
    }
}
