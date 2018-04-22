/*
    Program:  MountPointIn.java
    Author:   Michael Stokman
              Albert Einstein College of Medicine

 */

package pipegen.instances;

import java.io.*;
import java.awt.*;
import java.awt.geom.*;

import pipegen.*;
import pipegen.definitions.*;

/**
 * Represents an input attachment point on a ModuleElement or SinkElement to 
 * which ConnectionElements are attached with a MountPointOut on the other end.
 */
public class MountPointIn extends MountPoint implements ConnectableInput {

    // Static final fields
    private static final int DIAMETER = 8;

    private Ellipse2D.Double circle;

    public MountPointIn(BlockElement block, ParameterDef definition, int index) {
        super(block, definition, index);
        circle = new Ellipse2D.Double(0, 0, DIAMETER, DIAMETER);
    }

    public static MountPointIn[] wrapArray(BlockElement block, ParameterDef[] input) {
        MountPointIn[] output = new MountPointIn[input.length];

        for (int i=0; i < input.length; i++) {
            output[i] = new MountPointIn(block, input[i], i);
        }

        return output;
    }

    @Override
    public void addConnection(ConnectionElement c) {
        if (connections.size() == 0) {
            connections.add(c);
        }
    }

    public MountPointOut getPreviousMount() {
        return getConnection().getStart();
    }

    public BlockElement getPreviousBlock() {
        return getPreviousMount().getBlock();
    }

    public String getDependency(String id) {
        return getPreviousBlock().getBlockName() + "-" + getPreviousMount().getIndex() + "-" + id;
    }

    /*public boolean isRequired() {
        return definition.isRequired();
    }*/

    public String getFileName(File outputDir, DataTableFile table, int rowIndex) {
        return block.getOutputFileName(0, outputDir, table, rowIndex);
    }

    public void draw(Graphics2D g2d) {
        super.draw(g2d);
        g2d.fill(circle);
        g2d.draw(circle);
    }

    public void setPosition(int x, int y) {
        super.setPosition(x, y);
        circle.setFrame(x, y, DIAMETER, DIAMETER);
        attachPoint.setX(x + (DIAMETER / 2));
        attachPoint.setY(y);
    }

    public void move(int deltaX, int deltaY) {
        super.move(deltaX, deltaY);
        circle.setFrame(position.getX(), position.getY(), DIAMETER, DIAMETER);
    }

    public boolean contains(Point location) {
        return circle.contains(location);
    }

    public int getWidth() {
        return DIAMETER;
    }

    public int getHeight() {
        return DIAMETER;
    }

    public String getAttachedName() {
        // *** Temporary
        return "[" + getBlock().getBlockName() + "]";
    }

    public String getToolTip() {
        return block.inputSummary(index);
    }

    public String getJSONText() {
        return block.getJSONTextInput(index);
    }

    public String getFilename(String id, DataTableFile table) {
        ConnectionElement connection = connections.get(0);
        if (connection == null) {
            return null;
        }

        MountPointOut start = connection.getStart();
        if (start == null) {
            return null;
        }

        return start.getFilename(id, table);
    }
}
