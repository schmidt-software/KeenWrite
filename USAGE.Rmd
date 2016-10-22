# Introduction

This document describes how to write documentation (technical or otherwise) using a one master copy and produce a variety of output formats, such as: HTML pages, PDFs, and EPUBs. What's more, the document provides an overview of how to use variables and--for the unintimidated--leverage the power of R, a programming language.

# Software Requirements

Install Java, ConTeXt, Pandoc, R, and Lib V8. Then install the R packages knitr, yaml, and devtools, and pluralize by running the following commands:

    sudo su -
    apt-get install default-jre
    apt-get install context
    apt-get install pandoc
    apt-get install r
    apt-get install libv8-dev
    r
    url <- "http://cran.us.r-project.org"
    install.packages('knitr', repos=url)
    install.packages('yaml', repos=url)
    install.packages('devtools', repos=url)
    devtools::install_github("hrbrmstr/pluralize")

To exit R, press `Ctrl+d` or type `q()` followed by pressing `Enter`.

The required software packages are installed.

# Markdown

