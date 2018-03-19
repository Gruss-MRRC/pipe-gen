/*
    Program:  InvalidFileFormatDefException.java
    Author:   Michael Stokman
              Albert Einstein College of Medicine

    Purpose:  This exception should be thrown in response to an unusable 
              file format definition within workflow/pipeline definition.

 */

package edu.einstein.gmrrc.pipegen.exceptions;


public class InvalidFileFormatDefException extends Exception {
    
    public InvalidFileFormatDefException() {
        super();
    }

    public InvalidFileFormatDefException(Throwable e) {
        super(e);
    }
}
