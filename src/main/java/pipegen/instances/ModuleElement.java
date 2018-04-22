/*
    Program:  ModuleElement.java
    Author:   Michael Stokman
              Albert Einstein College of Medicine

 */

package pipegen.instances;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.font.*;
import org.json.*;
import java.lang.Integer;

import pipegen.*;
import pipegen.definitions.*;
import pipegen.exceptions.*;

/**
 * Represents a module through which data flows in a PipelineInstance.
 */
public class ModuleElement extends BlockElement {

    private static int maxID = -1;

    // Static final fields
    private static final int PADDING_VERTICAL = 8;
    private static final int PADDING_HORIZONTAL = 14;
    private static final int CORNER_ARCH = 8;
    private static final Font font = new Font("Bold", Font.BOLD, 14);

    // General class fields
    private ModuleDef module;
    private MountPointIn[] inputs;
    private MountPointOut[] outputs;
    private int id;

    // Dimension and location data
    private boolean areGraphicsSet;
    private Graphics2D g2d;
    private RoundRectangle2D.Double body;

    private DimensionInt textDim;
    private DimensionInt textOffset;
    private ElementPosition textPos;
    private DimensionInt dim;
    private int totalOutputWidth;
    private int totalInputWidth;


    /**
     * Constructs a new module at a particular location
     */
    public ModuleElement(ModuleDef module, ElementPosition position) {
        this.module = module;
        this.position = position;
        inputs = MountPointIn.wrapArray(this, module.getInputs());
        outputs = MountPointOut.wrapArray(this, module.getOutputs());

        maxID += 1;
        this.id = maxID;

        initGraphics();
    }

    public ModuleElement(ModuleDef module, ElementPosition position, int id) {
        this.module = module;
        this.position = position;
        inputs = MountPointIn.wrapArray(this, module.getInputs());
        outputs = MountPointOut.wrapArray(this, module.getOutputs());

        this.id = id;
        if (id > maxID) {
            maxID = id;
        }

        initGraphics();
    }

    /**
     * Sets up the 2D-graphics for displaying this module
     */
    private void initGraphics() {
        body = new RoundRectangle2D.Double();

        textDim = new DimensionInt();
        textOffset = new DimensionInt();
        textPos = new ElementPosition();

        dim = new DimensionInt();
    }

    /**
     * Loads an array of modules that were previously saved in a JSONArray. 
     * Ensures that each module was previously defined in the toolbox.
     */
    public static ModuleElement[] load(JSONArray moduleArray, ModuleDef[] moduleDefArray) throws InvalidModuleDefException {

        try {
            int len = moduleArray.length();
            ModuleElement[] out = new ModuleElement[len];
            for (int i=0; i < len; i++) {

                JSONObject moduleJSON = moduleArray.getJSONObject(i);
                String name = moduleJSON.getString("moduleName");
                int currID = moduleJSON.getInt("id");
                JSONObject positionObject = moduleJSON.getJSONObject("position");
                ElementPosition currPosition = ElementPosition.load(positionObject);

                int moduleIndex = findModuleIndex(name, moduleDefArray);
                ModuleDef currModule = moduleDefArray[moduleIndex];
                out[i] = new ModuleElement(currModule, currPosition, currID);
            }
            return out;
        } catch (Exception e) {
            e.printStackTrace();
            throw new InvalidModuleDefException(e);
        }
    }

    /**
     * Saves an array list of modules in a JSONArray. 
     */
    public static JSONArray save(ArrayList<ModuleElement> modules) throws JSONException {

        JSONArray outputJSON = new JSONArray();
        for (ModuleElement module : modules) {
            JSONObject moduleJSON = module.save();
            outputJSON.put(moduleJSON);
        }
        
        return outputJSON;
    }

    public JSONObject save() throws JSONException {

        JSONObject outputJSON = new JSONObject();

        // Save module name
        outputJSON.put("moduleName", getName());

        // Save id
        outputJSON.put("id", id);

        // Save position
        JSONObject positionJSON = new JSONObject();
        positionJSON.put("X", position.getX());
        positionJSON.put("Y", position.getY());
        outputJSON.put("position", positionJSON);

        return outputJSON;
    }

