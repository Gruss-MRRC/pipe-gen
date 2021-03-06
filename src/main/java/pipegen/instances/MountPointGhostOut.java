/*
    Program:  MountPointGhostOut.java
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
public class MountPointGhostOut extends MountPointGhost implements ConnectableOutput {

    // Static final fields


    public MountPointGhostOut(MountPointOut precursor, PipelineInstance pipeline) {
        super(precursor, pipeline);

        circleForm = false;
        this.isNew = true;
    }

    public void pressed() {

        ConnectionElement newConnection = new ConnectionElement((MountPointOut)precursor, this);
        newConnection.setStart((MountPointOut)precursor);  // *** Somehow these two lines are useful
        newConnection.setStop(this);  // *** Somehow these two lines are useful
        // *** Has something to do with the way the constructor is overloaded
        pipeline.getConnections().add(newConnection);
    }

    public void released(ArrayList<SourceElement> sources, ArrayList<SinkElement> sinks, ArrayList<ModuleElement> modules) {
        MountPointIn targetInput = getTargetInput(sinks, modules);
        if (targetInput != null) {
            getConnection().setStop(targetInput);
        } else {
            pipeline.getConnections().remove(getConnection());
            getConnection().clear();
        }
    }

    public void move(int deltaX, int deltaY) {
        super.move(deltaX, deltaY);
        double distance = this.distance(precursor);
        if (distance > MIN_DISTANCE) {
            circleForm = true;
        } else {
            circleForm = false;
        }
    }
}
