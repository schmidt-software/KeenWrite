/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io.downloads;

import com.keenwrite.io.MediaType;
import com.keenwrite.io.MediaTypeSniffer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;

import static java.lang.Boolean.TRUE;
import static java.lang.Math.toIntExact;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.setFollowRedirects;

/**
 * Responsible for downloading files and publishing status updates. This will
 * download a resource provided by an instance of {@link URL} into a given
 * {@link OutputStream}.
 */
public final class DownloadManager {
  static {
    setProperty( "http.keepAlive", "false" );
    setFollowRedirects( true );
  }

  /**
   * Number of bytes to read at a time.
   */
  private static final int BUFFER_SIZE = 16384;

  /**
   * HTTP request timeout.
   */
  private static final Duration TIMEOUT = Duration.ofSeconds( 30 );

  @FunctionalInterface
  public interface ProgressListener {
    /**
     * Called when a chunk of data has been read. This is called synchronously
     * when downloading the data; do not execute long-running tasks in this
     * method (a few milliseconds is fine).
     *
     * @param percentage A value between 0 and 100, inclusive, represents the
     *                   percentage of bytes downloaded relative to the total.
     *                   A value of -1 means the total number of bytes to
     *                   download is unknown.
     * @param bytes      When {@code percentage} is greater than or equal to
     *                   zero, this is the total number of bytes. When {@code
     *                   percentage} equals -1, this is the number of bytes
     *                   read so far.
     */
    void update( int percentage, long bytes );
  }

  public static final class DownloadToken implements Closeable {

    private final HttpURLConnection mConn;
    private final BufferedInputStream mInput;
    private final MediaType mMediaType;
    private final long mBytesTotal;

    private DownloadToken(
      final HttpURLConnection conn,
      final BufferedInputStream input,
      final MediaType mediaType
    ) {
      assert conn != null;
      assert input != null;
      assert mediaType != null;

      mConn = conn;
      mInput = input;
      mMediaType = mediaType;
      mBytesTotal = conn.getContentLength();
    }

    /**
     * Provides the ability to download remote files asynchronously while
     * being updated regarding the download progress. Callers are responsible
     * for calling {@link #close()} after the download completes.
     *
     * @param output   Where to write the file contents.
     * @param listener Receives download progress status updates.
     * @return {@link Boolean#TRUE} to indicate the download completed
     * successfully, otherwise an exception is thrown.
     * @throws IOException          Could not download the file.
     * @throws InterruptedException Download was cancelled.
     */
    public Callable<Boolean> download(
      final OutputStream output,
      final ProgressListener listener
    ) throws IOException, InterruptedException {
      return () -> {
        final var buffer = new byte[ BUFFER_SIZE ];
        final var stream = getInputStream();
        final var bytesTotal = mBytesTotal;

        long bytesTally = 0;
        int bytesRead;

        while( (bytesRead = stream.read( buffer )) != -1 ) {
          if( Thread.currentThread().isInterrupted() ) {
            throw new InterruptedException();
          }

          bytesTally += bytesRead;

          if( bytesTotal > 0 ) {
            listener.update(
              toIntExact( bytesTally * 100 / bytesTotal ),
              bytesTotal
            );
          }
          else {
            listener.update( -1, bytesRead );
          }

          output.write( buffer, 0, bytesRead );
        }

        return TRUE;
      };
    }

    @Override
    public void close() throws IOException {
      getInputStream().close();
      mConn.disconnect();
    }

    /**
     * Returns the input stream to the resource to download.
     *
     * @return The stream to read.
     */
    public BufferedInputStream getInputStream() {
      return mInput;
    }

    /**
     * Answers whether the type of content associated with the download stream
     * is a scalable vector graphic.
     *
     * @return {@code true} if the given {@link MediaType} has SVG contents.
     */
    public boolean isSvg() {
      return mMediaType.isSvg();
    }

    public MediaType getMediaType() {
      return mMediaType;
    }
  }

