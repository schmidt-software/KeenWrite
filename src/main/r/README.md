# R Scripts

These R scripts illustrate how R can be used within an application to perform calculations using variables. Authors are free to write their own scripts, of course. These scripts serve as an example of how to automate certain tasks while writing.

## Configuration

Configure the editor to use the R scripts as follows:

1. Copy the R scripts into same directory as your Markdown files.
1. Start the editor.
1. Click **Tools → R Script**.
1. Copy and paste the following:

        assign( 'anchor', as.Date( '$date.anchor$', format='%Y-%m-%d' ), envir = .GlobalEnv );
        setwd( '$application.r.working.directory$' );
        source( 'pluralize.R' );
        source( 'csv.R' );
        source( 'conversion.R' );

1. Click **File → New** to create a new file.
1. Click **File → Save As** to set a filename.
1. Set **Name** to: `variables.yaml`
1. Click **OK**.
1. Paste the following definitions:

        date:
          anchor: 2017-01-01
        editor:
          examples:
            season: 2017-09-02
            math:
              x: 1
              y: $editor.examples.math.x$ + 1
              z: $editor.examples.math.y$ + 1
            name:
              given: Josephene

1. Save and close the file.
1. Click **File → Open**
1. Change **Markdown Files** to **Definition Files**.
1. Select `variables.yaml`.
1. Click **Open**.

R functionality is configured.

## Definitions

The variables definitions within `variables.yaml` are available to R using the R syntax. An additional variable, `application.r.working.directory` is added to the list of variables. The value is set to the working directory of the file being edited. Hover the mouse cursor over the file tab in the editor to see the full path to the file.

## Examples

This section demonstrates how to use the R functions when editing. Complete the following steps to begin:

1. Click **File → New** to create a new file.
1. Click **File → Save As** to set a filename.
1. Set **Name** to: `example.Rmd`
1. Click **OK**.

The examples are ready for use within the editor.

### Arithmetic

Type the following to perform a simple calculation:

    `r# 1+1`

The preview pane shows `2.0`.

### Functions

Call the [format](https://stat.ethz.ch/R-manual/R-devel/library/base/html/format.html) function to truncate unwanted decimal places as follows:

    `r# format(1+1,digits=1)`

The preview pane shows `2`.

### Pluralize

Many English words can be pluralized as follows:

    `r# pl('wolf',2)`

The preview pane shows `wolves`. The `pluralize.R` file contains a partial implementation of Damian Conway's algorithmic approach to English pluralization.

### Chicago Manual of Style

Apply the Chicago Manual of Style for words less than one-hundred as follows:

       `r# cms(1)` `r# cms(99)` `r# cms(101)`

The preview pane shows numbers written out as `one` and `ninety-nine`, followed by the digits 101.

### Data Import

Import and display information from a CSV file as follows:

1. Click **File → New** to create a new file.
1. Click **File → Save As** to rename the file.
1. Set the filename to: `data.csv`
1. Paste the following into `data.csv`:

        Animal,Quantity,Country
        Aardwolf,1,Africa
        Keel-billed toucan,1,Belize
        Beaver,2,Canada
        Mute swan,3,Denmark
        Lion,5,Ethiopia
        Brown bear,8,Finland
        Dolphin,13,Greece
        Turul,21,Hungary
        Gyrfalcon,34,Iceland
        Red-billed streamertail,55,Jamaica

1. Click the `example.Rmd` tab.
1. Type the following:

       `r# csv2md('data.csv',total=F)`

1. Type the following to calculate a total for all numeric columns:

       `r# csv2md('data.csv')`

This imports the data from an external file and formats the information into a table, automatically. Update the data as follows:

1. Click the `data.csv` tab to edit the data.
1. Change the data by adding a new row.
1. Save the file.
1. Click the `example.Rmd` tab.

The preview pane shows the revised contents.

### Elapsed Time

The duration of a timeline, given in numbers of days, can be computed into English as follows:

    `r# elapsed(1,1)`

The preview pane shows `same day`. Change the expression to:

    `r# elapsed(1,2)`

The preview pane shows `one day`. Change the expression to:

    `r# elapsed(1,112358)`

The preview pane shows `307 years, seven months, and sixteen days`, combined using the Chicago Manual of Style, the pluralization function, and a [serial comma](https://www.behance.net/gallery/19417363/The-Oxford-Comma).

### Variable Syntax

The syntax for a variable changes when using an R Markdown file (denoted by the `.Rmd` filename extension), as opposed to a regular Markdown file (`.md`). Return to the example file and type the following:

    `r# v$date$anchor`

The preview pane shows the date.

### Autocomplete

Automatically insert a variable reference into the text as follows:

1. Type: `Jos`
    * Note the capital letter, matches are case sensitive.
1. Hold down the `Control` key.
1. Tap the `Spacebar`

The editor shows:

    `r#x( v$editor$examples$name$given )`

The preview pane shows:

    Josephine

Here, the `x` function evaluates its parameter as an expression. This allows variables to include expressions in their definition.

### Variable Definition Expressions

Definition file variables are have the ability to reference other definitions. Try the following:

    x = `r#x( v$editor$examples$math$x )`;
    y = `r#x( v$editor$examples$math$y )`;
    z = `r#x( v$editor$examples$math$z )`

The preview pane shows:

    x = 1.0; y = 2.0; z = 3.0

### Case

Ensure words begin with a lowercase letter as follows:

    `r#lc( v$editor$examples$name$given )`

The preview pane shows:

    josephine

Similarly, ensure an uppercase letter as follows:

    `r#uc( 'hello, world!' )`

The preview pane shows:

    Hello, world!

### Month

Display the month name given a month number as follows:

    `r# month( 1 )`

The preview pane shows:

    January

## Summary

Authors can inline R statements into documents, directly, so long as those statements generate text. Plots, graphs, and images must be referenced as external image files or URLs.
