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
# Substitute R expressions in a document with their evaluated value. The
# anchor variable must be set for functions that use relative dates.
# -----------------------------------------------------------------------------

# -----------------------------------------------------------------------------
# Evaluates an expression; writes s if there is no expression.
# -----------------------------------------------------------------------------
x <- function( s ) {
  tryCatch( {
    r = eval( parse( text = s ) )

    # If the result isn't primitive, then it was probably parsed into
    # an unprintable object (e.g., "gray" becomes a colour). In those
    # cases, return the original text string. Otherwise, an atomic
    # value means a primitive type (string, integer, etc.) that can be
    # written directly into the document.
    ifelse( is.atomic( r ), r, s );
  },
  warning = function( w ) { s },
  error = function( e ) { s } )
}

# -----------------------------------------------------------------------------
# Returns a date offset by a given number of days, relative to the given
# date (d). This does not use the anchor, but is used to get the anchor's
# value as a date.
# -----------------------------------------------------------------------------
when <- function( d, n = 0, format = "%Y-%m-%d" ) {
  as.Date( d, format = format ) + x( n )
}

# -----------------------------------------------------------------------------
# Full date (s) offset by an optional number of days before or after.
# This will remove leading zeros (applying leading spaces instead, which
# are ignored by any worthwhile typesetting engine).
# -----------------------------------------------------------------------------
annal <- function( days = 0, format = "%Y-%m-%d", oformat = "%B %d, %Y" ) {
  gsub( " 0", " ", format( when( anchor, days ), format = oformat ) )
}

# -----------------------------------------------------------------------------
# Extracts the year from a date string.
# -----------------------------------------------------------------------------
year <- function( days = 0, format = "%Y-%m-%d" ) {
  annal( days, format, "%Y" )
}

# -----------------------------------------------------------------------------
# Day of the week (in days since the anchor date).
# -----------------------------------------------------------------------------
weekday <- function( n ) {
  weekdays( when( anchor, n ) )
}

# -----------------------------------------------------------------------------
# String concatenate function alias because paste0 is a terrible name.
# -----------------------------------------------------------------------------
concat <- paste0

# -----------------------------------------------------------------------------
# Translates a number from digits to words using Chicago Manual of Style.
# This will translate numbers greater than one by truncating to nearest
# thousandth, millionth, billionth, etc. regardless of ordinal. If ordinal
# is TRUE, this will return the ordinal name. This will not produce ordinals
# for numbers greater than 100.
#
# If scaled is TRUE, this will write large numbers as comma-separated values.
# -----------------------------------------------------------------------------
cms <- function( n, ordinal = FALSE, scaled = TRUE ) {
  n <- x( n )

  if( n == 0 ) {
    if( ordinal ) {
      return( "zeroth" )
    }

    return( "zero" )
  }

  # Concatenate this a little later.
  if( n < 0 ) {
    result = "negative "
    n = abs( n )
  }

  if( n > 999 && scaled ) {
    scales <- c(
      "thousand", "million", "billion", "trillion", "quadrillion",
      "quintillion", "sextillion", "septillion", "octillion", "nonillion",
      "decillion", "undecillion", "duodecillion", "tredecillion",
      "quattuordecillion", "quindecillion", "sexdecillion", "septendecillion",
      "octodecillion", "novemdecillion", "vigintillion", "centillion",
      "quadrillion", "quitillion", "sextillion"
    );

    d <- round( n / (10 ^ (log10( n ) - log10( n ) %% 3)) );
    n <- floor( log10( n ) ) / 3;
    return( paste( cms( d ), scales[ n ] ) );
  }

  # Do not spell out numbers greater than one hundred.
  if( n > 100 ) {
    # Comma-separated numbers.
    return( commas( n ) )
  }

  # Don't go beyond 100.
  if( n == 100 ) {
    if( ordinal ) {
      return( "one hundredth" )
    }

    return( "one hundred" )
  }

  # Samuel Langhorne Clemens noted English has too many exceptions.
  small = c(
    "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
    "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen",
    "seventeen", "eighteen", "nineteen"
  )

  ord_small = c(
    "first", "second", "third", "fourth", "fifth", "sixth", "seventh",
    "eighth", "ninth", "tenth", "eleventh", "twelfth", "thirteenth",
    "fourteenth", "fifteenth", "sixteenth", "seventeenth", "eighteenth",
    "nineteenth", "twentieth"
  )

  # After this, the number (n) is between 20 and 99.
  if( n < 20 ) {
    if( ordinal ) {
      return( .subset( ord_small, n %% 100 ) )
    }

    return( .subset( small, n %% 100 ) )
  }

  tens = c( "",
    "twenty", "thirty", "forty", "fifty",
    "sixty", "seventy", "eighty", "ninety"
  )

  ord_tens = c( "",
    "twentieth", "thirtieth", "fortieth", "fiftieth",
    "sixtieth", "seventieth", "eightieth", "ninetieth"
  )

  ones_index = n %% 10
  n = n %/% 10

  # No number in the ones column, so the number must be a multiple of ten.
  if( ones_index == 0 ) {
    if( ordinal ) {
      return( .subset( ord_tens, n ) )
    }

    return( .subset( tens, n ) )
  }

  # Find the value from the ones column.
  if( ordinal ) {
    unit_1 = .subset( ord_small, ones_index )
  }
  else {
    unit_1 = .subset( small, ones_index )
  }

  # Find the tens column.
  unit_10 = .subset( tens, n )

  # Hyphenate the tens and the ones together.
  concat( unit_10, concat( "-", unit_1 ) )
}

