# -----------------------------------------------------------------------------
# Copyright 2021 Robin Gertenbach.
#
# Copyright 2021 White Magic Software, Ltd.
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
# See Damian Conway's "An Algorithmic Approach to English Pluralization":
#   http://goo.gl/oRL4MP
# See Oliver Glerke's Evo Inflector: https://github.com/atteo/evo-inflector/
# See Shevek's Pluralizer: https://github.com/shevek/linguistics/
# See also: http://www.freevectors.net/assets/files/plural.txt
# -----------------------------------------------------------------------------

# -----------------------------------------------------------------------------
# Applies all pluralization rules.
#
# @param word The word to pluralize.
# @param method The pluralization approach to apply to the word.
# @param n When any other value than 1, the word is pluralized.
# -----------------------------------------------------------------------------
pluralize <- function( word, method = c( "ac", "ca", "a", "c" ), n = 2 ) {
  if( n != 1 ) {
    method <- match.arg( method )

    coalesce( 
      pluralize_non_inflecting( word ),
      pluralize_pronoun( word ),
      pluralize_irregular( word, method ),
      pluralize_irregular_inflection_for_common_suffixes( word ),
      pluralize_fully_assimilated_classical_inflections( word ),
      pluralize_classical_variants_of_modern_inflections( word, method ),
      pluralize_ch_sh_ss_suffixes( word ),
      pluralize_f_and_fe_suffix( word ),
      pluralize_y_suffix( word ),
      pluralize_o_suffix( word ),
      pluralize_compound_words( word ),
      pluralize_regular( word )
    )
  }
  else {
    word
  }
}

# -----------------------------------------------------------------------------
# Rule 1
#
# Retain non-inflective user-mapped noun as is.
#
# Rule 2
#
# Irregular verbs that do not inflect in plural.
# -----------------------------------------------------------------------------
pluralize_non_inflecting <- function( word ) {
  coalesce( 
    ifelse( word %in% .uninflected_nouns, word, NA_character_ ),
    ifelse( word %in% .singular_nouns, word, NA_character_ ),
    ifelse( check_suffix( word, .irregular_patterns ), word, NA_character_ )
  )
} 

.check_non_inflecting <- function( word ) {
  is_uninflected <- word %in% .uninflected_nouns
  is_singular <- word %in% .singular_nouns
  is_irregular <- check_suffix( word, .irregular_patterns )

  is_uninflected | is_singular | is_irregular
}

.uninflected_nouns <- c( 
  "adonis",
  "anis",
  "bison",
  "bream",
  "breeches",
  "britches",
  "carp",
  "chassis",
  "clippers",
  "cod",
  "contretemps",
  "corps",
  "debris",
  "diabetes",
  "djinn",
  "eland",
  "elk",
  "flounder",
  "gallows",
  "graffiti",
  "headquarters",
  "herpes",
  "high-jinks",
  "homework",
  "innings",
  "jackanapes",
  "mackerel",
  "measles",
  "mews",
  "mumps",
  "news",
  "pants",
  "physics",
  "pincers",
  "pliers",
  "proceedings",
  "rabies",
  "salmon",
  "scissors",
  "sea-bass",
  "series",
  "shears",
  "species",
  "swine",
  "trout",
  "tuna",
  "whiting",
  "wildebeest"
)

.singular_nouns <- c( 
  "bathos",
  "caddis",
  "cannabis",
  "dais",
  "digitalis",
  "ethos",
  "glottis",
  "marquis",
  "pathos",
  "polis"
)

.irregular_patterns <- c( 
  "fish$", "ois$", "-sheep$", "deer$", "pox$", "[A-Z].*ese$", "itis$"
)

.prepositions <- c( 
  "about",
  "above",
  "across",
  "after",
  "among",
  "around",
  "at",
  "athwart",
  "before",
  "behind",
  "below",
  "beneath",
  "beside",
  "besides",
  "between",
  "betwixt",
  "beyond",
  "but",
  "by",
  "during",
  "except",
  "for",
  "from",
  "in",
  "into",
  "near",
  "of",
  "off",
  "on",
  "onto",
  "out",
  "over",
  "since",
  "till",
  "to",
  "under",
  "until",
  "unto",
  "upon",
  "with"
)

# -----------------------------------------------------------------------------
# Rule 3
#
# Handle pronouns in the nominative, accusative, and dative and propositional
# phrases.
# -----------------------------------------------------------------------------
pluralize_pronoun <- function( word ) {
  as.vector( .pluralized_pronouns[word] )
}

