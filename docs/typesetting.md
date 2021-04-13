# Overview

This document describes how to typeset from within the text editor.

# Background 

This text editor helps keep content separated from presentation. Plain text documents will remain readable long after proprietary formats have become obsolete. However, we've come to expect much more in what we read than mere text: from hyperlinked tables of contents to indexes, from footers to footnotes, from mathematical expressions to complex graphics, modern documents are nuanced and multifaceted.

Programming computers to typeset internationalized text automatically at the level we've become accustomed takes decades of development effort. Many free and open source software solutions can typeset text, including: ConTeXt, LaTeX, Sile, and others. ConTeXt is ideal for typesetting plain text into beautiful documents because it is developed with a notion of *setups*. These setups can wholly describe how text is to be typeset and---by being external to the text itself---configuring setups provides ample control over the document's final appearance without changing the prose.

# Installation

Install ConTeXt as follows:

1. Start the text editor.
1. Click **File → Export As → PDF** (or type `Ctrl+p`).
1. Note the operating system name, instruction set, and architecture (e.g., Windows X86 64-bit).
1. Click the [link](https://wiki.contextgarden.net/Installation) in the dialog.
1. Download the ConTeXt version for your computer's operating system.
1. Follow the step-by-step instructions on the ConTeXt installation web page.

ConTeXt is installed.

**Note:** The `PATH` environment variable must include the ConTeXt `bin` directory, otherwise the text editor will not be able to generate PDF files.

# Typeset document

Typeset a document as follows:

1. Start the text editor.
1. Click **File → New** (or type `Ctrl+n`).
1. Type in some text.
1. Click **File → Export As → PDF** (or type `Ctrl+p`).
1. Set the **File name** to the PDF file name.
1. Click **Save**.

The document is typeset; open the PDF file in any PDF reader to view the result.
