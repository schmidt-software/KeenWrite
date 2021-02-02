/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

import okhttp3.OkHttpClient;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.MediaType.UNDEFINED;
import static okhttp3.Request.Builder;

/**
 * Responsible for determining {@link MediaType} based on the content-type from
 * an HTTP request.
 */
public final class HttpMediaType {

  private final static OkHttpClient HTTP_CLIENT = new OkHttpClient();

  /**
   * Performs an HTTP HEAD request to determine the media type based on the
   * Content-Type header returned from the server.
   *
   * @param uri Determine the media type for this resource.
   * @return The data type for the resource or {@link MediaType#UNDEFINED} if
   * unmapped.
   * @throws MalformedURLException The {@link URI} could not be converted to
   *                               a {@link URL}.
   */
  public static MediaType valueFrom( final URI uri )
    throws MalformedURLException {
    final var mediaType = new MediaType[]{UNDEFINED};

    try {
      clue( "Main.status.image.request.init" );
      final var request = new Builder().url( uri.toURL() ).head().build();

      clue( "Main.status.image.request.fetch", uri.getHost() );
      final var response = HTTP_CLIENT.newCall( request ).execute();
      final var headers = response.headers();
      var header = headers.get( "Content-Type" );

      if( header == null ) {
        clue( "Main.status.image.request.error.media", uri );
      }
      else {
        // Remove the character encoding.
        var i = header.indexOf( ';' );
        header = header.substring( 0, i == -1 ? header.length() : i );

        // Split the type and subtype.
        i = header.indexOf( '/' );
        i = i == -1 ? header.length() : i;
        final var type = header.substring( 0, i );
        final var subtype = header.substring( i + 1 );

        mediaType[ 0 ] = MediaType.valueFrom( type, subtype );
        clue( "Main.status.image.request.success", mediaType[ 0 ] );
      }
    } catch( final Exception ex ) {
      clue( ex );
    }

    return mediaType[ 0 ];
  }
}
