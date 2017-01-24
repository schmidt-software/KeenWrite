# ######################################################################
#
# Copyright 2016, White Magic Software, Ltd.
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
# Converts CSV to Markdown.
#
# ######################################################################

# Reads a CSV file and converts the contents to a Markdown table. The
# file must be in the working directory as specified by setwd.
#
# @param f The filename to convert.
# @param decimals Rounded decimal places (default 1).
# @param totals Include total sums (default TRUE).
# @param align Right-align numbers (default TRUE).
csv2md <- function( f, decimals = 1, totals = T, align = T ) {
  # Read the CVS data from the file; ensure strings become characters.
  df <- read.table( f, sep=',', header=T, stringsAsFactors=F )

  if( totals ) {
    # Determine what columns can be summed.
    number <- which( unlist( lapply( df, is.numeric ) ) )

    # Use colSums when more than one summable column exists.
    if( length( number ) > 1 ) {
      f.sum <- colSums
    }
    else {
      f.sum <- sum
    }

    # Calculate the sum of all the summable columns and insert the
    # results back into the data frame.
    df[ (nrow( df ) + 1), number ] <- f.sum( df[, number], na.rm=TRUE )

    # pluralize would be heavyweight here.
    if( length( number ) > 1 ) {
      t <- "**Totals**"
    }
    else {
      t <- "**Total**"
    }

    # Change the first column of the last line to "Total(s)".
    df[ nrow( df ), 1 ] <- t

    # Don't clutter the output with "NA" text.
    df[ is.na( df ) ] <- ""
  }

  if( align ) {
    is.char <- vapply( df, is.character, logical( 1 ) )
    dashes <- paste( ifelse( is.char, ':---', '---:' ), collapse='|' )
  }
  else {
    dashes <- paste( rep( '---', length( df ) ), collapse = '|')
  }

  # Create a Markdown version of the data frame.
  paste(
    paste( names( df ), collapse = '|'), '\n',
    dashes, '\n', 
    paste(
      Reduce( function( x, y ) {
          paste( x, format( y, digits = decimals ), sep = '|' )
        }, df
      ),
      collapse = '|\n', sep=''
    )
  )
}

