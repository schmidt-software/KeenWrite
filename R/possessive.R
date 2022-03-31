# -----------------------------------------------------------------------------
# Copyright 2020, White Magic Software, Ltd.
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
# -----------------------------------------------------------------------------

# -----------------------------------------------------------------------------
# Returns leftmost n characters of s.
# -----------------------------------------------------------------------------
lstr <- function( s, n = 1 ) {
  substr( s, 0, n )
}

# -----------------------------------------------------------------------------
# Returns rightmost n characters of s.
# -----------------------------------------------------------------------------
rstr <- function( s, n = 1 ) {
  l <- nchar( s )
  substr( s, l - n + 1, l )
}

# -----------------------------------------------------------------------------
# Returns the possessive form of the given word, s.
# -----------------------------------------------------------------------------
pos <- function( s ) {
  lcs <- tolower( s )
  pronouns <- c( 'your', 'our', 'her', 'it', 'their' )

  if( lcs == 'my' ) {
    # Change "[Mm]y" to "[Mm]ine".
    s <- paste0( lstr( s, 1 ), "ine" )
  }
  else if( lcs %in% pronouns ) {
    # Append an s to most pronouns.
    s <- paste0( s, 's' )
  }
  else if( lcs != 'his' ) {
    # Possessive for all other words except 'his'.
    s <- paste0( s, ifelse( rstr( s, 1 ) == 's', "'" ,"'s" ) )
  }

  s
}

pro.sub <- function( s ) {
  if( s == 'm' ) {
    s <- 'he'
  }
  else if( s == 'f' ) {
    s <- 'she'
  }
  else {
    s <- 'their'
  }

  s
}

pro.obj <- function( s ) {
  if( s == 'm' ) {
    s <- 'him'
  }
  else if( s == 'f' ) {
    s <- 'her'
  }
  else {
    s <- 'them'
  }

  s
}

pro.ref <- function( s ) {
  if( s == 'm' ) {
    s <- 'himself'
  }
  else if( s == 'f' ) {
    s <- 'herself'
  }
  else {
    s <- 'themselves'
  }

  s
}

pro.adj <- function( s ) {
  if( s == 'm' ) {
    s <- 'his'
  }
  else if( s == 'f' ) {
    s <- 'her'
  }
  else {
    s <- 'their'
  }

  s
}

pro.pos <- function( s ) {
  if( s == 'm' ) {
    s <- 'his'
  }
  else if( s == 'f' ) {
    s <- 'hers'
  }
  else {
    s <- 'theirs'
  }

  s
}

pro.noun <- function( s ) {
  if( s == 'm' ) {
    s <- 'man'
  }
  else if( s == 'f' ) {
    s <- 'woman'
  }

  s
}

