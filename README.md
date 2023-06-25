![Total Downloads](https://img.shields.io/github/downloads/DaveJarvis/keenwrite/total?color=blue&label=Total%20Downloads&style=flat) ![Release Downloads](https://img.shields.io/github/downloads/DaveJarvis/keenwrite/latest/total?color=purple&label=Release%20Downloads&style=flat) ![Released](https://img.shields.io/github/release-date/DaveJarvis/keenwrite?color=red&style=flat&label=Released) ![Version](https://img.shields.io/github/v/release/DaveJarvis/keenwrite?style=flat&label=Release)

# ![Logo](docs/images/app-title.png)

A free, open-source, cross-platform desktop Markdown editor that can produce beautifully typeset PDFs.

## Download

Download one of the following editions:

* [Windows](https://github.com/DaveJarvis/keenwrite/releases/latest/download/keenwrite.exe)
* [Linux](https://github.com/DaveJarvis/keenwrite/releases/latest/download/keenwrite.bin)
* [Java Archive](https://github.com/DaveJarvis/keenwrite/releases/latest/download/keenwrite.jar)

## Run

Note that the first time the application runs, it will unpack itself into a local directory. Subsequent starts will be faster.

### Windows

Double-click the application to start; give the application permission to run.

### Linux

Execute the following commands in a terminal:

``` bash
chmod +x keenwrite.bin
./keenwrite.bin
```

### Other

On other platforms, such as MacOS, start the application as follows:

1. Download the *Full version* of the Java Runtime Environment, [JRE 20](https://bell-sw.com/pages/downloads).
  * Note that both Java 20+ and JavaFX are required. The *Full version* of
    BellSoft's JRE satisifies these requirements.
1. Install the JRE (include JRE's `bin` directory in the `PATH` environment variable).
1. Open a new terminal.
1. Verify the installation: `java -version`
1. Download [keenwrite.jar](https://github.com/DaveJarvis/keenwrite/releases/latest/download/keenwrite.jar).
1. Download [keenwrite.sh](https://raw.githubusercontent.com/DaveJarvis/keenwrite/master/keenwrite.sh).
1. Place the `.jar` and `.sh` in the same directory.
1. Make `keenwrite.sh` executable: `chmod +x keenwrite.sh`
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

## Typesetting

Typesetting to PDF files requires the following:

* [Theme Pack](https://github.com/DaveJarvis/keenwrite-themes/releases/latest/download/theme-pack.zip)
* [ConTeXt](https://wiki.contextgarden.net/Installation)

## Usage

Read the [detailed documentation](docs/README.md) for using the application.

### Skins

Read the [skins documentation](docs/skins.md) to learn about how to change
the user interface appearance.

## Screenshots

See [screenshots](docs/screenshots.md) for visuals.

## License

This software is licensed under the [BSD 2-Clause License](LICENSE.md) and
based on [Markdown-Writer-FX](https://github.com/JFormDesigner/markdown-writer-fx/blob/main/LICENSE).

