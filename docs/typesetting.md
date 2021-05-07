# Overview

This document describes how to typeset from within the text editor. The requirements include:

* Download and install typesetting software
* Download a theme pack

These are described in the subsequent sections. Once the requirements have been met, continue reading to learn how to typeset a document.

# Install typesetter

Install ConTeXt as follows:

1. Start the text editor.
1. Click **File → Export As → PDF** (or type `Ctrl+p`).
1. Note the operating system name, instruction set, and architecture (e.g., Windows X86 64-bit).
1. Click the [link](https://wiki.contextgarden.net/Installation) in the dialog.
1. Download the ConTeXt version for your computer's operating system.
1. Follow the step-by-step instructions on the ConTeXt installation web page.

ConTeXt is installed.

**Note:** The `PATH` environment variable must include the ConTeXt `bin` directory, otherwise the text editor will not be able to generate PDF files.

# Install theme packs

A theme defines how documents appear when typeset. A theme pack is a collection of themes in a zip file. Each theme has its own requirements, described in a separate section, below. Install and configure a theme as follows:

1. Download the <a href="https://github.com/DaveJarvis/keenwrite-themes/raw/main/theme-packs.zip">theme-packs.zip</a> archive.
1. Extract archive into a known location.
1. Start the text editor.
1. Click **Edit → Preferences**.
1. Click **Typesetting**.
1. Click **Browse** beside **Directories**.
1. Navigate to the location where the themes are extracted.
1. Select the **tarmes** directory.
1. Click **Open**.
1. Click **OK**.

The "tarmes" theme is applied.

## Tarmes theme

Typesets using a pre-packaged TeX Gyre Termes font.

## Handrit theme

Typesets a manuscript using the pre-packaged TeX Gyre Cursor font.

## Boschet theme

To use this theme, download and install the following font families:

* [Libre Baskerville](https://fonts.google.com/specimen/Libre+Baskerville)
* [Archivo Narrow](https://fonts.google.com/specimen/Archivo+Narrow)
* [Inconsolata](https://fonts.google.com/specimen/Inconsolata)

# Typeset document

Typeset a document as follows:

1. Start the text editor.
1. Click **File → New** (or type `Ctrl+n`).
1. Type in some text.
1. Click **File → Export As → PDF** (or type `Ctrl+p`).
1. Set the **File name** to the PDF file name.
1. Click **Save**.

The document is typeset; open the PDF file in any PDF reader to view the result.

# Background 

This text editor helps keep content separated from presentation. Plain text documents will remain readable long after proprietary formats have become obsolete. However, we've come to expect much more in what we read than mere text: from hyperlinked tables of contents to indexes, from footers to footnotes, from mathematical expressions to complex graphics, modern documents are nuanced and multifaceted.

## History

Before computer-based typesetting, much of mathematics was put to page by hand. Professional typesetters, who were often expensive and usually not mathematicians, would inadvertently introduce typographic errors into equations. Phototypesetting technology improved upon hand-typesetting, but well-known computer scientist Donald Knuth---whose third volume of *The Art of Computer Programming* was phototypeset in 1976---expressed dissatisfaction with its typographic quality. He set himself two goals: let anyone create high-quality books without much effort and provide software that typesets consistently on all capable computers. Two years later, he released a typesetting system and a font description language: TeX and METAFONT, respectively.

In short, TeX is software that helps typeset plain text documents.

## ConTeXt

Programming computers to typeset internationalized text automatically at the level we've become accustomed takes decades of development effort. Many free and open source software solutions can typeset text, including: ConTeXt, LaTeX, Sile, and others. ConTeXt, which builds upon TeX, is ideal for typesetting plain text into beautiful documents because it is developed with a notion of *setups*. These setups can wholly describe how text is to be typeset and---by being external to the text itself---configuring setups provides ample control over the document's final appearance without changing the prose.

# Further reading

Here are a few documents that introduce the typesetting system:

* *What is ConTeXt?* ([English](https://www.pragma-ade.com/general/manuals/what-is-context.pdf))
* *A not so short introduction to ConTeXt* ([English](https://github.com/contextgarden/not-so-short-introduction-to-context/raw/main/en/introCTX_eng.pdf) or [Spanish](https://raw.githubusercontent.com/contextgarden/not-so-short-introduction-to-context/main/es/introCTX_esp.pdf))
* *Dealing with XML in ConTeXt MKIV* ([English](https://pragma-ade.com/general/manuals/xml-mkiv.pdf))
* *Typographic Programming* ([English](https://www.pragma-ade.com/general/manuals/style.pdf))

The [documentation library](https://wiki.contextgarden.net/Documentation) includes the following gems:

* [ConTeXt Manual](https://www.pragma-ade.nl/general/manuals/ma-cb-en.pdf)
* [ConTeXt command reference](https://www.pragma-ade.nl/general/qrcs/setup-en.pdf)
* [METAFUN Manual](https://www.pragma-ade.nl/general/manuals/metafun-p.pdf)
* [It's in the Details](https://www.pragma-ade.nl/general/manuals/details.pdf)
* [Fonts out of ConTeXt](https://www.pragma-ade.com/general/manuals/fonts-mkiv.pdf)

Expert-level documentation includes the [LuaTeX Reference Manual](https://www.pragma-ade.nl/general/manuals/luatex.pdf).

