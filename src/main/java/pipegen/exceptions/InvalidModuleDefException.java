/*
    Program:  InvalidModuleDefException.java
    Author:   Michael Stokman
              Albert Einstein College of Medicine

    Purpose:  This exception should be thrown in response to an unusable 
              ModuleElement definition within workflow/pipeline definition file.

 */

package pipegen.exceptions;


public class InvalidModuleDefException extends Exception {
    
    public InvalidModuleDefException() {
        super();
    }

    public InvalidModuleDefException(Throwable e) {
        super(e);
    }
}
