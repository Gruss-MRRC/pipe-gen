
package pipegen.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.TableModelEvent;

import pipegen.*;

public class DataTab extends JPanel {

    private static final String FILLER_TEXT = "[ no input data loaded ]";
    private JLabel filenameLabel;
    private JTable table;
    private JScrollPane scrollPane;


    private DataTableFile inputData;

    public DataTab() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        filenameLabel = new JLabel(FILLER_TEXT);
        filenameLabel.setForeground(new Color(114, 139, 164));
        topPanel.add(Box.createRigidArea(new Dimension(20,0)));
        topPanel.add(filenameLabel);
        topPanel.add(Box.createHorizontalGlue());
        add(topPanel);


        //JPanel tempPanel = new JPanel();
        //tempPanel.setBackground(Color.WHITE);
        //tempPanel.add(new JLabel("this"));
        //scrollPane.add(tempPanel);

        JTextArea textArea = new JTextArea(5, 20);
        textArea.setText("");
        textArea.setEditable(false);
        //scrollPane.add(textArea);

        scrollPane = new JScrollPane(textArea);

        add(scrollPane);
    }

    public void setInputData(DataTableFile inputData) {
        this.inputData = inputData;
    }

    public void rebuild() {
        if (inputData != null) {
            filenameLabel.setText(inputData.getName());
        } else {
            filenameLabel.setText("[unsaved input data]");
        }

        String[] columnNames = inputData.getHeaders();
        Object[][] data = inputData.getContents();

        table = new JTable(data, columnNames);
        table.setFillsViewportHeight(true);
        table.setGridColor(new Color(200, 221, 242));

        remove(scrollPane);
        scrollPane = new JScrollPane(table);
        add(scrollPane);


    }
}