.pluralized_pronouns <- c( 
  "I" = "we",
  "me" = "us",
  "myself" = "ourselves",

  "you" = "you",
  "thou" = "ye",
  "thee" = "ye",
  "yourself" = "yourself",
  "thyself" = "yourself",

  "she" = "they",
  "he" = "they",
  "it" = "they",
  "they" = "they",

  "her" = "them",
  "him" = "them",
  "it" = "them",
  "them" = "them",

  "herself" = "themselves",
  "himself" = "themselves",
  "itself" = "themselves",

  "themself" = "themselves",
  "oneself" = "oneselves"
)

# -----------------------------------------------------------------------------
# Rule 4
#
# Change irregular plurals based on mapping.
# -----------------------------------------------------------------------------
pluralize_irregular <- function( word, method = c( "ac", "ca", "a", "c" ) ) {
  method <- match.arg( method )
  plurals <- .irregular_nouns[word]

  extract_plural <- function( plurals ) {
    if( is.null( plurals ) ) {
      return( NA_character_ )
    }

    return(
      switch( 
        method,
        "a" = plurals["a"],
        "c" = plurals["c"],
        "ac" = if.na( plurals["a"], plurals["c"] ),
        "ca" = if.na( plurals["c"], plurals["a"] )
      )
    )
  }

  as.character( lapply( plurals, extract_plural ) )
}

.irregular_nouns <- list( 
  "beef"      = c( "a" = "beefs",       "c" = "beeves" ),
  "brother"   = c( "a" = "brothers",    "c" = "brethren" ),
  "child"     = c( "a" = NA_character_, "c" = "children" ),
  "cow"       = c( "a" = "cows",        "c" = "kine" ),
  "ephemeris" = c( "a" = NA_character_, "c" = "ephemerides" ),
  "genie"     = c( "a" = "genies",      "c" = "genii" ),
  "money"     = c( "a" = "moneys",      "c" = "monies" ),
  "mongoose"  = c( "a" = "mongooses",   "c" = NA_character_ ),
  "mythos"    = c( "a" = NA_character_, "c" = "mythoi" ),
  "octopus"   = c( "a" = "octopuses",   "c" = "octopodes" ),
  "ox"        = c( "a" = NA_character_, "c" = "oxen" ),
  "soliloquy" = c( "a" = "soliloquies", "c" = NA_character_ ),
  "trilby"    = c( "a" = "trilbys",     "c" = NA_character_ )
)

# -----------------------------------------------------------------------------
# Rule 5
#
# Handle irregular inflections for common suffixes.
# -----------------------------------------------------------------------------
pluralize_irregular_inflection_for_common_suffixes <- function( word ) {
  output <- sub( "man$", "men", word )
  output <- sub( "([ml])(ouse)$", "\\1ice", output )
  output <- sub( "tooth$", "teeth", output )
  output <- sub( "goose$", "geese", output )
  output <- sub( "foot$", "feet", output )
  output <- sub( "zoon$", "zoa", output )
  output <- sub( "([csx])(is)$", "\\1es", output )

  ifelse( output == word, NA_character_, output )
}

# -----------------------------------------------------------------------------
# Rule 6
#
# Handle fully assimilated classical inflections.
# -----------------------------------------------------------------------------
pluralize_fully_assimilated_classical_inflections <- function( word ) {
  output <- replace_suffix(
    word, "", "e", c( "alumna", "alga", "vertebra" ) )
  output <- replace_suffix(
    output, "ex", "ices", c( "codex", "murex", "silex" ) )
  output <- replace_suffix(
    output, "on", "a", c( 
      "aphelion",
      "asyndeton",
      "criterion",
      "hyperbaton",
      "noumenon",
      "organon",
      "perihelion",
      "phenomenon",
      "prolegomenon"
    )
  )
  output <- replace_suffix(
    output, "um", "a", c( 
      "agendum",
      "bacterium",
      "candelabrum",
      "datum",
      "desideratum",
      "erratum",
      "extremum",
      "ovum",
      "stratum"
    )
  )

  ifelse( output == word, NA_character_, output )
}

