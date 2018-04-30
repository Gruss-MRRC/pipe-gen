/*
    Program:  AnalysisWorker.java
    Author:   Michael Stockman
              Albert Einstein College of Medicine

    Purpose:  This class extends SwingWorker for running MakefileAnalysis.

              Represents a worker that runs a makefile analysis described by an 
              instance of MakefileAnalysis in the background of the GUI.

 */

package pipegen;

import java.awt.*;
import javax.swing.*;


/**
 * Represents a worker that runs a makefile analysis described by an instance of
   MakefileAnalysis in the background of the GUI
 */
public class AnalysisWorker extends SwingWorker<Void,Void> {

    // Class fields
    private final MakefileAnalysis analysis;
    private int exitCode;

    public AnalysisWorker(MakefileAnalysis analysis) {
        this.analysis = analysis;
        exitCode = 1;
    }

    /**
     * Starts this worker running the analysis in the background
     */
    @Override
    public Void doInBackground() {
        exitCode = analysis.run();
        return null;
    }

    /**
     * Gives the exit code produced from running analysis
     */
    public int getExitCode() {
        return exitCode;
    }

    /**
     * Returns true if the file-stream stderr was empty during analysis
     */
    public boolean errorStreamEmpty() {
        return analysis.errorStreamEmpty();
    }
}
