![Total Downloads](https://img.shields.io/github/downloads/DaveJarvis/keenwrite/total?color=blue&label=Total%20Downloads&style=flat) ![Release Downloads](https://img.shields.io/github/downloads/DaveJarvis/keenwrite/latest/total?color=purple&label=Release%20Downloads&style=flat) ![Release Date](https://img.shields.io/github/release-date/DaveJarvis/keenwrite?color=red&style=flat&label=Release%20Date) ![Release Version](https://img.shields.io/github/v/release/DaveJarvis/keenwrite?style=flat&label=Release)

# ![Logo](docs/images/app-title.png)

A text editor that uses [interpolated strings](https://en.wikipedia.org/wiki/String_interpolation) to reference values defined externally.

## Download

Download one of the following editions:

* [Windows](https://gitreleases.dev/gh/DaveJarvis/keenwrite/latest/keenwrite.exe)
* [Linux](https://gitreleases.dev/gh/DaveJarvis/keenwrite/latest/keenwrite.bin)
* [Java Archive](https://gitreleases.dev/gh/DaveJarvis/keenwrite/latest/keenwrite.jar)

## Run

Note that the first time the application runs, it will unpack itself into a local directory. Subsequent starts will be faster.

### Windows

When upgrading to a new version, delete the following directory:

    C:\Users\%USERNAME%\AppData\Local\warp\packages\keenwrite.exe

Double-click the application to start; give the application permission to run.

### Linux

Execute the following commands in a terminal:

``` bash
chmod +x keenwrite.bin
./keenwrite.bin
```

### Other

On other platforms, start the application as follows:

1. Download the *full version* of the Java Runtime Environment, [JRE 17](https://bell-sw.com/pages/downloads/?version=java-17).
1. Install the JRE.
1. Open a terminal window.
1. Verify the installation: `java -version`
1. Make `keenwrite.sh` executable.
1. Run: `./keenwrite.sh`

The application is started.

## Features

The application offers:

* User-defined interpolated strings
* Auto-complete variable names based on variable values
* High-quality PDF exports
* Real-time spell check
* Real-time rendering of math using TeX notation
* Real-time document statistics (with CJK word separation)
* Diagrams: Mermaid, GraphViz, UML, sequence, timing, and more
* Dark, custom, and responsive user interface skins
* Integrated file manager
* Interactive document outline
* Internationalized font support (e.g., Chinese, Japanese, Korean, etc.)
* Support for Pandoc's fenced div extended attribute syntax
* R integration
* Customizable user interface having detachable tabs
* Platform-independent (Windows, Linux, MacOS)

## Usage

Read the [detailed documentation](docs/README.md) for using the application.

### Skins

Read the [skins documentation](docs/skins.md) to learn about how to change
the user interface appearance.

## Screenshots

See [screenshots](docs/screenshots.md) for visuals.

## License

This software is licensed under the [BSD 2-Clause License](LICENSE.md) and
based on [Markdown-Writer-FX](licenses/MARKDOWN-WRITER-FX.md).

