/*
    Program:  RunTab.java
    Author:   Michael Stokman
              Albert Einstein College of Medicine

    Purpose:  Represents the pipe-gen GUI tab pane labeled 'Run'.

 */

package pipegen.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.Timer;

import pipegen.*;

/**
 * Represents the pipe-gen GUI tab pane labeled 'Run'
 */
public class RunTab extends JPanel {

    // Miscellaneous private static final fields
    private static final String DEFAULT_COMMAND = "make -f Makefile";
    private static final String DEFAULT_COMMAND_QMAKE = "qmake -cwd -v PATH -- -f Makefile";
    private static final String DEFAULT_COMMAND_PARALLEL = "make -j 8 -f Makefile";
    private static final String DEFAULT_COMMAND_KEEPGOING = "make -k -f Makefile";
    private static final String DEFAULT_COMMAND_PARAKEEP = "make -j 8 -k -f Makefile";
    private static final String NA_AMOUNT = "N/A";
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color SUCCESS_GREEN = new Color(68, 180, 73);
    private static final Color ERROR_RED = new Color(255, 0, 0);

    // Dimensions for GUI layout
    private static final int VERTICAL_PAD = 15;
    private static final int HORIZONTAL_PAD = 20;
    private static final int ROW_1_2_SPACER = 20;
    private static final int ROW_2_3_SPACER = 10;
    private static final int INTER_COLUMN_SPACER = 40;
    private static final int SUMMARY_ROW_SPACER = 10;
	private static final Dimension MAKECOMMAND_DIM = new Dimension(450, 35);

    // The icon on the run button
    private static final ImageIcon RUN_ICON = new ImageIcon("resources/images/run_button.png");