# -----------------------------------------------------------------------------
# Returns a number as a comma-delimited string. This is a work-around
# until Renjin fixes https://github.com/bedatadriven/renjin/issues/338
# -----------------------------------------------------------------------------
commas <- function( n ) {
  n <- x( n )

  s <- sprintf( "%03.0f", n %% 1000 )
  n <- n %/% 1000

  while( n > 0 ) {
    s <- concat( sprintf( "%03.0f", n %% 1000 ), ',', s )
    n <- n %/% 1000
  }

  gsub( "^0*", '', s )
}

# -----------------------------------------------------------------------------
# Returns a human-readable string that provides the elapsed time between
# two numbers in terms of years, months, and days. If any unit value is zero,
# the unit is not included. The words (year, month, day) are pluralized
# according to English grammar. The numbers are written out according to
# Chicago Manual of Style. This applies the serial comma.
#
# Both numbers are offsets relative to the anchor date.
#
# If all unit values are zero, this returns s ("same day" by default).
#
# If the start date (began) is greater than end date (ended), the dates are
# swapped before calculations are performed. This allows any two dates
# to be compared and positive unit values are always returned.
# -----------------------------------------------------------------------------
elapsed <- function( began, ended, s = "same day" ) {
  began = when( anchor, began )
  ended = when( anchor, ended )

  # Swap the dates if the end date comes before the start date.
  if( as.integer( ended - began ) < 0 ) {
    tempd = began
    began = ended
    ended = tempd
  }

  # Calculate number of elapsed years.
  years = length( seq( from = began, to = ended, by = "year" ) ) - 1

  # Move the start date up by the number of elapsed years.
  if( years > 0 ) {
    began = seq( began, length = 2, by = concat( years, " years" ) )[2]
    years = pl.numeric( "year", years )
  }
  else {
    # Zero years.
    years = ""
  }

  # Calculate number of elapsed months, excluding years.
  months = length( seq( from = began, to = ended, by = "month" ) ) - 1

  # Move the start date up by the number of elapsed months
  if( months > 0 ) {
    began = seq( began, length = 2, by = concat( months, " months" ) )[2]
    months = pl.numeric( "month", months )
  }
  else {
    # Zero months
    months = ""
  }

  # Calculate number of elapsed days, excluding months and years.
  days = length( seq( from = began, to = ended, by = "day" ) ) - 1

  if( days > 0 ) {
    days = pl.numeric( "day", days )
  }
  else {
    # Zero days
    days = ""
  }

  if( years <= 0 && months <= 0 && days <= 0 ) {
    return( s )
  }

  # Put them all in a vector, then remove the empty values.
  s <- c( years, months, days )
  s <- s[ s != "" ]

  r <- paste( s, collapse = ", " )

  # If all three items are present, replace the last comma with ", and".
  if( length( s ) > 2 ) {
    return( gsub( "(.*),", "\\1, and", r ) )
  }

  # Does nothing if no commas are present.
  gsub( "(.*),", "\\1 and", r )
}

# -----------------------------------------------------------------------------
# Returns the number (n) in English followed by the plural or singular
# form of the given string (s; resumably a noun), if applicable, according
# to English grammar. That is, pl.numeric( "wolf", 5 ) will return
# "five wolves".
# -----------------------------------------------------------------------------
pl.numeric <- function( s, n ) {
  concat( cms( n ), concat( " ", pluralize( word=s, n=n ) ) )
}

# -----------------------------------------------------------------------------
# Pluralize s if n is not equal to 1.
# -----------------------------------------------------------------------------
pl <- function( s, count=2 ) {
  pluralize( word=s, n=count )
}

