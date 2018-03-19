
package edu.einstein.gmrrc.pipegen.gui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

import edu.einstein.gmrrc.pipegen.definitions.*;
import edu.einstein.gmrrc.pipegen.instances.*;

public class PipelinePanel extends JPanel implements MouseListener, MouseMotionListener {

    //private boolean flag;

    private static final int X_DIM = 1600;
    private static final int Y_DIM = 2000;
    private static final Dimension DIM = new Dimension(X_DIM, Y_DIM);

    private static final int GRID_SPACING = 10;
    private static final Color BACKGROUND_COLOR = new Color(255, 255, 255);
    private static final Color GRID_COLOR = new Color(240, 240, 240);

    private static final Color MODULE_COLOR = new Color(80, 80, 80);

    private static final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
    private static final Cursor MOVE_CURSOR = new Cursor(Cursor.MOVE_CURSOR);
    private static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);

    private boolean pipelineSet;
    private PipelineInstance pipeline;
    private ArrayList<SourceElement> sources;
    private ArrayList<SinkElement> sinks;
    private ArrayList<ModuleElement> modules;

    private MountPointGhost draggableGhost;
    private Draggable dragging;
    private ConnectionElement dragConnection;

    private int xPos;
    private int yPos;

    private PopupAddMenu addMenu;
    private PipegenGUI frame;

    public PipelinePanel(PipegenGUI gui) {
        super();
        frame = gui;
        addMouseListener(this);
        addMouseMotionListener(this);

        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setDismissDelay(30000);

        draggableGhost = null;

        pipelineSet = false;
        dragging = null;
        dragConnection = null;

        //flag = true;

        setIgnoreRepaint(true);
    }

    public void setPipeline(PipelineInstance pipeline) {
        pipelineSet = true;
        this.pipeline = pipeline;
        sources = pipeline.getSources();
        sinks = pipeline.getSinks();
        modules = pipeline.getModules();
        addMenu = new PopupAddMenu(this, frame, this.pipeline);
    }

    public void unsetPipeline(PipelineInstance pipeline) {
        pipelineSet = false;
        pipeline = null;
        sources = null;
        sinks = null;
        modules = null;
        addMenu = null;
    }

    private void doDrawing(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;
        //if (flag) {
            drawGrid(g2d);
        //    flag = false;
        //} else {
        //    drawGrid(g2d);
        //}

        if (pipelineSet) {
            if (! pipeline.areGraphicsSet()) {
                pipeline.setGraphics(g2d);
            }
            if (draggableGhost != null) {
                draggableGhost.draw(g2d);
            }
            pipeline.draw(g2d);
        }
    }

    private void drawGrid(Graphics2D g2d) {

        setBackground(BACKGROUND_COLOR);
        g2d.setColor(GRID_COLOR);

        for (int x=0; x < X_DIM; x++) {
            if ((x+1) % GRID_SPACING == 0) {
                g2d.drawLine(x, 0, x, Y_DIM);
            }
        }

        for (int y=0; y < Y_DIM; y++) {
            if ((y+1) % GRID_SPACING == 0) {
                g2d.drawLine(0, y, X_DIM, y);
            }
        }
    }

    public void mouseMoved(MouseEvent e) {

        if (pipelineSet) {
            Point loc = e.getPoint();
            MountPointGhostIn inPoint = null;
            MountPointGhostOut outPoint = null;

            if (overBlockElement(loc) != null) {
                // Check if cursor is over moveable block elements
                draggableGhost = null;
                setCursor(MOVE_CURSOR);
                this.setToolTipText(null);
            } else if ((inPoint = getTargetInput(loc)) != null) {
                // Check if cursor is over a MountPointIn
                draggableGhost = inPoint;
                setCursor(HAND_CURSOR);
                this.setToolTipText(inPoint.getToolTip());
            } else if ((outPoint = getTargetOutput(loc)) != null) {
                // Check if cursor is over a MountPointOut
                draggableGhost = outPoint;
                setCursor(HAND_CURSOR);
                this.setToolTipText(outPoint.getToolTip());
            } else {
                // Otherwise the cursor is not over anything notable
                draggableGhost = null;
                setCursor(DEFAULT_CURSOR);
                this.setToolTipText(null);
            }
        }
        this.repaint();
    }

    private BlockElement overBlockElement(Point loc) {

            for (SourceElement source : sources) {
                if (source.contains(loc)) {
                    return source;
                }
            }
            for (SinkElement sink : sinks) {
                if (sink.contains(loc)) {
                    return sink;
                }
            }
            for (ModuleElement module : modules) {
                if (module.contains(loc)) {
                    return module;
                }
            }

        return null;
    }

    private MountPointGhostIn getTargetInput(Point loc) {

        for (SinkElement sink : sinks) {
            MountPointIn target = sink.mountPointContaining(loc);
            if (target != null) {
                return new MountPointGhostIn(target, pipeline);
            }
        }
        for (ModuleElement module : modules) {
            MountPointIn[] targets = module.getInputs();
            for (MountPointIn target : targets) {
                if (target.contains(loc)) {
                    return new MountPointGhostIn(target, pipeline);
                }
            }
        }

        return null;
    }

    private MountPointGhostOut getTargetOutput(Point loc) {

        for (SourceElement source : sources) {
            MountPointOut target = source.mountPointContaining(loc);
            if (target != null) {
                return new MountPointGhostOut(target, pipeline);
            }
        }
        for (ModuleElement module : modules) {
            MountPointOut[] targets = module.getOutputs();
            for (MountPointOut target : targets) {
                if (target.contains(loc)) {
                    return new MountPointGhostOut(target, pipeline);
                }
            }
        }
        return null;
    }

    public void mouseDragged(MouseEvent e) {

        int deltaX = e.getX() - xPos;
        int deltaY = e.getY() - yPos;

        //System.out.println("dragged dx=" + deltaX + ", dy=" + deltaY);
        if (draggableGhost != null) {
            draggableGhost.move(deltaX, deltaY);
        } else if (dragging != null) {
            dragging.move(deltaX, deltaY);
        }

        xPos = e.getX();
        yPos = e.getY();
        //this.repaint(0, 0, 200, 200);
        this.repaint();
    }

    public void mousePressed(MouseEvent e) {

        if (pipelineSet) {
            Point loc = e.getPoint();
            xPos = e.getX();
            yPos = e.getY();

            if(SwingUtilities.isRightMouseButton(e)) {

                BlockElement selectedBlock = overBlockElement(loc);
                if (selectedBlock == null) {
                    addMenu.show(this, e.getX(), e.getY());
                    return;
                } else {
                    System.out.println("PipelinePanel.java - mousePressed() delete block.");
                    PopupDeleteMenu deleteMenu = new PopupDeleteMenu(this, pipeline, selectedBlock);
                    deleteMenu.show(this, e.getX(), e.getY());
                }
            }

            for (SourceElement source : sources) {
                if (source.contains(loc)) {
                    dragging = source;
                    return;
                }
            }
            for (SinkElement sink : sinks) {
                if (sink.contains(loc)) {
                    dragging = sink;
                    return;
                } /*else if (sink.inputContains(loc)) {
                    dragConnection = sink.getMountPointIn().breakConnection();
                    return;
                }*/
            }
            for (ModuleElement module : modules) {
                if (module.contains(loc)) {
                    dragging = module;
                    return;
                }
            }
            if (draggableGhost != null && draggableGhost.contains(loc)) {
                dragging = draggableGhost;
                draggableGhost.pressed();
                return;
            }

        }

    }

    public void mouseReleased(MouseEvent e) {

        if (draggableGhost != null) {
            draggableGhost.released(sources, sinks, modules);
        }

        draggableGhost = null;
        dragging = null;
        this.repaint();
    }

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {
        this.repaint();
    }

    public void mouseClicked(MouseEvent e) {}

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        doDrawing(g);
    }

    @Override
    public Dimension getPreferredSize() {
        return DIM;
    }

    @Override
    public Dimension getMinimumSize() {
        return DIM;
    }

    @Override
    public Dimension getMaximumSize() {
        return DIM;
    }
}

