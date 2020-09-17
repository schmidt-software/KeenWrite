# Logging

The files in this directory can be used to log the entry/exit to every
method for debugging purposes. These changes are not meant to be pushed
onto the mainline branch (i.e., not for production use).

The instructions are relative to the directory containing these instructions.

# Build

If modifications to the existing JAR are needed, rebuild the changes
as follows:

    git clone https://github.com/javaparser/javaparser
    cd javaparser
    cp Main.java ./javaparser-core/src/main/java/com/github/javaparser/.
    mvn package -Dmaven.test.skip=true
    cp javaparser-core/target/javaparser-core-3.16.2-SNAPSHOT.jar jp.jar

The file `jp.jar` is built with `Main.class`.

# Usage

Run the `inject` script to replace the original files with the logging
versions.

# Revert

When finished building a debug version of the application, reset the repo
as follows:

    git reset --hard HEAD

