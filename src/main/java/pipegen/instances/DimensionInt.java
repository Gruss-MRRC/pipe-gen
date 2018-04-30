/*
    Program:  DimensionInt.java
    Author:   Michael Stockman
              Albert Einstein College of Medicine

 */

package pipegen.instances;

import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.font.*;

public class DimensionInt extends Dimension {

    public DimensionInt() {
        super();
    }

    public DimensionInt(int width, int height) {
        super(width, height);
    }

    public int getX() {
        return (int)super.getWidth();
    }

    public int getY() {
        return (int)super.getHeight();
    }

    public void setSize(double width, double height) {
        super.setSize((int)width, (int)height);
    }
}
