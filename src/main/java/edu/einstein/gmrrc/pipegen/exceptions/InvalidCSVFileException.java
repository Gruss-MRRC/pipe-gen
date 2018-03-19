/*
    Program:  InvalidCSVFileException.java
    Author:   Michael Stokman
              Albert Einstein College of Medicine

    Purpose:  This exception should be thrown in response to an unusable 
              CSV-style data file.

 */

package edu.einstein.gmrrc.pipegen.exceptions;


public class InvalidCSVFileException extends Exception {
    
    public InvalidCSVFileException() {
        super();
    }

    public InvalidCSVFileException(Throwable e) {
        super(e);
    }
}
