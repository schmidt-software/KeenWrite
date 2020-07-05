setwd( '$application.r.working.directory$' )
assign( "anchor", '$date.anchor$', envir = .GlobalEnv )

source( 'pluralize.R' )
source( 'possessive.R' )
source( 'conversion.R' )
source( 'csv.R' )

