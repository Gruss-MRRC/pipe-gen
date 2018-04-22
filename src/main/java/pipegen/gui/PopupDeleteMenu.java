
package pipegen.gui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import pipegen.definitions.*;
import pipegen.instances.*;

public class PopupDeleteMenu extends JPopupMenu implements ActionListener {

    private static final Color TITLE_COLOR = new Color(233, 28, 36);
    private static final String YES_DELETE = "Yes";
    private static final String NO_DELETE = "No";
    private static final ImageIcon DELETE_ICON = new ImageIcon("resources/images/delete_component_icon.png");

    private Component invoker;
    private PipelineInstance pipeline;
    private SourceElement selectedSource;
    private SinkElement selectedSink;
    private ModuleElement selectedModule;
    private ElementPosition position;

    private boolean isSource = false;
    private boolean isSink = false;
    private boolean isModule = false;

    private PopupDeleteMenu(Component invoker, PipelineInstance pipeline) {
        this.invoker = invoker;
        this.pipeline = pipeline;
    }

    public PopupDeleteMenu(Component invoker, PipelineInstance pipeline, BlockElement selectedBlock) {

        this(invoker, pipeline);

        if (selectedBlock instanceof SourceElement) {
            add(new PopupMenuTitle("Delete Source?", DELETE_ICON));
            selectedSource = (SourceElement)selectedBlock;
            isSource = true;
        } else if (selectedBlock instanceof SinkElement) {
            add(new PopupMenuTitle("Delete Sink?", DELETE_ICON));
            selectedSink = (SinkElement)selectedBlock;
            isSink = true;
        } else if (selectedBlock instanceof ModuleElement) {
            add(new PopupMenuTitle("Delete Module?", DELETE_ICON));
            selectedModule = (ModuleElement)selectedBlock;
            isModule = true;
        } else {
            return;
        }

        JMenuItem item;
        item = new JMenuItem(YES_DELETE);
        item.addActionListener(this);
        add(item);
        item = new JMenuItem(NO_DELETE);
        item.addActionListener(this);
        add(item);
    }

    @Override
    public void show(Component invoker, int x, int y) {
        super.show(invoker, x, y);
        position = new ElementPosition(x, y);
    }


    public void actionPerformed(ActionEvent e) {

        String actionCommand = e.getActionCommand();
        if (actionCommand.equals(YES_DELETE)) {

            if (isSource) {
                System.out.println("PopupDeleteMenu.java - actionPerformed() Delete source");
                pipeline.deleteSource(selectedSource);
            } else if (isSink) {
                System.out.println("PopupDeleteMenu.java - actionPerformed() Delete sink");
                pipeline.deleteSink(selectedSink);
            } else if (isModule) {
                System.out.println("PopupDeleteMenu.java - actionPerformed() Delete module");
                pipeline.deleteModule(selectedModule);
            }
            invoker.repaint();
        }
    }

    private class PopupMenuTitle extends JLabel {

        public PopupMenuTitle() {
            super();
            setForeground(TITLE_COLOR);
        }

        public PopupMenuTitle(String text, Icon icon) {
            super("  " + text, icon, SwingConstants.LEADING);
            setForeground(TITLE_COLOR);
        }
    }
}