# -----------------------------------------------------------------------------
# Rule 7
#
# Classical variants of modern inflections (e.g., stigmata, soprani).
#
# See tables A.11 to A.13, A.15, A.16, A.18, A.21 to A.25.
# -----------------------------------------------------------------------------
pluralize_classical_variants_of_modern_inflections <- function( 
  word, method = c( "ac", "ca", "a", "c" ) ) {
  method <- match.arg( method )

  # -a to -as (anglicized) or -ae (classical)
  a11 <- c( 
    "abscissa",
    "amoeba",
    "antenna",
    "aurora",
    "formula",
    "hydra",
    "hyperbola",
    "lacuna",
    "medusa",
    "nebula",
    "nova",
    "parabola"
  )

  # Table A.12: -a to -as (anglicized) or -ata (classical)
  a12 <- c( 
    "anathema",
    "bema",
    "carcinoma",
    "charisma",
    "diploma",
    "dogma",
    "drama",
    "edema",
    "enema",
    "enigma",
    "gumma",
    "lemma",
    "lymphoma",
    "magma",
    "melisma",
    "miasma",
    "oedema",
    "sarcoma",
    "schema",
    "soma",
    "stigma",
    "stoma",
    "trauma"
  )
  
  # Table A.13: -en to -ens (anglicized) or -ina (classical)
  a13 <- c( "stamen", "foramen", "lumen" )
  
  # Table A.15: -ex to -exes (anglicized) or -ices (classical)
  a15 <- c( 
    "apex",
    "cortex",
    "index",
    "latex",
    "pontifex",
    "simplex",
    "vertex",
    "vortex"
  )
  
  # Table A.16: -is to -ises (anglicized) or -ides (classical)
  a16 <- c( "iris", "clitoris" )
  
  # Table A.18: -o to -os (anglicized) or -i (classical)
  a18 <- c( 
    "alto",
    "basso",
    "canto",
    "contralto",
    "crescendo",
    "solo",
    "soprano",
    "tempo"
  )
   
  # Table A.21: -um to -ums (anglicized) or -a (classical)
  a21 <- c( 
    "aquarium",
    "compendium",
    "consortium",
    "cranium",
    "curriculum",
    "dictum",
    "emporium",
    "enconium",
    "gymnasium",
    "honorarium",
    "interregnum",
    "lustrum",
    "maximum",
    "medium",
    "memorandum",
    "millenium",
    "minimum",
    "momentum",
    "optimum",
    "phylum",
    "quantum",
    "rostrum",
    "spectrum",
    "speculum",
    "stadium",
    "trapezium",
    "ultimatum",
    "vacuum",
    "velum"
  )
  
  # Table A.22: -us to -uses (anglicized) or -i (classical)
  a22 <- c( 
    "focus",
    "fungus",
    "genius",
    "incubus",
    "nimbus",
    "nucleolus",
    "radius",
    "stylus",
    "succubus",
    "torus",
    "umbilicus",
    "uterus"
  )
  
  # Table A.23: -us to -uses (anglicized) or -us (classical)
  a23 <- c( 
    "apparatus",
    "cantus",
    "coitus",
    "hiatus",
    "impetus",
    "nexus",
    "plexus",
    "prospectus",
    "sinus",
    "status"
  )
  
  output <- replace_suffix( word, "", "im", c( "cherub", "goy", "seraph"  ) )
  output <- replace_suffix( output, "", "i", c( "afreet", "afrit", "efreet" ) )
  
  if( method %in% c( "a", "ac" ) ) {
    output <- replace_suffix( output, "us", "uses", a23 )
    output <- replace_suffix( output, "us", "uses", a22 )
    output <- replace_suffix( output, "um", "ums", a21 )
    output <- replace_suffix( output, "o", "os", a18 )
    output <- replace_suffix( output, "is", "ises", a16 )
    output <- replace_suffix( output, "ex", "exes", a15 )
    output <- replace_suffix( output, "en", "ens", a13 )
    output <- replace_suffix( output, "a", "as", a12 )
    output <- replace_suffix( output, "a", "as", a11 )
  } else {
    output <- replace_suffix( output, "us", "us", a23 )
    output <- replace_suffix( output, "us", "i", a22 )
    output <- replace_suffix( output, "um", "a", a21 )
    output <- replace_suffix( output, "o", "i", a18 )
    output <- replace_suffix( output, "is", "ides", a16 )
    output <- replace_suffix( output, "ex", "ices", a15 )
    output <- replace_suffix( output, "en", "ina", a13 )
    output <- replace_suffix( output, "a", "ata", a12 )
    output <- replace_suffix( output, "a", "ae", a11 )
  }

  ifelse( 
    output == word & (method %in% c( "a", "ac" ) | !word %in% a23), 
    NA_character_, 
    output
  )
}

# -----------------------------------------------------------------------------
# Rule 8
#
# The suffixes -ch, -sh, and -ss all take -es in the plural (e.g., churches,
# classes).
# -----------------------------------------------------------------------------
pluralize_ch_sh_ss_suffixes <- function( word ) {
  output <- sub( "([cs]h)$", "\\1es", word )
  output <- replace_suffix( output, "ss", "sses" )

  ifelse( output == word, NA_character_, output )
}

# -----------------------------------------------------------------------------
# Rule 9
#
# Certain words ending in -f or -fe take -ves in the plural.
# -----------------------------------------------------------------------------
pluralize_f_and_fe_suffix <- function( word ) {
  output <- sub( "([aeo]l|[^d]ea|ar)f$", "\\1ves", word )
  output <- sub( "([nlw]i)fe$", "\\1ves", output )

  ifelse( output == word, NA_character_, output )
}

