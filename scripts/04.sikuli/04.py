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
# Demo bootstrapping
# -----------------------------------------------------------------------------

# Jump to the end.
type( Key.END, Key.CTRL )
paragraph()

type( "s", Key.CTRL + Key.ALT )
wait( 0.25 )


# -----------------------------------------------------------------------------
# Demo pluralization
# -----------------------------------------------------------------------------


# -----------------------------------------------------------------------------
# Demo possessives (it, she, he, Ross)
# -----------------------------------------------------------------------------


# -----------------------------------------------------------------------------
# Demo conversion, including ordinal numbers
# -----------------------------------------------------------------------------

# -----------------------------------------------------------------------------
# Demo Chicago Manual of Style
# -----------------------------------------------------------------------------


# -----------------------------------------------------------------------------
# Demo CSV file import
# -----------------------------------------------------------------------------


