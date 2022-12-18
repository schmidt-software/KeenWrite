/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

import com.keenwrite.io.downloads.DownloadManager;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.downloads.DownloadManager.DownloadToken;

/**
 * Responsible for making HTTP requests, a thin wrapper around the
 * {@link URLConnection} class. This will attempt to use compression.
 */
public class HttpFacade {

  public static Response httpGet( final String url ) throws IOException {
    return httpGet( url, MediaType.UNDEFINED );
  }

  /**
   * Sends an HTTP GET request to a server.
   *
   * @param url The remote resource to fetch.
   * @return The server response.
   */
  public static Response httpGet( final URL url, final MediaType mediaType )
    throws IOException {
    return new Response( url, mediaType );
  }

  /**
   * Convenience method to send an HTTP GET request to a server.
   *
   * @param uri The remote resource to fetch.
   * @return The server response.
   * @see #httpGet(URL, MediaType)
   */
  public static Response httpGet( final URI uri, final MediaType mediaType )
    throws IOException {
    return httpGet( uri.toURL(), mediaType );
  }

  /**
   * Convenience method to send an HTTP GET request to a server.
   *
   * @param url The remote resource to fetch.
   * @return The server response.
   * @see #httpGet(URL, MediaType)
   */
  public static Response httpGet( final String url, final MediaType mediaType )
    throws IOException {
    return httpGet( new URL( url ), mediaType );
  }

  /**
   * Callers are responsible for closing the response.
   */
  public static final class Response implements Closeable {
    private final DownloadToken mDownloadToken;

    private Response( final URL url, final MediaType mediaType )
      throws IOException {
      assert url != null;

      clue( "Main.status.image.request.init" );

      mDownloadToken = DownloadManager.open( url, mediaType );
    }

    @Override
    public void close() throws IOException {
      mDownloadToken.close();
    }

    public BufferedInputStream getInputStream() {
      return mDownloadToken.getInputStream();
    }

    public boolean isMediaType( final MediaType mediaType ) {
      return mDownloadToken.isMediaType( mediaType );
    }

    public MediaType getMediaType() {
      return mDownloadToken.getMediaType();
    }
  }
}
