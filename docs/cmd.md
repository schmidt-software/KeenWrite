# Command-line arguments

The application may be run from the command-line to convert Markdown and
R Markdown files to a variety of output formats. Without specifying any
command-line arguments, the application will launch a graphical user interface.

## Common arguments

The most common command-line arguments to use include:

* `-h` -- displays all command-line arguments, then exits.
* `-i` -- sets the input file name, must be a full path.
* `-o` -- sets the output file name, can be a relative path.

## Example usage

On Linux, simple usages include:

    keenwrite.bin -i $HOME/document/01.md -o document.xhtml

    keenwrite.bin -i $HOME/document/01.md -o document.md \
      -v $HOME/document/variables.yaml

That command will convert `01.md` into the respective file formats. In
the first case, it will become an HTML page. In the second case, it will
become a Markdown document with all variables interpolated and replaced.

A more complex example follows:

    keenwrite.bin -i $HOME/document/01.Rmd -o document.pdf \
      --image-dir=$HOME/document/images -v $HOME/document/variables.yaml \
      --metadata="title={{book.title}}" --metadata="author={{book.author}}" \
      --r-dir=$HOME/document/r --r-script=$HOME/document/r/bootstrap.R \
      --theme-dir=$HOME/document/themes/boschet

That command will convert `01.Rmd` to `document.pdf` and replace the metadata
using values from the variable definitions file.

Directory names containing spaces must be quoted. For example, on Windows:

    keenwrite.bin -i "C:\Users\My Documents\01.Rmd" -o document.pdf

