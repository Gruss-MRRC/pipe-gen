/*
    Program:  MakefileAnalysis.java
    Author:   Michael Stokman
              Albert Einstein College of Medicine

    Purpose:  This class represents an analysis to conduct using a makefile.
              The makefile is created using the class MakefileFactory. This 
              class in turn runs the analysis and monitors the results.

 */

package edu.einstein.gmrrc.pipegen;

import java.io.*;
import java.util.*;
import java.nio.charset.*;
import java.nio.file.*;

import org.json.*;

import edu.einstein.gmrrc.pipegen.exceptions.*;
import edu.einstein.gmrrc.pipegen.instances.*;
import edu.einstein.gmrrc.pipegen.definitions.*;


public class MakefileAnalysis {

    private static final String STDOUT_FILE = "STDOUT.txt";
    private static final String STDERR_FILE = "STDERR.txt";
    private static final String ERROR_RATIO = "[Error]";

    private String name;
    private String makeCommand;
    private DataTableFile table;
    private PipelineInstance pipeline;

    private File makefileDir;
    private File makefile;
    private MakefileFactory factory;

    public MakefileAnalysis(File analysisFile) {

    }

    public MakefileAnalysis(String name, DataTableFile table, PipelineInstance pipeline) throws InvalidMakefileException {

        this.name = name;
        this.table = table;
        this.pipeline = pipeline;

        makefileDir = new File(pipeline.getAnalysisPath(name));
        makefile = new File(makefileDir.getAbsolutePath() + "/Makefile");

        factory = new MakefileFactory(makefile, table, pipeline);
        factory.composeAndWrite();
    }

    /**
     * Runs this makefile to produce targeted files. First the target 'setupall'
     * is run to produce the necessary directory structure and clear the error 
     * logs. Next the 'all' target is run to produce targeted outputs.
     */
    public int run() {
        try {

            Process p = runAndWait("make -f Makefile setupall");
            if (p.exitValue() != 0) {
                return p.exitValue();
            }

            p = runAndWait(makeCommand + " 2> " + STDERR_FILE + " 1> " + STDOUT_FILE);
            return p.exitValue();
        } catch (IOException e) {
            System.err.println("MakefileAnalysis.java - run() found IOException");
        } catch (InterruptedException e) {
            System.err.println("MakefileAnalysis.java - run() found InterruptedException");
        }
        return 1;
    }

    /**
     * Runs the 'cleanall' target for this makefile. This deletes all 
     * intermediate and target files produced by the makefile.
     */
    public int clean() {
        System.err.println("MakefileAnalysis.java clean()");

        try {
            Process p = runAndWait("make -f Makefile cleanall");
            return p.exitValue();
        } catch (IOException e) {
            System.err.println("MakefileAnalysis.java - clean() found IOException");
        } catch (InterruptedException e) {
            System.err.println("MakefileAnalysis.java - clean() found InterruptedException");
        }
        return 1;
    }

    /**
     * Loads and returns a makefile analysis previously saved into a .json file
     */
    public void load(JSONObject json) throws InvalidMakefileException {

        try {
            String toolboxString = json.getString("toolbox");
            String analysisString = json.getString("analysis");
            String pipelineName = json.getString("pipeline");
            String tablePath = json.getString("tablePath");

            System.out.println("toolbox = " + toolboxString);
            System.out.println("analysis = " + analysisString);
            System.out.println("pipeline = " + pipelineName);
            System.out.println("tablePath = " + tablePath);
        } catch (JSONException e) {
            throw new InvalidMakefileException(e);
        }

        // *** Need to finish loading the data
        
    }

    /**
     * Saves a makefile analysis into a JSON object
     */
    public JSONObject save() throws JSONException {
        JSONObject outputJSON = new JSONObject();

        outputJSON.put("toolbox", pipeline.getToolbox().getName());
        outputJSON.put("analysis", name);
        outputJSON.put("pipeline", pipeline.getName());
        outputJSON.put("tablePath", table.getPath());

        return outputJSON;
    }

    /**
     * Returns the current fraction of target files that should be produced by 
     * this analysis by querrying the makefile.
     */
    public String getTargetRatio() {

        try {
            Process p = runAndWait("make -f Makefile target_ratio");
            String stdout = readStdout(p);
            if (stdout == null) {
                return ERROR_RATIO;
            } else {
                return stdout;
            }
        } catch (IOException e) {
            System.err.println("MakefileAnalysis.java - run() found IOException");
        } catch (InterruptedException e) {
            System.err.println("MakefileAnalysis.java - run() found InterruptedException");
        }
        return ERROR_RATIO;
    }

