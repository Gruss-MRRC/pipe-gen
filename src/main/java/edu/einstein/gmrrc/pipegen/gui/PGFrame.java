
package edu.einstein.gmrrc.pipegen.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class PGFrame extends JFrame implements WindowListener {

    private static final String TITLE = "pipe-gen";
    private static final int WIDTH = 850;
    private static final int HEIGHT = 600;
    private static final int X_POSITION = 200;
    private static final int Y_POSITION = 200;

    public PGFrame() {
        super(TITLE);
        addWindowListener(this);
        setSize(WIDTH, HEIGHT);
        setLocation(X_POSITION, Y_POSITION);
    }

    public void windowClosing(WindowEvent e) {
        System.exit(0);
    }
    public void windowActivated(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
}
