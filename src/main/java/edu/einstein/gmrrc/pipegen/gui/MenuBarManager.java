
package edu.einstein.gmrrc.pipegen.gui;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MenuBarManager implements ActionListener, ItemListener {

    private JMenuBar menuBar;
    private PipegenGUI gui;

    private ButtonGroup toolboxRadioGroup;

    public MenuBarManager(PipegenGUI gui) {
        this.gui = gui;
        menuBar = new JMenuBar();

        menuBar.add(buildFileMenu());
        menuBar.add(buildEditMenu());
        menuBar.add(buildSelectToolboxMenu());
        menuBar.add(buildCreateNewMenu());
        menuBar.add(buildHelpMenu());
    }

    private JMenu buildFileMenu() {

        JMenu menu = buildMenu("File", KeyEvent.VK_F);

        menu.add(buildMenuItem("Open Pipeline", KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK)));
        menu.add(buildMenuItem("Save Pipeline", KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK)));
        menu.add(buildMenuItem("Save Pipeline As", KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK)));

        menu.addSeparator();
        menu.add(buildMenuItem("Open Input Data", KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK)));
        //menu.add(buildMenuItem("Save Input Data", false));

        menu.addSeparator();
        menu.add(buildMenuItem("Open Analysis"));
        //menu.add(buildMenuItem("Open Recent Analysis", false));
        menu.add(buildMenuItem("Save Analysis", KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK)));
        menu.add(buildMenuItem("Save Analysis As"));
        //menu.add(buildMenuItem("Save Analysis then Run", KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK), false));

        return menu;
    }

    private JMenu buildEditMenu() {

        JMenu menu = buildMenu("Edit", KeyEvent.VK_E);

        menu.add(buildMenuItem("Undo", KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK), false));
        menu.add(buildMenuItem("Redo", KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK), false));

        menu.addSeparator();
        menu.add(buildMenuItem("Toolbox", KeyEvent.VK_T, false));
        menu.add(buildMenuItem("Pipeline", KeyEvent.VK_P));
        menu.add(buildMenuItem("Module", KeyEvent.VK_M, false));
        menu.add(buildMenuItem("Input Data", KeyEvent.VK_D));

        menu.addSeparator();
        menu.add(buildMenuItem("Preferences", KeyEvent.VK_F, false));

        return menu;
    }

    private JMenu buildSelectToolboxMenu() {

        JMenu menu = buildMenu("Select Toolbox", KeyEvent.VK_S);
        menu.getAccessibleContext().setAccessibleDescription("This menu allows the user to set the current toolbox from which to select pipeline components");
        menuBar.add(menu);
        toolboxRadioGroup = new ButtonGroup();
        JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem();

        File[] files = gui.getToolboxesDir().listFiles();
        Arrays.sort(files);
        if (files != null) {
            for (int i=0; i < files.length; i++) {

                rbMenuItem = new JRadioButtonMenuItem(files[i].getName());
                toolboxRadioGroup.add(rbMenuItem);
                rbMenuItem.setEnabled(true);
                rbMenuItem.addActionListener(this);
                menu.add(rbMenuItem);
            }
        } else {
            rbMenuItem = new JRadioButtonMenuItem("[no toolboxes found]");
            toolboxRadioGroup.add(rbMenuItem);
            rbMenuItem.setEnabled(false);
            menu.add(rbMenuItem);
        }

        return menu;
    }

    public void clearToolboxSelection() {
        toolboxRadioGroup.clearSelection();
    }

    private JMenu buildCreateNewMenu() {

        JMenu menu = buildMenu("Create New", KeyEvent.VK_N);

        menu.add(buildMenuItem("New Toolbox", false));
        menu.add(buildMenuItem("New Pipeline", false));
        menu.add(buildMenuItem("New Module", false));

        return menu;
    }

    private JMenu buildHelpMenu() {

        JMenu menu = buildMenu("Help", KeyEvent.VK_H);

        menu.add(buildMenuItem("Contents", KeyEvent.VK_C, false));
        menu.add(buildMenuItem("About", KeyEvent.VK_A, false));

        return menu;
    }

    private JMenu buildMenu(String name, int mnemonic) {
        JMenu menu = new JMenu(name);
        menu.setMnemonic(mnemonic);
        return menu;
    }

    private JMenuItem buildMenuItem(String name) {
        JMenuItem menuItem = new JMenuItem(name);
        menuItem.addActionListener(this);
        return menuItem;
    }

    private JMenuItem buildMenuItem(String name, boolean enabled) {
        JMenuItem menuItem = buildMenuItem(name);
        menuItem.setEnabled(enabled);
        return menuItem;
    }

    private JMenuItem buildMenuItem(String name, KeyStroke accelerator) {
        JMenuItem menuItem = buildMenuItem(name);
        menuItem.setAccelerator(accelerator);
        return menuItem;
    }

    private JMenuItem buildMenuItem(String name, KeyStroke accelerator, boolean enabled) {
        JMenuItem menuItem = buildMenuItem(name, accelerator);
        menuItem.setEnabled(enabled);
        return menuItem;
    }

    private JMenuItem buildMenuItem(String name, int mnemonic) {
        JMenuItem menuItem = new JMenuItem(name, mnemonic);
        menuItem.addActionListener(this);
        return menuItem;
    }

    private JMenuItem buildMenuItem(String name, int mnemonic, boolean enabled) {
        JMenuItem menuItem = buildMenuItem(name, mnemonic);
        menuItem.setEnabled(enabled);
        return menuItem;
    }

    public JMenuBar getMenuBar() {
        return menuBar;
    }

    public void actionPerformed(ActionEvent e) {
        //...Get information from the action event...
        //...Display it in the text area...

        String ac = e.getActionCommand();

        // *** Temporary debugging line
        //System.out.println("PipegenGUI.java selected - " + e.getActionCommand());
        JMenuItem jmi = (JMenuItem) e.getSource();
        JPopupMenu jpm = (JPopupMenu) jmi.getParent();
        JMenu menu = (JMenu) jpm.getInvoker();

        if (menu.getText().equals("File")) {
            fileMenuEvent(e);
        } else if (menu.getText().equals("Edit")) {
            System.out.println("PipegenGUI.java - EDIT - " + menu.getText());
            editMenuEvent(e);
        } else if (menu.getText().equals("Select Toolbox")) {
            selectToolboxMenuEvent(e);
        } else if (menu.getText().equals("Create New")) {
            System.out.println("PipegenGUI.java - CREATE - " + menu.getText());
            createMenuEvent(e);
        } else if (menu.getText().equals("Help")) {
            System.out.println("PipegenGUI.java - HELP - " + menu.getText());
            helpMenuEvent(e);
        }
    }

    private void fileMenuEvent(ActionEvent e) {
        if (e.getActionCommand().equals("Open Pipeline")) {
            gui.openPipeline();
        } else if (e.getActionCommand().equals("Save Pipeline")) {
            gui.savePipeline();
        } else if (e.getActionCommand().equals("Save Pipeline As")) {
            gui.savePipelineAs();
        } else if (e.getActionCommand().equals("Open Input Data")) {
            gui.openInputData();
        } else if (e.getActionCommand().equals("Open Analysis")) {
            gui.openAnalysis();      
        } else if (e.getActionCommand().equals("Open Recent Analysis")) {
            tempPrintAction(e);      
        } else if (e.getActionCommand().equals("Save Analysis")) {
            gui.saveAnalysis();
        } else if (e.getActionCommand().equals("Save Analysis As")) {
            gui.saveAnalysisAs();
        } else if (e.getActionCommand().equals("Save Analysis then Run")) {
            tempPrintAction(e);
        }
    }

    private void editMenuEvent(ActionEvent e) {
        if (e.getActionCommand().equals("Undo")) {
            tempPrintAction(e);
        } else if (e.getActionCommand().equals("Redo")) {
            tempPrintAction(e);
        } else if (e.getActionCommand().equals("Toolbox")) {
            tempPrintAction(e);
        } else if (e.getActionCommand().equals("Pipeline")) {
            tempPrintAction(e);
        } else if (e.getActionCommand().equals("Module")) {
            tempPrintAction(e);
        } else if (e.getActionCommand().equals("Input Data")) {
            tempPrintAction(e);
        } else if (e.getActionCommand().equals("Preferences")) {
            tempPrintAction(e);
        }
    }

    private void selectToolboxMenuEvent(ActionEvent e) {
        gui.setSelectedToolbox(e.getActionCommand());
    }

    private void createMenuEvent(ActionEvent e) {
        if (e.getActionCommand().equals("New Toolbox")) {
            tempPrintAction(e);
        } else if (e.getActionCommand().equals("New Pipeline")) {
            tempPrintAction(e);
        } else if (e.getActionCommand().equals("New Module")) {
            tempPrintAction(e);
        }
    }

    private void helpMenuEvent(ActionEvent e) {
        if (e.getActionCommand().equals("Contents")) {
            tempPrintAction(e);
        } else if (e.getActionCommand().equals("About")) {
            tempPrintAction(e);
        }
    }

    private void tempPrintAction(ActionEvent e) {
        // *** temporary function to simplify printing status of ActionEvent

        System.out.println("MenuBarManager.java doing nothing. actionCommand = " + e.getActionCommand());
    }

    public void itemStateChanged(ItemEvent e) {
        //...Get information from the item event...
        //...Display it in the text area...
    }

}
