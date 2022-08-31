/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.r;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test that English pluralization rules produce expected values.
 */
public class PluralizeTest {
  private static final ScriptEngine ENGINE =
      new ScriptEngineManager().getEngineByName( "Renjin" );

  private static final Map<String, String> PLURAL_MAP = ofEntries(
      entry( "beef", "beefs" ),
      entry( "brother", "brothers" ),
      entry( "child", "children" ),
      entry( "cow", "cows" ),
      entry( "ephemeris", "ephemerides" ),
      entry( "genie", "genies" ),
      entry( "money", "moneys" ),
      entry( "mongoose", "mongooses" ),
      entry( "mythos", "mythoi" ),
      entry( "octopus", "octopuses" ),
      entry( "ox", "oxen" ),
      entry( "soliloquy", "soliloquies" ),
      entry( "trilby", "trilbys" ),
      entry( "wolf", "wolves" )
  );

  @BeforeAll
  static void setup() throws ScriptException {
    r( "setwd( 'R' );" );
    r( "source( 'pluralize.R' );" );
  }

  @Test
  @SuppressWarnings("UnnecessaryLocalVariable")
  public void test_Pluralize_SingularForms_PluralForms()
      throws ScriptException {
    for( final var key : PLURAL_MAP.keySet() ) {
      final var expectedSingular = key;
      final var expectedPlural = PLURAL_MAP.get( key );
      final var actualSingular = pluralize( key, 1 );
      final var actualPlural = pluralize( key, 2 );

      assertEquals( expectedSingular, actualSingular );
      assertEquals( expectedPlural, actualPlural );
    }
  }

  private String pluralize( final String word, final int count )
      throws ScriptException {
    return r( format( "pluralize( '%s', %d );", word, count ) ).toString();
  }

  private static Object r( final String code ) throws ScriptException {
    return ENGINE.eval( code );
  }
}
