/*
    Program:  InvalidAbouFileException.java
    Author:   Michael Stockman
              Albert Einstein College of Medicine

    Purpose:  This exception should be thrown in response to an unusable 
              About file. About files are normally provided to describe the
              purpose of a pipe-gen toolbox.

 */

package pipegen.exceptions;

//import java.io.*;

//import org.json.*;

public class InvalidAboutFileException extends Exception {
    
    public InvalidAboutFileException() {
        super();
    }

    /*public InvalidAboutFileException(Throwable e) {
        super(e);
    }*/

}
