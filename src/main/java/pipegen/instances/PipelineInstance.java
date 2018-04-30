/*
    Program:  PipelineInstance.java
    Author:   Michael Stockman
              Albert Einstein College of Medicine

 */

package pipegen.instances;

import java.io.*;
import java.util.*;
import java.awt.*;
import org.json.*;

import pipegen.*;
import pipegen.exceptions.*;
import pipegen.definitions.*;

/**
 * Represents a file processing pipeline for pipe-gen
 */
public class PipelineInstance {

    private PipelineDef definition;

    private String name;
    private ArrayList<SourceElement> sources;
    private ArrayList<SinkElement> sinks;
    private ArrayList<ModuleElement> modules;
    private ArrayList<ConnectionElement> connections;

    private boolean areGraphicsSet;
    private Graphics2D g2d;

    private boolean hasUnsavedChanges;

    /**
     * Constructs a new empty representation of a pipeline from a PipelineDef
     */
    public PipelineInstance(PipelineDef definition) {
        this.definition = definition;
        areGraphicsSet = false;

        hasUnsavedChanges = true;
    }

    /**
     * Loads and returns a pipeline object previously saved into a .json file
     */
    public void load() throws InvalidWorkflowDefException {

        String jsonString = null;
        try {
            jsonString = ToolboxDef.readTextfile(definition.getFile());
        } catch (InvalidAboutFileException e) {
            throw new InvalidWorkflowDefException();
        }

        if (jsonString == null) {
            throw new InvalidWorkflowDefException();
        }

        ToolboxDef toolbox = definition.getToolbox();
        try {
            JSONObject rootObject = new JSONObject(jsonString);

            name = rootObject.getString("workflowName");

            // Load sources
            JSONArray sourceArray = rootObject.getJSONArray("sources");
            SourceElement[] sourcesTemp = SourceElement.load(sourceArray, toolbox.getFormats());
            sources = new ArrayList<SourceElement>(Arrays.asList(sourcesTemp));

            // Load sinks
            JSONArray sinkArray = rootObject.getJSONArray("sinks");
            SinkElement[] sinksTemp = SinkElement.load(sinkArray, toolbox.getFormats());
            sinks = new ArrayList<SinkElement>(Arrays.asList(sinksTemp));

            // Load modules
            JSONArray moduleArray = rootObject.getJSONArray("modules");
            ModuleElement[] modulesTemp = ModuleElement.load(moduleArray, toolbox.getModules());
            modules = new ArrayList<ModuleElement>(Arrays.asList(modulesTemp));

            // Load connections
            JSONArray connectionArray = rootObject.getJSONArray("connections");
            ConnectionElement[] connectionsTemp = ConnectionElement.load(connectionArray, sourcesTemp, sinksTemp, modulesTemp);
            connections = new ArrayList<ConnectionElement>(Arrays.asList(connectionsTemp));

        } catch (JSONException e) {
            e.printStackTrace();
            throw new InvalidWorkflowDefException();
        } catch (InvalidSourceDefException e) {
            e.printStackTrace();
            throw new InvalidWorkflowDefException();
        } catch (InvalidSinkDefException e) {
            e.printStackTrace();
            throw new InvalidWorkflowDefException();
        } catch (InvalidModuleDefException e) {
            e.printStackTrace();
            throw new InvalidWorkflowDefException();
        } catch (InvalidConnectionDefException e) {
            e.printStackTrace();
            throw new InvalidWorkflowDefException();
        }

        hasUnsavedChanges = false;
    }

    public JSONObject save() throws JSONException {
        JSONObject outputJSON = new JSONObject();

        // Save pipeline/workflow name
        outputJSON.put("workflowName", name);

        // Save sources
        JSONArray sourcesJSON = SourceElement.save(sources);
        outputJSON.put("sources", sourcesJSON);

        // Save sinks
        JSONArray sinksJSON = SinkElement.save(sinks);
        outputJSON.put("sinks", sinksJSON);

        // Save modules
        JSONArray modulesJSON = ModuleElement.save(modules);
        outputJSON.put("modules", modulesJSON);

        // Save connections
        JSONArray connectionsJSON = ConnectionElement.save(connections);
        outputJSON.put("connections", connectionsJSON);

        hasUnsavedChanges = false;

        return outputJSON;
    }

