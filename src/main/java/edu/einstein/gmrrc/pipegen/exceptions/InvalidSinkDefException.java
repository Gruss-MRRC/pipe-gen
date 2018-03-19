/*
    Program:  InvalidSinkDefException.java
    Author:   Michael Stokman
              Albert Einstein College of Medicine

    Purpose:  This exception should be thrown in response to an unusable 
              SinkElement definition within workflow/pipeline definition file.

 */

package edu.einstein.gmrrc.pipegen.exceptions;


public class InvalidSinkDefException extends Exception {
    
    public InvalidSinkDefException() {
        super();
    }

    public InvalidSinkDefException(Throwable e) {
        super(e);
    }
}
