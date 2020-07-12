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
# This script contains helper functions used by the other scripts.
#
# Do not run this script.
# -----------------------------------------------------------------------------

from sikuli import *
import sys
import os
from os.path import expanduser

home = expanduser( "~" )
app_home = home + "/bin"
app_bin = app_home + "/scrivenvar.jar"

wpm_default_speed = 80
wpm_typing_speed = wpm_default_speed

# -----------------------------------------------------------------------------
# Try to delete the file pointed to by the path variable. If there is no such
# file, this will silently ignore the exception.
# -----------------------------------------------------------------------------
def rm( path ):
    try:
        os.remove( path )
    except:
        print "Ignored"

# -----------------------------------------------------------------------------
# Changes the current typing speed, where speed is given in words per minute.
# -----------------------------------------------------------------------------
def set_typing_speed( wpm ):
    global wpm_typing_speed
    wpm_typing_speed = wpm

def restore_typing_speed():
    set_typing_speed( wpm_default_speed )

# -----------------------------------------------------------------------------
# Creates a delay between keystrokes to emulate typing at a particular speed.
# -----------------------------------------------------------------------------
def random_wait():
    from time import sleep
    from random import uniform
    cpm = wpm_typing_speed * 5.1
    cps = cpm / 60.0
    ms_per_char = 1000.0 / cps
    ms_per_stroke = ms_per_char / 2.0

    noise = uniform( 0, ms_per_stroke / 2 )
    duration = (ms_per_stroke + noise ) / 1000
    
    sleep( duration )

# -----------------------------------------------------------------------------
# Repeats a function call, f, n times.
# -----------------------------------------------------------------------------
def recur( n, f, *args ):
    for i in range( n ):
        f(*args)
        random_wait()

# -----------------------------------------------------------------------------
# Emulate a typist who is typing in the given text.
# -----------------------------------------------------------------------------
def typer( text ):
    # ~25 is a reasonably realistic, fast typist.
    for c in text:
        type( c )
        random_wait()

# -----------------------------------------------------------------------------
# Injects a definition.
# -----------------------------------------------------------------------------
def autoinsert():
    type( Key.SPACE, Key.CTRL )
    random_wait()

# -----------------------------------------------------------------------------
# Types the TAB key.
# -----------------------------------------------------------------------------
def tab():
    typer( Key.TAB )

# -----------------------------------------------------------------------------
# Types the ENTER key.
# -----------------------------------------------------------------------------
def enter():
    typer( Key.ENTER )

# -----------------------------------------------------------------------------
# Types the ESC key.
# -----------------------------------------------------------------------------
def esc():
    typer( Key.ESC )

# -----------------------------------------------------------------------------
# Types the DOWN arrow key.
# -----------------------------------------------------------------------------
def down():
    typer( Key.DOWN )

# -----------------------------------------------------------------------------
# Types the INSERT key, often to insert a new definition.
# -----------------------------------------------------------------------------
def insert():
    typer( Key.INSERT )

# -----------------------------------------------------------------------------
# Types the DELETE key, often to remove selected text.
# -----------------------------------------------------------------------------
def delete():
    typer( Key.DELETE )

# -----------------------------------------------------------------------------
# Moves the cursor one word to the right.
# -----------------------------------------------------------------------------
def skip_right():
    type( Key.RIGHT, Key.CTRL )
    random_wait()

def select_word_right():
    type( Key.RIGHT, Key.CTRL + Key.SHIFT )
    random_wait()

# -----------------------------------------------------------------------------
# Types ENTER twice to begin a new paragraph.
# -----------------------------------------------------------------------------
def paragraph():
    recur( 2, enter )
    wait( 1.5 )

# -----------------------------------------------------------------------------
# Writes a heading to the document using the given text value as the content.
# -----------------------------------------------------------------------------
def header( text ):
    typer( "# " + text )
    paragraph()

# -----------------------------------------------------------------------------
# Clicks the "Create" button to add a new definition.
# -----------------------------------------------------------------------------
def click_create():
    click("1594187923258.png")
    wait( .5 )

# -----------------------------------------------------------------------------
# Changes the text for the actively selected definition.
# -----------------------------------------------------------------------------
def rename_definition( text ):
    typer( Key.F2 )
    typer( text )
    enter()
    wait( .5 )

# -----------------------------------------------------------------------------
# Searches for the given text within the document.
# -----------------------------------------------------------------------------
def edit_find( text ):
    type( "f", Key.CTRL )
    typer( text )
    enter()
    wait( .25 )
    esc()
    wait( .5 )

# -----------------------------------------------------------------------------
# Searches for the next occurrence of the previous search term.
# -----------------------------------------------------------------------------
def edit_find_next():
    typer( Key.F3 )
    wait( .5 )
