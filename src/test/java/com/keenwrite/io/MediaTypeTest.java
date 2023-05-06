/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.keenwrite.io.MediaType.*;
import static com.keenwrite.io.downloads.DownloadManager.open;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test that {@link MediaType} instances can be queried and return reliable
 * results.
 */
public class MediaTypeTest {
  /**
   * Test that {@link MediaType#equals(String, String)} is case-insensitive.
   */
  @Test
  public void test_Equality_IgnoreCase_Success() {
    final var mediaType = TEXT_PLAIN;
    assertTrue( mediaType.equals( "TeXt", "Plain" ) );
    assertEquals( "text/plain", mediaType.toString() );
  }

  /**
   * Test that {@link MediaType#fromFilename(String)} can lookup by file name.
   */
  @Test
  public void test_FilenameExtensions_Supported_Success() {
    final var map = Map.of(
      "jpeg", IMAGE_JPEG,
      "png", IMAGE_PNG,
      "svg", IMAGE_SVG_XML,
      "md", TEXT_MARKDOWN,
      "Rmd", TEXT_R_MARKDOWN,
      "txt", TEXT_PLAIN,
      "yml", TEXT_YAML
    );

    map.forEach( ( k, v ) -> assertEquals( v, fromFilename( "f." + k ) ) );
  }

  /**
   * Test that remote fetches will pull and identify the type of resource
   * based on the HTTP Content-Type header (or shallow decoding).
   */
  @Test
  public void test_HttpRequest_Supported_Success() {
    //@formatter:off
    final var map = Map.of(
       "https://kroki.io/robots.txt", TEXT_PLAIN,
       "https://place-hold.it/300x500", IMAGE_GIF,
       "https://placekitten.com/g/200/300", IMAGE_JPEG,
       "https://upload.wikimedia.org/wikipedia/commons/9/9f/Vimlogo.svg", IMAGE_SVG_XML,
       "https://kroki.io//graphviz/svg/eNpLyUwvSizIUHBXqPZIzcnJ17ULzy_KSanlAgB1EAjQ", IMAGE_SVG_XML
    );
    //@formatter:on

    map.forEach( ( k, v ) -> {
      try( var response = open( k ) ) {
        assertEquals( v, response.getMediaType() );
      } catch( final Exception e ) {
        throw new RuntimeException( e );
      }
    } );
  }
}
