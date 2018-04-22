
package pipegen.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import pipegen.definitions.*;
import pipegen.instances.*;

public class PipelineTab extends JScrollPane {

    private static final int SCROLL_INC = 17;

    private PipegenGUI frame;
    private PipelinePanel panel;

    public PipelineTab(PipegenGUI gui) {
        super(VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_ALWAYS);
        frame = gui;

        getVerticalScrollBar().setUnitIncrement(SCROLL_INC);
		panel = new PipelinePanel(frame);
        setViewportView(panel);
    }

    public void setPipeline(PipelineInstance pipeline) {
        panel.setPipeline(pipeline);
    }

    public void draw() {
        setViewportView(panel);
    }
}
