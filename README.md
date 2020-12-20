# ![Logo](docs/images/app-title.png)

A text editor that uses [interpolated strings](https://en.wikipedia.org/wiki/String_interpolation) to reference externally defined values.

## Download

Download one of the following editions:

* [Windows](https://gitreleases.dev/gh/DaveJarvis/keenwrite/latest/keenwrite.exe)
* [Linux](https://gitreleases.dev/gh/DaveJarvis/keenwrite/latest/keenwrite.bin)
* [Java Archive](https://gitreleases.dev/gh/DaveJarvis/keenwrite/latest/keenwrite.jar)

## Run

Note that the first time the application runs, it will unpack itself into a local directory. Subsequent starts will be faster.

### Windows

On Windows, double-click the application to start. You will have to give the application permission to run.

When upgrading to a new version, delete the following directory:

    C:\Users\%USERNAME%\AppData\Local\warp\packages\keenwrite.exe

### Linux

On Linux, run `chmod +x keenwrite.bin` then `./keenwrite.bin`.

### Other

On other platforms, download and install a full version of [OpenJDK 14](https://bell-sw.com/pages/downloads/?version=java-14#mn) that includes JavaFX module support, then run:

``` bash
java -jar keenwrite.jar
```

## Features

* User-defined interpolated strings
* Real-time preview with variable substitution
* Auto-complete variable names based on variable values
* XML document transformation using XSLT3 or older
* Platform independent (Windows, Linux, MacOS)
* Spellcheck while typing
* Write mathematical formulas using a subset of TeX
* R integration

## Usage

See the [detailed documentation](docs/README.md) for information about
using the application.

## Screenshot

![Screenshot with Formulas](docs/images/equations.png)

## License

This software is licensed under the [BSD 2-Clause License](LICENSE.md) and
based on [Markdown-Writer-FX](licenses/MARKDOWN-WRITER-FX.md).

