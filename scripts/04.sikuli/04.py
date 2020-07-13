# -----------------------------------------------------------------------------
# Copyright 2020 White Magic Software, Ltd.
#
# Permission is hereby granted, free of charge, to any person obtaining a
# copy of this software and associated documentation files (the
# "Software"), to deal in the Software without restriction, including
# without limitation the rights to use, copy, modify, merge, publish,
# distribute, sublicense, and/or sell copies of the Software, and to
# permit persons to whom the Software is furnished to do so, subject to
# the following conditions:
#
# The above copyright notice and this permission notice shall be included
# in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
# OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
# MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
# IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
# CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
# TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
# SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
# -----------------------------------------------------------------------------

# -----------------------------------------------------------------------------
# This script demonstrates using R.
# -----------------------------------------------------------------------------
import sys

if not "../editor.sikuli" in sys.path:
    sys.path.append( "../editor.sikuli" )

from editor import *

set_typing_speed( 220 )

# -----------------------------------------------------------------------------
# Open the demo text.
# -----------------------------------------------------------------------------
file_open()
type( Key.UP, Key.ALT )
wait( 0.5 )
end()
wait( 0.25 )
enter()
wait( 0.5 )
down()
wait( 0.25 )
enter()
wait( 1 )

# -----------------------------------------------------------------------------
# Re-open the corresponding definition file.
# -----------------------------------------------------------------------------
file_open()
recur( 2, down )
wait( 1 )
enter()
wait( 1 )

# -----------------------------------------------------------------------------
# Brief introduction to R
# -----------------------------------------------------------------------------
type( Key.HOME, Key.CTRL )
end()
paragraph()

typer( "## Using R" )
paragraph()
typer( "Insert R code into documents as follows: `r# 1+1`. " )
wait( 1.5 )
typer( "Notice how the right-hand pane shows the computed result. I'll wait. " )
wait( 3 )
typer( "The syntax is: open backtick, r#, *computable expression*, close " )
typer( "backtick. That expression can be any valid R statement. The status bar " ) 
typer( "will provide clues when an R expression cannot be computed by the " )
typer( "editor. `r# glitch`" )
wait( 4 )
recur( 11, backspace )
typer( "Let's swap 34 storeys for a definition value and replace the number " )
typer( "according to the Chicago Manual of Style (cms) rules." )

# -----------------------------------------------------------------------------
# Demo pluralization
# -----------------------------------------------------------------------------
set_typing_speed( 80 )

edit_find( "34" )
autoinsert()

edit_find( "x(" )
typer( "cms(" )

edit_find( "storeys." )
typer( "34." )
autoinsert()
edit_find( "x(" )
typer( "pl( 'storey'," )
wait( 4 )

tab()
rename_definition( "1" )
wait( 4 )
rename_definition( "142" )
wait( 4 )
rename_definition( "34" )
wait( 4 )
tab()

# -----------------------------------------------------------------------------
# Demo possessives (it, her, his, Director)
# -----------------------------------------------------------------------------
type( Key.HOME, Key.CTRL )
edit_find( "Director" )
autoinsert()
edit_find_next()
autoinsert()
edit_find_next()
autoinsert()
type( Key.RIGHT )
recur( 2, delete )
autoinsert()
home()
edit_find( "x(" )
typer( "pos(" )
wait( 2 )

tab()
rename_definition( "Headmistress" )
wait( 4 )
rename_definition( "Director" )
wait( 2 )
tab()

type( Key.END, Key.CTRL )
paragraph()
typer( "Other possessives: `r# pos( 'it' )`, `r# pos( 'her' )`, `r# pos( 'his' )`, " )
typer( "and `r# pos( 'my' )`." )

# -----------------------------------------------------------------------------
# Demo conversion, including ordinal numbers
# -----------------------------------------------------------------------------
set_typing_speed( 160 )

paragraph()
heading( "Date Conversions" )
typer( "Mixing R code with definitions invites endless possibilities. " )
typer( "Imagine someone racing to the " ) 
typer( "`r#cms( v$location$breeder$storeys, ordinal=TRUE )` floor, whereby that " )
typer( "ordinal stems from the Hatchery's storeys' definition. Or how about " )
typer( "a complex timeline where dates are expressed in days relative to one " )
typer( "point in time. Let's call this the *anchor date* and define it." )

tab()
home()
typer( Key.SPACE )
insert()
rename_definition( "date" )
insert()
rename_definition( "anchor" )
insert()
rename_definition( "1969-10-29" )
tab()

paragraph()
typer( "Next, set an R variable named `now` to the current date" )
typer( "`r# now = format( Sys.time(), '%Y-%m-%d' ); ''`--- the empty single quotes " )
typer( "prevent the date from appearing in the output document. " )

paragraph()
typer( "We set the anchor date to `r# annal()`, which was " )
typer( "`r# elapsed( 0, days( v$date$anchor, format( Sys.time(), '%Y-%m-%d' ) ) )` " )
typer( "ago from `r# format( as.Date( now ), '%B %d, %Y' )`. " )

# -----------------------------------------------------------------------------
# Demo CSV file import
# -----------------------------------------------------------------------------
paragraph()
heading( "Tabular Data" )
typer( "The following table shows average Canadian lifespans by birth " )
typer( "year and sex:" )
paragraph()
typer( "`r# csv2md( '../data.csv', total=FALSE )`" )
paragraph()
typer( "Calling `csv2md` converts the comma-separated values in the spreadsheet " )
typer( "to a table formatted using Markdown. The HTML preview pane changes the " )
typer( "appearance of the resulting table. Using `../data.csv` instructs R to " )
typer( "open `data.csv` from one directory above the *working directory*." )

# -----------------------------------------------------------------------------
# Demo HTML export
# -----------------------------------------------------------------------------
paragraph()
heading( "Export" )
typer( "Retrieve the output HTML by using the **Edit > Copy HTML** menu. Let's " )
typer( "look at the output document." )

type( "e", Key.ALT )
wait( 0.5 )
down()
wait( 0.25 )
enter()
wait( 0.25 )

type( "a", Key.CTRL )
wait( 0.25 )
type( "v", Key.CTRL )
