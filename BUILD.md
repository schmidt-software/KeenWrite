# Introduction

This document describes how to build the application and platform binaries.

# Requirements

Download and install the following software packages:

* [OpenJDK 14](https://openjdk.java.net)
* [Gradle 6.4](https://gradle.org/releases)

# Build

Build the application überjar as follows:

    gradle clean jar

The application is built.

# Run

After the application is compiled, run it as follows:

    java -jar build/libs/keenwrite.jar

On Windows:

    java -jar build\libs\keenwrite.jar

# Installers

This section describes how to set up the development environment and
build native executables for supported operating systems.

## Setup

Follow these one-time setup instructions to begin:

1. Ensure `$HOME/bin` is set in the `PATH` environment variable.
1. Move `build-template` into `$HOME/bin`.

Setup is complete.

## Binaries

Run the `installer` script to build platform-specific binaries, such as:

    ./installer -V -o linux

The `installer` script:

* downloads a JDK;
* generates a run script;
* bundles the JDK, run script, and JAR file; and
* creates a standalone binary, so no installation required.

Run `./installer -h` to see all command-line options.

# Versioning

Version numbers are read directly from Git using a plugin. The version
number is written to `app.properties`, a properties file in the `resources`
directory that can be read from within the application.

