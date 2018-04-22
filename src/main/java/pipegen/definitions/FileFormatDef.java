/*
    Program:  FileFormatDef.java
    Author:   Michael Stokman
              Albert Einstein College of Medicine

    Purpose:  This class represents a file format definition. It provides 
              mechanisms to load/save these definitions to disk and some 
              accessor methods.

              File formats as handled by pipelines have a name and are 
              color-coded when rendered.

              Intermediate processing files produced during analysis will have 
              a file suffix given in this definition. The additional complexity 
              of pipe-gen handling suffixes hopefully makes it easier to 
              incorporate programs that assume file suffixes into pipelines.

              File format definitions also allow inheritance. For instance the
              CSV (character separated values) format may be thought of as 
              extending the simple text file format. In that all CSV files are 
              valid text files but have some additional constraints.

 */

package pipegen.definitions;

import java.io.*;
import java.util.*;
import java.awt.Color;
import org.json.*;

import pipegen.exceptions.*;


public class FileFormatDef implements Comparable<FileFormatDef> {
    
    private static final String ARG_NAME = "arg";
    private static final Color ARG_COLOR = new Color(80, 80, 80);
    private static final FileFormatDef ARG_DEF = new FileFormatDef(ARG_NAME, ARG_COLOR, "", "");

    private String name;
    private Color color;
    private String suffix;
    private String parentName;
    private FileFormatDef parent;

    public FileFormatDef(String name, Color color, String suffix, String parentName) {
        this.name = name;
        this.color = color;
        this.suffix = suffix;
        this.parentName = parentName;
    }

    /**
     * Load a file format definition from disk
     */
    public static FileFormatDef load(JSONObject jsonObject) throws InvalidFileFormatDefException {

        try {

            // Get name from JSON object
            String name = JSONObject.getNames(jsonObject)[0];
            if (name.equals("arg")) {
                throw new InvalidFileFormatDefException();
            }

            // Get JSON file format object from input JSON object based on name
            JSONObject jsonFormat = jsonObject.getJSONObject(name);

            // Get file format color
            JSONArray colorArray = jsonFormat.getJSONArray("color");
            int red   = colorArray.getInt(0);
            int green = colorArray.getInt(1);
            int blue  = colorArray.getInt(2);
            Color color = new Color(red, green, blue);
    
            // Get file format suffix
            String suffix = jsonFormat.getString("suffix");

            // Get parent formats if available
            String parent;
            try {
                parent = jsonFormat.getString("extends");
            } catch (JSONException e) {
                parent = "";
            }

            // Return the newly loaded file format
            return new FileFormatDef(name, color, suffix, parent);

        } catch (JSONException e) {
            e.printStackTrace();
            throw new InvalidFileFormatDefException(e);
        }
    }

    /**
     * This static method loads an array of file format definitions from disk
     */
    public static FileFormatDef[] load(File jsonFile) throws InvalidFileFormatDefException {

        try {
            
            // Read text from JSON file to create a new JSON object
            String text = ToolboxDef.readTextfile(jsonFile);
            if (text == null) {
                throw new InvalidFileFormatDefException();
            }
            JSONObject jsonObject = new JSONObject(text);

            // Select the array of format definitions from the JSON object
            JSONArray formatArray = jsonObject.getJSONArray("formatDefs");

            // Load the format definitions found in the array
            List<FileFormatDef> out = new ArrayList<FileFormatDef>();
            for (int i=0; i < formatArray.length(); i++) {
                JSONObject formatObject = formatArray.getJSONObject(i);
                out.add(load(formatObject));
            }

            // Add a definition for argument-type data
            out.add(ARG_DEF);

            // Insure that formats only extend other defined formats 
            for (FileFormatDef def : out) {
                def.setParent(out);
            }

            return out.toArray(new FileFormatDef[out.size()]);

        } catch (JSONException e) {
            e.printStackTrace();
            throw new InvalidFileFormatDefException(e);
        } catch (InvalidAboutFileException e) {
            e.printStackTrace();
            throw new InvalidFileFormatDefException(e);
        }
    }

    /**
     * Saves this file format definition to disk
     */
    /*public void save() {
        return;
    }*/

    /**
     * Sets a parent file format (immediate super-class) for this format. Throws
     * an exception if the requested parent format is not known.
     */
    private void setParent(List<FileFormatDef> defs) throws InvalidFileFormatDefException {
        if (this.parentName.equals("")) {
            return;
        }

        for (FileFormatDef def : defs) {
            if (def.getName().equals(this.parentName)) {
                this.parent = def;
                return;
            }
        }
        throw new InvalidFileFormatDefException();
    }

    /**
     * Returns true if files in this format can be passed to elements that 
     * require files in the target format. Checks if the definitions are the 
     * same then traces the format inheritance tree.
     */
    public boolean isValidInputTo(FileFormatDef target) {

        if (this == target) {
            return true;
        } else {
            FileFormatDef parentTrace = getParent();
            while (parentTrace != null) {
                if (target == parentTrace) {
                    return true;
                }
                parentTrace = parentTrace.getParent();
            }
            return false;
        }
    }

    /**
     * Returns the name of this file format definition
     */
    public String getName() {
        return name;
    }

    /**
     * Returns true if this format is argument-type (not actually a file)
     */
    public boolean isArg() {
        if (name.equals("arg")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the color in which data in this format is rendered in the GUI
     */
    public Color getColor() {
        return color;
    }

    /**
     * Returns the file format suffix attached to files of this format
     */
    public String getFormatSuffix() {
        return suffix;
    }

    /**
     * Returns the parent (super class) file format definition of this format
     */
    public FileFormatDef getParent() {
        return parent;
    }

    /**
     * Returns a text description of the current state of this file format definition
     */
    public String toString() {
        return name + " (" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + ")";
    }

    /**
     * Overrides compareTo so this class implements Comparable
     */
    @Override
    public int compareTo(FileFormatDef other) {
        return getName().compareTo(other.getName());
    }
}
