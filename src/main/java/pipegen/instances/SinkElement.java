/*
    Program:  SinkElement.java
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
 * Represents a sink for data flowing out of a PipelineInstance.
 */
public class SinkElement extends BlockElement {

    private static int maxID = -1;

    // Static final fields
    private static final int WIDTH = 30;
    private static final int HEIGHT = 24;

    // General class fields
    private String tableField;
    private MountPointIn input;
    private int id;

    // Dimension and location data
    private boolean areGraphicsSet;
    private Graphics2D g2d;
    private Polygon body;

    private DimensionInt textDim;
    private DimensionInt tagDim;

    private DimensionInt textOffset;
    private DimensionInt tagOffset;

    private ElementPosition textPos;
    private ElementPosition tagPos;


    /**
     * Constructs a new sink associated with a particular table field in 
     * DataTableFile, at a particular location, and with a particular parameter
     * definition (pairs a name with a file format). 
     */
    public SinkElement(String tableField, ElementPosition position, ParameterDef definition) {
        this.tableField = tableField;
        this.position = position;
        this.input = new MountPointIn(this, definition, 0);
        maxID += 1;
        this.id = maxID;

        initGraphics();
    }

    public SinkElement(String tableField, ElementPosition position, ParameterDef definition, int id) {
        this.tableField = tableField;
        this.position = position;
        this.input = new MountPointIn(this, definition, 0);
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
        int[] xArray = {xPos, xPos + WIDTH, xPos + (WIDTH/2)};
        int[] yArray = {yPos, yPos, yPos + HEIGHT};
        body = new Polygon(xArray, yArray, 3);

        textDim = new DimensionInt();
        textOffset = new DimensionInt();
        textPos = new ElementPosition();

        tagDim = new DimensionInt();
        tagOffset = new DimensionInt();
        tagPos = new ElementPosition();
    }

    /**
     * Loads an array of sinks that were previously saved in a JSONArray. 
     * Ensures that any file formats used are from the set of previously defined
     * formats.
     */
    public static SinkElement[] load(JSONArray sourceArray, FileFormatDef[] formats) throws InvalidSinkDefException {

        try {
            int len = sourceArray.length();
            SinkElement[] out = new SinkElement[len];
            for (int i=0; i < len; i++) {
                JSONObject currSink = sourceArray.getJSONObject(i);
                String currTableField = currSink.getString("dataTableField");

                int currID = currSink.getInt("id");

                JSONObject positionObject = currSink.getJSONObject("position");
                ElementPosition currPosition = ElementPosition.load(positionObject);

                JSONObject inputDefObject = currSink.getJSONObject("input");
                ParameterDef currDef = ParameterDef.load(inputDefObject, formats);

                out[i] = new SinkElement(currTableField, currPosition, currDef, currID);
            }
        
            return out;
        } catch (Exception e) {
            e.printStackTrace();
            throw new InvalidSinkDefException(e);
        }
    }

    public static JSONArray save(ArrayList<SinkElement> sinks) throws JSONException {

        JSONArray outputJSON = new JSONArray();
        for (SinkElement sink : sinks) {
            JSONObject sinkJSON = sink.save();
            outputJSON.put(sinkJSON);
        }
        
        return outputJSON;
    }


    public JSONObject save() throws JSONException {

        JSONObject outputJSON = new JSONObject();

        // Save table field
        outputJSON.put("dataTableField", tableField);

        // Save id
        outputJSON.put("id", id);

        // Save input mount point
        JSONObject mountpointJSON = new JSONObject();
        mountpointJSON.put("name", "");
        mountpointJSON.put("format", input.getDefinition().getFileFormat().getName());
        mountpointJSON.put("required", input.isRequired());
        outputJSON.put("input", mountpointJSON);

        // Save position
        JSONObject positionJSON = new JSONObject();
        positionJSON.put("X", position.getX());
        positionJSON.put("Y", position.getY());
        outputJSON.put("position", positionJSON);

        return outputJSON;
    }

    /**
     * Returns the in bound mount point on this sink
     */
    public MountPointIn getMountPointIn() {
        return input;
    }

    /**
     * Returns the name of this block
     */
    public String getBlockName() {
        return tableField;
    }

    /**
     * Returns the table field associated with this sink
     */
    public String getTableField() {
        return tableField;
    }

    public MountPointIn getInput() {
        return input;
    }

    /**
     * Sets up the graphics. This allows pre-computation of some values to make
     * using draw() more efficient 
     */
    public void setGraphics(Graphics2D g2d) {
        this.g2d = g2d;
        calcDimensions();
        calcPositions(0, 0);

        int xPos = position.getX();
        int yPos = position.getY();
        input.setPosition(xPos + (WIDTH / 2) - (input.getWidth() / 2), yPos - 3 - input.getHeight());
    }

    /**
     * Returns true if the graphics were previously set
     */
    public boolean areGraphicsSet() {
        return areGraphicsSet;
    }

    /**
     * Produces the makefile code associated with this sink
     */
    public String toMakefile(File outputDir, DataTableFile table) throws InvalidMakefileException {
        // *** Makes sure both versions of toMakefile() are needed
        return toMakefile(outputDir, table, -1);
    }

