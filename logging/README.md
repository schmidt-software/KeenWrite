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

Run `Main` by passing the name of the Java class file to parse as follows:

    pushd ../src/main/java
    for i in $(find . -type f -name "*.java"); do \
      java -cp ../../../logging/jp.jar com.github.javaparser.Main "$i" > \
        "$i.jp";
    done

That command creates a series of processed Java files with a `.jp` extension.
Replace the original files with the updated versions as follows:

    find . -type f -name "*jp" -size +100c -exec \
      sh -c 'mv {} $(dirname {})/$(basename {} .jp)' \;

The `+100c` is ensures that files without contents do not overwrite
existing files. Not sure why 0-byte files are created.

# Revert

When finished building a debug version of the application, reset the repo
as follows:

    popd
    git reset --hard HEAD

