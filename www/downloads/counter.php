<?php
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

  /**
   * Answers whether the user's download token has expired.
   *
   * @param int $lifetime Number of seconds before expiring the token.
   *
   * @return bool True indicates the token has expired (or was not set).
   */
  function download_token_expired( $lifetime ) {
    $TOKEN_NAME = 'LAST_DOWNLOAD';
    $now = time();
    $expired = !isset( $_SESSION[ $TOKEN_NAME ] );

    if( !$expired && ($now - $_SESSION[ $TOKEN_NAME ] > $lifetime) ) {
      $_SESSION = array();

      session_destroy();
    }

    $_SESSION[ $TOKEN_NAME ] = $now;

    $TOKEN_CREATE = 'CREATED';

    if( !isset( $_SESSION[ $TOKEN_CREATE ] ) ) {
      $_SESSION[ $TOKEN_CREATE ] = $now;
    }
    else if( $now - $_SESSION[ $TOKEN_CREATE ] > $lifetime ) {
      session_regenerate_id( true );
      $_SESSION[ $TOKEN_CREATE ] = $now;
    }

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
        ? mb_ereg_replace( '/\./', '%2e', $basename, $periods - 1 )
        : $basename;
    }

    $basename = mb_ereg_replace( '/\s+/', '', $basename );
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
    // Don't cache the file stats result.
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

    header( 'Pragma: public' );
    header( 'Expires: -1' );
    header( 'Cache-Control: public, must-revalidate, post-check=0, pre-check=0' );
    header( "Content-Disposition: attachment; filename=\"$filename\"" );

    $content_length = $size;

    // Send partial content header if downloading a piece (IE workaround).
    if( $seek_start > 0 || $seek_end < ($size - 1) ) {
      $range_bytes = $seek_start . '-' . $seek_end . '/' . $size;
      $content_length = $seek_end - $seek_start + 1;

      header( 'HTTP/1.1 206 Partial Content' );
      header( "Content-Range: bytes $range_bytes" );
    }

    header( 'Accept-Ranges: bytes' );
    header( "Content-Length: $content_length" );
    header( "Content-Type: $content_type" );

    // If the file doesn't exist, don't count it as a download.
    $bytes_sent = -1;

    // Open the file to be downloaded.
    $fp = @fopen( $path, 'rb' );

    if( $fp !== false ) {
      @fseek( $fp, $seek_start );

      $aborted = false;
      $bytes_sent = $seek_start;
      $chunk_size = 1024 * 16;

      while( !feof( $fp ) && !$aborted ) {
        print( @fread( $fp, $chunk_size ) );
        $bytes_sent += $chunk_size;

        if( ob_get_level() > 0 ) {
          ob_flush();
        }

        flush();

        $aborted = connection_aborted() || connection_status() != 0;
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

  $unique_hit = download_token_expired( 24 * 60 * 60 );

  if( !empty( $filename ) && download( $filename ) && $unique_hit ) {
    increment_count( "$filename-count.txt" );
  }
?>
