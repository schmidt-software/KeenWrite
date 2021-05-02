#!/bin/bash

CLASSPATH=".:antlr.jar"
java -Xmx500M -cp $CLASSPATH "org.antlr.v4.Tool" "English.g4"
javac -cp $CLASSPATH *.java
java -Xmx500M -cp $CLASSPATH "org.antlr.v4.gui.TestRig" English document \
  -tree "english.txt"

