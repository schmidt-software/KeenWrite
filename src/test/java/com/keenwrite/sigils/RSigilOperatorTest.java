/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.sigils;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Responsible for simulating R variable injection.
 */
class RSigilOperatorTest {

  private final SigilOperator mOperator = createRSigilOperator();

  /**
   * Test that a key name becomes an R variable.
   */
  @Test
  void test_Entoken_KeyName_Tokenized() {
    final var expected = "v$a$b$c$d";
    final var actual = mOperator.entoken( "{{a.b.c.d}}" );
    assertEquals( expected, actual );
  }

  /**
   * Test that a key name becomes a viable R expression.
   */
  @Test
  void test_Apply_KeyName_Expression() {
    final var expected = "`r#x(v$a$b$c$d)`";
    final var actual = mOperator.apply( "v$a$b$c$d" );
    assertEquals( expected, actual );
  }

  private StringProperty createToken( final String token ) {
    return new SimpleStringProperty( token );
  }

  private Tokens createRTokens() {
    return createTokens( "x(", ")" );
  }

  private Tokens createYamlTokens() {
    return createTokens( "{{", "}}" );
  }

  private Tokens createTokens( final String began, final String ended ) {
    return new Tokens( createToken( began ), createToken( ended ) );
  }

  private YamlSigilOperator createYamlSigilOperator() {
    return new YamlSigilOperator( createYamlTokens() );
  }

  private RSigilOperator createRSigilOperator() {
    return new RSigilOperator( createRTokens(), createYamlSigilOperator() );
  }
}
