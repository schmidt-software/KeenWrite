/*
 * Copyright 2020 White Magic Software, Ltd.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.scrivenvar.sigils;

import com.scrivenvar.Services;
import com.scrivenvar.preferences.UserPreferences;
import com.scrivenvar.service.Options;

/**
 * Brackets variable names between {@link #PREFIX} and {@link #SUFFIX} sigils.
 */
public class RSigilOperator implements SigilOperator {
  private static final Options sOptions = Services.load( Options.class );

  public static final String PREFIX = "`r#";
  public static final char SUFFIX = '`';

  private final String mDelimiterBegan =
      getUserPreferences().getRDelimiterBegan();
  private final String mDelimiterEnded =
      getUserPreferences().getRDelimiterEnded();

  /**
   * Returns the given string R-escaping backticks prepended and appended. This
   * is not null safe. Do not pass null into this method.
   *
   * @param variableName The string to decorate.
   * @return "`r#" + delimiterBegan + variableName+ delimiterEnded + "`".
   */
  @Override
  public String apply( String variableName ) {
    assert variableName != null;

    // Delete the $ $ sigils from Markdown variables.
    if( variableName.length() > 1 ) {
      variableName = variableName.substring( 1, variableName.length() - 1 );
    }

    return PREFIX
        + mDelimiterBegan
        + "v$"
        + variableName.replace( '.', '$' )
        + mDelimiterEnded
        + SUFFIX;
  }

  private UserPreferences getUserPreferences() {
    return sOptions.getUserPreferences();
  }
}
