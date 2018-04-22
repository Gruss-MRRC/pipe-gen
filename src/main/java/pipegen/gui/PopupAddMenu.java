
package pipegen.gui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

import pipegen.definitions.*;
import pipegen.instances.*;

public class PopupAddMenu extends JPopupMenu implements ActionListener {

    private static final Color TITLE_COLOR = new Color(122, 158, 195);
    private static final ImageIcon SOURCE_ICON = new ImageIcon("resources/images/add_source_icon.png");
    private static final ImageIcon SINK_ICON = new ImageIcon("resources/images/add_sink_icon.png");
    private static final ImageIcon MODULE_ICON = new ImageIcon("resources/images/add_module_icon.png");
    private static final ImageIcon SOURCE_DIALOG_ICON = new ImageIcon("resources/images/add_source_dialog_icon.png");
    private static final ImageIcon SINK_DIALOG_ICON = new ImageIcon("resources/images/add_sink_dialog_icon.png");


    private Component invoker;
    private PipegenGUI frame;

    private PipelineInstance pipeline;
    private FileFormatDef[] formats;
    private ModuleDef[] modules;
    private ElementPosition position;

    public PopupAddMenu(Component invoker, PipegenGUI gui, PipelineInstance pipeline) {

        this.invoker = invoker;
        this.frame = gui;
        this.pipeline = pipeline;

        ToolboxDef toolbox = pipeline.getDefinition().getToolbox();
        formats = toolbox.getFormats();
        modules = toolbox.getModules();

        //FileFormatDef testFormatDef = formats[0];
        //ParameterDef testParameterDef = new ParameterDef("TEST", testFormatDef, true);
        //ElementPosition testElementPosition = new ElementPosition(90, 90);
        //SourceElement testSource = new SourceElement("TEST", testElementPosition, testParameterDef);
        //pipeline.getSources().add(testSource);

        Arrays.sort(formats);
        Arrays.sort(modules);
        JMenuItem item;

        add(new PopupMenuTitle("Add Source", SOURCE_ICON));
        for (int i=0; i < formats.length; i++) {
            item = new JMenuItem(formats[i].getName());
            item.addActionListener(this);
            add(item);
        }

        addSeparator();
        add(new PopupMenuTitle("Add Sink", SINK_ICON));
        for (int i=0; i < formats.length; i++) {
            item = new JMenuItem(formats[i].getName() + " ");
            item.addActionListener(this);
            add(item);
        }

        addSeparator();
        add(new PopupMenuTitle("Add Module", MODULE_ICON));
        for (int i=0; i < modules.length; i++) {
            item = new JMenuItem(modules[i].getName() + "  ");
            item.addActionListener(this);
            add(item);
        }
    }

    @Override
    public void show(Component invoker, int x, int y) {
        super.show(invoker, x, y);
        position = new ElementPosition(x, y);
    }


    public void actionPerformed(ActionEvent e) {

        String actionCommand = e.getActionCommand();
        for (FileFormatDef format : formats) {
            if (actionCommand.equals(format.getName())) {
                addSource(format);
                invoker.repaint();
                return;
            } else if (actionCommand.equals(format.getName() + " ")) {
                addSink(format);
                invoker.repaint();
                return;
            }
        }
        for (ModuleDef module : modules) {
            if (actionCommand.equals(module.getName() + "  ")) {
                addModule(module);
                invoker.repaint();
                return;
            }
        }
    }

    private void addSource(FileFormatDef format) {

        String name = format.getName();
        Color color = format.getColor();
        String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());

        String line1 = "<html>Enter a column from Input Data to supply this source";
        String line2 = "<html>Files listed in column must be in " + name + " <font color=" + hex + ">\u2022</font> format";
        String line3 = "<html><font color=gray>Type column name exactly - case sensitive</font>";

        String fieldName = (String)JOptionPane.showInputDialog(
            frame,
            line1 + "\n\n" + line2 + "\n\n" + line3 + "\n",
            "New " + format.getName() + " source",
             JOptionPane.PLAIN_MESSAGE,
             SOURCE_DIALOG_ICON,
             null,
             null);

        if (fieldName != null) {
            ParameterDef definition = new ParameterDef(fieldName, format, true);
            SourceElement sourceElement = new SourceElement(fieldName, position, definition);
            pipeline.getSources().add(sourceElement);
        }
    }

    private void addSink(FileFormatDef format) {
        String name = format.getName();
        Color color = format.getColor();
        String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());

        String line1 = "<html>Enter column from Input Data that this sink will write to";
        String line2 = "<html>Files listed in column will be saved in " + name + " <font color=" + hex + ">\u2022</font> format";
        String line3 = "<html><font color=gray>Type column name exactly - case sensitive</font>";

        String fieldName = (String)JOptionPane.showInputDialog(
            frame,
            line1 + "\n\n" + line2 + "\n\n" + line3 + "\n",
            "New " + format.getName() + " source",
             JOptionPane.PLAIN_MESSAGE,
             SINK_DIALOG_ICON,
             null,
             null);

        if (fieldName != null) {
            ParameterDef definition = new ParameterDef(fieldName, format, true);
            SinkElement sinkElement = new SinkElement(fieldName, position, definition);
            pipeline.getSinks().add(sinkElement);
        }
    }

    private void addModule(ModuleDef module) {

        ModuleElement moduleElement = new ModuleElement(module, position);
        pipeline.getModules().add(moduleElement);
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
