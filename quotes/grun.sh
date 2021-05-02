#!/bin/bash

CLASSPATH=".:antlr.jar"
ARG_FILENAME="$1"

if [ -z "$ARG_FILENAME" ]; then
  ARG_FILENAME="english.txt"
fi

java -Xmx500M -cp $CLASSPATH "org.antlr.v4.Tool" "English.g4"
javac -cp $CLASSPATH *.java
java -Xmx500M -cp $CLASSPATH "org.antlr.v4.gui.TestRig" English document \
  -tree "$ARG_FILENAME"

