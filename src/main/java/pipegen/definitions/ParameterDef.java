/*
    Program:  ParameterDef.java
    Author:   Michael Stockman
              Albert Einstein College of Medicine

    Purpose:  This class represents a parameter definition. It provides 
              mechanisms to load/save parameters to disk and some accessor 
              methods.

              Parameters are the input and output points on pipeline elements: 
              sources, sinks, and modules. See ModuleDef. Parameters within 
              pipe-gen are somewhat analogous to programming variables.

 */

package pipegen.definitions;

import java.io.*;
import java.awt.*;
import org.json.*;

import pipegen.exceptions.*;


public class ParameterDef {
    
    private String name;
    private FileFormatDef fileFormat;
    private boolean required;

    public ParameterDef(String name, FileFormatDef fileFormat, boolean required) {
        this.name = name;
        this.fileFormat = fileFormat;
        this.required = required;
    }

    /**
     * Loads this of parameter from file (JSONObject)
     */
    public static ParameterDef load(JSONObject parameterObject, FileFormatDef[] formats) throws InvalidParameterDefException {

        try {
            String name = parameterObject.getString("name");
            String format = parameterObject.getString("format");
            boolean required = parameterObject.getBoolean("required");

            int j = 0;
            while (j < formats.length) {
                if (formats[j].getName().equals(format)) {
                    break;
                }
                j++;
            }

            return new ParameterDef(name, formats[j], required);

        } catch (Exception e) {
            e.printStackTrace();
            throw new InvalidParameterDefException(e);
        }
    }

    /**
     * This static method loads an array of parameters from file (JSONArray)
     */
    public static ParameterDef[] load(JSONArray array, FileFormatDef[] formats) throws InvalidParameterDefException {

        int len = array.length();
        ParameterDef[] out = new ParameterDef[len];

        for (int i=0; i < len; i++) {
            try {
                JSONObject currParameter = array.getJSONObject(i);
                String currName = currParameter.getString("name");
                String currFormat = currParameter.getString("format");
                boolean currRequired = currParameter.getBoolean("required");

                int j = 0;
                while (j < formats.length) {
                    if (formats[j].getName().equals(currFormat)) {
                        break;
                    }
                    j++;
                }

                out[i] = new ParameterDef(currName, formats[j], currRequired);
            } catch (JSONException e) {
                e.printStackTrace();
                throw new InvalidParameterDefException(e);
            }
        }

        return out;
    }

    /**
     * Saves this parameter to disk
     */
    /*public void save() {
        return;
    }*/

    /**
     * Returns the name of this parameter
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the FileFormat associate with this parameter
     */
    public FileFormatDef getFileFormat() {
        return fileFormat;
    }

    /**
     * Returns the color that this parameter should be drawn with in the GUI
     */
    public Color getColor() {
        return fileFormat.getColor();
    }

    /**
     * Returns the file suffix associated with this parameter
     */
    public String getFormatSuffix() {
        return fileFormat.getFormatSuffix();
    }

    /**
     * Returns true if this parameter is a required input/output for a pipeline 
     * element.
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Returns a text description of the current state of this parameter 
     * definition.
     */
    public String toString() {
        return name + "-" + fileFormat;
    }

    /**
     * Returns true if this parameter is argument-type. These are a sort of 
     * virtual file that contains a command or snippet of text passed between 
     * programs in a pipeline.
     */
    public boolean isArg() {
        return fileFormat.isArg();  
    }
}

