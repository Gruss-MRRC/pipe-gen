/*
    Program:  ErrorTreeVisitor.java
    Author:   Michael Stokman
              Albert Einstein College of Medicine

    Purpose:  This class implements the VISITOR PATTERN to traverse a graph of 
              connected BlockElements.

              The visitors accrue the appropriate error tree text for each node
              as they visit them. The visitor starts from a given module and 
              then visits all down-stream modules and sinks that make use of the
              module's output.

 */

package edu.einstein.gmrrc.pipegen;

import java.io.*;

import edu.einstein.gmrrc.pipegen.exceptions.*;
import edu.einstein.gmrrc.pipegen.instances.*;


public class ErrorTreeVisitor {

    private DataTableFile table;
    private String id;
    private StringBuilder sb;
    private int level;

    public ErrorTreeVisitor(DataTableFile table, String id) {
        this.table = table;
        this.id = id;
        sb = new StringBuilder();
        level = 0;
    }

    /**
     * Visit a BlockElement and generate relevant makefile text. This is a 
     * slightly awkward way to mimic polymorphism, but it will work for now.
     */
    public void visit(BlockElement block) {
        if (block instanceof ModuleElement) {
            visit((ModuleElement)block);
        } else if (block instanceof SinkElement) {
            visit((SinkElement)block);
        }
    }

    /**
     * Visit a ModuleElement and generate relevant error tree text
     */
    public void visit(ModuleElement module) {

        for (int i=0; i < level; i++) {
            sb.append("    ");
        }
        sb.append("<span class=\"red\">+ " + module.getName() + "</span>\n");

        level += 1;
        for (BlockElement child : module.getChildren()) {
            visit(child);
        }
    }

    /**
     * Visit a SinkElement and generate relevant makefile text
     */
    public void visit(SinkElement sink) {

        String outputFile = sink.getFilename(id, table);

        outputFile = table.replaceMakeVariables(outputFile, id);

        for (int i=0; i < level; i++) {
            sb.append("    ");
        }
        sb.append("<span class=\"red\">- " + sink.getBlockName() + ":</span><span class=\"black\"> " + outputFile + "</span>\n");

    }

    /**
     * Returns the portion of makefile text generated during this visitor's 
     * traversal of the workflow.
     */
    public String getText() {
        return sb.toString();
    }

}
