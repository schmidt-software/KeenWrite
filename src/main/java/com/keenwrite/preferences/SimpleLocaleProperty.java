/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

import javafx.beans.property.SimpleObjectProperty;

import java.util.Locale;

import static java.util.Locale.forLanguageTag;

public class SimpleLocaleProperty extends SimpleObjectProperty<Locale> {
  public SimpleLocaleProperty( final Locale locale ) {
    super( locale );
  }

  public void setValue( final String locale ) {
    setValue( forLanguageTag( locale ) );
  }
}
