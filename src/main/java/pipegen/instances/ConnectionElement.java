/*
    Program:  ConnectionElement.java
    Author:   Michael Stokman
              Albert Einstein College of Medicine

 */

package pipegen.instances;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;
//import java.awt.font.*;
import org.json.*;

import pipegen.exceptions.*;

public class ConnectionElement {
    
    private ConnectableOutput start;
    private ConnectableInput stop;

    private boolean areGraphicsSet;
    private Graphics2D g2d;

    public ConnectionElement() {
        start = null;
        stop = null;
    }

    public ConnectionElement(MountPointOut start, MountPointIn stop) {
        setStart(start);
        setStop(stop);
        areGraphicsSet = false;
    }

    public ConnectionElement(ConnectableOutput start, ConnectableInput stop) {
        setStart(start);
        setStop(stop);
        areGraphicsSet = false;
    }

    public static ConnectionElement[] load(JSONArray connectionArray, SourceElement[] sources, SinkElement[] sinks, ModuleElement[] modules) throws InvalidConnectionDefException {

        try {
            int len = connectionArray.length();
            ConnectionElement[] out = new ConnectionElement[len];
            for (int i=0; i < len; i++) {
                JSONObject currConnection = connectionArray.getJSONObject(i);

                String startString = currConnection.getString("start");
                String stopString = currConnection.getString("stop");

                MountPointOut start = parseOutStr(startString, sources, modules);
                MountPointIn stop = parseInStr(stopString, sinks, modules);

                out[i] = new ConnectionElement(start, stop);
            }
        
            return out;
        } catch (Exception e) {
            e.printStackTrace();
            throw new InvalidConnectionDefException(e);
        }
    }

    public static JSONArray save(ArrayList<ConnectionElement> connections) throws JSONException {

        JSONArray outputJSON = new JSONArray();
        for (ConnectionElement connection : connections) {
            JSONObject connectionJSON = connection.save();
            outputJSON.put(connectionJSON);
        }
        
        return outputJSON;
    }


    public JSONObject save() throws JSONException {

        JSONObject outputJSON = new JSONObject();

        String startText = start.getJSONText();
        String stopText = stop.getJSONText();

        /*if (startText == null) {
            throw new JSONException("Could not produce valid text for connection start point.");
        }

        if (stopText == null) {
            throw new JSONException("Could not produce valid text for connection stop point.");
        }

        // Save start and stop*/
        outputJSON.put("start", startText);
        outputJSON.put("stop", stopText);
        // Save start and stop
        //outputJSON.put("start", "start dummy");
        //outputJSON.put("stop", "stop dummy");


        return outputJSON;
    }

    private static MountPointOut parseOutStr(String str, SourceElement[] sources, ModuleElement[] modules) throws InvalidConnectionDefException {
        if (str.startsWith("sources[")) {
            str = str.replaceFirst("^sources\\[", "").replaceFirst("\\]$", "");
            int id = Integer.parseInt(str);
            for (SourceElement source : sources) {
                if (source.getID() == id) {
                    return source.getMountPointOut();
                }
            }
        } else if (str.startsWith("modules[")) {

            String[] tokens = str.split("\\.");
            if (tokens.length != 2) {
                throw new InvalidConnectionDefException();
            }

            String moduleToken = tokens[0];
            String outName = tokens[1];

            moduleToken = moduleToken.replaceFirst("^modules\\[", "").replaceFirst("\\]$", "");
            int id = Integer.parseInt(moduleToken);
            for (ModuleElement module : modules) {
                if (module.getID() == id) {
                    return module.getMountPointOut(outName);
                }
            }
        }
        throw new InvalidConnectionDefException();
    }

    private static MountPointIn parseInStr(String str, SinkElement[] sinks, ModuleElement[] modules) throws InvalidConnectionDefException {
        if (str.startsWith("sinks[")) {
            str = str.replaceFirst("^sinks\\[", "").replaceFirst("\\]$", "");
            int id = Integer.parseInt(str);
            for (SinkElement sink : sinks) {
                if (sink.getID() == id) {
                    return sink.getMountPointIn();
                }
            }

        } else if (str.startsWith("modules[")) {

            String[] tokens = str.split("\\.");
            if (tokens.length != 2) {
                throw new InvalidConnectionDefException();
            }

            String moduleToken = tokens[0];
            String inName = tokens[1];

            moduleToken = moduleToken.replaceFirst("^modules\\[", "").replaceFirst("\\]$", "");
            int id = Integer.parseInt(moduleToken);
            for (ModuleElement module : modules) {
                if (module.getID() == id) {
                    return module.getMountPointIn(inName);
                }
            }
        }
        throw new InvalidConnectionDefException();
    }

    public String toString() {
        return " (" + start + " --- " + stop + ")";
        //return startString + " --- " + stopString + " (" + start + " --- " + stop + ")";
        //return startString + " --- " + stopString;
    }

    public void setStart(ConnectableOutput output) {
        clearStart();
        start = output;
        output.addConnection(this);
    }

    // *** Not sure what to do with these
    public MountPointOut getStart() {
        return (MountPointOut)start;
    }
    /*public ConnectableOutput getStart() {
        return start;
    }*/
    // ***

    public void setStop(ConnectableInput input) {
        clearStop();
        stop = input;
        input.addConnection(this);
    }

    public void clearStart() {
        if (start != null) {
            start.removeConnection(this);
            start = null;
        }
    }

    public void clearStop() {
        if (stop != null) {
            stop.clearConnections();
            stop = null;
        }
    }

    public void clear() {
        System.out.println("ConnectionElement.java clear() this = " + this);
        clearStart();
        clearStop();
    }

    /*public ConnectableInput getStop() {
        return stop;
    }*/

    public MountPointIn getStop() {
        return (MountPointIn)stop;
    }

    public boolean hasNull() {
        if (start == null || stop == null) {
            return true;
        }
        return false;
    }

    public boolean areGraphicsSet() {
        return areGraphicsSet;
    }

    public void setGraphics(Graphics2D g2d) {
        this.g2d = g2d;
    }

    public void draw(Graphics2D g2d) {

        if (hasNull()) {
            return;
        }

        g2d.setColor(start.getColor());

        int x1 = start.getAttachPointX();
        int y1 = start.getAttachPointY();
        int x2 = stop.getAttachPointX();
        int y2 = stop.getAttachPointY();

        int ctrlx1 = x1;
        int ctrlx2 = x2;
        int ctrly1 = (y1 + y2) / 2;
        int ctrly2 = ctrly1;
        CubicCurve2D.Float test = new CubicCurve2D.Float(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2);
        g2d.draw(test);

    }

}
