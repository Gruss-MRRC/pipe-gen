/*
    Program:  InvalidSourceDefException.java
    Author:   Michael Stockman
              Albert Einstein College of Medicine

    Purpose:  This exception should be thrown in response to an unusable 
              SourceElement definition within workflow/pipeline definition file.

 */

package pipegen.exceptions;


public class InvalidSourceDefException extends Exception {
    
    public InvalidSourceDefException() {
        super();
    }

    public InvalidSourceDefException(Throwable e) {
        super(e);
    }

}
