
package pipegen.gui;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import org.json.*;

import pipegen.*;
import pipegen.definitions.*;
import pipegen.instances.*;
import pipegen.exceptions.*;

public class PipegenGUI extends PGFrame {

    private static final String NO_TOOLBOX_SELECTED = "[no toolbox selected]";

    private static final ImageIcon FRAME_ICON_16x16 = new ImageIcon("resources/images/frame_icon_16x16_mod.png");
    private static final ImageIcon FRAME_ICON_32x32 = new ImageIcon("resources/images/frame_icon_32x32.png");
    private static final ImageIcon FRAME_ICON_64x64 = new ImageIcon("resources/images/frame_icon_64x64.png");
    private static final ImageIcon FRAME_ICON_128x128 = new ImageIcon("resources/images/frame_icon_128x128.png");

    private static final ImageIcon PIPELINE_TAB_ICON = new ImageIcon("resources/images/pipeline_tab_icon.png");
    private static final ImageIcon INPUTDATA_TAB_ICON = new ImageIcon("resources/images/inputdata_tab_icon.png");
    private static final ImageIcon MAKEFILE_TAB_ICON = new ImageIcon("resources/images/makefile_tab_icon.png");
    private static final ImageIcon RUN_TAB_ICON = new ImageIcon("resources/images/run_tab_icon.png");

    private static final ImageIcon PIPELINE_TAB_ICON_DARK = new ImageIcon("resources/images/pipeline_tab_icon_dark.png");
    private static final ImageIcon INPUTDATA_TAB_ICON_DARK = new ImageIcon("resources/images/inputdata_tab_icon_dark.png");
    private static final ImageIcon MAKEFILE_TAB_ICON_DARK = new ImageIcon("resources/images/makefile_tab_icon_dark.png");
    private static final ImageIcon RUN_TAB_ICON_DARK = new ImageIcon("resources/images/run_tab_icon_dark.png");

    private static final Color BORDER_COLOR = new Color(166, 159, 145); 

    // Set GUI white space dimensions
    private static final Dimension LEFT_MAIN_MARGIN = new Dimension(30, 0);
    private static final Dimension RIGHT_MAIN_MARGIN = new Dimension(30, 0);
    private static final Dimension TOP_MAIN_MARGIN = new Dimension(0, 30);
    private static final Dimension BOTTOM_MAIN_MARGIN = new Dimension(0, 10);
    private static final Dimension MAIN_VERTICAL_SPACER = new Dimension(0, 20);

    // Internal GUI data
    private JMenuBar menuBar;
    private MenuBarManager mbm;

    // Tabbed pane and component panels
    private JTabbedPane tabPane;
    private PipelineTab pipelineTab;
    private DataTab dataTab;
    private MakefileTab makefileTab;
    private RunTab runTab;

    // Internal non-GUI data
    private File toolboxesDir;
    private File toolboxFile;
    private JLabel toolboxDisplayName;
    private ToolboxDef toolboxDef;

    private PipelineInstance pipeline;
    private DataTableFile inputData;
    private String analysisName;

    public PipegenGUI(File toolboxesDir) {
        super();

        ArrayList<Image> iconList = new ArrayList<Image>();
        iconList.add(FRAME_ICON_16x16.getImage());
        iconList.add(FRAME_ICON_32x32.getImage());
        iconList.add(FRAME_ICON_64x64.getImage());
        iconList.add(FRAME_ICON_128x128.getImage());
        this.setIconImages(iconList);

        this.toolboxesDir = toolboxesDir;
        toolboxDef = null;

        analysisName = null;

        buildMenuBar();
        buildRootPanel();
        setVisible(true);
    }

