/*
    Program:  ConnectableOutput.java
    Author:   Michael Stockman
              Albert Einstein College of Medicine

 */

package pipegen.instances;

import java.util.*;
import java.awt.*;

public interface ConnectableOutput {

    public int getAttachPointX();
    public int getAttachPointY();

    public Color getColor();
    public ElementPosition getAttachPoint();

    public void addConnection(ConnectionElement c);
    public void removeConnection(ConnectionElement c);
    public void clearConnections();
    public ArrayList<ConnectionElement> getConnections();
    public ConnectionElement getConnection(int index);
    public ConnectionElement getConnection();

    public String getJSONText();
}