    /**
     * Returns the current fraction of intermediate files that should be 
     * produced by this analysis by querrying the makefile.
     */
    public String getIntermediateRatio() {

        try {
            Process p = runAndWait("make -f Makefile intermediate_ratio");
            String stdout = readStdout(p);
            if (stdout == null) {
                return ERROR_RATIO;
            } else {
                return stdout;
            }
        } catch (IOException e) {
            System.err.println("MakefileAnalysis.java - getIntermediateRatio() found IOException");
        } catch (InterruptedException e) {
            System.err.println("MakefileAnalysis.java - getIntermediateRatio() found InterruptedException");
        }
        return ERROR_RATIO;
    }

    /**
     * Runs a command on the system and waits for it to complete then returns 
     * the executing Process.
     */
    private Process runAndWait(String command) throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", command);
        pb.directory(makefileDir);
        Process p = pb.start();
        p.waitFor();
        return p;
    }

    /**
     * Returns the stdout stream from an executed Process
     */
    private String readStdout(Process p) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = reader.readLine();
        reader.close();
        return line;
    }

    /**
     * Returns true if this analysis has completed execution
     */
    public boolean isComplete() {

        // Check that the complete set of target files are found
        String targetRatio = getTargetRatio();
        if (targetRatio.equals(ERROR_RATIO)) {
            return false;
        }

        String[] tokens = targetRatio.split(" / ");
        if (tokens.length != 2) {
            return false;
        }

        int numerator = Integer.parseInt(tokens[0]);
        int denomenator = Integer.parseInt(tokens[1]);
        if (numerator != denomenator) {
            return false;
        }

        // Check that the complete set of intermediate files are found
        String intermediateRatio = getIntermediateRatio();
        if (intermediateRatio.equals(ERROR_RATIO)) {
            return false;
        }

        tokens = targetRatio.split(" / ");
        if (tokens.length != 2) {
            return false;
        }

        numerator = Integer.parseInt(tokens[0]);
        denomenator = Integer.parseInt(tokens[1]);
        if (numerator != denomenator) {
            return false;
        }

        return true;
    }

    /**
     * Returns name of this analysis
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the make command used to run this makefile
     */
    public void setMakeCommand(String makeCommand) {
        this.makeCommand = makeCommand;
    }

    /**
     * Returns the text contents of the makefile
     */
    public String getMakefileText() {
        return factory.getMakefileText();
    }

    /**
     * Returns the stdout stream from running this makefile analysis
     */
    public String getStdout() {
        return getFileText(STDOUT_FILE);
    }

    /**
     * Returns the error trees representing portions of the makefile dependency
     * tree that could not be produced from running this makefile analysis.
     */
    public String getErrorTrees() {

        String tempText = "";

        File errorLogs = new File(makefileDir.toString() + "/ERROR_LOGS/");
        for (File subjectDir : errorLogs.listFiles()) {
            if (! subjectDir.isDirectory()) {
                continue;
            }
            for (File moduleDir : subjectDir.listFiles()) {
                if (! moduleDir.isDirectory()) {
                    continue;
                }
                try {
                    String subjectID = subjectDir.getName();
                    int moduleID = Integer.parseInt(moduleDir.getName());

                    ArrayList<ModuleElement> modules = pipeline.getModules();
                    for (ModuleElement module : modules) {
                        if (module.getID() == moduleID) {
                            tempText += buildErrorTree(module, subjectID);
                            tempText += "\n";
                        }
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    System.err.println("NumberFormatException in MakefileAnalysis.java getErrorTrees()");
                    continue;
                }
            }
        }

        return tempText;
    }

    /**
     * Returns the error tree for a single module
     */
    private String buildErrorTree(ModuleElement module, String id) {

        String tempText = "";
        tempText += "<span class=\"redbold\">id " + id + " at module " + module.getName() + "</span>\n";

        DataTableFile table = this.table;
        ErrorTreeVisitor errorTree = new ErrorTreeVisitor(table, id);
        errorTree.visit(module);
        tempText += errorTree.getText();

        return tempText;
    }

    /**
     * Returns the stderr stream from running this makefile analysis
     */
    public String getStderr() {
        return getFileText(STDERR_FILE);
    }

    /**
     * Returns the contents of a text file
     */
    public String getFileText(String fileName) {
        String path = makefileDir.toString() + "/" + fileName;        
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            Charset encoding = Charset.defaultCharset();
            String out = new String(encoded, encoding);
            System.out.println(out);
            return out;
        } catch (IOException e) {
            return "ERROR: Could not read path " + path;
        }
    }

    /**
     * Returns true if the file-stream stderr was empty during analysis
     */
    public boolean errorStreamEmpty() {
        try {
            String pathToErrors = makefileDir.toString() + "/" + STDERR_FILE;
            BufferedReader br = new BufferedReader(new FileReader(pathToErrors));     
            if (br.readLine() == null) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error in MakefileAnalysis.java function errorStreamEmpty(). Could not read error stream file.");
            System.exit(1);
        }
        return false;
    }
}