    // Fonts used on the 'Run' tab pane
    private static final Font MONOSPACED_FONT_16PT = new Font(Font.MONOSPACED, Font.BOLD, 16);
    private static final Font BOLD_FONT_18PT = new Font(Font.SANS_SERIF, Font.BOLD, 18);
    private static final Font PLAIN_FONT_18PT = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
    private static final Font CLEAR_BUTTON_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 10);

    private PipegenGUI gui;

    // The analysis the GUI will run
    private MakefileAnalysis analysis;

    // GUI element fields
    private JTextArea makeCommand;
    private JButton runButton;
    private JButton cleanButton;
    private JCheckBox checkMultiNode;
    private JCheckBox checkParallel;
    private JCheckBox checkKeepGoing;

    private JLabel timeElapsedValue;
    private JLabel targetFilesValue;
    private JLabel intermediateFilesValue;
    private JLabel errorsValue;
    private JEditorPane errorsTextArea;

    private boolean checkMultiNodePrevious;
    private boolean checkParallelPrevious;
    private boolean checkKeepGoingPrevious;

    // Fields used for running tasks in the background and monitoring progress
    private int count;
    private Timer clockTimer;
    private Timer summaryTimer;
    private AnalysisWorker analysisWorker;
    private long startTime;

    private int waitForFilesCounter;
    private static final int MAX_WAIT = 20;

    public RunTab(PipegenGUI gui) {
        super();

        this.gui = gui;

        // Sets this tab's background color / sets layout to horizonal box layout
        setBackground(BACKGROUND_COLOR);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // Adds the main GUI column sandwitched between horizontal padding
        add(Box.createRigidArea(new Dimension(HORIZONTAL_PAD, 0)));
        JPanel mainColumn = buildMainColumn();
        add(mainColumn);
        add(Box.createRigidArea(new Dimension(HORIZONTAL_PAD, 0)));

        waitForFilesCounter = 0;
        count = 0;
    }

    /**
     * Builds and returns the main column of GUI layout
     */
    private JPanel buildMainColumn() {

        // Creates the mainColumn
        JPanel mainColumn = new JPanel();
        mainColumn.setBackground(BACKGROUND_COLOR);
        mainColumn.setLayout(new BoxLayout(mainColumn, BoxLayout.Y_AXIS));

        // Adds some rigid spacers to the top of the mainColumn
        mainColumn.add(Box.createRigidArea(new Dimension(0, VERTICAL_PAD)));
        mainColumn.add(Box.createRigidArea(new Dimension(0, VERTICAL_PAD)));

        // Adds the row that displays makefile information
        mainColumn.add(buildMakefileRow());
        mainColumn.add(Box.createRigidArea(new Dimension(0, ROW_1_2_SPACER)));

        // Adds the row that summarizes progress running the analysis
        mainColumn.add(buildSummaryRow());
        mainColumn.add(Box.createRigidArea(new Dimension(0, ROW_2_3_SPACER)));

        // Adds the row for displaying details on errors encountered
        mainColumn.add(buildErrorPanel());
        mainColumn.add(Box.createRigidArea(new Dimension(0, VERTICAL_PAD)));

        return mainColumn;
    }

    /**
     * Builds and returns the row of the GUI for displaying makefile settings
     */
    private JPanel buildMakefileRow() {

        // Creates the rootPanel for the makefile row
        JPanel rootPanel = new JPanel();
        rootPanel.setBackground(BACKGROUND_COLOR);
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.X_AXIS));

        // Creates and adds the left side of the makefile row
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(BACKGROUND_COLOR);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        rootPanel.add(leftPanel);

        // Adds a column spacer between the left side and right side
        rootPanel.add(Box.createRigidArea(new Dimension(INTER_COLUMN_SPACER, 0)));

        // Creates and adds the right side of the makefile row
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(BACKGROUND_COLOR);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rootPanel.add(rightPanel);

        // Adds horizontal glue to the right of the right side
        rootPanel.add(Box.createHorizontalGlue());

        // Builds the top-left region this displays the make command to be run
        // along with a run button to launch it
        JPanel topLeft = new JPanel();
        topLeft.setLayout(new BoxLayout(topLeft, BoxLayout.X_AXIS));
        topLeft.setBackground(BACKGROUND_COLOR);
        topLeft.setAlignmentX(RIGHT_ALIGNMENT);
        topLeft.add(Box.createRigidArea(new Dimension(10, 0)));
        makeCommand = new JTextArea(DEFAULT_COMMAND);
        makeCommand.setFont(MONOSPACED_FONT_16PT);
		makeCommand.setMargin(new Insets(10,15,10,0)); 
        makeCommand.setBackground(new Color(245, 245, 245));
        makeCommand.setMaximumSize(MAKECOMMAND_DIM);
        makeCommand.setMinimumSize(MAKECOMMAND_DIM);
        makeCommand.setPreferredSize(MAKECOMMAND_DIM);
        makeCommand.setSize(MAKECOMMAND_DIM);

        // Action listener for changes to the make command text area
        makeCommand.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent arg0) {}

            @Override
            public void keyReleased(KeyEvent arg0) {}

            @Override
            public void keyPressed(KeyEvent arg0) {
                changedMakeCommand();
            }
        });


        topLeft.add(makeCommand);
        //topLeft.add(Box.createRigidArea(new Dimension(INTER_COLUMN_SPACER, 0)));
        runButton = new JButton("Run", RUN_ICON);
        runButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { runButtonPressed(); }
        });
        runButton.setEnabled(false);
        topLeft.add(runButton);
        leftPanel.add(topLeft);

        // Builds the bottom-left region of formatting whitespace
        JPanel bottomLeft = new JPanel();
        bottomLeft.setLayout(new BoxLayout(bottomLeft, BoxLayout.X_AXIS));
        bottomLeft.setAlignmentX(RIGHT_ALIGNMENT);
        bottomLeft.setBackground(Color.BLACK);
        bottomLeft.add(Box.createRigidArea(new Dimension(450, 0)));
        leftPanel.add(bottomLeft);

        // Adds some vertical glue to the bottom of leftPanel
        leftPanel.add(Box.createVerticalGlue());

        // Action listener for the checkboxes
    	ActionListener checkBoxListener = new ActionListener() {
      		public void actionPerformed(ActionEvent actionEvent) {
				changedCheckBox();
      		}
    	};

        checkMultiNode = new JCheckBox("Run on multiple nodes");
        checkMultiNode.setBackground(BACKGROUND_COLOR);
        checkMultiNode.setEnabled(false);
    	checkMultiNode.addActionListener(checkBoxListener);
        rightPanel.add(checkMultiNode);

        checkParallel = new JCheckBox("Parallelize within a node");
        checkParallel.setBackground(BACKGROUND_COLOR);
        checkParallel.setEnabled(false);
    	checkParallel.addActionListener(checkBoxListener);
        rightPanel.add(checkParallel);

        checkKeepGoing = new JCheckBox("Keep going after errors");
        checkKeepGoing.setBackground(BACKGROUND_COLOR);
        checkKeepGoing.setEnabled(false);
    	checkKeepGoing.addActionListener(checkBoxListener);
        rightPanel.add(checkKeepGoing);

        // Adds space the right region between the parameter checkboxes and the clean button
        rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Builds the 'Clean Intermediate' button
        cleanButton = new JButton("Clean All");
        cleanButton.setFont(CLEAR_BUTTON_FONT);
        cleanButton.setForeground(new Color(207, 95, 95));
        cleanButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { cleanButtonPressed(); }
        });
        cleanButton.setEnabled(false);
        rightPanel.add(cleanButton);

        // Adds some vertical glue to the bottom of rightPanel
        rightPanel.add(Box.createVerticalGlue());

        return rootPanel;
    }

    /**
     * Builds and returns the row of the GUI displaying a summary of progress 
     * while the makefile analysis runs 
     */
    private JPanel buildSummaryRow() {

        // Create rootPanel
        JPanel rootPanel = new JPanel();
        rootPanel.setBackground(BACKGROUND_COLOR);
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.X_AXIS));

        // Create leftPanel and add to rootPanel
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(BACKGROUND_COLOR);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        rootPanel.add(leftPanel);
        rootPanel.add(Box.createRigidArea(new Dimension(INTER_COLUMN_SPACER, 0)));

        // Create rightPanel and add to rootPanel
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(BACKGROUND_COLOR);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rootPanel.add(rightPanel);
        rootPanel.add(Box.createHorizontalGlue());

        // Create timeElapsedLabel and add to leftPanel
        JLabel timeElapsedLabel = new JLabel("Time Elapsed:");
        timeElapsedLabel.setFont(BOLD_FONT_18PT);
        timeElapsedLabel.setForeground(Color.BLACK);
        timeElapsedLabel.setAlignmentX(RIGHT_ALIGNMENT);
        leftPanel.add(timeElapsedLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, SUMMARY_ROW_SPACER)));

        // Create targetFilesLabel and add to leftPanel
        JLabel targetFilesLabel = new JLabel("Target Files:");
        targetFilesLabel.setFont(BOLD_FONT_18PT);
        targetFilesLabel.setForeground(Color.BLACK);
        targetFilesLabel.setAlignmentX(RIGHT_ALIGNMENT);
        leftPanel.add(targetFilesLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, SUMMARY_ROW_SPACER)));

        // Create intermediateFilesLabel and add to leftPanel
        JLabel intermediateFilesLabel = new JLabel("Intermediate Files:");
        intermediateFilesLabel.setFont(PLAIN_FONT_18PT);
        intermediateFilesLabel.setForeground(Color.GRAY);
        intermediateFilesLabel.setAlignmentX(RIGHT_ALIGNMENT);
        leftPanel.add(intermediateFilesLabel);

        leftPanel.add(Box.createRigidArea(new Dimension(0, SUMMARY_ROW_SPACER)));
        leftPanel.add(Box.createRigidArea(new Dimension(0, SUMMARY_ROW_SPACER)));
        leftPanel.add(Box.createRigidArea(new Dimension(0, SUMMARY_ROW_SPACER)));

        // Create errorsLabel and add to leftPanel
        /*JLabel errorsLabel = new JLabel("Errors:");
        errorsLabel.setFont(PLAIN_FONT_18PT);
        errorsLabel.setForeground(Color.GRAY);
        errorsLabel.setAlignmentX(RIGHT_ALIGNMENT);
        leftPanel.add(errorsLabel);*/

        // Create timeElapsedValue and add to rightPanel
        timeElapsedValue = new JLabel("0:00");
        timeElapsedValue.setFont(BOLD_FONT_18PT);
        timeElapsedValue.setAlignmentX(LEFT_ALIGNMENT);
        rightPanel.add(timeElapsedValue);
        rightPanel.add(Box.createRigidArea(new Dimension(0, SUMMARY_ROW_SPACER)));

        // Create targetFilesValue and add to rightPanel
        targetFilesValue = new JLabel(NA_AMOUNT);
        targetFilesValue.setFont(BOLD_FONT_18PT);
        rightPanel.add(targetFilesValue);
        rightPanel.add(Box.createRigidArea(new Dimension(0, SUMMARY_ROW_SPACER)));

        // Create intermediateFilesValue and add to rightPanel
        intermediateFilesValue = new JLabel(NA_AMOUNT);
        intermediateFilesValue.setFont(PLAIN_FONT_18PT);
        intermediateFilesValue.setForeground(Color.GRAY);
        rightPanel.add(intermediateFilesValue);
        rightPanel.add(Box.createRigidArea(new Dimension(0, SUMMARY_ROW_SPACER)));
        rightPanel.add(Box.createRigidArea(new Dimension(0, SUMMARY_ROW_SPACER)));
        rightPanel.add(Box.createRigidArea(new Dimension(0, SUMMARY_ROW_SPACER)));

        // Create errorsValue and add to rightPanel
        /*errorsValue = new JLabel("0");
        errorsValue.setFont(PLAIN_FONT_18PT);
        errorsValue.setForeground(Color.GRAY);
        rightPanel.add(errorsValue);*/

        // Return the rootPanel
        return rootPanel;
    }

    /**
     * Builds and returns the row of the GUI for displaying details on errors 
     * encountered while running the makefile analysis 
     */
    private JPanel buildErrorPanel() {

        JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.X_AXIS));
        rootPanel.setBackground(BACKGROUND_COLOR);

        errorsTextArea = new JEditorPane();
        errorsTextArea.setContentType("text/html");
        errorsTextArea.setText("");
        errorsTextArea.setEditable(true);

        JScrollPane scrollPane = new JScrollPane(errorsTextArea);
        scrollPane.setPreferredSize(new Dimension(0,2000));
        rootPanel.add(scrollPane);

        return rootPanel;
    }

    /**
     * Loads a makefile based analysis to be run from the 'Run' tab
     */
    public void loadMakefileAnalysis(MakefileAnalysis analysis) {
        this.analysis = analysis;
        reset();
    }

    /**
     * Resets the GUI to a default state
     */
    private void reset() {
        System.out.println("RunTab.java - reset()");

        runButton.setEnabled(true);
        cleanButton.setEnabled(true);
        resetCheckboxes();

        makeCommand.setText(DEFAULT_COMMAND);
        timeElapsedValue.setText("0:00");
        timeElapsedValue.setForeground(Color.BLACK);
    }

    /**
     * Resets the checkboxes
     */
    private void resetCheckboxes() {
        checkMultiNode.setEnabled(true);
        checkMultiNode.setSelected(false);
        checkParallel.setEnabled(true);
        checkParallel.setSelected(false);
        checkKeepGoing.setEnabled(true);
        checkKeepGoing.setSelected(false);
    }

    /**
     * Executes actions attached to the 'Run' button. Starts the analysis
     * running and monitors the progress.
     */
    private void runButtonPressed() {

        analysis.setMakeCommand(makeCommand.getText());
        analysisWorker = new AnalysisWorker(analysis);

        System.out.println("RunTab.java - runButtonPressed() analysis started");
        runButton.setEnabled(false);
        cleanButton.setEnabled(false);

        checkMultiNodePrevious = checkMultiNode.isEnabled();
        checkMultiNode.setEnabled(false);

        checkParallelPrevious = checkParallel.isEnabled();
        checkParallel.setEnabled(false);

        checkKeepGoingPrevious = checkKeepGoing.isEnabled();
        checkKeepGoing.setEnabled(false);

        cleanStreamsAndErrorTrees();
        timeElapsedValue.setForeground(Color.BLACK);
        startTime = System.currentTimeMillis();
        analysisWorker.execute();
        System.out.println("RunTab.java - runButtonPressed() analysis executing");

        // Setup and start clockTimer
        clockTimer = new Timer(0, null);
        ActionListener clockUpdater = new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
                long currentTime = System.currentTimeMillis();
                long elapsedTime = (currentTime - startTime) / 1000;
                timeElapsedValue.setText(formatSecs(elapsedTime));
                if (analysisWorker.isDone()) {
                    updateSummary();

                    if (analysis.isComplete()) {
                        clockTimer.stop();
                        summaryTimer.stop();
                        runButton.setEnabled(true);
                        cleanButton.setEnabled(true);
                        checkMultiNode.setEnabled(checkMultiNodePrevious);
                        checkParallel.setEnabled(checkParallelPrevious);
                        checkKeepGoing.setEnabled(checkKeepGoingPrevious);

                        int exitCode = analysisWorker.getExitCode();
                        boolean errorStreamEmpty = analysisWorker.errorStreamEmpty();
                        if (exitCode == 0 && errorStreamEmpty) {
                            timeElapsedValue.setForeground(SUCCESS_GREEN);
                        } else {
                            timeElapsedValue.setForeground(ERROR_RED);
                        }

                    } else {
                        waitForFilesCounter += 1;
                        if (waitForFilesCounter > MAX_WAIT) {
                            clockTimer.stop();
                            summaryTimer.stop();
                            runButton.setEnabled(true);
                            cleanButton.setEnabled(true);
                            checkMultiNode.setEnabled(checkMultiNodePrevious);
                            checkParallel.setEnabled(checkParallelPrevious);
                            checkKeepGoing.setEnabled(checkKeepGoingPrevious);

                            timeElapsedValue.setForeground(ERROR_RED);
                            System.err.println("Warning: Could not find all files produced by makefile.");
                        }
                    }

					reportStreamsAndErrorTrees();
                }
            }
        };
        clockTimer.setDelay(50);
        clockTimer.addActionListener(clockUpdater);
        clockTimer.start();

        // Setup and start summaryTimer
        summaryTimer = new Timer(0, null);
        ActionListener summaryUpdater = new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {

                updateSummary();

                if (analysisWorker.isDone()) {
                    summaryTimer.stop();
                    updateSummary();
                }
            }
        };
        summaryTimer.setDelay(5000);
        summaryTimer.addActionListener(summaryUpdater);
        summaryTimer.start();

        //reset();
    }

    /**
     * Updates the progress summary the ratios under 'Target Files' and 'Total Files'
     */
    private void updateSummary() {
        targetFilesValue.setText(analysis.getTargetRatio());
        intermediateFilesValue.setText(analysis.getIntermediateRatio());
    }

    /**
     * Produces a report from standard streams stdout and stderr. This report 
     * also includes error trees on specific sub-trees of the makefile that 
     * failed and the files these sub-trees should have produced.
     */
    private void reportStreamsAndErrorTrees() {

        String stdout = analysis.getStdout();
        String stderr = analysis.getStderr();
        String errorTrees = analysis.getErrorTrees();

        String tempText = "";
        tempText += "<html>\n";
        tempText += "  <head>\n";
        tempText += "     <style type=\"text/css\">\n";
        tempText += "       pre { background-color: white; color: #7e9ebf; font-size: 10px; font-family: monospace; }\n";
        tempText += "       span.section { font-weight: bold; }\n";
        tempText += "       span.stdout { color: #000000; }\n";
        tempText += "       span.stderr { color: #FF0000; }\n";
        tempText += "       span.red { color: #FF0000; }\n";
        tempText += "       span.redbold { color: #FF0000; font-weight: bold; }\n";
        tempText += "       span.black { color: #000000; }\n";
        tempText += "     </style>\n";
        tempText += "   </head>\n";
        tempText += "   <body>\n";
        tempText += "<pre>\n";
        tempText += "\n";
        tempText += "    <span class=\"section\">stdout</span>\n";
        tempText += "\n";
        tempText += "<span class=\"stdout\">";
        tempText += stdout;
        tempText += "</span>\n";
        tempText += "________________________________________________________________________________\n";
        tempText += "\n";
        tempText += "    <span class=\"section\">stderr</span>\n";
        tempText += "\n";
        tempText += "<span class=\"stderr\">";
        tempText += stderr;
        tempText += "</span>\n";
        tempText += "________________________________________________________________________________\n";
        tempText += "\n";
        tempText += "    <span class=\"section\">error trees</span>\n";
        tempText += "\n";
        tempText += errorTrees;
        tempText += "________________________________________________________________________________\n";
        tempText += "</pre>\n";
        tempText += "   </body>\n";
        tempText += "</html>\n";

        errorsTextArea.setText(tempText);
	}

    private void cleanStreamsAndErrorTrees() {
        errorsTextArea.setText("");
    }

    /**
     * Executes actions attached to the 'Clean Intermediate' button
     */
    private void cleanButtonPressed() {

        /*JOptionPane.showMessageDialog(this,
            "\n\nClicking OK will delete all intermediate files for this analysis\n\n<html><font color=#888888>Hint: </font>\n\n",
             ,
             JOptionPane.WARNING_MESSAGE);*/

        String title = "Clean All";
        String message = "Delete all intermediate files from this analysis?";
        int response = gui.guiDialog(title, message);
        if (response != 0) {
            return;
        }

        int exitCode = analysis.clean();

        /*if (exitCode == 0) {
            timeElapsedValue.setForeground(new Color(68, 180, 73));
        } else {
            timeElapsedValue.setForeground(Color.RED);
        }*/

        //reset();
        cleanStreamsAndErrorTrees();
        timeElapsedValue.setText("0:00");
        timeElapsedValue.setForeground(Color.BLACK);
        targetFilesValue.setText(NA_AMOUNT);
        intermediateFilesValue.setText(NA_AMOUNT);
    }

    /**
     * Executes when actions are taken on the checkboxes
     */
    private void changedCheckBox() {

        boolean multiNode = checkMultiNode.isSelected();
        boolean parallel = checkParallel.isSelected();
        boolean keepGoing = checkKeepGoing.isSelected();

		System.out.println("changedCheckBox()");

		if (multiNode) {
        	checkParallel.setEnabled(false);
        	checkKeepGoing.setEnabled(false);
        	makeCommand.setText(DEFAULT_COMMAND_QMAKE);
		} else {
            checkParallel.setEnabled(true);
        	checkKeepGoing.setEnabled(true);

            if (parallel && keepGoing) {
                checkMultiNode.setEnabled(false);
                makeCommand.setText(DEFAULT_COMMAND_PARAKEEP);
            } else if (parallel) {
                checkMultiNode.setEnabled(false);
                makeCommand.setText(DEFAULT_COMMAND_PARALLEL);
            } else if (keepGoing) {
                checkMultiNode.setEnabled(false);
                makeCommand.setText(DEFAULT_COMMAND_KEEPGOING);
            } else {
                checkMultiNode.setEnabled(true);
                makeCommand.setText(DEFAULT_COMMAND);
            }
        }
	}

    /**
     * Executes when the user edits the make command
     */
    private void changedMakeCommand() {
        resetCheckboxes();
    }

    /**
     * Formats the time elapsed string displayed while the GUI's times running 
     * analyses 
     */
    private String formatSecs(long sec) {
        if (sec < 3600) {
            return (sec / 60) + ":" + String.format("%02d", (sec % 60));
        } else {
            long hours = sec / 3600;
            sec = sec % 3600;
            return hours + ":" + String.format("%02d", (sec / 60)) + ":" + String.format("%02d", (sec % 60));
        }
    }
}
