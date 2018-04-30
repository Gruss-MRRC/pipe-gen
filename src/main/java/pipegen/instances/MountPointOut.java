/*
    Program:  MountPointIn.java
    Author:   Michael Stockman
              Albert Einstein College of Medicine

 */

package pipegen.instances;

import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pipegen.*;
import pipegen.definitions.*;
import pipegen.instances.*;

/**
 * Represents an output attachment point on a ModuleElement or SourceElement to 
 * which ConnectionElements are attached with a MountPointIn on the other end.
 */
public class MountPointOut extends MountPoint implements ConnectableOutput {

    // Static final fields
    private static final int WIDTH = 10;
    private static final int HEIGHT = 8;
    private static final DimensionInt SIZE = new DimensionInt(WIDTH, HEIGHT);

    private Polygon triangle;

    public MountPointOut(BlockElement block, ParameterDef definition, int index) {
        super(block, definition, index);

        int[] xPoints = {0, WIDTH, (WIDTH/2)};
        int[] yPoints = {0, 0, HEIGHT};
        triangle = new Polygon(xPoints, yPoints, 3);
    }

    public static MountPointOut[] wrapArray(BlockElement block, ParameterDef[] input) {
        MountPointOut[] output = new MountPointOut[input.length];

        for (int i=0; i < input.length; i++) {
            output[i] = new MountPointOut(block, input[i], i);
        }

        return output;
    }

    /*public MountPointIn[] getNextMounts() {
        // *** can stay but needs to use connections rather than next.
        MountPointIn[] outputs = new MountPointIn[next.size()];
        outputs = next.toArray(outputs);
        return outputs;
    }*/

    /*public BlockElement getNextBlock() {
        return getNextMounts()[0].getBlock();
    }*/
    public BlockElement getNextBlock() {
        // *** Dummied out
        return null;
    }

    public String getFileName(File outputDir, DataTableFile table, int rowIndex) {
        return block.getOutputFileName(0, outputDir, table, rowIndex);
    }

    public boolean contains(Point location) {
        return triangle.contains(location);
    }

    public int getHeight() {
        return HEIGHT;
    }

    public int getWidth() {
        return WIDTH;
    }

    public DimensionInt getSize() {
        return SIZE;
    }

    public void draw(Graphics2D g2d) {
        super.draw(g2d);
        g2d.fillPolygon(triangle);
        g2d.drawPolygon(triangle);
    }

    public void setPosition(int x, int y) {
        int deltaX = x - position.getX();
        int deltaY = y - position.getY();

        super.setPosition(x, y);
        triangle.translate(deltaX, deltaY);
        attachPoint.setX(x + (WIDTH / 2));
        attachPoint.setY(y + HEIGHT);
    }

    public void move(int deltaX, int deltaY) {
        super.move(deltaX, deltaY);
        triangle.translate(deltaX, deltaY);
    }

    public String getToolTip() {
        return block.outputSummary(index);
    }

    public String getJSONText() {
        return block.getJSONTextOutput(index);
    }

    public String getFilename(String id, DataTableFile table) {
        if (block instanceof SourceElement) {
            SourceElement thisSource = (SourceElement)block;
            return thisSource.getFilename(id, table);
        } else if (block instanceof ModuleElement) {
            ModuleElement thisModule = (ModuleElement)block;
            return thisModule.getFilename(id, index);
        } else {
            return null;
        }
    }

    public BlockElement getFirstOutput() {
        if (connections == null || connections.size() == 0) {
            return null;
        } else {
            MountPointIn stop = connections.get(0).getStop();
            if (stop == null) {
                return null;
            }
            return stop.getBlock();
        }
    }
}
