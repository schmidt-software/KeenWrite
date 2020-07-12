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
# This script introduces the editor and its purpose.
# -----------------------------------------------------------------------------
import sys

if not "../editor.sikuli" in sys.path:
    sys.path.append( "../editor.sikuli" )

from editor import *

# ---------------------------------------------------------------
# Fresh start
# ---------------------------------------------------------------
rm( app_home + "/variables.yaml" )
rm( app_home + "/untitled.md" )
rm( home + "/.scrivenvar" )

# ---------------------------------------------------------------
# Wait for application to launch
# ---------------------------------------------------------------
openApp( "java -jar " + app_bin )

wait("1594187265140.png", 30)
# ---------------------------------------------------------------
# Introduction
# ---------------------------------------------------------------
set_typing_speed( 240 )

header( "What is this application?" )
typer( "Well, this application is a text editor that supports interpolated definitions, ")
typer( "a few different plain text formats, real-time preview, spell check ") 
typer( "as you tipe" ) 
wait( 0.5 )
recur( 3, typer, Key.BACKSPACE )
typer( "ype, and R statements." )
paragraph()
wait( 1 )

# ---------------------------------------------------------------
# Definition demo
# ---------------------------------------------------------------
header( "What are definitions?" )
typer( "Watch. " )
wait( .5 )

# Focus the definition editor.
click_create()
recur( 4, typer, Key.TAB )

wait( .5 )
rename_definition( "application" )

insert()
rename_definition( "title" )

insert()
rename_definition( "Scrivenvar" )

# Set focus to the text editor.
typer( Key.TAB )

typer( "The left-hand pane contains a nested, folder-like structure of names " )
typer( "and values that are called *definitions*. " )
wait( .5 )
typer( "Such definitions can simplify updating documents. " )
wait( 1 )

edit_find( "this application" )
typer( "$application.title$" )

edit_find_next()
typer( "$application.title$" )

type( Key.END, Key.CTRL )

typer( "The right-hand pane shows the result after having substituted definition " )
typer( "values into the document." )
wait( 2 )
paragraph()

header( "What is interpolation?" )
typer( "Definition values can reference definition names. " )
wait( .5 )
typer( "The definition names act as placeholders. Substituting placeholders with " )
typer( "their defined value is called *interpolation*. Let's see how it works." )
wait( 3 )
