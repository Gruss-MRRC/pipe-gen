/*
    Program:  ModuleDef.java
    Author:   Michael Stokman
              Albert Einstein College of Medicine

    Purpose:  This class represents a module definition. It provides 
              mechanisms to load/save parameters to disk and some accessor 
              methods.

              Modules are a consituent element of pipelines. See PipelineDef.

 */

package pipegen.definitions;

import java.io.*;
import java.util.*;
import org.json.*;

import pipegen.exceptions.*;


public class ModuleDef implements Comparable<ModuleDef> {
    
    private String name;
    private ParameterDef[] inputs;
    private ParameterDef[] outputs;
    private String enclosedCommand;

    public ModuleDef(String name, ParameterDef[] inputs, ParameterDef[] outputs, String enclosedCommand) {
        this.name = name;
        this.inputs = inputs;
        this.outputs = outputs;
        this.enclosedCommand = enclosedCommand;
    }

    private static ModuleDef factory(String jsonString, FileFormatDef[] formats) throws InvalidModuleDefException {

        try {

            JSONObject rootObject = new JSONObject(jsonString);
            String name = rootObject.getString("moduleName");
            String enclosedCommand = rootObject.getString("enclosedCommand");
            JSONArray inputArray = rootObject.getJSONArray("inputs");
            ParameterDef[] inputs = ParameterDef.load(inputArray, formats);
            JSONArray outputArray = rootObject.getJSONArray("outputs");
            ParameterDef[] outputs = ParameterDef.load(outputArray, formats);

            ModuleDef out = new ModuleDef(name, inputs, outputs, enclosedCommand);
            return out;
        } catch (JSONException e) {
            e.printStackTrace();
            throw new InvalidModuleDefException();
        } catch (InvalidParameterDefException e) {
            throw new InvalidModuleDefException(e);
        }

    }

    /**
     * This static method loads an array of modules from disk
     */
    public static ModuleDef[] load(File moduleDir, FileFormatDef[] formats) throws InvalidModuleDefException {

        File[] moduleFiles = moduleDir.listFiles();
        List<ModuleDef> moduleList = new ArrayList<ModuleDef>();

        for (int i=0; i < moduleFiles.length; i++) {

            // Ignore any backup copies ending with ~
            if (moduleFiles[i].getName().endsWith("~")) {
                continue;
            }

            // Read current module into ModuleDef and add to List
            try {
                String jsonString = ToolboxDef.readTextfile(moduleFiles[i]);
                if (jsonString == null) {
                    throw new InvalidModuleDefException();
                }

                ModuleDef currModule = ModuleDef.factory(jsonString, formats);
                if (currModule != null) {
                    moduleList.add(currModule);
                }

            } catch (InvalidAboutFileException e) {
                throw new InvalidModuleDefException(e);
            }
        }

        return moduleList.toArray(new ModuleDef[moduleList.size()]);
    }

    /**
     * Saves this module to disk
     */
    /*public void save() {
        return;
    }*/

    /**
     * Returns the name of this module
     */
    public String getName() {
        return name;
    }

    /**
     * Returns an array of inputs used by this module
     */
    public ParameterDef[] getInputs() {
        return inputs;
    }

    /**
     * Returns an array of outputs produced by this module
     */
    public ParameterDef[] getOutputs() {
        return outputs;
    }

    /**
     * Returns the command this module calls
     */
    public String getEnclosedCommand() {
        return enclosedCommand;
    }

    /**
     * Returns a text description of the current state of this module
     */
    public String toString() {

        String command = enclosedCommand;

        // Color code inputs and replace braces {} with <> angle brackets for
        // required inputs and [] square brackets for optional inputs. 
        for (int i=0; i < inputs.length; i++) {
            String str = inputs[i].getName();
            if (inputs[i].isRequired()) {
                command = command.replaceFirst("\\{" + str + "\\}", "<\u001B[32m" + str + "\u001B[0m>");
            } else {
                command = command.replaceFirst("\\{" + str + "\\}", "[\u001B[32m" + str + "\u001B[0m]");
            }
        }

        // Color code outputs and replace braces {} with <> angle brackets for
        // required outputs and [] square brackets for optional outputs. 
        for (int i=0; i < outputs.length; i++) {
            String str = outputs[i].getName();
            if (outputs[i].isRequired()) {
                command = command.replaceFirst("\\{" + str + "\\}", "<\u001B[34m" + str + "\u001B[0m>");
            } else {
                command = command.replaceFirst("\\{" + str + "\\}", "[\u001B[34m" + str + "\u001B[0m]");
            }
        }

        String out = name + " '" + command + "'";
        return out;
    }

    /**
     * Overrides compareTo so this class implements Comparable
     */
    @Override
	public int compareTo(ModuleDef compareModule) {
		return name.compareTo(compareModule.getName());
	}
}
