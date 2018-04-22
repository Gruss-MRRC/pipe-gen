/*
    Program:  InvalidElementPositionException.java
    Author:   Michael Stokman
              Albert Einstein College of Medicine

    Purpose:  This exception should be thrown in response to an unusable 
              BlockElement position within workflow/pipeline definition.

 */

package pipegen.exceptions;


public class InvalidElementPositionException extends Exception {
    
    public InvalidElementPositionException() {
        super();
    }

    public InvalidElementPositionException(Throwable e) {
        super(e);
    }
}