    /**
     * Returns an input mount point to the module based on input name
     */
    public MountPointIn getMountPointIn(String inputName) {
        for (int i=0; i < inputs.length; i++) {
            if (inputs[i].getName().equals(inputName)) {
                return inputs[i];
            }
        }
        return null;
    }

    /**
     * Returns an output mount point to the module based on output name
     */
    public MountPointOut getMountPointOut(String outputName) {
        for (int i=0; i < outputs.length; i++) {
            if (outputs[i].getName().equals(outputName)) {
                return outputs[i];
            }
        }
        return null;
    }

    /**
     * Returns the name of this block/module
     */
    public String getBlockName() {
        return getName();
    }

    /**
     * Sets up the graphics. This allows pre-computation of some values to make
     * using draw() more efficient 
     */
    public void setGraphics(Graphics2D g2d) {
        this.g2d = g2d;
        calcDimensions();
        calcPositions();
        calcMountPointPositions();
    }

    /**
     * Returns true if the graphics were previously set
     */
    public boolean areGraphicsSet() {
        return areGraphicsSet;
    }

    /**
     * Produces the makefile code associated with this module
     */
    public String toMakefile(File outputDir, DataTableFile table, int index) throws InvalidMakefileException {

        String name = getName();
        String target = getOutputNames(outputDir, table, index);
        String dependencies = getInputNames(outputDir, table, index);
        String command = module.getEnclosedCommand();
        
        command = interpretCommand(command, outputDir, table, index);

        String text = "";
        text += "# Module Name: " + getName() + "\n";
        text += target + " :" + dependencies + "\n";
        text += "\t" + command + "\n";
        text += "\n";

        for (int i=0; i < inputs.length; i++) {

            text += "# input " + i + "\n";
            MountPointOut previous = inputs[i].getPreviousMount();
            if (previous == null) {
                if (inputs[i].isRequired()) {
                    throw new InvalidMakefileException();
                } else {
                    continue;
                }
            } else {
                BlockElement upstream = previous.getBlock();
                text += upstream.toMakefile(outputDir, table, index);
            }
            text += "\n";
        }

        return text;
    }

    /**
     * Returns the name of the input files used by this module in the makefile
     */
    private String getInputNames(File outputDir, DataTableFile table, int rowIndex) {
        // *** Maybe redundant with getInputFileName
        String text = "";
        for (int i=0; i < inputs.length; i++) {
            text += " " + getInputFileName(i, outputDir, table, rowIndex);
        }
        return text;
    }

    /**
     * Returns the name of the output files produced by this module in the makefile
     */
    public String getOutputNames(File outputDir, DataTableFile table, int index) {
        // *** Maybe redundant with getOutputFileName
        return getOutputFileName(0, outputDir, table, index);
    }

    /**
     * Returns the name of the output file produced by this module in the makefile based on outputIndex
     */
    public String getOutputFileName(int outputIndex, File outputDir, DataTableFile table, int rowIndex) {
        // *** Maybe redundant with getInputNames

        BlockElement next = outputs[outputIndex].getNextBlock();
        if (next == null) {
            return "# Warning in ModuleElement.getOutputFileName() could not find filename.";
        }

        if (next instanceof SinkElement) {
            return next.getOutputFileName(outputIndex, outputDir, table, rowIndex);
        }

        return getName() + "-" + outputIndex + "-" + rowIndex;
    }

    /**
     * Returns the name of the input files used by this module in the makefile
     */
    public String getInputFileName(int inputIndex, File outputDir, DataTableFile table, int rowIndex) {

        BlockElement previous = inputs[inputIndex].getPreviousBlock();
        if (previous == null) {
            return "# Warning in ModuleElement.getInputFileName() could not find filename.";
        } else {
            return previous.getOutputFileName(inputIndex, outputDir, table, rowIndex);
        }
    }

    /**
     * Parse the enclosed commands such that variables are evaluated
     */
    private String interpretCommand(String uninterpretedCommand, File outputDir, DataTableFile table, int rowIndex) {

        String text = uninterpretedCommand;

        for (int i=0; i < inputs.length; i++) {
            text = text.replaceFirst("\\{" + inputs[i].getName() + "\\}", inputs[i].getFileName(outputDir, table, rowIndex));
        }

        for (int j=0; j < outputs.length; j++) {
            text = text.replaceFirst("\\{" + outputs[j].getName() + "\\}", outputs[j].getFileName(outputDir, table, rowIndex));
        }

        return text;
    }

