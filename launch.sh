#!/bin/sh

JAVAMODULE=java/1.7.0_67

# Absolute path to this script
# A roughly portable version of "readlink -f"
SCRIPT="${PWD}/${0}"
# Absolute path to the directory this script is in
PROJECT_DIR=`dirname ${SCRIPT}`
# Absolute path to compiled class files
BUILD_DIR=${PROJECT_DIR}/build/classes/
# Absolute path to library jar files
LIB_DIR=${PROJECT_DIR}/src/main/lib/

cd ${PROJECT_DIR}

# Set class path
CLASSPATH=${BUILD_DIR}:${LIB_DIR}/java-json.jar


#for f in lib/*.jar lib/dev/*.jar resources/; do
#    CLASSPATH=$CLASSPATH:$f
#done

if [ "${OSTYPE}" = "cygwin" ]; then
    CLASSPATH=`cygpath -wp ${CLASSPATH}`
fi

TEST_DIR=${PROJECT_DIR}/src/test

makefileName=copySomeFiles.mk
outputDir=${TEST_DIR}/workingDir/results/
toolboxDir=${PROJECT_DIR}/toolboxes/sampleToolbox/
workflowName=workflow_copyFiles.json
inputTable=${TEST_DIR}/workingDir/inputs/data.csv

#java -cp "${CLASSPATH}" StartPipegen ${makefileName} ${outputDir} ${toolboxDir} ${workflowName} ${inputTable}
module load ${JAVAMODULE}
java -cp "${CLASSPATH}" StartPipegen

#makefileFile=${toolboxDir}/makefiles/${makefileName}
#if [ -f ${makefileFile} ]; then
#    echo "-----------------------------------------------------"
#    cat ${makefileFile}
#    echo "-----------------------------------------------------"
#fi

module unload ${JAVAMODULE}
exit 0
