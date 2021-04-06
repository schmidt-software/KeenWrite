/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

import java.io.IOException;
import java.net.URI;

import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.HttpFacade.httpGet;
import static com.keenwrite.io.MediaType.UNDEFINED;

/**
 * Responsible for determining {@link MediaType} based on the content-type from
 * an HTTP request.
 */
public final class HttpMediaType {

  /**
   * Performs an HTTP request to determine the media type based on the
   * Content-Type header returned from the server.
   *
   * @param uri Determine the media type for this resource.
   * @return The data type for the resource or {@link MediaType#UNDEFINED} if
   * unmapped.
   * @throws IOException The {@link URI} could not be fetched.
   */
  public static MediaType valueFrom( final URI uri ) throws IOException {
    var mediaType = UNDEFINED;

    clue( "Main.status.image.request.init" );

    try( final var response = httpGet( uri ) ) {
      clue( "Main.status.image.request.fetch", uri.getHost() );
      mediaType = response.getMediaType();
      clue( "Main.status.image.request.success", mediaType );
    }

    return mediaType;
  }
}
