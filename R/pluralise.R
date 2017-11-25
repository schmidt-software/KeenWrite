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
# See Damian Conway's "An Algorithmic Approach to English Pluralization":
#   http://goo.gl/oRL4MP
# See Oliver Glerke's Evo Inflector: https://github.com/atteo/evo-inflector/
# See Shevek's Pluralizer: https://github.com/shevek/linguistics/
# See also: http://www.freevectors.net/assets/files/plural.txt
#
# ######################################################################

pluralise <- function( s, n ) {
  result <- s

  # Partial implementation of Conway's algorithm for nouns.
  if( n != 1 ) {
    if( pl.noninflective( s ) ||
				pl.suffix( "es", s ) ||
        pl.suffix( "fish", s ) ||
        pl.suffix( "ois", s ) ||
        pl.suffix( "sheep", s ) ||
        pl.suffix( "deer", s ) ||
        pl.suffix( "pox", s ) ||
        pl.suffix( "[A-Z].*ese", s ) ||
        pl.suffix( "itis", s ) ) {
      # 1. Retain non-inflective user-mapped noun as is.
      # 2. Retain non-inflective plural as is.
      result <- s
    }
    else if( pl.is.irregular.pl( s ) ) {
      # 4. Change irregular plurals based on mapping.
      result <- pl.irregular.pl( s )
    }
    else if( pl.is.irregular.es( s ) ) {
      # x. From Shevek's
      result <- pl.inflect( s, "", "es" )
    }
    else if( pl.suffix( "man", s ) ) {
      # 5. For -man, change -an to -en
      result <- pl.inflect( s, "an", "en" )
    }
    else if( pl.suffix( "[lm]ouse", s ) ) {
      # 5. For [lm]ouse, change -ouse to -ice
      result <- pl.inflect( s, "ouse", "ice" )
    }
    else if( pl.suffix( "tooth", s ) ) {
      # 5. For -tooth, change -ooth to -eeth
      result <- pl.inflect( s, "ooth", "eeth" )
    }
    else if( pl.suffix( "goose", s ) ) {
      # 5. For -goose, change -oose to -eese
      result <- pl.inflect( s, "oose", "eese" )
    }
    else if( pl.suffix( "foot", s ) ) {
      # 5. For -foot, change -oot to -eet
      result <- pl.inflect( s, "oot", "eet" )
    }
    else if( pl.suffix( "zoon", s ) ) {
      # 5. For -zoon, change -on to -a
      result <- pl.inflect( s, "on", "a" )
    }
    else if( pl.suffix( "[csx]is", s ) ) {
      # 5. Change -cis, -sis, -xis to -es
      result <- pl.inflect( s, "is", "es" )
    }
    else if( pl.suffix( "([cs]h|ss|zz|x|s)", s ) ) {
      # 8. Change -ch, -sh, -ss, -zz, -x, -s to -es
      result <- pl.inflect( s, "", "es" )
    }
    else if( pl.suffix( "([aeo]lf|[^d]eaf|arf)", s ) ) {
      # 9. Change -f to -ves
      result <- pl.inflect( s, "f", "ves" )
    }
    else if( pl.suffix( "[nlw]ife", s ) ) {
      # 10. Change -fe to -ves
      result <- pl.inflect( s, "fe", "ves" )
    }
    else if( pl.suffix( "[aeiou]y", s ) ) {
      # 11. Change -[aeiou]y to -ys
      result <- pl.inflect( s, "", "s" )
    }
    else if( pl.suffix( "y", s ) ) {
      # 12. Change -y to -ies
      result <- pl.inflect( s, "y", "ies" )
    }
    else if( pl.suffix( "z", s ) ) {
      # x. Change -z to -zzes
      result <- pl.inflect( s, "", "zes" )
    }
    else {
      # 13. Default plural: add -s
      result <- pl.inflect( s, "", "s" )
    }
  }

  result
}

# Returns the given string (s) with its suffix replaced by r.
pl.inflect <- function( s, suffix, r ) {
  gsub( paste( suffix, "$", sep="" ), r, s )
}

# Answers whether the given string (s) has the given ending.
pl.suffix <- function( ending, s ) {
  grepl( paste( ending, "$", sep="" ), s )
}

# Answers whether the given string (s) is a noninflective noun.
pl.noninflective <- function( s ) {
  v <- c(
		"aircraft",
		"Bhutanese",
		"bison",
		"bream",
		"Burmese",
		"carp",
		"chassis",
		"Chinese",
		"clippers",
		"cod",
		"contretemps",
		"corps",
		"debris",
		"djinn",
		"eland",
		"elk",
		"flounder",
		"fracas",
		"gallows",
		"graffiti",
		"headquarters",
		"high-jinks",
		"homework",
		"hovercraft",
		"innings",
		"Japanese",
		"Lebanese",
		"mackerel",
		"means",
		"mews",
		"mice",
		"mumps",
		"news",
		"pincers",
		"pliers",
		"Portuguese",
		"proceedings",
		"salmon",
		"scissors",
		"sea-bass",
		"Senegalese",
		"shears",
		"Siamese",
		"Sinhalese",
		"spacecraft",
		"swine",
		"trout",
		"tuna",
		"Vietnamese",
		"watercraft",
		"whiting",
		"wildebeest"
  )

  is.element( s, v )
}

# Answers whether the given string (s) is an irregular plural.
pl.is.irregular.pl <- function( s ) {
  # Could be refactored with pl.irregular.pl...
  v <- c(
    "beef", "brother", "child", "cow", "ephemeris", "genie", "money",
    "mongoose", "mythos", "octopus", "ox", "soliloquy", "trilby"
  )

  is.element( s, v )
}

# Call to pluralise an irregular noun. Only call after confirming
# the noun is irregular via pl.is.irregular.pl.
pl.irregular.pl <- function( s ) {
  v <- list(
    "beef" = "beefs",
    "brother" = "brothers",
    "child" = "children",
    "cow" = "cows",
    "ephemeris" = "ephemerides",
    "genie" = "genies",
    "money" = "moneys",
    "mongoose" = "mongooses",
    "mythos" = "mythoi",
    "octopus" = "octopuses",
    "ox" = "oxen",
    "soliloquy" = "soliloquies",
    "trilby" = "trilbys"
  )

  # Faster version of v[[ s ]]
  .subset2( v, s )
}

# Answers whether the given string (s) pluralises with -es.
pl.is.irregular.es <- function( s ) {
  v <- c(
    "acropolis", "aegis", "alias", "asbestos", "bathos", "bias", "bronchitis",
    "bursitis", "caddis", "cannabis", "canvas", "chaos", "cosmos", "dais",
    "digitalis", "epidermis", "ethos", "eyas", "gas", "glottis", "hubris",
    "ibis", "lens", "mantis", "marquis", "metropolis", "pathos", "pelvis",
    "polis", "rhinoceros", "sassafrass", "trellis"
  )

  is.element( s, v )
}

