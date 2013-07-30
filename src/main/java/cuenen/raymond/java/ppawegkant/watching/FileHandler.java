package cuenen.raymond.java.ppawegkant.watching;

import java.io.File;

/**
 * Dit is de interface beschrijving voor het
 * afhandelen van een bestand.
 * 
 * @author R. Cuenen
 */
public interface FileHandler {

    /**
     * Verwerk het aangeleverde bestand.
     * 
     * @param file het te verwerken bestand
     */
    void handleFile(File file);
}
