
package edu.einstein.gmrrc.pipegen.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.*;
import java.util.regex.*;

import edu.einstein.gmrrc.pipegen.*;

public class MakefileTab extends JPanel {

    private static final String FILLER_TEXT = "[ no makefile loaded ]";

    private JEditorPane editorPane;
    private JScrollPane scrollPane;
    private JLabel filenameLabel;

    public MakefileTab() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        topPanel.add(Box.createRigidArea(new Dimension(20,0)));
        filenameLabel = new JLabel(FILLER_TEXT);
        filenameLabel.setForeground(new Color(114, 139, 164));
        topPanel.add(filenameLabel);
        topPanel.add(Box.createHorizontalGlue());
        add(topPanel);

        editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setText(convertToHTML(""));
        editorPane.setEditable(true);

        scrollPane = new JScrollPane(editorPane); 
        add(scrollPane);
    }

    public void loadMakefileAnalysis(MakefileAnalysis analysis) {

        filenameLabel.setText(analysis.getName());
        editorPane.setText(convertToHTML(analysis.getMakefileText()));
    }

    private String convertToHTML(String input) {

        String inputHTML = input.replace("<", "&lt;").replace(">", "&gt;");

        // *** Not so well coded, but potential problems should only be aesthetic
        //inputHTML = inputHTML.replace("#", "<span><b>#");
        //inputHTML = inputHTML.replace("\n", "<br></span>");

        String[] lines = inputHTML.split(System.getProperty("line.separator"));

        String outputHTML = "";

        for (int i=0; i < lines.length; i++) {
            if (lines[i].contains("#")) {
                outputHTML += lines[i].replaceFirst("#", "<b>#") + "</b>\n";
            } else {
                outputHTML += lines[i] + "\n";
            }
        }

        /*String pattern = "#.*\n";
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(inputHTML);
        while (m.find()) {
            String val = m.group();
            //System.out.println("find() val = " + val);
            //System.out.println("find() group = " + m.group(0));
            inputHTML = inputHTML.replaceFirst(m.group(0), "<b>" + m.group(0) + "</b>\n");
            //m = p.matcher(inputHTML);
        }*/

        String tempText = "";
        tempText += "<html>\n";
        tempText += "  <head>\n";
        tempText += "     <title>An example HTMLDocument</title>\n";
        tempText += "     <style type=\"text/css\">\n";
        tempText += "       div { background-color: white; }\n";
        tempText += "       ul { color: red; }\n";
        tempText += "       span.comment { color: #2790b3; font-weight: bold; }\n";
        tempText += "       span.var { color: #11c9ab; }\n";
        tempText += "       span.target { color: #dd4b39; }\n";
        tempText += "       b { color: #5A5AFF; }\n";
        tempText += "     </style>\n";
        tempText += "   </head>\n";
        tempText += "   <body>\n";
        tempText += "<pre>\n";
        tempText += outputHTML;
        tempText += "</pre>\n";
        tempText += "   </body>\n";
        tempText += " </html>\n";

        return tempText;
    }
}
