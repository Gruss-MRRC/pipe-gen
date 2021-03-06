/*
    Program:  ElementPosition.java
    Author:   Michael Stockman
              Albert Einstein College of Medicine

 */

package pipegen.instances;

import java.io.*;
import org.json.*;

import pipegen.exceptions.*;

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

    public ElementPosition copy() {
        return new ElementPosition(this.x, this.y);
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

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double distance(ElementPosition target) {
        return Math.sqrt(Math.pow(getX()-target.getX(), 2) + Math.pow(getY()-target.getY(), 2));
    }

    public void move(int deltaX, int deltaY) {
        x += deltaX;
        y += deltaY;
    }

    public String toString() {
        return "(" + x + ", " + y + ")"; 
    }
}
