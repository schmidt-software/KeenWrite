<?php
  // Log all errors to a temporary file.
  ini_set( "log_errors", 1 );
  ini_set( "error_log", "/tmp/php-errors.log" );

  // Keep running upon client disconnect (helps catch file transfer failures).
  // This setting requires checking whether the connection has been aborted at
  // a regular interval to prevent bogging the server with abandoned requests.
  ignore_user_abort( true );

  // Allow setting session variables (cookies).
  if( session_id() === PHP_SESSION_NONE ) {
    session_start();
  }

  /**
   * Answers whether the user's session has expired.
   *
   * @param int $lifetime Number of seconds the session lasts before expiring.
   *
   * @return bool True indicates the session has expired (or was not set).
   */
  function session_expired( $lifetime ) {
    // Session cookie, not used for user tracking, tracks last download date.
    $COOKIE_NAME = 'LAST_DOWNLOAD';
    $now = time();
    $expired = !isset( $_SESSION[ $COOKIE_NAME ] );

    if( !$expired && ($now - $_SESSION[ $COOKIE_NAME ]) > $lifetime ) {
      $_SESSION = array();

      session_destroy();

      $expired = true;
    }

    // Update last activity timestamp.
    $_SESSION[ $COOKIE_NAME ] = $now;

    return $expired;
  }

  function create_lock_filename( $filename ) {
    return $filename .'.lock';
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
   * Increments the number of times a file has been accessed, respecting
   * session expiration and atomic read/write operations.
   *
   * @param string $filename The file containing a number to increment.
   */
  function hit_count( $filename ) {
    if( session_expired( 7 * 24 * 60 * 60 ) ) {
      increment_count( $filename );
    }
  }

  /**
   * Isolate the file name being downloaded.
   *
   * @param array $fileinfo The result from calling pathinfo.
   *
   * @return string The normalized file name.
   */
  function normalize_filename( $fileinfo ) {
    $basename = $fileinfo[ 'basename' ];

    if( isset( $_SERVER[ 'HTTP_USER_AGENT' ] ) ) {
      $periods = substr_count( $basename, '.' );

      // Address IE bug regarding multiple periods in filename.
      $basename = strstr( $_SERVER[ 'HTTP_USER_AGENT' ], 'MSIE' )
        ? preg_replace( '/\./', '%2e', $basename, $periods - 1 )
        : $basename;
    }

    $basename = preg_replace( '/\s+/', '', $basename );
    $basename = mb_ereg_replace( '([^\w\d\-_~,;\[\]\(\).])', '', $basename );
    $basename = mb_ereg_replace( '([\.]{2,})', '', $basename );

    return $basename;
  }

  /**
   * Determine the content type based on the file name extension, rather
   * than the file contents. This could be inaccurate, but we'll trust that
   * the website administrator is posting files whose content reflects the
   * file name extension.
   * <p>
   * If the file name extension is not known, the content type will force
   * the download (to prevent the browser from trying to play the content
   * directly).
   *
   * @param array $fileinfo The result from calling pathinfo.
   *
   * @return string The IANA-defined Media Type for the file name extension.
   */
  function get_content_type( $fileinfo ) {
    $extension = strtolower( $fileinfo[ 'extension' ] );

    switch( $extension ) {
      case 'app': $ctype='application/octet-stream'; break;
      case 'bin': $ctype='application/octet-stream'; break;
      case 'exe': $ctype='application/octet-stream'; break;
      case 'jar': $ctype='application/octet-stream'; break;
      case 'zip': $ctype='application/zip'; break;
      case 'avi': $ctype='video/msvideo'; break;
      case 'mp3': $ctype='audio/mpeg'; break;
      case 'mpg': $ctype='video/mpeg'; break;
      case 'mpv': $ctype='video/mpv'; break;
      case 'webm': $ctype='video/webm'; break;
      default: $ctype='application/force-download'; break;
    }

    return $ctype;
  }

  /**
   * Downloads a file, allowing for resuming partial downloads.
   *
   * @param string $path Fully qualified path of a file to download.
   *
   * @return bool True if the download succeeded.
   */
  function download( $path ) {
    // Don't cache the result of the file stats.
    clearstatcache();

    $size = @filesize( $path );
    $size = $size === false || empty( $size ) ? 0 : $size;
    $fileinfo = pathinfo( $path );
    $filename = normalize_filename( $fileinfo );
    $content_type = get_content_type( $fileinfo );
    $range = "0-$size";

    // Check if a range is sent by browser or download manager.
    if( isset( $_SERVER[ 'HTTP_RANGE' ] ) ) {
      list( $units, $range_orig ) = explode( '=', $_SERVER[ 'HTTP_RANGE' ], 2 );

      if( $units == 'bytes' ) {
        // Multiple ranges could be specified, but only serve the first range.
        list( $range, $extra_ranges ) = explode( ',', $range_orig, 2 );
      }
    }

    // Figure out download piece from range.
    list( $seek_start, $seek_end ) = explode( '-', $range, 2 );

    // Set start and end based on range, otherwise use defaults.
    $seek_end = empty( $seek_end )
      ? max( $size - 1, 0 )
      : min( abs( $seek_end + 0 ), $size - 1 );
    $seek_start = empty( $seek_start || $seek_end < abs( $seek_start + 0 ) )
      ? 0
      : max( abs( $seek_start + 0 ), 0 );

    // Send partial content header if downloading a piece (IE workaround).
    if( $seek_start > 0 || $seek_end < ($size - 1) ) {
      header( 'HTTP/1.1 206 Partial Content' );
    }

    $range_bytes = $seek_start . '-' . $seek_end . '/' . $size;

    if( ob_get_level() > 0 ) {
      ob_end_clean();
    }

    header( 'Accept-Ranges: bytes' );
    header( 'Content-Range: bytes ' . $range_bytes );
    header( 'Content-Type: ' . $content_type );
    header( 'Content-Disposition: attachment; filename="' . $filename . '"' );
    header( 'Content-Length: ' . ($seek_end - $seek_start + 1) );

    if( ob_get_level() == 0 ) {
      ob_start();
    }

    // If the file doesn't exist, don't count it as a download.
    $bytes_sent = -1;

    // Open the file to be downloaded.
    $fp = @fopen( $path, 'rb' );

    if( $fp !== false ) {
      @fseek( $fp, $seek_start );

      $aborted = false;
      $bytes_sent = $seek_start;
      $chunk_size = 1024 * 8;

      while( !feof( $fp ) && !$aborted ) {
        set_time_limit( 0 );
        print( fread( $fp, $chunk_size ) );
        $bytes_sent += $chunk_size;

        if( ob_get_level() > 0 ) {
          ob_flush();
        }

        flush();

        $aborted = connection_aborted();
      }

      if( ob_get_level() > 0 ) {
        ob_end_flush();
      }

      fclose( $fp );
    }

    // Download succeeded if the total bytes matches or exceeds the file size.
    return $bytes_sent >= $size;
  }

  $filename = isset( $_GET[ 'filename' ] ) ? $_GET[ 'filename' ] : '';

  if( !empty( $filename ) && download( $filename ) ) {
    hit_count( "$filename-count.txt" );
  }
?>
