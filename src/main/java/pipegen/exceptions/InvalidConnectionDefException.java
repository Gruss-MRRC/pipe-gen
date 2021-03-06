/*
    Program:  InvalidConnectionDefException.java
    Author:   Michael Stockman
              Albert Einstein College of Medicine

    Purpose:  This exception should be thrown in response to an unusable 
              ConnectionElement definition within workflow/pipeline definition.

 */

package pipegen.exceptions;


public class InvalidConnectionDefException extends Exception {
    
    public InvalidConnectionDefException() {
        super();
    }

    public InvalidConnectionDefException(Throwable e) {
        super(e);
    }
}
