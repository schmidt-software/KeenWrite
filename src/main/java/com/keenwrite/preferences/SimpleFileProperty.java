/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

import com.keenwrite.io.File;
import javafx.beans.property.SimpleObjectProperty;

public class SimpleFileProperty extends SimpleObjectProperty<File> {
  public SimpleFileProperty( final File file ) {
    super( file );
  }

  public void setValue( final String filename ) {
    setValue( new File( filename ) );
  }
}
