assign( 'anchor', as.Date( '$date.anchor$', format='%Y-%m-%d' ), envir = .GlobalEnv );
setwd( '$application.r.working.directory$' );
source( '../bin/pluralize.R' );
source( '../bin/common.R' );