    /**
     * Produces the makefile code associated with this sink
     */
    public String toMakefile(File outputDir, DataTableFile table, int index) throws InvalidMakefileException {
        // *** Makes sure both versions of toMakefile() are needed

        String[] idList = table.getColumnByHeader("id");
        String[] outputList = table.getColumnByHeader(tableField);

        String text = "";
        for (int j=0; j < outputList.length; j++) {

            text += "# Dependency tree for: " + outputList[j] + "\n";
            text += "# SINK: " + index + "\n";
            text += "# ID: " + idList[j] + "\n\n";

            try {
                File currentSink = new File(outputDir.toString() + "/" + outputList[j]);
                String canonicalPath = currentSink.getCanonicalPath();
                BlockElement upstream = input.getPreviousBlock();
                text += upstream.toMakefile(outputDir, table, index);
                text += "\n";
            } catch (IOException e) {
                throw new InvalidMakefileException(e);
            }
        }

        return text;
    }

    /**
     * Draws a graphic depiction of this source as part of drawing the pipeline
     */
    public void draw(Graphics2D g2d) {

        if (! areGraphicsSet) {
            setGraphics(g2d);
        }

        // Draw label tag
        g2d.setColor(TAG_COLOR);
        g2d.fillRect(tagPos.getX(), tagPos.getY(), tagDim.getX(), tagDim.getY());
        g2d.setColor(input.getColor());
        g2d.drawString(tableField, textPos.getX(), textPos.getY());

        // Draw sink body
        g2d.setColor(MODULE_COLOR);
        g2d.fill(body);
        g2d.setColor(MODULE_RIM_COLOR);
        g2d.draw(body);

        // Draw the MountPointOut for this source
        input.draw(g2d);
    }

    /**
     * Calculates the dimensions and offsets needed to draw this sink
     */
    private void calcDimensions() {

        // Calculate text dimensions
        Font font = g2d.getFont();
        FontRenderContext context = g2d.getFontRenderContext();
        Rectangle2D textBounds = font.getStringBounds(tableField, context);
        textDim.setSize(textBounds.getWidth(), textBounds.getHeight());
        textOffset.setSize(WIDTH + TAG_HORIZONTAL_PAD, (HEIGHT / 2) + (textDim.getY() / 2));

        // Calculate tag dimensions
        tagDim.setSize(textDim.getX() + 2*TAG_HORIZONTAL_PAD + (WIDTH / 2), textDim.getY() + 2*TAG_VERTICAL_PAD);
        tagOffset.setSize((WIDTH / 2), (HEIGHT / 2) - (textDim.getY() / 2) - TAG_VERTICAL_PAD);
    }

    /**
     * Calculates the positions needed to draw this sink
     */
    private void calcPositions(int deltaX, int deltaY) {

        int xPos = position.getX();
        int yPos = position.getY();

        // Calculate text position
        textPos.setX(xPos + textOffset.getX());
        textPos.setY(yPos + textOffset.getY());

        // Calculate tag position
        tagPos.setX(xPos + tagOffset.getX());
        tagPos.setY(yPos + tagOffset.getY());

        // Translate body position
        body.translate(deltaX, deltaY);
    }

    /**
     * Returns true if the body of this sink contains the given point
     */
    public boolean contains(Point location) {
        return body.contains(location);
    }

    public boolean inputContains(Point location) {
        return input.contains(location);
    }

    public MountPointIn mountPointContaining(Point location) {
        // *** Maybe this is a silly function to have
        if (input.contains(location)) {
            return input;
        } else {
            return null;
        }
    }

    /**
     * Moves the location of this element by deltaX and deltaY
     */
    public void move(int deltaX, int deltaY) {
        super.move(deltaX, deltaY);
        calcPositions(deltaX, deltaY);
        input.move(deltaX, deltaY);
    }

    /**
     * Produces a String that describes this sink 
     */
    public String toString() {
        //return tableField + " (" + position.getX() + ", " + position.getY() + ")";
        return tableField;
    }

    public int getID() {
        return id;
    }

    public String inputSummary(int index) {
        String varType = input.getDefinition().getFileFormat().getName();
        String varName = tableField;
        return "<html><b>" + varName + "</b> - " + varType + "</html>";
    }

    public ArrayList<ConnectionElement> getConnections() {
        return input.getConnections();
    }

    public static ArrayList<String> getFields(ArrayList<SinkElement> sinks) {
        ArrayList<String> fields = new ArrayList<String>();
        for (int i=0; i < sinks.size(); i++) {
            String field = sinks.get(i).getTableField();
            if (! fields.contains(field)) {
                fields.add(field);
            }
        }
        return fields;
    }

    public String getJSONTextInput(int index) {
        return "sinks[" + id + "]";
    }

    public BlockElement getParent(int index) {
        ConnectionElement connection = input.getConnection();
        if (connection == null) {
            return null;
        }

        MountPointOut start = connection.getStart();
        if (start == null) {
            return null;
        }

        BlockElement block = start.getBlock();
        if (block == null) {
            return null;
        }

        return block;
    }

    public String asTarget(String id) {
        return "id" + id + "_sink" + this.getID();
    }

    public String getFilename(String id, DataTableFile table) {
        return table.getDataByHeaderAndRowid(tableField, id);
    }

    public String getPhoneyname(String id) {
        return "id" + id + "_sink" + this.id;
    }

    public BlockElement getFirstOutput() {
        return null;
    }

    public boolean isFirstOutputOf(BlockElement parent) {
        if (this == parent.getFirstOutput()) {
            return true;
        } else {
            return false;
        }
    }
}
