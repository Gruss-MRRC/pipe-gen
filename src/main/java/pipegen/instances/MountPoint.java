/*
    Program:  MountPoint.java
    Author:   Michael Stockman
              Albert Einstein College of Medicine

 */


// Contextual Menu Link: http://stackoverflow.com/questions/766956/how-do-i-create-a-right-click-context-menu-in-java-swing

package pipegen.instances;

import java.io.*;
import java.util.*;
import java.awt.*;
import org.json.*;

import pipegen.*;
import pipegen.definitions.*;

/**
 * Represents an input/output attachment point on a ModuleElement, 
 * SourceElement, or SinkElement. These are the points where ConnectionElements 
 * are attached.
 */
public abstract class MountPoint {
    
    protected BlockElement block;
    protected ParameterDef definition;
    protected int index;

    protected ElementPosition position;
    protected ElementPosition attachPoint;
    protected ArrayList<ConnectionElement> connections;

    public MountPoint(BlockElement block, ParameterDef definition, int index) {
        this.block = block;
        this.definition = definition;
        this.index = index;

        position = new ElementPosition();
        attachPoint = new ElementPosition();
        connections = new ArrayList<ConnectionElement>();
    }

    public String toString() {
        if (definition.isRequired()) {
            return "<" + definition.getFileFormat() + ">";
        } else {
            return "[" + definition.getFileFormat() + "]";
        }
    }

    public String getName() {
        return definition.getName();
    }

    public ParameterDef getDefinition() {
        return definition;
    }

    public FileFormatDef getFileFormat() {
        return definition.getFileFormat();
    }

    public String getFormatSuffix() {
        return definition.getFormatSuffix();
    }

    public BlockElement getBlock() {
        return block;
    }

    public Color getColor() {
        return definition.getColor();
    }

    public boolean isRequired() {
        return definition.isRequired();
    }

    public void addConnection(ConnectionElement c) {
        connections.add(c);
    }

    public void removeConnection(ConnectionElement c) {
        connections.remove(c);
    }

    public void clearConnections() {
        connections = new ArrayList<ConnectionElement>();
    }

    public ArrayList<ConnectionElement> getConnections() {
        return connections;
    }

    public ConnectionElement getConnection(int index) {
        if (connections == null || connections.size() <= index) {
            return null;
        }
        return connections.get(index);
    }

    public int getConnectionSize() {
        return connections.size();
    }

    public ConnectionElement getConnection() {
        return getConnection(0);
    }

    public void setPosition(int x, int y) {
        position.setX(x);
        position.setY(y);
        attachPoint.setX(x);
        attachPoint.setY(y);
    }

    public int getIndex() {
        return index;
    }

    public ElementPosition getPosition() {
        return position;
    }

    public ElementPosition getCenter() {
        return new ElementPosition(position.getX() + getWidth()/2, position.getY() + getHeight()/2);
    }

    public int getHeight() {
        return 0;
    }

    public int getWidth() {
        return 0;
    }

    public double distance(MountPoint target) {
        ElementPosition thisCenter = getCenter();
        ElementPosition targetCenter = target.getCenter();
        return thisCenter.distance(targetCenter);
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(getColor());
    }

    public void move(int deltaX, int deltaY) {
        position.move(deltaX, deltaY);
        attachPoint.move(deltaX, deltaY);
    }

    public ElementPosition getAttachPoint() {
        return attachPoint;
    }

    public int getAttachPointX() {
        return attachPoint.getX();
    }

    public int getAttachPointY() {
        return attachPoint.getY();
    }

    public String getToolTip() {
        return null;
    }

    public String getJSONText() {
        return null;
    }

    public boolean isArg() {
        return getFileFormat().isArg();
    }
}
