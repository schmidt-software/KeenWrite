/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static com.keenwrite.Constants.DIAGRAM_SERVER_NAME;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.MediaType.UNDEFINED;
import static java.net.http.HttpClient.Redirect.NORMAL;
import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static java.net.http.HttpResponse.BodyHandlers.discarding;
import static java.time.Duration.ofSeconds;

/**
 * Responsible for determining {@link MediaType} based on the content-type from
 * an HTTP request.
 */
public final class HttpMediaType {

  private final static HttpClient HTTP_CLIENT = HttpClient
    .newBuilder()
    .connectTimeout( ofSeconds( 5 ) )
    .followRedirects( NORMAL )
    .sslContext( createSslContext() )
    .build();

  /**
   * Performs an HTTP HEAD request to determine the media type based on the
   * Content-Type header returned from the server.
   *
   * @param uri Determine the media type for this resource.
   * @return The data type for the resource or {@link MediaType#UNDEFINED} if
   * unmapped.
   * @throws MalformedURLException The {@link URI} could not be converted to
   *                               an instance of {@link URL}.
   */
  public static MediaType valueFrom( final URI uri )
    throws MalformedURLException {
    final var mediaType = new MediaType[]{UNDEFINED};

    try {
      clue( "Main.status.image.request.init" );
      final var request = HttpRequest
        .newBuilder( uri )
        .method( "HEAD", noBody() )
        .build();
      clue( "Main.status.image.request.fetch", uri.getHost() );
      final var response = HTTP_CLIENT.send( request, discarding() );
      final var headers = response.headers();
      final var map = headers.map();

      map.forEach( ( key, values ) -> {
        if( "content-type".equalsIgnoreCase( key ) ) {
          var header = values.get( 0 );
          // Trim off the character encoding.
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
      } );
    } catch( final Exception ex ) {
      clue( ex );
    }

    return mediaType[ 0 ];
  }

  private static SSLContext createSslContext() {
    try {
      final var context = SSLContext.getInstance( "TLS" );
      context.init(
        new KeyManager[ 0 ],
        new TrustManager[]{new DefaultTrustManager()},
        new SecureRandom() );
      SSLContext.setDefault( context );
      return context;
    } catch( final Exception ex ) {
      clue( ex );
      throw new RuntimeException( ex );
    }
  }

  private static class DefaultTrustManager implements X509TrustManager {

    @Override
    public void checkServerTrusted(
      final X509Certificate[] chain, final String authType )
      throws CertificateException {
      for( final var cert : chain ) {
        for( final var names : cert.getSubjectAlternativeNames() ) {
          for( final var name : names ) {
            if( DIAGRAM_SERVER_NAME.equalsIgnoreCase( name.toString() ) ) {
              return;
            }
          }
        }
      }

      clue( "Main.status.image.request.error.cert", DIAGRAM_SERVER_NAME );
      throw new CertificateException();
    }

    @Override
    public void checkClientTrusted(
      final X509Certificate[] chain, final String authType ) {}

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return null;
    }
  }
}
