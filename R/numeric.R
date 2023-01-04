# TODO: Finish the implementation

# -----------------------------------------------------------------------------
# Converts an integer value into English words. Negative numbers are prefixed
# with the word minus. This is useful for very large numbers.
#
# See https://english.stackexchange.com/a/111837/22099
#
# @param n Any integer value, including zero, and negative numbers.
# -----------------------------------------------------------------------------
to.words <- function( n ) {
  s <- 'zero';

  if( n > 0 ) {
    s <- to.words.nz( n );
  }
  else if( n < 0 ) {
    s <- paste0( 'minus ', to.words.nz( -n ) );
  }

  s
}

# -----------------------------------------------------------------------------
# Converts a non-zero number into English words.
# -----------------------------------------------------------------------------
to.words.nz <- function( n ) {
  scales <- c(
    "thousand", "million", "billion", "trillion", "quadrillion",
    "quintillion", "sextillion", "septillion", "octillion", "nonillion",
    "decillion", "undecillion", "duodecillion", "tredecillion",
    "quattuordecillion", "quindecillion", "sexdecillion", "septendecillion",
    "octodecillion", "novemdecillion", "vigintillion", "centillion",
    "quadrillion", "quitillion", "sextillion"
  );

  i <- 0;
  s <- "";

  while( n > 0 ) {
    if( !(n %% 1000 == 0) ) {
      j <- if( n < 100 ) "," else "";
      s <- paste( to.words.help( n %% 1000 ), scales[ i ], j, s );
    }

    n <- floor( n / 1000 );
    i <- i + 1;
  }

  s
}

to.words.help <- function( n ) {
  low <- c( 
    "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
    "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen",
    "seventeen", "eighteen", "nineteen"
  );

  tens <- c(
    "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"
  );

  if( n < 20 ) {
    s <- low[ n ];
  }
  else if( n < 100 ) {
    d <- n %% 10;
    j <- if( d > 0 ) "-" else "";
    s <- paste0( tens[ (n / 10) - 1 ], j, to.words.help( d ) );
  }
  else {
    d <- (n / 100);
    r <- (n %% 100);
    j <- if( r > 0 ) "and" else "";
    s <- paste( low[ d ], "hundred", j, to.words.help( r ) );
  }

  s
}

