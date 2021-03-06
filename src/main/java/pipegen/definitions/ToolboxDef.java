/*
    Program:  ToolboxDef.java
    Author:   Michael Stockman
              Albert Einstein College of Medicine

    Purpose:  This class represents a toolbox definition. It provides mechanisms
              to load/save toolboxes to disk and some accessor methods.

 */

package pipegen.definitions;

import java.io.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import pipegen.exceptions.*;


public class ToolboxDef {

    private static final String formatFileName = "config/formats.json";
    private static final String modulesDirName = "modules/";
    //private static final String workflowsDirName = "workflows/";
    private static final String aboutFileName = "config/ABOUT.txt";

    private File dir;
    private String name;
    private FileFormatDef[] formats;
    private ModuleDef[] modules;
    //private PipelineDef[] workflows;
    

    private String about = "";
    
    public ToolboxDef(File dir) {
        this.dir = dir;
        this.name = dir.getName();
    }

    /**
     * Loads this toolbox from disk
     */
    public void load() throws InvalidFileFormatDefException, InvalidModuleDefException, InvalidWorkflowDefException, InvalidAboutFileException {

        // Load all file format definitions
        File formatFile = new File(dir.getAbsolutePath() + "/" + formatFileName);
        formats = FileFormatDef.load(formatFile);

        // Load all modules
        File modulesDir = new File(dir.getAbsolutePath() + "/" + modulesDirName);
        modules = ModuleDef.load(modulesDir, formats);

        // Load all workflows
        //File workflowsDir = new File(dir.getAbsolutePath() + "/" + workflowsDirName);
        //workflows = PipelineDef.load(this, workflowsDir);

        // Load ABOUT.txt
        File aboutFile = new File(dir.getAbsolutePath() + "/" + aboutFileName);
        about = readTextfile(aboutFile);
    }

    /**
     * Saves this toolbox to disk
     */
    /*public void save() {
        return;
    }*/

    /**
     * Returns the name of this toolbox
     */
    public String getName() {
        return name;
    }

    /**
     * Returns an array of all modules defined in this toolbox
     */
    public ModuleDef[] getModules() {
        return modules;
    }

    /**
     * Returns an array of all file formats defined in this toolbox
     */
    public FileFormatDef[] getFormats() {
        return formats;
    }

    /**
     * Returns an array of workflows/pipelines stored in this toolbox
     */
    /*public WorkflowDef[] getWorkflows() {
        return workflows;
    }*/

    /**
     * Returns the contents of this toolbox's About file 
     */
    public String about() {
        return about;
    }

    /**
     * Returns a text description of the state of this ToolboxDef
     */
    public String toString() {

        String out = "";

        // Formats
        out += "FORMATS:\n";
        if (formats != null) {
            for (int i=0; i < formats.length; i++) {
                out += "[" + i + "] " + formats[i].toString() + "\n";
            }
        }
        out += "\n";

        // Modules
        out += "MODULES:\n";
        if (modules != null) {
            for (int i=0; i < modules.length; i++) {
                out += "[" + i + "] " + modules[i].toString() + "\n";
            }
        }
        out += "\n";

        // Workflows
        /*out += "WORKFLOWS:\n";
        if (workflows != null) {
            for (int i=0; i < workflows.length; i++) {
                out += "[" + i + "] " + workflows[i].toString() + "\n";
            }
        }
        out += "\n";*/

        // About
        out += "ABOUT:\n" + about;

        return out;
    }

    /**
     * Returns the directory associated with this toolbox
     */
    public File getDir() {
        return dir;
    }

    /**
     * Reads a text file using a BufferedReader and returns the contents in a
     * String.
     */
    public static String readTextfile(File textfile) throws InvalidAboutFileException {

	    BufferedReader br = null;
	    String line = "";
        StringBuilder sb = new StringBuilder();
 
	    try {
		    br = new BufferedReader(new FileReader(textfile));
		    while ((line = br.readLine()) != null) {
                sb.append(line);
		    }
	    } catch (FileNotFoundException e) {
		    e.printStackTrace();
            throw new InvalidAboutFileException();
	    } catch (IOException e) {
		    e.printStackTrace();
            throw new InvalidAboutFileException();
	    } finally {
		    if (br != null) {
		    	try {
		    		br.close();
		    	} catch (IOException e) {
		            e.printStackTrace();
                    throw new InvalidAboutFileException();
		    	}
		    }
	    }
        return sb.toString();
    }
}