    /**
     * Draws a graphic depiction of this module as part of drawing the pipeline
     */
    public void draw(Graphics2D g2d) {

        if (! areGraphicsSet) {
            setGraphics(g2d);
        }

        // Draw body
        g2d.setColor(MODULE_COLOR);
        g2d.fill(body);
        g2d.setColor(MODULE_RIM_COLOR);
        g2d.draw(body);

        // Draw name
        Font oldFont = g2d.getFont();
        g2d.setFont(font);
        g2d.setColor(Color.WHITE);
        g2d.drawString(getName(), textPos.getX(), textPos.getY());
        g2d.setFont(oldFont);

        // Draw the outputs for this module
        for (MountPointOut output : outputs) {
            output.draw(g2d);
        }

        // Draw the inputs for this module
        for (MountPointIn input : inputs) {
            input.draw(g2d);
        }
    }

    /**
     * Calculates the dimensions and offsets needed to draw this module
     */
    private void calcDimensions() {

        // Calculate text dimensions
        String name = getName();
        Font oldFont = g2d.getFont();
        g2d.setFont(font);
        FontRenderContext context = g2d.getFontRenderContext();
        Rectangle2D textBounds = font.getStringBounds(name, context);
        g2d.setFont(oldFont);
        textDim.setSize(textBounds.getWidth(), textBounds.getHeight());

        // Set text offset
        textOffset.setSize(PADDING_HORIZONTAL, PADDING_VERTICAL + textDim.getY());

        // Calculate module dimensions
        int height = textDim.getY() + 2*PADDING_VERTICAL;
        int width = textDim.getX() + 2*PADDING_HORIZONTAL;

        totalOutputWidth = outputs.length * outputs[0].getWidth();
        totalInputWidth = inputs.length * inputs[0].getWidth();

        if (width < 2 * totalOutputWidth) {
            width = 2 * totalOutputWidth;
        }
        if (width < 2 * totalInputWidth) {
            width = 2 * totalInputWidth;
        }
        dim.setSize(width, height);
    }

    /**
     * Calculates the positions needed to draw this module
     */
    private void calcPositions() {

        int xPos = position.getX();
        int yPos = position.getY();

        body.setRoundRect(xPos, yPos, dim.getX(), dim.getY(), CORNER_ARCH, CORNER_ARCH);

        textPos.setX(xPos + textOffset.getX());
        textPos.setY(yPos + textOffset.getY());
    }

    /**
     * Calculates the starting positions needed to draw the mount points on this
     * module.
     */
    private void calcMountPointPositions() {

        int xPosModule = position.getX();
        int yPosModule = position.getY();

        int width;
        int height;
        int spacer;
        int xPos;
        int yPos;

        // Calculate output mount points
        if (outputs.length > 0) {

            width = outputs[0].getWidth();
            height = outputs[0].getHeight();
            spacer = (dim.getX() - totalOutputWidth) / (1 + outputs.length);

            yPos = yPosModule + dim.getY() + 3;
            for (int i=0; i < outputs.length; i++) {
                xPos = xPosModule + (i+1)*spacer + (i*width);
                outputs[i].setPosition(xPos, yPos);
            }
        }

        // Calculate input mount points
        if (inputs.length > 0) {

            width = inputs[0].getWidth();
            height = inputs[0].getHeight();
            spacer = (dim.getX() - totalInputWidth) / (1 + inputs.length);

            yPos = yPosModule - height - 3;
            for (int i=0; i < inputs.length; i++) {
                xPos = xPosModule + (i+1)*spacer + i*width;
                inputs[i].setPosition(xPos, yPos);
            }
        }
    }

    /**
     * Returns true if the body of this module contains the given point
     */
    public boolean contains(Point location) {
        return body.contains(location);
    }

    /**
     * Moves the location of this element by deltaX and deltaY
     */
    public void move(int deltaX, int deltaY) {
        super.move(deltaX, deltaY);
        calcPositions();
        for (MountPointOut output : outputs) {
            output.move(deltaX, deltaY);
        }
        for (MountPointIn input : inputs) {
            input.move(deltaX, deltaY);
        }
    }

