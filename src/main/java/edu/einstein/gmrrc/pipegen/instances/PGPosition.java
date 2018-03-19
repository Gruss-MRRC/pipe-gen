/*
    Program:  ElementPosition.java
    Author:   Michael Stokman
              Albert Einstein College of Medicine

    Purpose:  

 */

package edu.einstein.gmrrc.pipegen.instances;

import java.io.*;

import org.json.*;

import edu.einstein.gmrrc.pipegen.exceptions.*;

public class ElementPosition {

    private int x;
    private int y;

    public ElementPosition() {
        x = 0;
        y = 0;
    }

    public ElementPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static ElementPosition load(JSONObject positionObject) throws InvalidElementPositionException {

        int x = 0;
        int y = 0;

        try {
            x = positionObject.getInt("X");
            y = positionObject.getInt("Y");
        } catch(Exception e) {
            throw new InvalidElementPositionException(e);
        }

        return new ElementPosition(x, y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void move(int deltaX, int deltaY) {
        x += deltaX;
        y += deltaY;
    }

    public String toString() {
        return "(" + x + ", " + y + ")"; 
    }
}
