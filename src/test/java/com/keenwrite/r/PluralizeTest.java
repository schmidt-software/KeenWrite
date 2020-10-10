package com.keenwrite.r;

import com.keenwrite.util.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PluralizeTest {
  private static final ScriptEngine ENGINE =
      (new ScriptEngineManager()).getEngineByName( "Renjin" );

  private static final Map<Pair<String, Integer>, String> TESTS = Map.of(
      new Pair<>( "wolf", 2 ), "wolves"
  );

  @BeforeAll
  static void setup() throws ScriptException {
    r( "setwd( 'R' );" );
    r( "source( 'pluralize.R' );" );
  }

  @Test
  public void test_Pluralize_SingularForms_PluralForms()
      throws ScriptException {
    for( final var entry : TESTS.keySet() ) {
      final var expected = TESTS.get( entry );
      final var actual = pluralize( entry.getKey(), entry.getValue() );

      assertEquals( expected, actual );
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