    private void buildRootPanel() {

        // Initialize rootPanel and add to frame
        JPanel rootPanel = new JPanel();
        rootPanel.setBackground(BORDER_COLOR);
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.X_AXIS));
        this.add(rootPanel);

        // Add the center column with left and right margins
        rootPanel.add(Box.createRigidArea(LEFT_MAIN_MARGIN));
        JPanel centerColumn = new JPanel();
        centerColumn.setBackground(BORDER_COLOR);
        centerColumn.setLayout(new BoxLayout(centerColumn, BoxLayout.Y_AXIS));
        rootPanel.add(centerColumn);
        rootPanel.add(Box.createRigidArea(RIGHT_MAIN_MARGIN));

        // Add toolboxPanel to center column
        centerColumn.add(Box.createRigidArea(TOP_MAIN_MARGIN));
        JPanel toolboxPanel = buildToolboxPanel();
        toolboxPanel.setBackground(BORDER_COLOR);
        centerColumn.add(toolboxPanel);

        // Add tabPane to center column
        centerColumn.add(Box.createRigidArea(MAIN_VERTICAL_SPACER));
        tabPane = buildTabPane();
        centerColumn.add(tabPane);
        centerColumn.add(Box.createVerticalGlue());
        centerColumn.add(Box.createRigidArea(BOTTOM_MAIN_MARGIN));
    }

    private JPanel buildToolboxPanel() {
        JPanel toolboxPanel = new JPanel();
        toolboxPanel.setLayout(new BoxLayout(toolboxPanel, BoxLayout.X_AXIS));
        toolboxDisplayName = new JLabel();
        if (toolboxDef != null) {
            toolboxDisplayName.setText(toolboxDef.getName());
        } else {
            toolboxDisplayName.setText(NO_TOOLBOX_SELECTED);
        }
        toolboxDisplayName.setForeground(Color.white);
        toolboxPanel.add(toolboxDisplayName);
        toolboxPanel.add(Box.createHorizontalGlue());
        return toolboxPanel;
    }

    private JTabbedPane buildTabPane() {

        JTabbedPane tabPane = new JTabbedPane();

        tabPane.setPreferredSize(new Dimension(6000, 6000));

        pipelineTab = new PipelineTab(this);
        dataTab = new DataTab();
        makefileTab = new MakefileTab();
        runTab = new RunTab(this);

        tabPane.addTab("Pipeline", PIPELINE_TAB_ICON_DARK, pipelineTab);
        tabPane.addTab("Input Data", INPUTDATA_TAB_ICON, dataTab);
        tabPane.addTab("Makefile", MAKEFILE_TAB_ICON, makefileTab);
        tabPane.addTab("Run", RUN_TAB_ICON, runTab);

        // Build a ChangeListener and attach it to the tabPane
        ChangeListener changeListener = new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
                int index = sourceTabbedPane.getSelectedIndex();

                if (sourceTabbedPane.getTitleAt(index).equals("Pipeline")) {
                    pipelineTabActive();
                } else if (sourceTabbedPane.getTitleAt(index).equals("Input Data")) {
                    dataTabActive();
                } else if (sourceTabbedPane.getTitleAt(index).equals("Makefile")) {
                    makefileTabActive();
                } else if (sourceTabbedPane.getTitleAt(index).equals("Run")) {
                    runTabActive();
                }
            }
        };
        tabPane.addChangeListener(changeListener);

        return tabPane;
    }

    private void pipelineTabActive() {
        tabPane.setIconAt(0, PIPELINE_TAB_ICON_DARK);
        tabPane.setIconAt(1, INPUTDATA_TAB_ICON);
        tabPane.setIconAt(2, MAKEFILE_TAB_ICON);
        tabPane.setIconAt(3, RUN_TAB_ICON);
    }

    private void dataTabActive() {
        tabPane.setIconAt(0, PIPELINE_TAB_ICON);
        tabPane.setIconAt(1, INPUTDATA_TAB_ICON_DARK);
        tabPane.setIconAt(2, MAKEFILE_TAB_ICON);
        tabPane.setIconAt(3, RUN_TAB_ICON);
    }

    private void makefileTabActive() {
        tabPane.setIconAt(0, PIPELINE_TAB_ICON);
        tabPane.setIconAt(1, INPUTDATA_TAB_ICON);
        tabPane.setIconAt(2, MAKEFILE_TAB_ICON_DARK);
        tabPane.setIconAt(3, RUN_TAB_ICON);
    }

    private void runTabActive() {
        tabPane.setIconAt(0, PIPELINE_TAB_ICON);
        tabPane.setIconAt(1, INPUTDATA_TAB_ICON);
        tabPane.setIconAt(2, MAKEFILE_TAB_ICON);
        tabPane.setIconAt(3, RUN_TAB_ICON_DARK);
    }

    private void buildMenuBar() {

        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        mbm = new MenuBarManager(this);
        menuBar = mbm.getMenuBar();
        setJMenuBar(menuBar);
    }

    private void selectPipelineTab() {
        tabPane.setSelectedIndex(0);
    }

    private void selectDataTab() {
        tabPane.setSelectedIndex(1);
    }

    private void selectMakefileTab() {
        tabPane.setSelectedIndex(2);
    }

    private void selectRunTab() {
        tabPane.setSelectedIndex(3);
    }

    public void openPipeline() {

        if (toolboxFile == null) {
            JOptionPane.showMessageDialog(this,
                "\n\nFirst select a toolbox before opening pipelines\n\n<html><font color=#888888>Hint: Alt-S</font>\n\n",
                "No Toolbox Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        UIManager.put("FileChooser.readOnly", Boolean.TRUE);
        final File dirToLock = new File(toolboxFile + "/pipelines/");
        JFileChooser fc = new JFileChooser(dirToLock);
        fc.setFileView(new FileView() {
            @Override
            public Boolean isTraversable(File f) {
                return dirToLock.equals(f);
            }
        });

        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setDialogTitle("Open Pipeline");

        FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON Files", "json", "json");
        fc.setFileFilter(filter);

        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            PipelineInstance oldPipeline = pipeline;

            try {
                PipelineDef definition = new PipelineDef(toolboxDef, file);
                pipeline = new PipelineInstance(definition);
                pipeline.load();

                pipelineTab.setPipeline(pipeline);
                pipelineTab.draw();
                selectPipelineTab();
            } catch (InvalidWorkflowDefException e) {
                pipeline = oldPipeline;
                e.printStackTrace();
                System.err.println("Exception: Could not use pipeline file - " + file.getAbsolutePath() + ". See stack trace.");
                JOptionPane.showMessageDialog(this,
                    "\n\n\nThe file you selected does not contain a valid pipeline.\nPlease verify this is the file you intended and check its format.\n\nFile = " + file.getName() + "\n\n\n",
                    "Invalid Pipeline",
                    JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private boolean isPipelineReady() {
        if (toolboxFile == null) {
            JOptionPane.showMessageDialog(this,
                "\n\n\nYou must select a toolbox before saving this pipeline.\n\n\n",
                "No Toolbox Selected",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    private void savePipeline(String pipelineName) {
        PrintWriter writer = null;
        BufferedWriter buffer = null;
        try {
            JSONObject pipelineJSON = pipeline.save();

            writer = new PrintWriter(pipeline.getFile(), "UTF-8");
            buffer = new BufferedWriter(writer);
            buffer.write(pipelineJSON.toString());
            buffer.close();

        } catch (JSONException e) {
            e.printStackTrace();
            System.err.println("Exception: Could not save pipeline. See stack trace.");
            JOptionPane.showMessageDialog(this,
                "\n\n\nError: Could not save pipeline. JSONException.\n\n\n",
                "Error Saving Pipeline",
                JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Exception: Could not save pipeline. See stack trace.");
            JOptionPane.showMessageDialog(this,
                "\n\n\nError: Could not save pipeline. IOException.\n\n\n",
                "Error Saving Pipeline",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
                if (buffer != null) {
                    buffer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void savePipeline() {

        if (! isPipelineReady()) {
            return;
        }

        if (pipeline.getName() == null) {
            savePipelineAs();
            return;
        }

        savePipeline(pipeline.getName());
    }

    public void savePipelineAs() {

        UIManager.put("FileChooser.readOnly", Boolean.FALSE);
        final File dirToLock = new File(toolboxFile + "/pipelines/");
        JFileChooser fc = new JFileChooser(dirToLock);
        fc.setFileView(new FileView() {
            @Override
            public Boolean isTraversable(File f) {
                return dirToLock.equals(f);
            }
        });

        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setDialogTitle("Save Pipeline As");

        FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON Files", "json", "json");
        fc.setFileFilter(filter);

        int returnVal = fc.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            String fileName = file.getName();

            if (! fileName.endsWith(".json")) {
                fileName = fileName + ".json";
            }

            File saveFile = pipeline.getSaveFile(fileName);

            try {
                String saveCanonical = saveFile.getCanonicalPath();
                String pipelineCanonical = pipeline.getFile().getCanonicalPath();

                if (! pipeline.getFile().isFile() || saveCanonical.equals(pipelineCanonical)) {
                    savePipeline(fileName);
                } else {
                    int i = guiDialog("Warning: Overwrite Existing Pipeline?", "Do you want to overwrite existing pipeline '" + fileName + "'?");
                    if (i == 0) {
                        pipeline.setFile(fileName);
                        savePipeline(fileName);
                    }
                }
            } catch (IOException e) {
                int i = guiDialog("Warning: Overwrite Existing Pipeline?", "Do you want to overwrite existing pipeline '" + fileName + "'?");
                if (i == 0) {
                    pipeline.setFile(fileName);
                    savePipeline(fileName);
                }
            }
        }
    }

    public void openInputData() {
        UIManager.put("FileChooser.readOnly", Boolean.TRUE);
        final JFileChooser fc = new JFileChooser("./src/test/workingDir/inputs/");
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            DataTableFile oldInputData = inputData;
            inputData = new DataTableFile(file.getAbsolutePath());
            try {
                inputData.loadData();
                dataTab.setInputData(inputData);
                dataTab.rebuild();
                selectDataTab();
            } catch (InvalidCSVFileException e) {
                inputData = oldInputData;
                e.printStackTrace();
                System.err.println("Exception: Could not use input CSV file - " + file.getAbsolutePath() + ". See stack trace.");
                JOptionPane.showMessageDialog(this,
                    "\n\n\nThe file you selected does not have valid input data.\nPlease verify this is the file you intended and check its format.\n\nFile = " + file.getName() + "\n\n\n",
                    "Invalid Input Data",
                    JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    public void openAnalysis() {

        System.out.println("PipegenGUI.java - openAnalysis()");
        if (toolboxFile == null) {
            JOptionPane.showMessageDialog(this,
                "\n\nFirst select a toolbox before opening analyses\n\n<html><font color=#888888>Hint: Alt-S</font>\n\n",
                "No Toolbox Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        UIManager.put("FileChooser.readOnly", Boolean.TRUE);
        final File dirToLock = new File(toolboxFile + "/analyses/");
        JFileChooser fc = new JFileChooser(dirToLock);
        fc.setFileView(new FileView() {
            @Override
            public Boolean isTraversable(File f) {
                return dirToLock.equals(f);
            }
        });

        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setDialogTitle("Open Analysis");

        FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON Files", "json", "json");
        fc.setFileFilter(filter);

        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File analysisFile = fc.getSelectedFile();

            // ***
            //System.out.println("In PipegenGUI.java - openAnalysis() not opening analysis yet...");

            MakefileAnalysis analysis = new MakefileAnalysis(analysisFile);
    


            // 1.) make a MakefileAnalysis constructor that takes the .json file
            // 2.) MakefileAnalysis load() that fills in PipelineInstance and DataTableFile
            // 3.) update displays for PipelineInstance and DataTableFile to show the newly loaded data
            // 4.) basically save the analysis generate the make file display that and move to the RunTab

            //PipelineInstance oldPipeline = pipeline;

            

            /*try {
                PipelineDef definition = new PipelineDef(toolboxDef, file);
                pipeline = new PipelineInstance(definition);
                pipeline.load();

                pipelineTab.setPipeline(pipeline);
                pipelineTab.draw();
                selectPipelineTab();
            } catch (InvalidWorkflowDefException e) {
                pipeline = oldPipeline;
                e.printStackTrace();
                System.err.println("Exception: Could not use pipeline file - " + file.getAbsolutePath() + ". See stack trace.");
                JOptionPane.showMessageDialog(this,
                    "\n\n\nThe file you selected does not contain a valid pipeline.\nPlease verify this is the file you intended and check its format.\n\nFile = " + file.getName() + "\n\n\n",
                    "Invalid Pipeline",
                    JOptionPane.WARNING_MESSAGE);
            }*/
        }
    }

    private boolean isAnalysisReady() {
        if (toolboxFile == null) {
            guiWarning("No Toolbox Selected", "First select a toolbox in which to build the analysis", "Alt-S");
            return false;
        }

        if (pipeline == null) {
            guiWarning("No Pipeline", "First open a pipeline describing the workflow for the analysis", "Ctrl-O");
            return false;
        }

        if (inputData == null) {
            guiWarning("No Input Data", "First open input data on which to perform the analysis", "Ctrl-D");
            return false;
        }

        if (pipeline.hasUnsavedChanges()) {
            guiWarning("Unsaved Pipeline Changes", "This pipeline has unsaved changes.", "Ctrl-S");
            return false;
        }

        String[] missingData = pipeline.getMissingData(inputData);
        String[] unusedFields = pipeline.getUnusedData(inputData);
        if (missingData != null || unusedFields != null) {

            String title = "Error Mismatched Data Fields";
            String message = "";
            message += "<html><font color=#dd2222>Pipeline:</font> " + pipeline.getName() + "\n\n";
            message += "<html><font color=#888888>The data table must have a column corresponding to each source and sink in</font>\n";
            message += "<html><font color=#888888>the pipeline. The column names must match the source/sink names exactly.</font>\n\n";

            if (missingData != null) {
                message += "<html><font color=#dd2222>[-] Missing data fields, N = " + missingData.length + " :</font>\n";
                for (int i=0; i < missingData.length; i++) {
                    if (i < 9) {
                        message += "     " + missingData[i] + "\n";
                    } else {
                        message += "     . . .\n";
                        break;
                    }
                }
                message += "\n";
            }

            if (unusedFields != null) {
                message += "<html><font color=#dd2222>[+] Unused data fields, N = " + unusedFields.length + " :</font>\n";
                for (int i=0; i < unusedFields.length; i++) {
                    if (i < 9) {
                        message += "     " + unusedFields[i] + "\n";
                    } else {
                        message += "     . . .\n";
                        break;
                    }
                }
            }

            guiError(title, message);
            return false;
        }

        return true;
    }

    private void saveAnalysis(String analysisName, DataTableFile inputData, PipelineInstance pipeline) {
        try {
            File toolboxDir = new File(pipeline.getToolbox().getDir().toString());
            MakefileAnalysis analysis = new MakefileAnalysis(analysisName, inputData, pipeline);

            File analysisFile = new File(toolboxFile + "/analyses/" + analysisName + ".json");
            JSONObject analysisJSON = analysis.save();
            PrintWriter writer = new PrintWriter(analysisFile, "UTF-8");
            BufferedWriter buffer = new BufferedWriter(writer);
            buffer.write(analysisJSON.toString());
            buffer.close();

            makefileTab.loadMakefileAnalysis(analysis);
            runTab.loadMakefileAnalysis(analysis);
            selectRunTab();
        } catch (InvalidMakefileException e) {
            e.printStackTrace();
            System.err.println("Exception: Could not save analysis. See stack trace.");
            guiError("Error Saving Analysis", "Could not save analysis.");
        } catch (JSONException e) {
            e.printStackTrace();
            System.err.println("Exception: Could not save analysis. See stack trace.");
            guiError("Error Saving Analysis", "Could not save analysis.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("Exception: Could not save analysis. See stack trace.");
            guiError("Error Saving Analysis", "Could not save analysis.");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Exception: Could not save analysis. See stack trace.");
            guiError("Error Saving Analysis", "Could not save analysis.");
        }
    }

    public void saveAnalysis() {

        if (! isAnalysisReady()) {
            return;
        }

        if (analysisName == null) {
            saveAnalysisAs();
            return;
        }

        saveAnalysis(analysisName, inputData, pipeline);
    }

    public void saveAnalysisAs() {

        if (! isAnalysisReady()) {
            return;
        }

        UIManager.put("FileChooser.readOnly", Boolean.FALSE);
        final File dirToLock = new File(toolboxFile + "/analyses/");
        JFileChooser fc = new JFileChooser(dirToLock);
        fc.setFileView(new FileView() {
            @Override
            public Boolean isTraversable(File f) {
                return dirToLock.equals(f);
            }
        });

        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setDialogTitle("Save Analysis As");

        FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON Files", "json", "json");
        fc.setFileFilter(filter);

        int returnVal = fc.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            String fileName = file.getName();

            if (fileName.endsWith(".json")) {
                analysisName = fileName.substring(0, fileName.length() - 5);
            } else {
                analysisName = fileName;
            }

            File analysisFile = new File(toolboxFile + "/analyses/" + analysisName + ".json");
            if (! analysisFile.isFile()) {
                saveAnalysis(analysisName, inputData, pipeline);
            } else {
                int i = guiDialog("Warning: Overwrite Existing Analysis?", "Do you want to overwrite this analysis '" + analysisName + "'?");
                if (i == 0) {
                    saveAnalysis(analysisName, inputData, pipeline);
                }
            }
        }
    }

    public void guiWarning(String title, String warning) {

        String message = "\n\n\n" + warning + "\n\n\n";
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }

    public void guiWarning(String title, String warning, String hint) {

        String message = "\n\n\n" + warning + "\n\n<html><font color=#888888>Hint: " + hint + "</font>\n\n\n";
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }

    public void guiError(String title, String error) {

        String message = "\n\n\n" + error + "\n\n\n";
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public void guiError(String title, String error, String hint) {

        String message = "\n\n\n" + error + "\n\n<html><font color=#888888>Hint: " + hint + "</font>\n\n\n";
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public int guiDialog(String title, String dialog) {

        String message = "\n\n\n" + dialog + "\n\n\n";
        Object[] options = {"OK", "Cancel"};
        int n = JOptionPane.showOptionDialog(this,
            message,
            title,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,     //do not use a custom Icon
            options,  //the titles of buttons
            options[1]); //default button title

        return n;
    }

    public File getToolboxesDir() {
        return toolboxesDir;
    }

    public void setSelectedToolbox(String toolboxName) {

        toolboxFile = new File(toolboxesDir.getAbsolutePath() + "/" + toolboxName + "/");
        toolboxDef = new ToolboxDef(toolboxFile);

        try {
            toolboxDef.load();
            toolboxDisplayName.setText(toolboxName);
        } catch (InvalidFileFormatDefException e) {
            JOptionPane.showMessageDialog(this,
                "\n\n\nThe toolbox you selected does not have a valid file formats configuration.\n\n\n",
                "Invalid Toolbox",
                JOptionPane.WARNING_MESSAGE);
            unsetSelectedToolbox();
        } catch (InvalidModuleDefException e) {
            JOptionPane.showMessageDialog(this,
                "\n\n\nThe toolbox you selected has invalid module definitions.\n\n\n",
                "Invalid Toolbox",
                JOptionPane.WARNING_MESSAGE);
            unsetSelectedToolbox();
        } catch (InvalidWorkflowDefException e) {
            JOptionPane.showMessageDialog(this,
                "\n\n\nThe toolbox you selected has invalid pipeline definitions.\n\n\n",
                "Invalid Toolbox",
                JOptionPane.WARNING_MESSAGE);
            unsetSelectedToolbox();
        } catch (InvalidAboutFileException e) {
            JOptionPane.showMessageDialog(this,
                "\n\n\nThe toolbox you selected does not have a valid ABOUT file.\n\n\n",
                "Invalid Toolbox",
                JOptionPane.WARNING_MESSAGE);
            unsetSelectedToolbox();
        }
    }

    private void unsetSelectedToolbox() {
        toolboxDef = null;
        toolboxDisplayName.setText(NO_TOOLBOX_SELECTED);
        mbm.clearToolboxSelection();
    }
}