    /**
     * Produces a String that describes this sink 
     */
    public String toString() {
        /*String out = moduleName + " " + position + " inputs-";
        for (int i=0; i < inputs.length; i++) {
            out += " " + inputs[i];
        }
        out += " outputs-";
        for (int i=0; i < outputs.length; i++) {
            out += " " + outputs[i];
        }
        return out;*/
        
        return getName();
    }

    public int getID() {
        return id;
    }

    /**
     * Returns the index of a module based on its name
     */
    private static int findModuleIndex(String moduleName, ModuleDef[] moduleDefs) {
        // *** This seems likely to be the wrong way to find this
        java.util.Arrays.sort(moduleDefs);
        for (int i=0; i < moduleDefs.length; i++) {
            if (moduleName.equals(moduleDefs[i].getName())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Sets the input mount points
     */
    private void setInputs(MountPointIn[] inputs) {
        this.inputs = inputs;
    }

    /**
     * Returns the input mount points
     */
    public MountPointIn[] getInputs() {
        return inputs;
    }

    /**
     * Sets the output mount points
     */
    private void setOutputs(MountPointOut[] outputs) {
        this.outputs = outputs;
    }

    /**
     * Returns the output mount points
     */
    public MountPointOut[] getOutputs() {
        return outputs;
    }

    /**
     * Returns the number of outputs produced by this module
     */
    public int getOutputsLength() {
        return outputs.length;
    }

    /**
     * Returns the number of inputs used by this module
     */
    public int getInputsLength() {
        return inputs.length;
    }

    public String inputSummary(int i) {
        String varType = inputs[i].getDefinition().getFileFormat().getName();
        String varName = inputs[i].getName();
        return "<html><b>" + varName + "</b> - " + varType + "</html>";
    }

    public String outputSummary(int i) {
        String varType = outputs[i].getDefinition().getFileFormat().getName();
        String varName = outputs[i].getName();
        return "<html><b>" + varName + "</b> - " + varType + "</html>";
    }

    public ArrayList<ConnectionElement> getConnections() {
        ArrayList<ConnectionElement> jointConnections = new ArrayList<ConnectionElement>();

        for (MountPointIn input : inputs) {
            ArrayList<ConnectionElement> inputConnections = input.getConnections();
            jointConnections.addAll(inputConnections);
        }

        for (MountPointOut output : outputs) {
            ArrayList<ConnectionElement> outputConnections = output.getConnections();
            jointConnections.addAll(outputConnections);
        }

        return jointConnections;
    }

    public String getJSONTextInput(int index) {
        return "modules[" + id + "]." + inputs[index].getName();
    }

    public String getJSONTextOutput(int index) {
        return "modules[" + id + "]." + outputs[index].getName();
    }

    public BlockElement getParent(int index) {
        ConnectionElement connection = inputs[index].getConnection();
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

    public BlockElement[] getParents() {
        int numInputs = inputs.length;

        BlockElement[] out = new BlockElement[numInputs];
        for (int i=0; i < numInputs; i++) {
            out[i] = getParent(i);
        }

        return out;
    }

    public BlockElement getChild(int outIndex, int connectIndex) {
        ConnectionElement connection = outputs[outIndex].getConnection(connectIndex);
        if (connection == null) {
            return null;
        }

        MountPointIn stop = connection.getStop();
        if (stop == null) {
            return null;
        }

        BlockElement block = stop.getBlock();
        if (block == null) {
            return null;
        }

        return block;
    }

    public BlockElement[] getChildren() {

        ArrayList<BlockElement> children = new ArrayList<BlockElement>();

        for (int out=0; out < outputs.length; out++) {
            for (int con=0; con < outputs[out].getConnectionSize(); con++) {
                BlockElement child = getChild(out, con);
                if (child != null) {
                    children.add(child);
                }
            }
        }

        return children.toArray(new BlockElement[children.size()]);
    }

    public String asTarget(String id) {
        return "id" + id + "_module" + this.getID();
    }

    public String getName() {
        return module.getName();
    }

    public String getRecipe(String id, DataTableFile table) {
        String command = module.getEnclosedCommand();

        ParameterDef[] inputDefs = module.getInputs();
        ParameterDef[] outputDefs = module.getOutputs();

        for (int i=0; i < inputDefs.length; i++) {
            String varName = inputDefs[i].getName();
            if (inputDefs[i].isRequired()) {
                if (inputs[i].getConnection() == null) {
                    //*** throw new exception instead
                    return null;
                    // ***
                }

                String inputFilename = "";
                if (inputDefs[i].isArg()) {
                    inputFilename = "`cat \\" + inputs[i].getFilename(id, table) + "`";
                } else {
                    inputFilename = "\\" + inputs[i].getFilename(id, table);
                }

                // *** inputFilename = inputFilename.replace("REGISTRATION", "ZZZREGISTRATIONZZZ");

                //System.out.println("-------------------------------------------------------------");
                //System.out.println("ModuleElement.java - getRecipe() command=" + command);
                //System.out.println("ModuleElement.java - getRecipe() varName=" + varName);
                //System.out.println("ModuleElement.java - getRecipe() inputFilename=" + inputFilename);
                command = command.replaceFirst("\\{" + varName + "\\}", inputFilename);
                //System.out.println("ModuleElement.java - getRecipe() command=" + command);
                //System.out.println("-------------------------------------------------------------");
            } else {
                if (inputs[i].getConnection() == null) { 
                    command = command.replaceFirst("\\{" + varName + "\\}", "");
                }

                String inputFilename = "";
                if (inputDefs[i].isArg()) {
                    inputFilename = "`cat \\" + inputs[i].getFilename(id, table) + "`";
                } else {
                    inputFilename = "\\" + inputs[i].getFilename(id, table);
                }
                command = command.replaceFirst("\\{" + varName + "\\}", inputFilename);
            }

        }

        for (int i=0; i < outputDefs.length; i++) {
            String varName = outputDefs[i].getName();
            command = command.replaceFirst("\\{" + varName + "\\}", "\\$(PROCESSING)" + id + "/" + getOutputName(i));
        }

        return command;
    }

    public String getInputName(int i) {
        return id + "_in" + i + inputs[i].getFormatSuffix();
    }

    public String getOutputName(int i) {
        return id + "_" + i + outputs[i].getFormatSuffix();
    }

    public String getFilename(String id, int outIndex) {

        return "$(PROCESSING)" + id + "/" + getOutputName(outIndex);
    }

    public String getFilenameInput(String id, int inIndex) {

        return "$(PROCESSING)" + id + "/" + getInputName(inIndex);
    }

    public String getFilenamePattern(String id, int outIndex) {
        return "%/" + id + "/" + getOutputName(outIndex);
    }

    public String getInputPattern(String id, int inIndex) {
        return "%/" + id + "/" + getInputName(inIndex);
    }

    /**
     * Returns the first output file produced by this block
     * Note: This is redundant as each sub-class has a general purpose 
     * getFilename() that takes into account output number
     * Note: This should be called something more like getOutputFilename() 
     */
    public String getFilename(String id, DataTableFile table) {
        // *** Assumes a single output per module

        return getFilename(id, 0);
    }

    /**
     * Returns a phoney name for this BlockElement
     * Note: this might only be needed by SinkElements and this could be avoided
     * if sinks target explicitly named files.
     */
    public String getPhoneyname(String id) {
        return "id" + id + "_module" + this.id;
    }

    public String getErrorCatch(String id) {
        return "|| mkdir -p $(ERRORS)" + id + "/" + this.id;
    }

    /**
     * Returns the first output element of this BlockElement
     * Note: should differentiate between output blocks and output files
     * This produces output block of class BlockElement
     */
    public BlockElement getFirstOutput() {

        for (MountPointOut mountPoint : outputs) {
            BlockElement blockElement = mountPoint.getFirstOutput();
            if (blockElement != null) {
                return blockElement;
            }
        }
        return null;
    }

    /**
     * Returns true if this BlockElement is the first valid output element of 
     * parent BlockElement.
     */
    public boolean isFirstOutputOf(BlockElement parent) {
        if (this == parent.getFirstOutput()) {
            return true;
        } else {
            return false;
        }
    }
}