  /**
   * Opens the input stream for the resource to download.
   *
   * @param url The {@link URL} resource to download.
   * @return A token that can be used for downloading the content with
   * periodic updates or retrieving the stream for downloading the content.
   * @throws IOException The stream could not be opened.
   */
  public static DownloadToken open( final String url ) throws IOException {
    // Pass an undefined media type so that any type of file can be retrieved.
    return open( new URL( url ) );
  }

  /**
   * Opens the input stream for the resource to download and verifies that
   * the given {@link MediaType} matches the requested type. Callers are
   * responsible for closing the {@link DownloadManager} to close the
   * underlying stream and the HTTP connection.
   *
   * @param url The {@link URL} resource to download.
   * @return A token that can be used for downloading the content with
   * periodic updates or retrieving the stream for downloading the content.
   * @throws IOException The stream could not be opened.
   */
  public static DownloadToken open( final URL url )
    throws IOException {
    final var conn = connect( url );

    MediaType mediaType;

    try {
      final var contentType = conn.getContentType();
      mediaType = MediaType.valueFrom( contentType );
    } catch( final Exception ex ) {
      // If the media type couldn't be detected, try using the stream.
      mediaType = MediaType.UNDEFINED;
    }

    final var input = open( conn );

    // Peek at the magic header bytes to determine the media type.
    final var dataMediaType = MediaTypeSniffer.getMediaType( input );

    // If the transport protocol's Content-Type doesn't align with the
    // media type for the remote data, differ to the derived media type.
    if( !mediaType.equals( dataMediaType ) && !dataMediaType.isUndefined() ) {
      mediaType = dataMediaType;
    }

    return new DownloadToken( conn, input, mediaType );
  }

  /**
   * Establishes a connection to the remote {@link URL} resource.
   *
   * @param url The {@link URL} representing a resource to download.
   * @return The connection manager for the {@link URL}.
   * @throws IOException         Could not establish a connection.
   * @throws ArithmeticException Could not compute a timeout value (this
   *                             should never happen because the timeout is
   *                             less than a minute).
   * @see #TIMEOUT
   */
  private static HttpURLConnection connect( final URL url )
    throws IOException, ArithmeticException {
    // Both HTTP and HTTPS are covered by this condition.
    if( url.openConnection() instanceof HttpURLConnection conn ) {
      conn.setUseCaches( false );
      conn.setInstanceFollowRedirects( true );
      conn.setRequestProperty( "Accept-Encoding", "gzip" );
      conn.setRequestProperty( "User-Agent", getProperty( "http.agent" ) );
      conn.setRequestMethod( "GET" );
      conn.setConnectTimeout( toIntExact( TIMEOUT.toMillis() ) );
      conn.setRequestProperty( "connection", "close" );
      conn.connect();

      final var code = conn.getResponseCode();

      if( code != HTTP_OK ) {
        final var message = format(
          "%s [HTTP %d: %s]",
          url.getFile(),
          code,
          conn.getResponseMessage()
        );

        throw new IOException( message );
      }

      return conn;
    }

    throw new UnsupportedOperationException( url.toString() );
  }

  /**
   * Returns a stream in an open state. Callers are responsible for closing.
   *
   * @param conn The connection to open, which could be compressed.
   * @return The open stream.
   * @throws IOException Could not open the stream.
   */
  private static BufferedInputStream open( final HttpURLConnection conn )
    throws IOException {
    return open( conn.getContentEncoding(), conn.getInputStream() );
  }

  /**
   * Returns a stream in an open state. Callers are responsible for closing.
   * The input stream may be compressed.
   *
   * @param encoding The content encoding for the stream.
   * @param is       The stream to wrap with a suitable decoder.
   * @return The open stream, with any gzip content-encoding decoded.
   * @throws IOException Could not open the stream.
   */
  private static BufferedInputStream open(
    final String encoding, final InputStream is ) throws IOException {
    return new BufferedInputStream(
      "gzip".equalsIgnoreCase( encoding )
        ? new GZIPInputStream( is )
        : is
    );
  }
}
