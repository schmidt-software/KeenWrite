package com.keenwrite.io;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import static com.keenwrite.events.StatusEvent.clue;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.setFollowRedirects;

/**
 * Responsible for making HTTP requests, a thin wrapper around the
 * {@link URLConnection} class. This will attempt to use compression.
 * <p>
 * This class must be used within a try-with-resources block to ensure all
 * resources are released, even if only calling {@link Response#getMediaType()}.
 * </p>
 */
public class HttpFacade {
  static {
    setProperty( "http.keepAlive", "false" );
    setFollowRedirects( true );
  }

  /**
   * Sends an HTTP GET request to a server.
   *
   * @param url The remote resource to fetch.
   * @return The server response.
   */
  public static Response httpGet( final URL url ) throws Exception {
    return new Response( url );
  }

  /**
   * Convenience method to send an HTTP GET request to a server.
   *
   * @param uri The remote resource to fetch.
   * @return The server response.
   * @see #httpGet(URL)
   */
  public static Response httpGet( final URI uri ) throws Exception {
    return httpGet( uri.toURL() );
  }

  /**
   * Convenience method to send an HTTP GET request to a server.
   *
   * @param url The remote resource to fetch.
   * @return The server response.
   * @see #httpGet(URL)
   */
  public static Response httpGet( final String url ) throws Exception {
    return httpGet( new URL( url ) );
  }

  /**
   * Callers are responsible for closing the response.
   */
  public static final class Response implements Closeable {
    private final HttpURLConnection mConn;
    private final BufferedInputStream mStream;

    private Response( final URL url ) throws IOException {
      assert url != null;

      clue( "Main.status.image.request.init" );

      if( url.openConnection() instanceof HttpURLConnection conn ) {
        conn.setUseCaches( false );
        conn.setInstanceFollowRedirects( true );
        conn.setRequestProperty( "Accept-Encoding", "gzip" );
        conn.setRequestProperty( "User-Agent", getProperty( "http.agent" ) );
        conn.setRequestMethod( "GET" );
        conn.setConnectTimeout( 30000 );
        conn.setRequestProperty( "connection", "close" );
        conn.connect();

        clue( "Main.status.image.request.fetch", url.getHost() );

        final var code = conn.getResponseCode();

        // Even though there are other "okay" error codes, tell the user when
        // a resource has changed in any unexpected way.
        if( code != HTTP_OK ) {
          throw new IOException( url + " [HTTP " + code + "]" );
        }

        mConn = conn;
        mStream = openBufferedInputStream();
      }
      else {
        throw new UnsupportedOperationException( url.toString() );
      }
    }

    /**
     * Returns the {@link MediaType} based on the resulting HTTP content type
     * provided by the server. If the content type from the server is not
     * found, this will probe the first several bytes to determine the type.
     *
     * @return The stream's IANA-defined {@link MediaType}.
     */
    public MediaType getMediaType() throws IOException {
      final var contentType = mConn.getContentType();
      var mediaType = MediaType.valueFrom( contentType );

      if( mediaType.isUndefined() ) {
        mediaType = MediaTypeSniffer.getMediaType( mStream );
      }

      clue( "Main.status.image.request.success", mediaType );
      return mediaType;
    }

    /**
     * Returns the stream opened using an HTTP connection, decompressing if
     * the server supports gzip compression. The caller must close the stream
     * by calling {@link #close()} on this object.
     *
     * @return The stream representing the content at the URL used to
     * construct the {@link HttpFacade}.
     */
    public InputStream getInputStream() throws IOException {
      return mStream;
    }

    /**
     * This will disconnect the HTTP request and close the associated stream.
     */
    @Override
    public void close() {
      mConn.disconnect();
    }

    /**
     * Opens the connection for reading. It is an error to call this more than
     * once. This may use gzip compression. A {@link BufferedInputStream} is
     * returned to allow peeking at the stream when checking the content
     * type.
     *
     * @return The {@link InputStream} containing content from an HTTP request.
     * @throws IOException Could not open the stream.
     */
    private BufferedInputStream openBufferedInputStream() throws IOException {
      final var encoding = mConn.getContentEncoding();
      final var is = mConn.getInputStream();

      return new BufferedInputStream(
        "gzip".equalsIgnoreCase( encoding ) ? new GZIPInputStream( is ) : is );
    }
  }
}
