# ######################################################################
#
# Copyright 2017, White Magic Software, Ltd.
# 
# Permission is hereby granted, free of charge, to any person obtaining
# a copy of this software and associated documentation files (the
# "Software"), to deal in the Software without restriction, including
# without limitation the rights to use, copy, modify, merge, publish,
# distribute, sublicense, and/or sell copies of the Software, and to
# permit persons to whom the Software is furnished to do so, subject to
# the following conditions:
# 
# The above copyright notice and this permission notice shall be
# included in all copies or substantial portions of the Software.
# 
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
# EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
# MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
# NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
# LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
# OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
# WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#
# ######################################################################

# ######################################################################
#
# Changes a word into its possessive form.
#
# ######################################################################

# Returns leftmost n characters of s.
lstr <- function( s, n = 1 ) {
  substr( s, 0, n )
}

# Returns rightmost n characters of s.
rstr <- function( s, n = 1 ) {
  l = nchar( s )
  substr( s, l - n + 1, l )
}

# Returns the possessive form of the given word.
pos <- function( s ) {
  result <- s

  # Check to see if the last character is an s.
  ch <- rstr( s, 1 )

  if( ch != "s" ) {
    result <- concat( result, "'s" )
  }
  else {
    result <- concat( result, "'" )
  }

  result
}

