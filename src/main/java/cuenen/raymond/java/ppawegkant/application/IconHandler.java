package cuenen.raymond.java.ppawegkant.application;

import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.util.TimerTask;

/**
 * Dit is een taak die gebruikt wordt in een Timer.
 * Het igestelde icoon wordt op de Systeem Tray gezet.
 * 
 * @author R. Cuenen
 */
public class IconHandler extends TimerTask {

    private final TrayIcon trayIcon;
    private final BufferedImage icon;

    public IconHandler(TrayIcon trayIcon, BufferedImage icon) {
        this.trayIcon = trayIcon;
        this.icon = icon;
    }

    @Override
    public void run() {
        trayIcon.setImage(icon);
    }
}
