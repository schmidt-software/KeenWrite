/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.security;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import static javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier;
import static javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory;

/**
 * Responsible for trusting all certificate chains. The purpose of this class
 * is to work-around certificate issues caused by software that blocks
 * HTTP requests. For example, Zscaler may block HTTP requests to kroki.io
 * when generating diagrams.
 */
public final class PermissiveCertificate {
  /**
   * Create a trust manager that does not validate certificate chains.
   */
  private static final TrustManager[] TRUST_ALL_CERTS = new TrustManager[]{
    new X509TrustManager() {
      @Override
      public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[ 0 ];
      }

      @Override
      public void checkClientTrusted(
        X509Certificate[] certs, String authType ) {
      }

      @Override
      public void checkServerTrusted(
        X509Certificate[] certs, String authType ) {
      }
    }
  };

  /**
   * Responsible for permitting all hostnames for making HTTP requests.
   */
  private static class PermissiveHostNameVerifier implements HostnameVerifier {
    @Override
    public boolean verify( final String hostname, final SSLSession session ) {
      return true;
    }
  }

  /**
   * Install the all-trusting trust manager. If this fails it means that in
   * certain situations the HTML preview may fail to render diagrams. A way
   * to work around the issue is to install a local server for generating
   * diagrams.
   */
  public static boolean installTrustManager() {
    try {
      final var context = SSLContext.getInstance( "SSL" );
      context.init( null, TRUST_ALL_CERTS, new SecureRandom() );
      setDefaultSSLSocketFactory( context.getSocketFactory() );
      setDefaultHostnameVerifier( new PermissiveHostNameVerifier() );
      return true;
    } catch( NoSuchAlgorithmException | KeyManagementException e ) {
      return false;
    }
  }

  /**
   * Use {@link #installTrustManager()}.
   */
  private PermissiveCertificate() {
  }
}
