/*
    Program:  InvalidWorkflowDefException.java
    Author:   Michael Stokman
              Albert Einstein College of Medicine

    Purpose:  This exception should be thrown in response to an unusable 
              workflow/pipeline definition file.

 */

package pipegen.exceptions;

public class InvalidWorkflowDefException extends Exception {
    
    public InvalidWorkflowDefException() {
        super();
    }

    public InvalidWorkflowDefException(Throwable e) {
        super(e);
    }

}
