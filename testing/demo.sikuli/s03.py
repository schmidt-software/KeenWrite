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
# This script introduces images and R.
# -----------------------------------------------------------------------------
from sikuli import *
import sys

if not "../editor.sikuli" in sys.path:
    sys.path.append( "../editor.sikuli" )

from editor import *

set_typing_speed( 80 )

file_open()
type( Key.UP, Key.ALT )
wait( 0.5 )
home()
wait( 0.25 )
enter()
wait( 1 )
end()
wait( 0.25 )
enter()
wait( 1 )

set_typing_speed( 200 )

paragraph()
heading( "What text formats are supported?" )

typer( "Scr" )
autoinsert()
typer( " supports Markdown, R Markdown, XML, and R XML; however, the software " )
typer( "architecture enables it to easily add new formats. The following figure " )
typer( "depicts the overall architecture: " )
paragraph()
typer( "![](../writing/images/architecture)" )
paragraph()
typer( "Many text editors can only open one type of plain text markup format that is " )
typer( "only output as HTML. With a little more effort, text editors could support " )
typer( "multiple input and output formats. Scr" )
autoinsert()
typer( " does so and goes one step further by introducing interpolated definitions." )
paragraph()
typer( "Kitten interlude:" )
paragraph()
typer( "![](https://i.imgur.com/jboueQH.jpg)" )
paragraph()

heading( "What is R?" )
typer( "R is a programming language. You might have noticed a few potential grammar " )
typer( "problems with direct substitution. Rules for possessive forms, numbers, and " )
typer( "other quirks can be tackled using R." )

# -----------------------------------------------------------------------------
# Demo bootstrapping
# -----------------------------------------------------------------------------

# Jump to the end
type( Key.END, Key.CTRL )
paragraph()

set_typing_speed( 300 )
heading( "How is R used?" )
typer( "R must be instructed where to find script files and what ones to load. The " )
typer( "*working directory* is the full path to those R files; the *startup script* " )
typer( "defines what R files to load. Both preferences must be changed before prose " )
typer( "may be processed. Preferences can be opened using either the " )
typeln( "**Edit > Preferences** menu or by pressing `Ctrl+Alt+s`. Here goes!" ) 
wait( 2 )

# -----------------------------------------------------------------------------
# Select the R script directory
# -----------------------------------------------------------------------------

# Change the working directory by clicking "Browse"
type( "s", Key.CTRL + Key.ALT )
wait("1594592396134.png", 1)
click("1594592396134.png")
wait( 0.5 )

# Navigate to and select the "r" directory
type( Key.UP, Key.ALT )
wait( 0.5 )
end()
wait( 0.5 )
enter()
wait( 0.5 )
end()
wait( 0.5 )
type( Key.UP )
wait( 0.5 )
recur( 2, tab )
wait( 0.5 )
enter()
wait( 1 )

# -----------------------------------------------------------------------------
# Set the R startup script instructions
# -----------------------------------------------------------------------------

wait("1594593710440.png", 5)
click("1594593710440.png")

set_typing_speed( 440 )

typeln( "setwd( '$application.r.working.directory$' )" )
typeln( "assign( 'anchor', '$date.anchor$', envir = .GlobalEnv )" )
typeln( "source( 'pluralize.R' )" )
typeln( "source( 'possessive.R' )" )
typeln( "source( 'conversion.R' )" )
typeln( "source( 'csv.R' )" )

wait("1594593794335.png", 3)
click("1594593794335.png")

paragraph()
set_typing_speed( 220 )

typer( "R is now configured. The startup script and other R " )
typer( "files can be found in the " )
typer( "[repository](https://github.com/DaveJarvis/scrivenvar/tree/master/R). " )
wait( 1.5 )

# Wait for the browser to appear.
wait("1594594984108.png", 5)
click("1594594984108.png")

wait( 5 )
click("1594689573764.png")

paragraph()
typer( "Next, we'll see how definitions and R can work together." )
wait( 2 )
