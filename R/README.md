R Functions
===

Import the files in this directory into the application, which include:

* pluralise.R
* possessive.R

pluralise.R
===

This file defines a function that implements most of Damian Conway's [An Algorithmic Approach to English Pluralization](http://blob.perl.org/tpc/1998/User_Applications/Algorithmic%20Approach%20Plurals/Algorithmic_Plurals.html).

Usage
---
Example usages of the pluralise function include:

    `r#pluralise( 'mouse' )` - mice
    `r#pluralise( 'buzz' )` - buzzes
    `r#pluralise( 'bus' )` - busses

possessive.R
===

This file defines a function that applies possessives to English words.

Usage
---
Example usages of the possessive function include:

    `r#pos( 'Ross' )` - Ross'
    `r#pos( 'Ruby' )` - Ruby's
    `r#pos( 'Lois' )` - Lois'

