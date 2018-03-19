#-------------------------------------------------------------------------------
# Name: makefile
# Author: Michael Stockman
# Albert Einstein College of Medicine
#
# Purpose: Makes java byte code for pipe-gen
#-------------------------------------------------------------------------------


# Define project directory variables
SRC = ./src/
BUILD = ./build/classes/

PIPEGEN_SRC = ./src/main/java/edu/einstein/gmrrc/pipegen/
DEFINE_SRC = $(PIPEGEN_SRC)definitions/
DEFINITION_SRC = $(PIPEGEN_SRC)definitions/
INSTANCE_SRC = $(PIPEGEN_SRC)instances/
EXCEPTION_SRC = $(PIPEGEN_SRC)exceptions/
GUI_SRC = $(PIPEGEN_SRC)gui/

PIPEGEN_BUILD = ./build/edu/einstein/gmrrc/pipegen/
DEFINITION_BUILD = $(PIPEGEN_BUILD)definitions/
INSTANCE_BUILD = $(PIPEGEN_BUILD)instances/
EXCEPTION_BUILD = $(PIPEGEN_BUILD)exceptions/
GUI_BUILD = $(PIPEGEN_BUILD)gui/

CP = 'build/classes:src/main/lib/java-json.jar'


# Define compiler and compiler flag variables
JFLAGS = -g -d $(BUILD)
JC = javac


# Reset default values of SUFFIXES to find Java source files and Java class 
# files
.SUFFIXES: .java .class


# Define a general rule for creating .class files from .java files
# **** Doesn't seem useful yet
#.java.class:
#	$(JC) $(JFLAGS) $*.java


# List each java source code file that needs to be compiled into a class file
CLASSES = \
	$(PIPEGEN_SRC)StartPipegen.java \
	$(PIPEGEN_SRC)DataTableFile.java \
    $(PIPEGEN_SRC)MakefileAnalysis.java \
    $(PIPEGEN_SRC)MakefileFactory.java \
    $(PIPEGEN_SRC)BlockElementVisitor.java \
    $(PIPEGEN_SRC)ErrorTreeVisitor.java \
    $(PIPEGEN_SRC)AnalysisWorker.java \
	$(DEFINE_SRC)ToolboxDef.java \
    $(DEFINE_SRC)FileFormatDef.java \
    $(DEFINE_SRC)ModuleDef.java \
    $(DEFINE_SRC)ParameterDef.java \
    $(DEFINE_SRC)PipelineDef.java \
    $(INSTANCE_SRC)PipelineInstance.java \
    $(INSTANCE_SRC)BlockElement.java \
    $(INSTANCE_SRC)SourceElement.java \
    $(INSTANCE_SRC)SinkElement.java \
    $(INSTANCE_SRC)ModuleElement.java \
    $(INSTANCE_SRC)ConnectionElement.java \
    $(INSTANCE_SRC)ElementPosition.java \
    $(INSTANCE_SRC)DimensionInt.java \
    $(INSTANCE_SRC)MountPoint.java \
    $(INSTANCE_SRC)MountPointIn.java \
    $(INSTANCE_SRC)MountPointOut.java \
    $(INSTANCE_SRC)MountPointGhost.java \
    $(INSTANCE_SRC)MountPointGhostIn.java \
    $(INSTANCE_SRC)MountPointGhostOut.java \
    $(INSTANCE_SRC)Draggable.java \
    $(INSTANCE_SRC)ConnectableInput.java \
    $(INSTANCE_SRC)ConnectableOutput.java \
    $(EXCEPTION_SRC)InvalidCSVFileException.java \
    $(EXCEPTION_SRC)InvalidElementPositionException.java \
    $(EXCEPTION_SRC)InvalidFileFormatDefException.java \
    $(EXCEPTION_SRC)InvalidModuleDefException.java \
    $(EXCEPTION_SRC)InvalidSourceDefException.java \
    $(EXCEPTION_SRC)InvalidSinkDefException.java \
    $(EXCEPTION_SRC)InvalidConnectionDefException.java \
    $(EXCEPTION_SRC)InvalidWorkflowDefException.java \
    $(EXCEPTION_SRC)InvalidAboutFileException.java \
    $(EXCEPTION_SRC)InvalidParameterDefException.java \
    $(EXCEPTION_SRC)InvalidMakefileException.java \
    $(GUI_SRC)PipegenGUI.java \
    $(GUI_SRC)PGFrame.java \
    $(GUI_SRC)MenuBarManager.java \
    $(GUI_SRC)PipelineTab.java \
    $(GUI_SRC)PipelinePanel.java \
    $(GUI_SRC)PopupAddMenu.java \
    $(GUI_SRC)PopupDeleteMenu.java \
    $(GUI_SRC)DataTab.java \
    $(GUI_SRC)MakefileTab.java \
    $(GUI_SRC)RunTab.java \

# Set the default make target
all: classes


# This target entry uses Suffix Replacement to replace all .java suffixes with 
# .class suffixes
# **** Maybe this is not useful
#classes: $(CLASSES:.java=.class)


# Make all the .class files for the project
classes: $(CLASSES)
	$(JC) -g -d $(BUILD) -cp $(CP) $(CLASSES)


# Set the clean behavior
clean:
	$(RM) $(BUILD)*.class ; $(RM) -rf ./build/classes/*

