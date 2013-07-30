package cuenen.raymond.java.ppawegkant.icon;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.ShortLookupTable;
import java.io.IOException;
import java.util.Timer;
import javax.imageio.ImageIO;

/**
 * Deze feature geeft een icoon op de Systeem Tray.
 * 
 * @author R.Cuenen
 */
public final class ApplicationIcon {

    private static final String APPLICATION_TITLE = "VISSIM-PPA Koppeling";
    private static final ApplicationIcon APPLICATION_ICON = new ApplicationIcon();
    private final Timer timer = new Timer();
    private final BufferedImage icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
    private final short[][] colorData;
    private boolean debugMode = false;
    private TrayIcon trayIcon;

    /**
     * Verander het icoon op de systeem tray volgens
     * de aangeduide status.</br>
     * 0 - Ok (groen)</br>
     * 1 - Waarschuwing (oranje)</br>
     * 2 - Fout (rood)
     * 
     * @param state de gewenste notificatie status
     */
    public static void notifyState(int state) {
        switch (state) {
            case 0:
                APPLICATION_ICON.update(Color.green);
                break;
            case 1:
                APPLICATION_ICON.update(Color.orange);
                break;
            case 2:
                APPLICATION_ICON.update(Color.red);
                break;
        }
    }

    /**
     * Creeër de {@link ApplicationIcon}.
     */
    private ApplicationIcon() {
        assert debugMode = true; //NOSONAR
        short[] red = new short[256];
        short[] green = new short[256];
        short[] blue = new short[256];
        short[] alpha = new short[256];
        for (short i = 0; i < 256; i++) {
            red[i] = i;
            green[i] = i;
            blue[i] = i;
            alpha[i] = i;
        }
        colorData = new short[][]{red, green, blue, alpha};
        if (!debugMode && SystemTray.isSupported()) {
            createIcon();
            SystemTray systemTray = SystemTray.getSystemTray();
            trayIcon = new TrayIcon(icon, APPLICATION_TITLE);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(new ExitHandler());
            try {
                systemTray.add(trayIcon);
            } catch (AWTException ex) {
                trayIcon = null;
            }
        }
    }

    /**
     * Zet het systeem tray icoon op de aangegeven kleur.
     * Deze kleur blijft 750 milliseconden zichtbaar.
     * 
     * @param color de gewenste kleur
     */
    public void update(Color color) {
        if (trayIcon != null) {
            colorData[0][255] = (short) color.getRed();
            colorData[1][255] = (short) color.getGreen();
            colorData[2][255] = (short) color.getBlue();
            colorData[3][255] = (short) color.getAlpha();
            LookupTable lookupTable = new ShortLookupTable(0, colorData);
            BufferedImageOp op = new LookupOp(lookupTable, null);
            BufferedImage target = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            op.filter(icon, target);
            timer.schedule(new IconHandler(trayIcon, target), 0L);
            timer.schedule(new IconHandler(trayIcon, icon), 750L);
        }
    }

    /**
     * Creeër het icoon.
     */
    private void createIcon() {
        Graphics2D g2d = icon.createGraphics();
        try {
            BufferedImage image = ImageIO.read(ApplicationIcon.class.getResourceAsStream("/icon.png"));
            g2d.drawImage(image, 0, 0, 32, 32, null);
        } catch (IOException ex) {
            g2d.setColor(Color.white);
            g2d.fillOval(0, 0, 32, 32);
        }
        g2d.dispose();
    }
}
