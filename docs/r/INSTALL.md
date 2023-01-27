# R Functions

Import the files in this directory into the application, which include:

* bootstrap.R
* pluralize.R
* possessive.R
* conversion.R
* csv.R

# bootstrap.R

Copy the contents of `bootstrap.R` into the R script preferences, shown in the
following figure, then restart the application:

# ![Bootstrap](images/bootstrap.png)

Setting the **Working Directory** allows the startup script to load files
using a relative to said directory.

# pluralize.R

This file defines a function that implements most of Damian Conway's [An Algorithmic Approach to English Pluralization](http://blob.perl.org/tpc/1998/User_Applications/Algorithmic%20Approach%20Plurals/Algorithmic_Plurals.html).

## Usage

Example usages of the pluralize function include:

    `r#pluralize( "mouse" )` - mice
    `r#pluralize( "buzz" )` - buzzes
    `r#pluralize( "bus" )` - buses

# possessive.R

This file defines a function that applies possessives to English words.

## Usage

Example usages of the possessive function include:

    `r#pos( "Ross" )` - Ross'
    `r#pos( "Ruby" )` - Ruby's
    `r#pos( "Lois" )` - Lois'
    `r#pos( "my" )` - mine
    `r#pos( "Your" )` - Yours

