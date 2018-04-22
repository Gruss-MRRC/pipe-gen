/*
    Program:  StartPipegen.java
    Author:   Michael Stokman
              Albert Einstein College of Medicine

    Purpose:  This the main executable file for pipe-gen.

              Build workflows for data analysis. Efficiently apply a set of 
              operations to a dataflow of inputs. Pipe-gen auto-generates 
              makefiles based on this confluence of process and data.

    Usage:    Usage: java -cp '<classPath>' StartPipegen

 */

package pipegen;

import java.io.*;

import pipegen.*;
import pipegen.exceptions.*;
import pipegen.instances.*;
import pipegen.definitions.*;
import pipegen.gui.*;


class StartPipegen {

	private static final File TOOLBOX_DIR = new File("./toolboxes/");
	//private static final File PIPELINE_FILE = new File("/home/mstockma/pipe-gen/toolboxes/sampleToolbox/workflows/workflow_copyFiles.json");

    public static void main(String[] argv) {

        // Check argument count
        if (argv.length != 0) {
            printUsage();
            System.exit(1);
        }

        // Assign inputs
        //File makefileName = new File(argv[2] + "/makefiles/" + argv[0]);
        //File outputDir = new File(argv[1]);
        //File toolBoxDir = new File(argv[2]);
        //File workflowFile = new File(argv[2] + "/workflows/" + argv[3]);  //*** Should just be a name 
                                                                          //that can be retrieved from
                                                                          //the toolBox
        //DataTableFile inputTable = new DataTableFile(argv[4]);

        // Check inputs
        /*if (!outputDir.isDirectory()) {
            System.err.println("Error: Pipegen could not find output directory. Exit with error.");
            System.exit(1);
        }
        if (!toolBoxDir.isDirectory()) {
            System.err.println("Error: Pipegen could not find toolbox directory. Exit with error.");
            System.exit(1);
        }
        if (!workflowFile.isFile()) {
            System.err.println("Error: Pipegen could not find workflow file. Exit with error.");
            System.exit(1);
        }
        if (!inputTable.isFile()) {
            System.err.println("Error: Pipegen could not find data file. Exit with error.");
            System.exit(1);
        }

        // *** Print inputs
        /*System.out.println("-------------------------------------------------");
        System.out.println("makefileName = " + makefileName);
        System.out.println("outputDir = " + outputDir);
        System.out.println("toolBoxDir = " + toolBoxDir);
        System.out.println("workflowFile = " + workflowFile);
        System.out.println("inputTable = " + inputTable.getName());
        System.out.println("-------------------------------------------------");*/

        // *** Read the inputTable and print to see that it is working
        /*try {
            //inputTable.loadData();
            /*System.out.println();
            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.print(inputTable);
            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++");*/
        /*} catch (InvalidCSVFileException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // *** Test the existence of Toolbox
        //System.out.println("toolBoxDir = " + toolBoxDir);
        //System.exit(1);
        //ToolboxDef toolbox = null;
        try {
            //toolbox = new ToolboxDef(toolBoxDir);
            //toolbox.load();
            /*System.out.println();
            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println(toolbox);
            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++");*/
        /*} catch (InvalidFileFormatDefException e) {
            System.err.println("ERROR: Toolbox is unusable due to invalid file format definitions.");
            e.printStackTrace();
            System.exit(1);
        } catch (InvalidModuleDefException e) {
            System.err.println("ERROR: Toolbox is unusable due to invalid module definition.");
            e.printStackTrace();
            System.exit(1);
        } catch (InvalidWorkflowDefException e) {
            System.err.println("ERROR: Toolbox is unusable due to invalid workflow definition.");
            e.printStackTrace();
            System.exit(1);
        } catch (InvalidAboutFileException e) {
            System.err.println("ERROR: Toolbox is unusable due to invalid about file.");
            e.printStackTrace();
            System.exit(1);
        }

        try {

            //PipelineDef pipelineDef = new PipelineDef(toolbox, PIPELINE_FILE);
            //PipelineInstance pipeline = new PipelineInstance(pipelineDef);
            //pipeline.load();

            /*System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.print(pipeline);
            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++");*/

            //MakefileAnalysis makefile = new MakefileAnalysis("StartPipegen", toolBoxDir, outputDir, outputDir, inputTable, pipeline);

            //makefile.writeTo(makefileName);
            /*System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println(makefile);
            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++");*/
        /*} catch (InvalidWorkflowDefException e) {
            System.err.println("ERROR: Invalid pipeline definition.");
            e.printStackTrace();
            System.exit(1);
        } catch (InvalidMakefileException e) {
            System.err.println("ERROR: Unable to generate makefile.");
            e.printStackTrace();
            System.exit(1);
        }*/

        new PipegenGUI(TOOLBOX_DIR);
    }

    private static void printUsage() {
        // Prints explaination of correct usage for this program

        System.err.println();
        System.err.println("Purpose: ");
        System.err.println();
        //System.err.println("Usage: java -cp '<classPath>' <makefile> <outputDir> <toolBoxDir> <workflowFile> <inputTable>");
        System.err.println("Usage: java -cp '<classPath>' StartPipegen");

        System.err.println();
     }
}
