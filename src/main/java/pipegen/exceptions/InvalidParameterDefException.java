/*
    Program:  InvalidParameterDefException.java
    Author:   Michael Stockman
              Albert Einstein College of Medicine

    Purpose:  This exception should be thrown in response to an unusable 
              parameter definition (i.e. specific inputs and outputs on 
              BlockElements).

 */

package pipegen.exceptions;


public class InvalidParameterDefException extends Exception {
    
    public InvalidParameterDefException() {
        super();
    }

    public InvalidParameterDefException(Throwable e) {
        super(e);
    }
}