    public PipelineDef getDefinition() {
        return definition;
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return definition.getFile();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFile(String name) {
        definition.setFileName(name);
        setName(name);
    }

    public File getSaveFile(String name) {
        return definition.getSaveFile(name);
    }

    /**
     * Returns all sources that are part of this pipeline
     */
    public ArrayList<SourceElement> getSources() {
        //return sources.toArray(new SourceElement[sources.size()]);
        return sources;
    }

    /**
     * Returns all sinks that are part of this pipeline
     */
    public ArrayList<SinkElement> getSinks() {
        return sinks;
    }

    /**
     * Returns all modules that are part of this pipeline
     */
    public ArrayList<ModuleElement> getModules() {
        return modules;
    }

    /**
     * Returns all connections that are part of this pipeline
     */
    public ArrayList<ConnectionElement> getConnections() {
        return connections;
    }

    public String[] getMissingData(DataTableFile data) {

        java.util.List<String> headers = Arrays.asList(data.getHeaders());
        java.util.List<String> missing = new ArrayList<String>();
        java.util.List<String> sourceFields = SourceElement.getFields(sources);
        java.util.List<String> sinkFields = SinkElement.getFields(sinks);

        for (String source : sourceFields) {
            if (! headers.contains(source)) {
                missing.add(source);
            }
        }
        for (String sink : sinkFields) {
            if (! headers.contains(sink)) {
                missing.add(sink);
            }
        }

        if (missing.size() == 0) {
            return null;
        } else {
            return missing.toArray(new String[missing.size()]);
        }
    }

    public String[] getUnusedData(DataTableFile data) {
        java.util.List<String> headers = Arrays.asList(data.getHeaders());
        java.util.List<String> unused = new ArrayList<String>();
        java.util.List<String> sourceFields = SourceElement.getFields(sources);
        java.util.List<String> sinkFields = SinkElement.getFields(sinks);

        for (String field : headers) {

            if (! field.equals("id") && ! DataTableFile.isMakeVariable(field) && ! sourceFields.contains(field) && ! sinkFields.contains(field)) {
                unused.add(field);
            }
        }

        if (unused.size() == 0) {
            return null;
        } else {
            return unused.toArray(new String[unused.size()]);
        }
    }

    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }

    public void deleteSource(SourceElement source) {

        ArrayList<ConnectionElement> toDelete = new ArrayList<ConnectionElement>(source.getConnections());
        if (toDelete != null) {
            for (ConnectionElement connection : toDelete) {
                connection.clear();
                connections.remove(connection);
            }
        }
        sources.remove(source);

        hasUnsavedChanges = true;
    }

    public void deleteSink(SinkElement sink) {

        ArrayList<ConnectionElement> toDelete = new ArrayList<ConnectionElement>(sink.getConnections());
        if (toDelete != null) {
            for (ConnectionElement connection : toDelete) {
                connection.clear();
                connections.remove(connection);
            }
        }
        sinks.remove(sink);

        hasUnsavedChanges = true;
    }

    public void deleteModule(ModuleElement module) {

        ArrayList<ConnectionElement> toDelete = new ArrayList<ConnectionElement>(module.getConnections());
        if (toDelete != null) {
            for (ConnectionElement connection : toDelete) {
                connection.clear();
                connections.remove(connection);
            }
        }
        modules.remove(module);

        hasUnsavedChanges = true;
    }

    public void addConnection(ConnectionElement e) {
        connections.add(e);

        hasUnsavedChanges = true;
    }

    public ToolboxDef getToolbox() {
        return definition.getToolbox();
    }

    public String getAnalysisPath(String analysisName) {
        
        return getToolbox().getDir().toString() + "/data/" + analysisName + "/";
    }

    /**
     * Creates a string that represents this pipeline object 
     */
    public String toString() {

        // Get the toolbox name and the pipeline name
        String out = "name: " + definition.getToolbox().getName() + "." + name + " ";

        // Get a representation of all the source elements
        out += "\n    sources:";
        if (sources != null) {
            for (int i=0; i < sources.size(); i++) {
                out += " " + sources.get(i);
            }
        }

        // Get a representation of all the sink elements
        out += "\n    sinks:";
        if (sinks != null) {
            for (int i=0; i < sinks.size(); i++) {
                out += " " + sinks.get(i);
            }
        }

        // Get a representation of all the module elements
        out += "\n    modules:";
        if (modules != null) {
            for (int i=0; i < modules.size(); i++) {
                out += " " + modules.get(i);
            }
        }

        // Get a representation of all the connections between elements
        out += "\n    connections:";
        /*if (connections != null) {
            for (int i=0; i < connections.length; i++) {
                out += "\n        " + connections[i];
            }
        }*/

        /*for (int i=0; i < connections.size(); i++) {
            out += "\n        " + connections.get(i);
        }*/

        for (ConnectionElement connection : connections) {
            out += "\n        " + connection;
        }

        out += "\n\n";

        return out;
    }

    /**
     * Sets up the graphics. This allows pre-computation of some values to make
     * using draw() more efficient 
     */
    public void setGraphics(Graphics2D g2d) {
        this.g2d = g2d;
        for (ConnectionElement e : connections) {
            e.setGraphics(g2d);
        }
        for (SourceElement e : sources) {
            e.setGraphics(g2d);
        }
        for (ModuleElement e : modules) {
            e.setGraphics(g2d);
        }
        for (SinkElement e : sinks) {
            e.setGraphics(g2d);
        }
        areGraphicsSet = true;
    }

    /**
     * Returns true if the graphics were previously set
     */
    public boolean areGraphicsSet() {
        return areGraphicsSet;
    }

    /**
     * Draws a graphic depiction of this pipeline
     */
    public void draw(Graphics2D g2d) {

        for (ConnectionElement e : connections) {
            e.draw(g2d);
        }
        for (SourceElement e : sources) {
            e.draw(g2d);
        }
        for (ModuleElement e : modules) {
            e.draw(g2d);
        }
        for (SinkElement e : sinks) {
            e.draw(g2d);
        }
    }
}
