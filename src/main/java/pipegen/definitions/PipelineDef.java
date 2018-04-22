/*
    Program:  PipelineDef.java
    Author:   Michael Stokman
              Albert Einstein College of Medicine

    Purpose:  This class represents a pipeline definition. It provides 
              mechanisms to load/save pipelines to disk and some accessor 
              methods.

              Pipeline definitions and pipeline instances are poorly 
              differentiated. Possibly this class should be removed and its 
              functionality merged into PipelineInstance.java.

 */

package pipegen.definitions;

import java.io.*;
import java.util.*;
import java.awt.*;

import org.json.*;

import pipegen.exceptions.*;
import pipegen.instances.*;

public class PipelineDef {
    
    private ToolboxDef toolbox;
    private File file;

    public PipelineDef(ToolboxDef toolbox, File file) {
        this.toolbox = toolbox;
        this.file = file;
    }

    /**
     * Returns the toolbox associated with this pipeline definition
     */
    public ToolboxDef getToolbox() {
        return toolbox;
    }

    /**
     * Returns the file this pipeline definition is saved in
     */
    public File getFile() {
        return file;
    }

    public File getSaveFile(String name) {
        return new File(toolbox.getDir() + "/pipelines/" + name);
    }

    public void setFileName(String name) {
        file = getSaveFile(name);
    }
}
