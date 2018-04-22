/*
    Program:  SourceElement.java
    Author:   Michael Stokman
              Albert Einstein College of Medicine

 */

package pipegen.instances;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.font.*;
import org.json.*;

import pipegen.*;
import pipegen.definitions.*;
import pipegen.exceptions.*;

/**
 * Represents a source of data flowing into a PipelineInstance.
 */
public class SourceElement extends BlockElement {

    private static int maxID = -1;

    // Static final fields
    private static final int DIAMETER = 30;
    
    // General class fields
    private String tableField;
    private MountPointOut output;
    private int id;

    // Dimension and location data
    private boolean areGraphicsSet;
    private Graphics2D g2d;
    private Ellipse2D.Double body;

    private DimensionInt textDim;
    private DimensionInt tagDim;
    private DimensionInt outputDim;

    private DimensionInt textOffset;
    private DimensionInt tagOffset;
    private DimensionInt outputOffset;

    private ElementPosition textPos;
    private ElementPosition tagPos;

    //private boolean toDraw;


    /**
     * Constructs a new source associated with a particular table field in 
     * DataTableFile, at a particular location, and with a particular parameter
     * definition (pairs a name with a file format). 
     */
    public SourceElement(String tableField, ElementPosition position, ParameterDef definition) {

        this.tableField = tableField;
        this.position = position;
        this.output = new MountPointOut(this, definition, 0);
        maxID += 1;
        this.id = maxID;

        initGraphics();
    }

    public SourceElement(String tableField, ElementPosition position, ParameterDef definition, int id) {
        this.tableField = tableField;
        this.position = position;
        this.output = new MountPointOut(this, definition, 0);
        this.id = id;
        if (id > maxID) {
            maxID = id;
        }

        initGraphics();
    }

    private void initGraphics() {
        areGraphicsSet = false;
        int xPos = position.getX();
        int yPos = position.getY();
        body = new Ellipse2D.Double(xPos, yPos, DIAMETER, DIAMETER);

        textDim = new DimensionInt();
        textOffset = new DimensionInt();
        textPos = new ElementPosition();

        tagDim = new DimensionInt();
        tagOffset = new DimensionInt();
        tagPos = new ElementPosition();

        outputDim = new DimensionInt();
        outputOffset = new DimensionInt();
    }

    /**
     * Loads an array of sources that were previously saved in a JSONArray. 
     * Ensures that any file formats used are from the set of previously defined
     * formats.
     */
    public static SourceElement[] load(JSONArray sourceArray, FileFormatDef[] formats) throws InvalidSourceDefException {

        try {
            int len = sourceArray.length();
            SourceElement[] out = new SourceElement[len];
            for (int i=0; i < len; i++) {
                JSONObject currSource = sourceArray.getJSONObject(i);
                String currTableField = currSource.getString("dataTableField");

                int currID = currSource.getInt("id");

                JSONObject positionObject = currSource.getJSONObject("position");
                ElementPosition currPosition = ElementPosition.load(positionObject);

                JSONObject outputDefObject = currSource.getJSONObject("output");
                ParameterDef currDef = ParameterDef.load(outputDefObject, formats);

                out[i] = new SourceElement(currTableField, currPosition, currDef, currID);
            }
        
            return out;
        } catch (Exception e) {
            e.printStackTrace();
            throw new InvalidSourceDefException(e);
        }
    }

    public static JSONArray save(ArrayList<SourceElement> sources) throws JSONException {

        JSONArray outputJSON = new JSONArray();
        for (SourceElement source : sources) {
            JSONObject sourceJSON = source.save();
            outputJSON.put(sourceJSON);
        }
        
        return outputJSON;
    }

    public JSONObject save() throws JSONException {

        JSONObject outputJSON = new JSONObject();

        // Save table field
        outputJSON.put("dataTableField", tableField);

        // Save id
        outputJSON.put("id", id);

        // Save output mount point
        JSONObject mountpointJSON = new JSONObject();
        mountpointJSON.put("name", "");
        mountpointJSON.put("format", output.getDefinition().getFileFormat().getName());
        mountpointJSON.put("required", output.isRequired());
        outputJSON.put("output", mountpointJSON);

        // Save position
        JSONObject positionJSON = new JSONObject();
        positionJSON.put("X", position.getX());
        positionJSON.put("Y", position.getY());
        outputJSON.put("position", positionJSON);

        return outputJSON;
    }

    /**
     * Returns the out bound mount point on this source
     */
    public MountPointOut getMountPointOut() {
        return output;
    }

    /**
     * Returns the name of this block
     */
    public String getBlockName() {
        return tableField;
    }

    public String getName() {
        return tableField;
    }

    /**
     * Returns the table field associated with this source
     */
    public String getTableField() {
        return tableField;
    }

    /**
     * Sets up the graphics. This allows pre-computation of some values to make
     * using draw() more efficient 
     */
    public void setGraphics(Graphics2D g2d) {
        this.g2d = g2d;

        calcDimensions();
        calcPositions();

        int xPos = position.getX();
        int yPos = position.getY();
        output.setPosition(xPos + outputOffset.getX(), yPos + outputOffset.getY());
    }

