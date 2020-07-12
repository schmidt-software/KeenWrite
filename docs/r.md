# Introduction

This document describes how to use the [R](https://www.r-project.org/)
programming language from within the application. The application uses an
interpreter known as [Renjin](https://www.renjin.org/) to integrate with R.

# Hello world

Complete the following steps to see R in action:

1. Start the application.
1. Click **File → New** to create a new file.
1. Click **File → Save As**.
1. Set **Name** to: `addition.Rmd`
1. Click **Save**.

Setting the file name extension tells the application what processor to
use when transforming the contents for display in the preview pane. Continue
by typing in the following text, including the backticks:

```r
`r#1 + 1`
```

The preview pane shows the result of `1` plus `1`:

```
2.0
```

# Bootstrap script

Being able to run R code while editing an R Markdown document is convenient.
Having the ability to call functions is where the power of R can be
leveraged.

Complete the following steps to call an R function from your own library:

1. Click **File → New** to create a new file.
1. Click **File → Save As**.
1. Browse to your home directory.
1. Set **Name** to: `library.R`.
1. Click **Save**.
1. Set the contents to:
    ``` r
    sum <- function( a, b ) {
      a + b
    }
    ```
1. Click the **Save** icon.
1. Click **R → Script**.
1. Set the **R Startup Script** contents to:
    ``` r
    source( 'library.R' );
    ```
1. Click **OK**.
1. Create a new file.
1. Set the contents to:
    ``` r
    `r#sum( 5, 5 )`
    ```
1. Save the file as `sum.R`.

The preview panel shows the result of calling the `sum` function:

```
10.0
```

This shows how the bootstrap script can load `library.R`, which defines
a `sum` function that is called by name in the Markdown document.

# Working directory

R files may be sourced from any directory, not just the user's home
directory. Accomplish this as follows:

1. Click **R → Directory**.
1. Set **Directory** to a different directory.
1. Click **OK**.
1. Create the directory if it does not exist.
1. Move `library.R` into the directory.
1. Append a new function to `library.R` as follows:
    ``` r
    mul <- function( a, b ) {
      a * b
    }
    ```
1. Click **R → Script**.
1. Set the **R Startup Script** contents to:
    ``` r
    setwd( '$application.r.working.directory$' );
    source( 'library.R' );
    ```
1. Change `sum.Rmd` to:
    ``` r
    `r#mul( 5, 5 )`
    ```
1. Close the file `sum.Rmd`.
1. Confirm saving the file when prompted.
1. Re-open `sum.Rmd`.

The preview panel shows:

```
25.0
```

Calling `setwd` using `'$application.r.working.directory$'` changes the
working directory where the R engine searches for source files.

# YAML definitions

To see how variable definitions work in R, try the following:

1. Create a new file.
1. Change the contents to (use spaces not tabs):
    ``` yaml
    project:
      title: Project Title
      author: Author Name
    ```
1. Save the file as `definitions.yaml`.
1. Click **File → Open**.
1. Set **Source Files** to **Definition Files**.
1. Select `definitions.yaml`.
1. Click **Open**.
1. Open `sum.Rmd` if it is not already open.
1. Type: `je`
1. Press `Ctrl+Space`

The editor inserts the following text (matches `je` against Pro**je**ct):

``` r
`r#x( v$project$title )`
```

The preview panel shows:

```
r#x( 'Project Title' )
```

This is because the application inserts definition reference names based
on the type of file being edited. By default, the R engine does not have
a function named `x` defined.

Continue as follows:

1. Click **R → Script**.
1. Append the following:
    ``` r
    x <- function( s ) {
      tryCatch( {
        r = eval( parse( text = s ) )

        ifelse( is.atomic( r ), r, s );
      },
      warning = function( w ) { s },
      error = function( e ) { s } )
    }
    ```
1. Click **OK**.
1. Close and re-open `sum.Rmd`.

The preview panel shows:

```
25.0

Project Title
```

The `x` function attempts to evaluate the expression defined by the YAML
variable. This means that the YAML definitions can also include expressions
that R is capable of evaluating.

While the `x` function can be defined within the R Startup Script, it is
better practice to put it into its own library so that it can be reused
outside of the application.

