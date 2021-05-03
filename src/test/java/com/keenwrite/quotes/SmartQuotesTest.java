/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test that English straight quotes are converted to curly quotes and
 * apostrophes.
 */
public class SmartQuotesTest {
  @Disabled
  @SuppressWarnings( "unused" )
  public void test_Parse_StraightQuotes_CurlyQuotes() throws IOException {
    final var fixer = new SmartQuotes();

    try( final var reader = openResource( "smartypants.txt" ) ) {
      String line;
      String testLine = "";
      String expected = "";

      while( ((line = reader.readLine()) != null) ) {
        if( line.startsWith( "#" ) || line.isBlank() ) { continue; }

        // Read the first line of the couplet.
        if( testLine.isBlank() ) {
          testLine = line;
          continue;
        }

        // Read the second line of the couplet.
        if( expected.isBlank() ) {
          expected = line;
        }

        final var actual = fixer.replace( testLine );
        assertEquals(expected, actual);

        testLine = "";
        expected = "";
      }
    }
  }

  @SuppressWarnings( "SameParameterValue" )
  private BufferedReader openResource( final String filename ) {
    final var is = getClass().getResourceAsStream( filename );
    assertNotNull( is );

    return new BufferedReader( new InputStreamReader( is ) );
  }
}
