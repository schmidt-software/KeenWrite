package com.keenwrite.quotes;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class EnglishParserTest {

  @Test
  public void testParserForSyntaxErrors() throws IOException {
    try( final var reader = openResource( "smartypants.txt" ) ) {
      String line;

      while( ((line = reader.readLine()) != null) ) {
        if (line.trim().isBlank() || line.trim().startsWith("#")) {
          continue;
        }

        // Discard the expected line
        reader.readLine();

        try {
          parser(line).document();
        }
        catch (Exception e) {
          fail("Could not parse: " + line);
        }
      }
    }
  }

  @SuppressWarnings( "SameParameterValue" )
  public static BufferedReader openResource(final String filename ) {
    final var is = EnglishParserTest.class.getResourceAsStream( filename );
    assertNotNull( is );

    return new BufferedReader( new InputStreamReader( is ) );
  }

  public static EnglishParser parser(String text) {
    EnglishLexer lexer = new EnglishLexer(CharStreams.fromString(text));
    EnglishParser parser = new EnglishParser(new CommonTokenStream(lexer));

    // Remove error listeners that possibly try to recover from syntax errors
    parser.removeErrorListeners();

    // On any syntax error, we'll let an exception be thrown by using BailErrorStrategy
    parser.setErrorHandler(new BailErrorStrategy());

    return parser;
  }
}
