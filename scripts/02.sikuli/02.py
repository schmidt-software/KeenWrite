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
# This script demonstrates how to use interpolated strings.
# -----------------------------------------------------------------------------
import sys

if not "../editor.sikuli" in sys.path:
    sys.path.append( "../editor.sikuli" )

from editor import *

# -----------------------------------------------------------------------------
# Open sample chapter.
# -----------------------------------------------------------------------------
file_open()
type( Key.UP, Key.ALT )
wait( 1 )
typer( Key.END )
wait( 1 )
enter()
wait( 0.5 )
enter()
wait( 1 )

# -----------------------------------------------------------------------------
# Open the corresponding definition file.
# -----------------------------------------------------------------------------
file_open()
recur( 2, down )
wait( 1 )
enter()
wait( 1 )

# -----------------------------------------------------------------------------
# Edit the sample document.
# -----------------------------------------------------------------------------
set_typing_speed( 80 )

type( Key.HOME, Key.CTRL )
recur( 2, down )

# Grey
recur( 3, skip_right )
autoinsert()

# 34
recur( 4, skip_right )
autoinsert()

# Central
recur( 10, skip_right )
autoinsert()

# London
skip_right()
autoinsert()

# Hatchery
skip_right()
autoinsert()

# and Conditioning
recur( 2, select_word_right )
delete()

# Centre
skip_right()
autoinsert()

set_typing_speed( 220 )

typer( " Let's interpolate those four definitions instead!" )
wait( 4 )
recur( 13, type, Key.BACKSPACE, Key.CTRL )
recur( 9, backspace )

set_typing_speed( 60 )

typer( "name$" )
wait( 2 )

# Collapse all definitions
tab()
recur( 8, typer, Key.LEFT )

# Expand to city
recur( 4, typer, Key.RIGHT )

# Jump to name
recur( 2, down )
recur( 2, typer, Key.RIGHT )

# Open the text field to show the full value
typer( Key.F2 )

# Traverse the text field
home()
recur( 16, type, Key.RIGHT, Key.CTRL )
esc()

restore_typing_speed()

tab()
type( Key.HOME, Key.CTRL )
edit_find( "Director" )
autoinsert()

edit_find_next()
autoinsert()

edit_find_next()
typer( Key.RIGHT )
recur( 2, delete )
autoinsert()
typer( "'s" )

edit_find( "Hatcheries" )
autoinsert()

# and Conditioning
recur( 2, select_word_right )
delete()

edit_find( "Central" )
autoinsert()

skip_right()
autoinsert()

typer( " How about a different city?" )
wait( 2 )
recur( 5, type, Key.BACKSPACE, Key.CTRL )
wait( 1 )
tab()
typer( Key.F2 )
typer( "Seattle" )
enter()
tab()
wait( 2 )

type( Key.END, Key.CTRL )
paragraph()
typer( "No?" )
paragraph()

tab()
typer( Key.F2 )
typer( "London" )
enter()

tab()
typer( "Organizing definitions is left to your ")
typer( "doub" )
wait( .25 )
autoinsert()
wait( 1 )
typer( " Good imagination." )
tab()

# Jump to "char" definition
home()

# Jump to "char.a.primary.name" definition
recur( 6, typer, Key.RIGHT )

# Jump to "char.a.primary.caste" definition
down()
typer( Key.RIGHT )

# Jump to root-level "caste" definition
recur( 7, down )

# Reselect "super"
recur( 5, typer, Key.RIGHT )
wait( 2 )

# Close the window, no save
type( "w", Key.CTRL )
wait( 0.5 )
tab()
wait( 0.5 )
typer( Key.SPACE )
wait( 1 )
