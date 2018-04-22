/*
    Program:  InvalidMakefileFileException.java
    Author:   Michael Stokman
              Albert Einstein College of Medicine

    Purpose:  This exception should be thrown in response to a problem creating
              a makefile.

 */

package pipegen.exceptions;


public class InvalidMakefileException extends Exception {
    
    public InvalidMakefileException() {
        super();
    }

    public InvalidMakefileException(Throwable e) {
        super(e);
    }

}
