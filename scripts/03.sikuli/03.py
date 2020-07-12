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
header( "What text formats are supported?" )

typer( "Scr" )
autoinsert()
typer( " supports Markdown, R Markdown, XML, and R XML; however, the software " )
typer( "architecture enables it to easily add new formats. The following figure " )
typer( "depicts the overall architecture: " )
paragraph()
typer( "![](../writing/images/architecture)" )
paragraph()
typer( "Most text editors read a single format and convert it to one other format. " )
typer( "With a little more effort, text editors can support many input and output " )
typer( "formats. Scr" )
autoinsert()
typer( " goes one step further by introducing interpolated definitions." )
paragraph()
typer( "Kitten interlude:" )
paragraph()
typer( "![](https://i.imgur.com/jboueQH.jpg)" )
paragraph()

header( "What is R?" )
typer( "R is a programming language. You might have noticed a few potential grammar " )
typer( "problems with direct substitution. Rules for possessive forms, numbers, and " )
typer( "other linguistic exceptions can be addressed using R. Let's take a look!" )
