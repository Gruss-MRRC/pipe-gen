/*
    Program:  BlockElement.java
    Author:   Michael Stockman
              Albert Einstein College of Medicine

 */

package pipegen.instances;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.font.*;

import pipegen.*;
import pipegen.exceptions.*;

/**
 * Defines an abstract base class. Represents building blocks (i.e. elements)
 * that comprise a PipelineInstance. SourceElements, SinkElements, and 
 * ModuleElements extend this class.
 */
public abstract class BlockElement implements Draggable {

    protected static final Color MODULE_COLOR = new Color(80, 80, 80);
    protected static final Color MODULE_RIM_COLOR = new Color(80, 80, 80, 100);
    protected static final Color TAG_COLOR = new Color(50, 50, 50, 20);
    protected static final int TAG_VERTICAL_PAD = 4; 
    protected static final int TAG_HORIZONTAL_PAD = 8;

    protected ElementPosition position;

    /**
     * Returns the name of this BlockElement
     * Note: Should maybe changed to getName() 
     */
    abstract String getBlockName();

    /**
     * Converts this BlockElement to a snippet makefile text
     */
    abstract String toMakefile(File outputDir, DataTableFile table, int rowIndex) throws InvalidMakefileException;

    /**
     * Returns the name of the output file produced by this module in the makefile based on outputIndex
     * Note: Does not appear to need outputDir
     */
    public String getOutputFileName(int outputIndex, File outputDir, DataTableFile table, int rowIndex) {
        return null;
    }

    /**
     * Returns the name of the input file required by this module in the makefile based on inputIndex
     * Note: Does not appear to need outputDir
     */
    public String getInputFileName(int inputIndex, File outputDir, DataTableFile table, int rowIndex) {
        return null;
    }

    /**
     * Moves the position of this BlockElement by deltaX and deltaY
     */
    public void move(int deltaX, int deltaY) {
        position.move(deltaX, deltaY);
    }

    /**
     * Returns the coordinate position of this BlockElement on the Pipeline
     * panel of the GUI.
     */
    public ElementPosition getPosition() {
        return position;
    }

    /**
     * Returns the input MountPoint summary for input given by index that is to 
     * be shown on Pipeline panel of GUI when mousing over a input MountPoint
     * Note: Could have a better name
     */
    public String inputSummary(int index) {
        return null;
    }

    /**
     * Returns an ArrayList of ConnectionElements representing every in-going 
     * and out-going connection on this BlockElement
     */
    public String outputSummary(int index) {
        return null;
    }

    /**
     * Returns an ArrayList of ConnectionElements representing every in-going 
     * and out-going connection on this BlockElement
     */
    ArrayList<ConnectionElement> getConnections() {
        return null;
    }

    /**
     * Returns a JSON snippet for input of given index
     * Note: Need to trace where this is used
     */
    public String getJSONTextInput(int index) {
        return null;
    }

    /**
     * Returns a JSON snippet for output of given index
     * Note: Need to trace where this is used
     */
    public String getJSONTextOutput(int index) {
        return null;
    }

    /**
     * Returns the index-th BlockElement parent of this BlockElement
     * Note: This should be called something more like getParentBlock() maybe
     */
    public BlockElement getParent(int index) {
        System.out.println("BlockElement.java - getParent() return null");
        return null;
    }

    /**
     * Returns taget for this block
     * Note: This is not really used. This could be removed?
     */
    public String asTarget(String id) {
        return "# BlockElement.asTarget()";
    }

    /**
     * Returns setup taget for this block based on subject id
     * Note: This should be called something more like getSetupTarget()
     * Note: This functionality should maybe be handled by MakefileFactory
     */
    public String asSetup(String id) {
        return "setup" + id;
    }

    /**
     * Returns the first output file produced by this block
     * Note: This is redundant as each sub-class has a general purpose 
     * getFilename() that takes into account output number
     * Note: This should be called something more like getOutputFilename() 
     */
    public String getFilename(String id, DataTableFile table) {
        return "# BlockElement.getFilename()";
    }

    /**
     * Returns a phoney name for this BlockElement
     * Note: this might only be needed by SinkElements and this could be avoided
     * if sinks target explicitly named files.
     */
    public String getPhoneyname(String id) {
        return "";
    }

    /**
     * Returns the first output element of this BlockElement
     * Note: should differentiate between output blocks and output files
     * This produces output block of class BlockElement
     */
    public BlockElement getFirstOutput() {
        return null;
    }

    /**
     * Returns true if this BlockElement is the first valid output element of 
     * parent BlockElement.
     */
    public boolean isFirstOutputOf(BlockElement parent) {
        return false;
    }
}
