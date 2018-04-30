/*
    Program:  StartPipegen.java
    Author:   Michael Stockman
              Albert Einstein College of Medicine

    Purpose:  This the main executable file for pipe-gen.

              Build workflows for data analysis. Efficiently apply a set of 
              operations to a dataflow of inputs. Pipe-gen auto-generates 
              makefiles based on this confluence of process and data.

    Usage:    java -jar build/libs/pipe-gen.jar 

 */

package pipegen;

import java.io.*;

import pipegen.*;
import pipegen.exceptions.*;
import pipegen.instances.*;
import pipegen.definitions.*;
import pipegen.gui.*;


class StartPipegen {

	private static final File TOOLBOX_DIR = new File("./data/toolboxes/");

    public static void main(String[] argv) {
        if (argv.length != 0) {
            printUsage();
            System.exit(1);
        }

        new PipegenGUI(TOOLBOX_DIR);
    }

    private static void printUsage() {
        System.err.println();
        System.err.println("Purpose:  This the main executable file for pipe-gen.");
        System.err.println();
        System.err.println("          Build workflows for data analysis. Efficiently apply a set of");
        System.err.println("          operations to a dataflow of inputs. Pipe-gen auto-generates");
        System.err.println("          makefiles based on this confluence of process and data.");
        System.err.println();
        System.err.println("Usage:    java -cp '<classPath>' StartPipegen");
        System.err.println();
     }
}