# -----------------------------------------------------------------------------
# Name of the season, starting with an capital letter.
# -----------------------------------------------------------------------------
season <- function( n, format = "%Y-%m-%d" ) {
  WS <- as.Date("2016-12-15", "%Y-%m-%d") # Winter Solstice
  SE <- as.Date("2016-03-15", "%Y-%m-%d") # Spring Equinox
  SS <- as.Date("2016-06-15", "%Y-%m-%d") # Summer Solstice
  AE <- as.Date("2016-09-15", "%Y-%m-%d") # Autumn Equinox

  d <- when( anchor, n )
  d <- as.Date( strftime( d, format="2016-%m-%d" ) )

  ifelse( d >= WS | d < SE, "Winter",
    ifelse( d >= SE & d < SS, "Spring",
      ifelse( d >= SS & d < AE, "Summer", "Autumn" )
    )
  )
}

# -----------------------------------------------------------------------------
# Converts the first letter in a string to lowercase
# -----------------------------------------------------------------------------
lc <- function( s ) {
  concat( tolower( substr( s, 1, 1 ) ), substr( s, 2, nchar( s ) ) )
}

# -----------------------------------------------------------------------------
# Converts the entire string to lowercase
# -----------------------------------------------------------------------------
lower <- tolower

# -----------------------------------------------------------------------------
# Converts the first letter in a string to uppercase
# -----------------------------------------------------------------------------
uc <- function( s ) {
  concat( toupper( substr( s, 1, 1 ) ), substr( s, 2, nchar( s ) ) )
}

# -----------------------------------------------------------------------------
# Returns the number of days between the given dates.
# -----------------------------------------------------------------------------
days <- function( d1, d2, format = "%Y-%m-%d" ) {
  dates = c( d1, d2 )
  dt = strptime( dates, format = format )
  as.integer( difftime( dates[2], dates[1], units = "days" ) )
}

weeks <- function( began, ended ) {
  began = when( anchor, began )
  ended = when( anchor, ended )

  if( as.integer( ended - began ) < 0 ) {
    tempd = began
    began = ended
    ended = tempd
  }

  # Calculate number of elapsed weeks.
  length( seq( from = began, to = ended, by = "weeks" ) ) - 1
}

# -----------------------------------------------------------------------------
# Returns the number of years elapsed.
# -----------------------------------------------------------------------------
years <- function( began, ended ) {
  began = when( anchor, began )
  ended = when( anchor, ended )

  # Swap the dates if the end date comes before the start date.
  if( as.integer( ended - began ) < 0 ) {
    tempd = began
    began = ended
    ended = tempd
  }

  # Calculate number of elapsed years.
  length( seq( from = began, to = ended, by = "year" ) ) - 1
}

# -----------------------------------------------------------------------------
# Full name of the month, starting with a capital letter.
# -----------------------------------------------------------------------------
month <- function( n ) {
  # Faster than month.name[ x( n ) ]
  .subset( month.name, x( n ) )
}

# -----------------------------------------------------------------------------
# -----------------------------------------------------------------------------
money <- function( n ) {
  commas( x( n ) )
}

# -----------------------------------------------------------------------------
# -----------------------------------------------------------------------------
timeline <- function( n ) {
  concat( weekday( n ), ", ", annal( n ), " (", season( n ), ")" )
}

# -----------------------------------------------------------------------------
# Rounds to the nearest base value (e.g., round to nearest 10).
#
# @param base The nearest value to round to.
# -----------------------------------------------------------------------------
round.up <- function( n, base = 5 ) {
  base * round( x( n ) / base )
}

# -----------------------------------------------------------------------------
# Removes common accents from letters.
#
# @param s The string to remove diacritics from.
# -----------------------------------------------------------------------------
accentless <- function( s ) {
  chartr(
    "áéóūáéíóúÁÉÍÓÚýÝàèìòùÀÈÌÒÙâêîôûÂÊÎÔÛãõÃÕñÑäëïöüÄËÏÖÜÿçÇ",
    "aeouaeiouAEIOUyYaeiouAEIOUaeiouAEIOUaoAOnNaeiouAEIOUycC",
    s );
}

# -----------------------------------------------------------------------------
# Computes linear distance between two points using Haversine formula.
# Although Earth is an oblate spheroid, this will produce results close
# enough for most purposes.
#
# @param lat1/lon1 The source latitude and longitude.
# @param lat2/lon2 The destination latitude and longitude.
# @param radius The radius of the sphere.
#
# @return The distance between the two coordinates in meters.
# -----------------------------------------------------------------------------
haversine <- function( lat1, lon1, lat2, lon2, radius = 6371 ) {
  # Convert decimal degrees to radians
  lon1 = lon1 * pi / 180
  lon2 = lon2 * pi / 180
  lat1 = lat1 * pi / 180
  lat2 = lat2 * pi / 180

  # Haversine formula
  dlon = lon2 - lon1
  dlat = lat2 - lat1
  a = sin( dlat / 2 ) ** 2 + cos( lat1 ) * cos( lat2 ) * sin( dlon / 2 ) ** 2
  c = 2 * atan2( sqrt( a ), sqrt( 1-a ) )

  return( radius * c * 1000 )
}

