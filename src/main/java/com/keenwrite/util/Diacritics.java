/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.util;

import static java.text.Normalizer.Form.NFD;
import static java.text.Normalizer.normalize;

/**
 * Responsible for modifying diacritics.
 */
public class Diacritics {
  private static final String UNCRITIC = "\\p{M}+";

  /**
   * Returns the value of the string without diacritic marks.
   *
   * @param text The text to normalize.
   * @return A non-null, possibly empty string.
   */
  public static String remove( final String text ) {
    return normalize( text, NFD ).replaceAll( UNCRITIC, "" );
  }
}
