/*
    Program:  InvalidParameterDefException.java
    Author:   Michael Stokman
              Albert Einstein College of Medicine

    Purpose:  This exception should be thrown in response to an unusable 
              parameter definition (i.e. specific inputs and outputs on 
              BlockElements).

 */

package edu.einstein.gmrrc.pipegen.exceptions;


public class InvalidParameterDefException extends Exception {
    
    public InvalidParameterDefException() {
        super();
    }

    public InvalidParameterDefException(Throwable e) {
        super(e);
    }
}