# -----------------------------------------------------------------------------
# Rule 10
#
# Words ending in -y take -ies.
# -----------------------------------------------------------------------------
pluralize_y_suffix <- function( word ) {
  output <- sub( "([aeiou]y)$", "\\1s", word )
  output <- sub( "([A-Z].*y)$", "\\1s", output )
  output <- replace_suffix( output, "y", "ies" )

  ifelse( output == word, NA_character_, output )
}

# -----------------------------------------------------------------------------
# Rule 11
#
# Some words ending in -o take -os (lassos, solos). See tables A.17 and A.18.
# Others take -oes (potatoes, dominoes).
# When -o is preceded by a vowel always take -os (folios, bamboos).
# -----------------------------------------------------------------------------
pluralize_o_suffix <- function( word, method = c( "ac", "ca", "a", "c" ) ) {
  method <- match.arg( method )

  # Table A.17: -o to -os
  a17 <- c( 
    "albino",
    "archipelago",
    "armadillo",
    "commando",
    "ditto",
    "dynamo",
    "embryo",
    "fiasco",
    "generalissimo",
    "ghetto",
    "guano",
    "inferno",
    "jumbo",
    "lingo",
    "lumbago",
    "magneto",
    "manifesto",
    "medico",
    "octavo",
    "photo",
    "pro",
    "quarto",
    "rhino",
    "stylo"
  )

  # Table A.18: -o to -os (anglicized) or -i (classical)
  a18 <- c( 
    "alto",
    "basso",
    "canto",
    "contralto",
    "crescendo",
    "solo",
    "soprano",
    "tempo"
  )

  output <- replace_suffix( word, "o", "os", a17 )
  replacement <- if( method %in% c( "c", "ca" ) ) "i" else "os"
  output <- replace_suffix( output, "o", replacement, a18 )

  ifelse( output == word, NA_character_, output )
}

# -----------------------------------------------------------------------------
# Rule 12
#
# Compound word pluralization.
# -----------------------------------------------------------------------------
pluralize_compound_words <- function(
  word, method = c( "ac", "ca", "a", "c" ) ) {
  method <- match.arg( method )
  military <- c(
    "Adjutant",
    "Brigadier",
    "Lieutenant",
    "Major",
    "Quartermaster"
  )

  pluralize_cw <- Vectorize(
    function( cw, seps ) {
      if( cw[length( cw )] %in% c( "General", "general" ) && 
          (!cw[length( cw )] %in% military) ) {
        cw[1] <- pluralize( cw[1], method )
      } else {
        cw[1] <- pluralize( cw[1], method )
      }

      paste( paste0( seps, cw ), collapse = "" )
    }
  )

  parts <- strsplit( word, "[- ]+" )
  seps <- strsplit( word, "[^ -]+" )
  is_compound <- grepl( "[- ]", word )
  output <- word
  output[!is_compound] <- NA_character_
  output[is_compound] <- pluralize_cw( parts[is_compound], seps[is_compound] )

  output
}

# -----------------------------------------------------------------------------
# Rule 13
#
# Otherwise add -es if ending in -s; otherwise, append -s (e.g., tennis,
# lychnis, penis, and other singular forms).
# -----------------------------------------------------------------------------
pluralize_regular <- function( word ) {
  ending <- 's'

  if( endsWith( word, ending ) ) {
    ending <- "es"
  }

  paste0( word, ending )
}

# -----------------------------------------------------------------------------
# Determines whether the word ends with one of the given suffixes.
# -----------------------------------------------------------------------------
check_suffix <- function( x, suffixes ) {
  pattern <- paste0( "(", paste( suffixes, collapse = "|" ), ")$" )
  grepl( pattern, x, ignore.case = TRUE )
}

# -----------------------------------------------------------------------------
# Replaces the suffix of the word.
# -----------------------------------------------------------------------------
replace_suffix <- function( x, suffix, replacement, eligible = NULL ) {
  ifelse( 
    is.null( eligible ) | x %in% eligible, 
    sub( paste0( suffix, "$" ), replacement, x ),
    x
  )
}

# -----------------------------------------------------------------------------
# Returns y if x is na, otherwise x.
# -----------------------------------------------------------------------------
if.na <- function( x, y ) {
  ifelse( is.na( x ), y, x )
}

# -----------------------------------------------------------------------------
# Reduces the given function list.
# -----------------------------------------------------------------------------
coalesce <- function( ... ) {
  args <- list( ... )
  Reduce( if.na, args )
}

