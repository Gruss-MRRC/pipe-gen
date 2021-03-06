/*
    Program:  MountPointGhostIn.java
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
public class MountPointGhostIn extends MountPointGhost implements ConnectableInput {

    // Static final fields
    private static final double MIN_DISTANCE = 8.0;

    public MountPointGhostIn(MountPointIn precursor, PipelineInstance pipeline) {
        super(precursor, pipeline);

        circleForm = true;
        if (getConnections().size() == 0) {
            System.out.println("MountPointGhost.java - isNew=true");
            isNew = true;
        } else {
            isNew = false;
        }
    }

    public void pressed() {

        if (isNew) {
            ConnectionElement newConnection = new ConnectionElement(this, (MountPointIn)precursor);
            newConnection.setStart(this);  // *** Somehow these two lines are useful
            newConnection.setStop((MountPointIn)precursor);  // *** Somehow these two lines are useful
            pipeline.getConnections().add(newConnection);
            //return newConnection;
            //return null;
        } else {
            getConnection().setStop(this);
            //return null;
        }
    }

    public void released(ArrayList<SourceElement> sources, ArrayList<SinkElement> sinks, ArrayList<ModuleElement> modules) {

        if (isNew) {
            MountPointOut targetOutput = getTargetOutput(sources, modules);
            if (targetOutput != null) {
                getConnection().setStart(targetOutput);
            } else {
                pipeline.getConnections().remove(getConnection());
                getConnection().clear();
            }
        } else {
            MountPointIn targetInput = getTargetInput(sinks, modules);
            if (targetInput != null) {
                getConnection().setStop(targetInput);
            } else {
                pipeline.getConnections().remove(getConnection());
                getConnection().clear();
            }
        }
    }

    public void move(int deltaX, int deltaY) {
        super.move(deltaX, deltaY);
        if (isNew) {
            double distance = this.distance(precursor);
            if (distance > MIN_DISTANCE) {
                circleForm = false;
            } else {
                circleForm = true;
            }
        }
    }

    public String getAttachedName() {
        // *** Temporary
        return "[Ghost]";
    }
}
