<?php
  /* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
   *
   * SPDX-License-Identifier: MIT
   */

  // Log all errors to a temporary file.
  ini_set( 'log_errors', 1 );
  ini_set( 'error_log', '/tmp/php-errors.log' );

  // Turn off server-side compression.
  @ini_set( 'zlib.output_compression', 'Off' );

  // Do not impose a time limit for downloads.
  set_time_limit( 0 );

  // Flush any previous output buffers.
  while( ob_get_level() > 0 ) {
    ob_end_flush();
  }

  if( session_id() === "" ) {
    session_start();
  }

  // Keep running upon client disconnect (helps catch file transfer failures).
  // This setting requires checking whether the connection has been aborted at
  // a regular interval to prevent bogging the server with abandoned requests.
  ignore_user_abort( true );

  $filename = get_sanitized_filename();
  $valid_filename = !empty( $filename );
  $expiry = 24 * 60 * 60;

  if( $valid_filename && download( $filename ) && token_expired( $expiry ) ) {
    increment_count( "$filename-count.txt" );
  }

  /**
   * Retrieve the file name being downloaded from the HTTP GET request.
   *
   * @return string The sanitized file name (without path information).
   */
  function get_sanitized_filename() {
    $filepath = isset( $_GET[ 'filename' ] ) ? $_GET[ 'filename' ] : '';
    $fileinfo = pathinfo( $filepath );

    // Remove path information (no /etc/passwd or ../../etc/passwd for you).
    $basename = $fileinfo[ 'basename' ];

    if( isset( $_SERVER[ 'HTTP_USER_AGENT' ] ) ) {
      $periods = substr_count( $basename, '.' );

      // Address IE bug regarding multiple periods in filename.
      $basename = strstr( $_SERVER[ 'HTTP_USER_AGENT' ], 'MSIE' )
        ? mb_ereg_replace( '/\./', '%2e', $basename, $periods - 1 )
        : $basename;
    }

    // Trim all spaces, even internal ones.
    $basename = mb_ereg_replace( '/\s+/', '', $basename );

    // Sanitize.
    $basename = mb_ereg_replace( '([^\w\d\-_~,;\[\]\(\).])', '', $basename );

    return $basename;
  }

  /**
   * Answers whether the user's download token has expired.
   *
   * @param int $lifetime Number of seconds before expiring the token.
   *
   * @return bool True indicates the token has expired (or was not set).
   */
  function token_expired( $lifetime ) {
    $TOKEN_NAME = 'LAST_DOWNLOAD';
    $now = time();
    $expired = !isset( $_SESSION[ $TOKEN_NAME ] );

    if( !$expired && ($now - $_SESSION[ $TOKEN_NAME ] > $lifetime) ) {
      $expired = true;
      $_SESSION = array();

      session_destroy();
    }

    $_SESSION[ $TOKEN_NAME ] = $now;

    $TOKEN_CREATE = 'CREATED';

    if( !isset( $_SESSION[ $TOKEN_CREATE ] ) ) {
      $_SESSION[ $TOKEN_CREATE ] = $now;
    }
    else if( $now - $_SESSION[ $TOKEN_CREATE ] > $lifetime ) {
      // Avoid session fixation attacks by regenerating tokens.
      session_regenerate_id( true );
      $_SESSION[ $TOKEN_CREATE ] = $now;
    }

    return $expired;
  }

  /**
   * Downloads a file, allowing for resuming partial downloads.
   *
   * @param string $filename File to download, must be in script directory.
   *
   * @return bool True if the file was transferred.
   */
  function download( $filename ) {
    // Don't cache the file stats result (e.g., file size).
    clearstatcache();

    $size = @filesize( $filename );
    $size = $size === false || empty( $size ) ? 0 : $size;
    $content_type = mime_content_type( $filename );
    list( $seek_start, $content_length ) = parse_range( $size );

    // Added by PHP, removed by us.
    header_remove( 'x-powered-by' );

    // HTTP/1.1 clients must treat invalid date formats, especially 0, as past.
    header( 'Expires: 0' );

    // Prevent local caching.
    header( 'Cache-Control: public, must-revalidate, post-check=0, pre-check=0' );

    // No response message portion may be cached (e.g., by a proxy server).
    header( 'Cache-Control: private', false );

    // Force the browser to download, rather than display the file inline.
    header( "Content-Disposition: attachment; filename=\"$filename\"" );
    header( 'Accept-Ranges: bytes' );
    header( "Content-Length: $content_length" );
    header( "Content-Type: $content_type" );

    // Honour HTTP HEAD requests.
    return $_SERVER[ 'REQUEST_METHOD' ] === 'HEAD'
      ? false
      : transmit( $filename, $seek_start, $size );
  }

  /**
   * Parses the HTTP range request header, provided one was sent by the
   * client. This provides download resume functionality.
   *
   * @param int $size The total file size (as stored on disk). 
   *
   * @return int The starting offset for resuming the download, or 0 to
   * download the entire file (i.e., no offset could be parsed).
   */
  function parse_range( $size ) {
    // By default, start transmitting at the beginning of the file.
    $seek_start = 0;
    $content_length = $size;

    // Check if a range is sent by browser or download manager.
    if( isset( $_SERVER[ 'HTTP_RANGE' ] ) ) {
      $range_format = '/^bytes=\d*-\d*(,\d*-\d*)*$/';
      $request_range = $_SERVER[ 'HTTP_RANGE' ];

      // Ensure the content request range is in a valid format.
      if( !preg_match( $range_format, $request_range, $matches ) ) {
        header( 'HTTP/1.1 416 Requested Range Not Satisfiable' );
        header( "Content-Range: bytes */$size" );

        // Terminate because the range is invalid.
        exit;
      }

      // Multiple ranges could be specified, but only serve the first range.
      $seek_start = isset( $matches[ 1 ] ) ? $matches[ 1 ] + 0 : 0;
      $seek_end = isset( $matches[ 2 ] ) ? $matches[ 2 ] + 0 : $size - 1;
      $range_bytes = $seek_start . '-' . $seek_end . '/' . $size;
      $content_length = $seek_end - $seek_start + 1;

      header( 'HTTP/1.1 206 Partial Content' );
      header( "Content-Range: bytes $range_bytes" );
    }

    return array( $seek_start, $content_length );
  }

  /**
   * Transmits a file from the server to the client.
   *
   * @param string $filename File to download, must be this script directory.
   * @param integer $seek_start Offset into file to start downloading.
   * @param integer $size Total size of the file.
   *
   * @return bool True if the file was transferred.
   */
  function transmit( $filename, $seek_start, $size ) {
    // Buffering after sending HTTP headers to allow client download estimates.
    if( ob_get_level() == 0 ) {
      ob_start();
    }

    // If the file doesn't exist, don't count it as a download.
    $bytes_sent = -1;

    // Open the file to be downloaded.
    $fp = @fopen( $filename, 'rb' );

    if( $fp !== false ) {
      @fseek( $fp, $seek_start );

      $aborted = false;
      $bytes_sent = $seek_start;
      $chunk_size = 1024 * 16;

      while( !feof( $fp ) && !$aborted ) {
        print( @fread( $fp, $chunk_size ) );
        $bytes_sent += $chunk_size;

        // Send the file to download in small chunks.
        if( ob_get_level() > 0 ) {
          ob_flush();
        }

        flush();

        // Chunking the file allows detecting when the connection has closed.
        $aborted = connection_aborted() || connection_status() != 0;
      }

      // Indicate that transmission is complete.
      if( ob_get_level() > 0 ) {
        ob_end_flush();
      }

      fclose( $fp );
    }

    // Download succeeded if the total bytes matches or exceeds the file size.
    return $bytes_sent >= $size;
  }

  /**
   * Increments the number in a file using an exclusive lock. If the file
   * doesn't exist, it will be created and the initial value set to 0.
   *
   * @param string $filename The file containing a number to increment.
   */
  function increment_count( $filename ) {
    try {
      lock_open( $filename );

      // Coerce value to largest natural numeric data type.
      $count = @file_get_contents( $filename ) + 0;

      // Write the new counter value.
      file_put_contents( $filename, $count + 1 );
    }
    finally {
      lock_close( $filename );
    }
  }

  /**
   * Acquires a lock for a particular file. Callers would be prudent to
   * call this function from within a try/finally block and close the lock
   * in the finally section. The amount of time between opening and closing
   * the lock must be minimal because parallel processes will be waiting on
   * the lock's release.
   *
   * @param string $filename The name of file to lock.
   *
   * @return bool True if the lock was obtained, false upon excessive attempts.
   */
  function lock_open( $filename ) {
    $lockdir = create_lock_filename( $filename );

    // Track the number of times a lock attempt is made.
    $iterations = 0;

    do {
      // Creates and tests lock file existence atomically.
      if( @mkdir( $lockdir, 0777 ) ) {
        // Exit the loop.
        $iterations = 0;
      }
      else {
        $iterations++;
        $lifetime = time() - filemtime( $lockdir );

        if( $lifetime > 10 ) {
          // If the lock has gone stale, delete it.
          @rmdir( $lockdir );
        }
        else {
          // Wait a random duration to avoid concurrency conflicts.
          usleep( rand( 1000, 10000 ) );
        }
      }
    }
    while( $iterations > 0 && $iterations < 10 );

    // Indicate whether the maximum number of lock attempts were exceeded.
    return $iterations == 0;
  }

  /**
   * Releases the lock on a particular file.
   *
   * @param string $filename The name of file that was locked.
   */
  function lock_close( $filename ) {
    @rmdir( create_lock_filename( $filename ) );
  }

  /**
   * Creates a uniquely named lock directory name.
   *
   * @param string $filename The name of the file under contention.
   *
   * @return string A unique lock file reference for the given filename.
   */
  function create_lock_filename( $filename ) {
    return $filename .'.lock';
  }
?>
