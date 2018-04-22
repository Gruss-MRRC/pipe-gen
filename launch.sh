#!/bin/sh

# Absolute path to this script
# A roughly portable version of "readlink -f"
SCRIPT="${PWD}/${0}"

# Absolute path to the directory this script is in
PROJECT_DIR=`dirname ${SCRIPT}`

# Run jarfile
java -jar "${PROJECT_DIR}/build/libs/pipe-gen.jar"
