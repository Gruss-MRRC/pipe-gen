/*
    Program:  DataTableFile.java
    Author:   Michael Stockman
              Albert Einstein College of Medicine

    Purpose:  This class extends File representing files containing tabular data

              Reads a .csv style text file that uses | (i.e. vertical bar) as a
              delimeter. Data is then stored in a 2D string array, String[][].

 */

package pipegen;

import java.io.*;
import java.util.*;

import pipegen.exceptions.*;


public class DataTableFile extends File {

    private static final String cvsSplitBy = "\\|";
    private String[][] table;

    public DataTableFile(String pathname) {
        super(pathname);
    }

    /**
     * Loads the text file table data into this object
     */
    public void loadData() throws InvalidCSVFileException {

        // Read file line-by-line and load data into tableList
	    BufferedReader br = null;
	    String line = "";
        List<String[]> tableList = new ArrayList<String[]>();
	    try {
 
		    br = new BufferedReader(new FileReader(this));
		    while ((line = br.readLine()) != null) {
		    	String[] tokens = line.split(cvsSplitBy);
                for (int i=0; i < tokens.length; i++) {
                    tokens[i] = tokens[i].trim();
                }
                tableList.add(tokens);
		    }
 
	    } catch (FileNotFoundException e) {
		    throw new InvalidCSVFileException();
	    } catch (IOException e) {
		    throw new InvalidCSVFileException();
	    } finally {
		    if (br != null) {
		    	try {
		    		br.close();
		    	} catch (IOException e) {
		    		e.printStackTrace();
		    	}
		    }
	    }

        // Create table based on dimensions of tableList
        int tableHeight = tableList.size();
        int tableWidth = tableList.get(0).length;
        if (tableHeight == 0 || tableWidth == 0) {
		    throw new InvalidCSVFileException();
        }
        table = new String[tableHeight][tableWidth];

        // Copy tableList to array format table
        for (int i=0; i < tableHeight; i++) {
            table[i] = tableList.get(i);
            if (table[i].length != tableWidth) {
		        throw new InvalidCSVFileException();
            }
        }

        // Verify that table has valid data
        if (! isValid()) {
		    throw new InvalidCSVFileException();
        }
    }

    /**
     * Returns true iff this table has valid data
     */
    private boolean isValid() {

        // Verify that the headers are unique
        String[] headers = getHeaders();
        if (hasDuplicates(headers)) {
            return false;
        }

        // Verify that there is an 'id' field
        int idIndex = Arrays.asList(getHeaders()).indexOf("id");
        if (idIndex == -1) {
            return false;
        }

        // Verify that rowid's found in id are unique
        String[] idColumn = getColumnByHeader("id");
        if (hasDuplicates(idColumn)) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if the input array has duplicate entries
     */
    private static boolean hasDuplicates(String[] array) {
        for (int i=0; i < array.length-1; i++) {
            for (int j=i+1; j < array.length; j++) {
                if (array[i].equals(array[j])) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the table excluding the header row
     */
    public String[] getHeaders() {
        if (table.length == 0) {
            return null;
        }
        return table[0];
    }

    /**
     * Returns the table excluding the header row
     */
    public String[][] getContents() {

        String[][] outputTable = new String[table.length-1][table[0].length];
        for (int row=1; row < table.length; row++) {
            outputTable[row-1] = table[row];
        }
        return outputTable;
    }

    /**
     * Returns a whole column of data from the table based on the field header
     */
    public String[] getColumnByHeader(String header) {

        // Get column index for the requested field
        // Return null if no such field is found
        int col = Arrays.asList(getHeaders()).indexOf(header);
        if (col == -1) {
            return null;
        }

        // Copy selected column into output column, excludes the header row
        String[] outputColumn = new String[table.length-1];
        for (int row=1; row < table.length; row++) {
            outputColumn[row-1] = table[row][col];
        }
        return outputColumn;
    }

    /**
     * Returns the contents of a specific data cell based on its field header and rowid
     */
    public String getDataByHeaderAndRowid(String header, String rowid) {

        String[] idColumn = getColumnByHeader("id");
        String[] dataColumn = getColumnByHeader(header);

        // Either column is missing from the table return null
        if (idColumn == null || dataColumn == null) {
            return null;
        }

        // Find the internal arrayIndex of rowid
        // Return null if rowid is not found in the idColumn
        int arrayIndex = Arrays.asList(idColumn).indexOf(rowid);
        if (arrayIndex == -1) {
            return null;
        }

        return dataColumn[arrayIndex];
    }

    /**
     * Returns an array of the fields in this table that have makefile-style variables
     */
    public String[] getMakeVariables() {

        List<String> outputList = new ArrayList<String>();
        // Check each field header, if it is a makefile variable append it to outputList
        String[] headers = getHeaders();
        if (headers == null) {
            return null;
        }
        for (String header : getHeaders()) {
            if (isMakeVariable(header)) {
                String varName = removeMakeVarBrackets(header);
                outputList.add(varName);
            }
        }
        return outputList.toArray(new String[outputList.size()]);
    }

    /**
     * Replaces 
     */
    public String replaceMakeVariables(String input, String id) {
        String output = input;

        String[] makeVariables = getMakeVariables();
        for (String var : makeVariables) {
            String name = "$(" + var + ")";
            String value = getDataByHeaderAndRowid(name, id);
            output = output.replace(name, value);
        }
        return output;
    }

    /**
     * Returns true if input String is formated as a makefile-style variable 
     */
    public static boolean isMakeVariable(String header) {
        if (header.matches("^\\$\\(.*\\)$")) {
            return true;
        }
        return false;
    }

    /**
     * Removes brackets from makefile-style variables "$(varName)" -> "varname" 
     */
    private static String removeMakeVarBrackets(String input) {
        return input.replaceFirst("^\\$\\(", "").replaceFirst("\\)$", "");
    }

    /**
     * Adds brackets to produce makefile-style variables "varName" -> "$(varname)" 
     */
    private static String addMakeVarBrackets(String input) {
        return "$(" + input + ")";
    }

    /**
     * Formats and returns a String representation of this object 
     */
    public String toString() {

        String out = "";
        for (int i=0; i < table.length; i++) {
            for (int j=0; j < table[i].length; j++) {
                out += table[i][j] + " ";
            }
            out += "\n";
        }
        return out;
    }
}
