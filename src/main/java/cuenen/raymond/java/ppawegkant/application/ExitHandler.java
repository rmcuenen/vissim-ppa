package cuenen.raymond.java.ppawegkant.application;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

/**
 * Dubbel-klik op het applicatie icoon geeft het
 * afsluit-dialoogvenster.
 * 
 * @author R. Cuenen
 */
public class ExitHandler implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        int result = JOptionPane.showConfirmDialog(null,
                "Wilt u de applicatie afsluiten?",
                "Aflsuiten", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}
