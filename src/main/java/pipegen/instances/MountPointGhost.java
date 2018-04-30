/*
    Program:  MountPointGhost.java
    Author:   Michael Stockman
              Albert Einstein College of Medicine

 */

package pipegen.instances;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;

import pipegen.*;
import pipegen.definitions.*;

/**
 * Represents a temporary draggable attachment on a ModuleElement, SinkElement, 
 * or SourceElement. Dragging the "ghost" from one attachment to the other 
 * animates reconfiguration of the dragged end of the ConnectionElement.
 */
public abstract class MountPointGhost extends MountPoint implements Draggable, ConnectableInput, ConnectableOutput {

    // Static final fields
    protected static final double MIN_DISTANCE = 8.0;
    protected static final int DIAMETER = 16;
    protected static final int WIDTH = 18;
    protected static final int HEIGHT = 16;

    private static final int ALPHA_GHOSTED = 80;
    private static final int PADDING = 4;

    protected MountPoint precursor;
    protected PipelineInstance pipeline;
    protected boolean isNew;
    protected boolean circleForm;

    protected Ellipse2D.Double asCircle;
    protected Polygon asTriangle;


    public MountPointGhost(MountPoint precursor, PipelineInstance pipeline) {
        super(null, precursor.getDefinition(), -1);
        this.pipeline = pipeline;

        this.connections = precursor.getConnections();
        this.precursor = precursor;
        this.isNew = false;

        ElementPosition precursorPosition = precursor.getPosition();
        int xPos = precursorPosition.getX() - PADDING;
        int yPos = precursorPosition.getY() - PADDING;

        position = new ElementPosition(xPos, yPos);
        asCircle = new Ellipse2D.Double(xPos, yPos, DIAMETER, DIAMETER);
        int[] xPoints = {xPos, xPos + WIDTH, xPos + (WIDTH/2)};
        int[] yPoints = {yPos, yPos, yPos + HEIGHT};
        asTriangle = new Polygon(xPoints, yPoints, 3);
        attachPoint = precursor.getAttachPoint().copy();
    }

    @Override
    public void addConnection(ConnectionElement c) {
        if (connections.size() == 0) {
            connections.add(c);
        }
    }

    public Color getColor() {
        Color precursorColor = precursor.getColor();
        return new Color(precursorColor.getRed(), precursorColor.getGreen(), precursorColor.getBlue(), ALPHA_GHOSTED);
    }

    public void draw(Graphics2D g2d) {
        super.draw(g2d);

        if (circleForm) {
            g2d.fill(asCircle);
            g2d.draw(asCircle);
        } else {
            g2d.fill(asTriangle);
            g2d.draw(asTriangle);
        }
    }

    public boolean contains(Point location) {

        if (circleForm) {
            return asCircle.contains(location);
        } else {
            return asTriangle.contains(location);
        }
    }

    public void move(int deltaX, int deltaY) {
        position.move(deltaX, deltaY);
        asCircle.setFrame(position.getX(), position.getY(), DIAMETER, DIAMETER);
        asTriangle.translate(deltaX, deltaY);
        attachPoint.move(deltaX, deltaY);
    }

    public int getWidth() {
        if (circleForm) {
            return DIAMETER;
        } else {
            return WIDTH;
        }
    }

    public int getHeight() {
        if (circleForm) {
            return DIAMETER;
        } else {
            return HEIGHT;
        }
    }

    protected MountPointIn getTargetInput(ArrayList<SinkElement> sinks, ArrayList<ModuleElement> modules) {

        FileFormatDef thisDef = this.definition.getFileFormat();

        for (SinkElement sink : sinks) {
            MountPointIn target = sink.getMountPointIn();
            FileFormatDef targetDef = target.getDefinition().getFileFormat();
            double distance = this.distance(target);

            if (distance <= MIN_DISTANCE) {
                if (thisDef.isValidInputTo(targetDef) && target.getConnection() == null) {
                    return target;
                } else {
                    return null;
                }
            }
        }

        for (ModuleElement module : modules) {
            MountPointIn[] targets = module.getInputs();
            for (MountPointIn target : targets) {
                FileFormatDef targetDef = target.getDefinition().getFileFormat();
                double distance = this.distance(target);

                if (distance <= MIN_DISTANCE) {
                    if (thisDef.isValidInputTo(targetDef) && target.getConnection() == null) {
                        return target;
                    } else {
                        return null;
                    }
                }
            }
        }

        return null;
    }

    protected MountPointOut getTargetOutput(ArrayList<SourceElement> sources, ArrayList<ModuleElement> modules) {

        FileFormatDef thisDef = this.definition.getFileFormat();

        for (SourceElement source : sources) {
            MountPointOut target = source.getMountPointOut();
            FileFormatDef targetDef = target.getDefinition().getFileFormat();
            double distance = this.distance(target);

            if (distance <= MIN_DISTANCE) {
                if (targetDef.isValidInputTo(thisDef) && target.getConnection() == null) {
                    return target;
                } else {
                    return null;
                }
            }
        }

        for (ModuleElement module : modules) {
            MountPointOut[] targets = module.getOutputs();
            for (MountPointOut target : targets) {
                FileFormatDef targetDef = target.getDefinition().getFileFormat();
                double distance = this.distance(target);

                if (distance <= MIN_DISTANCE) {
                    if (targetDef.isValidInputTo(thisDef) && target.getConnection() == null) {
                        return target;
                    } else {
                        return null;
                    }
                }
            }
        }

        return null;
    }

    public void pressed() {}

    public void released(ArrayList<SourceElement> sources, ArrayList<SinkElement> sinks, ArrayList<ModuleElement> modules) {}

    public String getAttachedName() {
        // *** Temporary
        return "[Ghost]";
    }

    public String getToolTip() {
        return precursor.getToolTip();
    }


}
