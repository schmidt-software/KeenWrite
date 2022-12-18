package com.keenwrite.io.downloads;

import com.keenwrite.io.MediaType;

import java.io.IOException;
import java.net.URL;

/**
 * Indicates that the requested media type for a resource was incongruent
 * with that requested for download.
 */
public class InvalidMediaTypeException extends IOException {
  private final URL mUrl;
  private final MediaType mMediaType;

  /**
   * Creates a new exception that indicates the resource and its media type
   * were incongruent.
   *
   * @param url  The resource that was requested to be downloaded.
   * @param type The required media type to be downloaded.
   */
  public InvalidMediaTypeException( final URL url, final MediaType type ) {
    super( url.toString() );

    mUrl = url;
    mMediaType = type;
  }

  public URL getUrl() {
    return mUrl;
  }

  public MediaType getMediaType() {
    return mMediaType;
  }
}
