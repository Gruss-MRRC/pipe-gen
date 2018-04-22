#!/bin/sh

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

java -cp "${CLASSPATH}" StartPipegen