    /**
     * Returns true if the graphics were previously set
     */
    public boolean areGraphicsSet() {
        return areGraphicsSet;
    }

    /**
     * Produces the makefile code associated with this source
     */
    public String toMakefile(File outputDir, DataTableFile table, int index) {
        return "# SourceElement name='" + tableField + "'. (No make code written for sources)\n";
    }

    /**
     * Draws a graphic depiction of this source as part of drawing the pipeline
     */
    public void draw(Graphics2D g2d) {

        //System.out.println("SourceElement.java - draw() toDraw = " + toDraw);

        if (! areGraphicsSet) {
            setGraphics(g2d);
        }

        //if (toDraw) {
        // Draw the label tag
        g2d.setColor(TAG_COLOR);
        g2d.fillRect(tagPos.getX(), tagPos.getY(), tagDim.getX(), tagDim.getY());
        g2d.setColor(output.getColor());
        g2d.drawString(tableField, textPos.getX(), textPos.getY());

        // Draw source body
        g2d.setColor(MODULE_COLOR);
        g2d.fill(body);
        g2d.setColor(MODULE_RIM_COLOR);
        g2d.draw(body);

        // Draw the MountPointOut for this source
        output.draw(g2d);
        //toDraw = false;
        //}
    }

    /**
     * Calculates the dimensions and offsets needed to draw this source
     */
    private void calcDimensions() {

        // Calculate text dimensions
        Font font = g2d.getFont();
        FontRenderContext context = g2d.getFontRenderContext();
        Rectangle2D textBounds = font.getStringBounds(tableField, context);
        textDim.setSize(textBounds.getWidth(), textBounds.getHeight());
        textOffset.setSize(DIAMETER + 3, (DIAMETER / 2) + (textDim.getY() / 2));

        // Calculate tag dimensions
        tagDim.setSize(textDim.getX() + 2*TAG_HORIZONTAL_PAD, textDim.getY() + 2*TAG_VERTICAL_PAD);
        tagOffset.setSize(DIAMETER - TAG_HORIZONTAL_PAD, (DIAMETER / 2) - (textDim.getY() / 2) - TAG_VERTICAL_PAD);

        // Get output dimensions
        outputDim = output.getSize();
        outputOffset.setSize((DIAMETER / 2) - (outputDim.getX() / 2), DIAMETER + 3);
    }

    /**
     * Calculates the positions needed to draw this source
     */
    private void calcPositions() {

        int xPos = position.getX();
        int yPos = position.getY();

        // Calculate text position
        textPos.setX(xPos + textOffset.getX());
        textPos.setY(yPos + textOffset.getY());

        // Calculate tag position
        tagPos.setX(xPos + tagOffset.getX());
        tagPos.setY(yPos + tagOffset.getY());

        // Set body position
        body.setFrame(xPos, yPos, DIAMETER, DIAMETER);
    }

    /**
     * Returns true if the body of this source contains the given point
     */
    public boolean contains(Point location) {
        return body.contains(location);
    }

    public MountPointOut mountPointContaining(Point location) {
        // *** This is a silly function to have
        if (output.contains(location)) {
            return output;
        } else {
            return null;
        }
    }

    /**
     * Moves the location of this element by deltaX and deltaY
     */
    public void move(int deltaX, int deltaY) {

        //System.out.println("SourceElement.java - move()");

        super.move(deltaX, deltaY);
        calcPositions();
        output.move(deltaX, deltaY);
        //toDraw = true;
    }

    /**
     * Produces a String that describes this source 
     */
    public String toString() {
        //return tableField + " (" + position.getX() + ", " + position.getY() + ")";
        return tableField;
    }

    public int getID() {
        return id;
    }

    public String outputSummary(int index) {
        String varType = output.getDefinition().getFileFormat().getName();
        String varName = tableField;
        return "<html><b>" + varName + "</b> - " + varType + "</html>";
    }

    public ArrayList<ConnectionElement> getConnections() {
        return output.getConnections();
    }

    public static ArrayList<String> getFields(ArrayList<SourceElement> sources) {
        ArrayList<String> fields = new ArrayList<String>();
        for (int i=0; i < sources.size(); i++) {
            String field = sources.get(i).getTableField();
            if (! fields.contains(field)) {
                fields.add(field);
            }
        }
        return fields;
    }

    public String getJSONTextOutput(int i) {
        return "sources[" + id + "]";
    }

    public String asTarget(String id) {
        return "id" + id + "_source" + this.getID();
    }

    public String getOutputName(int i) {
        return id + "_" + i + output.getFormatSuffix();
    }

    public String getFilename(String id, DataTableFile table) {
        if (isArg()) {
            return "$(PROCESSING)" + id + "/" + getOutputName(0);
        } else {
            return table.getDataByHeaderAndRowid(tableField, id);
        }
    }

    public String getContents(String id, DataTableFile table) {
        return table.getDataByHeaderAndRowid(tableField, id);
    }

    public String getPhoneyname(String id) {
        return "id" + id + "_source" + this.id;
    }

    public BlockElement getFirstOutput() {
        return output.getFirstOutput();
    }

    public boolean isFirstOutputOf(BlockElement parent) {
        return false;
    }

    public boolean isArg() {
        return output.getFileFormat().isArg();
    }
}
