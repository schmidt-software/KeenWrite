# Overview

Typesetting PDF files entails the following:

* Download and install typesetting software
* Download a theme pack

These are described in the subsequent sections. Once the requirements have been met, continue reading to learn how to typeset a document.

# Download typesetter

Download the typesetting software as follows:

1. Start the text editor.
1. Click **File → Export As → PDF**.
    * Note the following details (e.g., Windows X86 64-bit):
        * operating system name;
        * instruction set; and
        * architecture.
1. Click the [link](https://wiki.contextgarden.net/Installation) in the dialog.
1. Download the appropriate archive file.

# Install typesetter

This section describes the installation steps for various platforms. Follow the steps that apply to the computer's operating system:

* [Windows](#windows) (includes Windows 7, Windows 10, and similar)
* [Unix](#unix) (includes MacOS, FreeBSD, Linux, and similar)

## Windows

Proceed with a Windows installation of the typesetting software as follows:

1. Extract the `.zip` file into `C:\Users\%USERNAME%\AppData\Local\context` (the "root" directory)
1. Run **install.bat** to download and install the software.
    * If prompted, click **Run anyway** (or click **More info** first).
1. Right-click [localpath.bat](https://gitlab.com/DaveJarvis/KeenWrite/-/raw/main/scripts/localpath.bat).
1. Select **Save Link As** (or similar).
1. Save the file to the typesetting software's "root" directory.
1. Rename `localpath.bat.txt` to `localpath.bat`, if necessary.
1. Run `localpath.bat` (to set and save the `PATH` environment variable).

Installation is complete. Verify the installation as follows:

1. Type: `context --version`
1. Press `Enter`.

If version information is displayed then the software is installed correctly.

Continue by installing a [theme pack](#theme-pack).

## Unix

For Linux, MacOS, FreeBSD, and similar operating systems, proceed as follows:

1. Create `$HOME/.local/bin/context`
1. Extract the `.zip` file within `$HOME/.local/bin/context`
1. Run `sh install.sh`
1. Add `export PATH=$PATH:$HOME/.local/bin/context/tex/texmf-linux-64/bin` to the login script.

Installation is complete. Verify the installation as follows:

1. Open a new terminal (to export the new PATH setting).
1. Type: `context --version`
1. Press `Enter`.

If version information is displayed then the software is installed correctly.

Continue by installing a [theme pack](#theme-pack).

# Theme pack

A theme pack is a set of themes that define how documents appear when typeset. Broadly, themes are applied as follows:

* Install a theme pack
* Configure individual themes

## Install theme pack

Install and configure the default theme pack as follows:

1. Download the <a href="https://gitlab.com/DaveJarvis/keenwrite-themes/-/releases/permalink/latest/downloads/theme-pack.zip">theme-pack.zip</a> archive.
1. Extract archive into a known location.
1. Start the text editor, if not already running.
1. Click **Edit → Preferences**.
1. Click **Typesetting**.
1. Click **Browse** beside **Themes**.
1. Navigate to the `themes` directory.
1. Click **Open**.
1. Click **OK**.

The theme pack is installed.

Each theme has its own requirements, described below. 

## Configure Boschet theme

Download and install the following font families:

* [Libre Baskerville](https://fonts.google.com/specimen/Libre+Baskerville)
* [Archivo Narrow](https://fonts.google.com/specimen/Archivo+Narrow)
* [Inconsolata](https://fonts.google.com/specimen/Inconsolata)

The theme is configured.

# Typeset single document

Typeset a document as follows:

1. Start the text editor, if not already running.
1. Click **File → New** (or type `Ctrl+n`).
1. Type in some text.
1. Click **File → Export As → PDF** (or type `Ctrl+p`).
1. Select a theme from the drop-down list.
1. Click **OK** (or press `Enter`).
1. Set the **File name** to the PDF file name.
1. Click **Save**.

The document is typeset; open the PDF file in a PDF reader to view the result.

# Typeset multiple documents

Typeset multiple documents similar to single documents, with one difference:

* Click **File → Export As → Joined PDF** (or type `Ctrl+Shift+p`).

All documents having the same file name extension in the same directory
(or sub-directories) as the actively edited file are first concatenated then
typeset into a single PDF document. The order that files are concatenated
is numeric and alphabetic.

For example, if `1.Rmd` is a sibling of the following files in the same
directory, then all the files will be included in the PDF, as expected:

    chapter_1.Rmd
    chapter_2.Rmd
    chapter_2a.Rmd
    chapter_2b.Rmd
    chapter_3.Rmd
    chapter_10.Rmd

Basically, sorting honours numbers and letters in file names.

# Background 

This text editor helps keep content separated from presentation. Plain text documents will remain readable long after proprietary formats have become obsolete. However, we've come to expect much more in what we read than mere text: from hyperlinked tables of contents to indexes, from footers to footnotes, from mathematical expressions to complex graphics, modern documents are nuanced and multifaceted.

## History

Before computer-based typesetting, much of mathematics was put to page by hand. Professional typesetters, who were often expensive and usually not mathematicians, would inadvertently introduce typographic errors into equations. Phototypesetting technology improved upon hand-typesetting, but well-known computer scientist Donald Knuth---whose third volume of *The Art of Computer Programming* was phototypeset in 1976---expressed dissatisfaction with its typographic quality. He set himself two goals: let anyone create high-quality books without much effort and provide software that typesets consistently on all capable computers. Two years later, he released a typesetting system and a font description language: TeX and METAFONT, respectively.

In short, TeX is software that helps typeset plain text documents.

## ConTeXt

Programming computers to typeset internationalized text automatically at the level we've become accustomed takes decades of development effort. Many free and open source software solutions can typeset text, including: ConTeXt, LaTeX, Sile, and others. ConTeXt, which builds upon TeX, is ideal for typesetting plain text into beautiful documents because it is developed with a notion of *setups*. These setups can wholly describe how text is to be typeset and---by being external to the text itself---configuring setups provides ample control over the document's final appearance without changing the prose.

# Further reading

Here are a few documents that introduce the typesetting system:

* [What is ConTeXt?](https://www.pragma-ade.com/general/manuals/what-is-context.pdf)
* [A not so short introduction to ConTeXt](https://github.com/contextgarden/not-so-short-introduction-to-context)
* [Dealing with XML in ConTeXt MKIV](https://pragma-ade.com/general/manuals/xml-mkiv.pdf)
* [Typographic Programming](https://www.pragma-ade.com/general/manuals/style.pdf)

The [documentation library](https://wiki.contextgarden.net/Documentation) includes the following gems:

* [ConTeXt Manual](https://www.pragma-ade.nl/general/manuals/ma-cb-en.pdf)
* [ConTeXt command reference](https://www.pragma-ade.nl/general/qrcs/setup-en.pdf)
* [METAFUN Manual](https://www.pragma-ade.nl/general/manuals/metafun-p.pdf)
* [It's in the Details](https://www.pragma-ade.nl/general/manuals/details.pdf)
* [Fonts out of ConTeXt](https://www.pragma-ade.com/general/manuals/fonts-mkiv.pdf)

Expert-level documentation includes the [LuaTeX Reference Manual](https://www.pragma-ade.nl/general/manuals/luatex.pdf).

