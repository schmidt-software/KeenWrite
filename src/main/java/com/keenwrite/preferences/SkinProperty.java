/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

import com.keenwrite.constants.Constants;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.keenwrite.constants.Constants.SKIN_DEFAULT;
import static com.keenwrite.preferences.Workspace.listProperty;

/**
 * Maintains a list of look and feels that the user may choose.
 */
public final class SkinProperty extends SimpleObjectProperty<String> {
  /**
   * Ordered set of available skins.
   */
  private static final Set<String> sSkins = new LinkedHashSet<>();

  static {
    sSkins.add( "Count Darcula" );
    sSkins.add( "Haunted Grey" );
    sSkins.add( "Modena Dark" );
    sSkins.add( SKIN_DEFAULT );
    sSkins.add( "Silver Cavern" );
    sSkins.add( "Solarized Dark" );
    sSkins.add( "Vampire Byte" );
  }

  public SkinProperty( final String skin ) {
    super( skin );
  }

  public static ObservableList<String> skinListProperty() {
    return listProperty( sSkins );
  }

  /**
   * Returns the given skin name as a sanitized file name, which must map
   * to a stylesheet file bundled with the application. This does not include
   * the path to the stylesheet. If the given name is not known, the file
   * name for {@link Constants#SKIN_DEFAULT} is returned. The extension must
   * be added separately.
   *
   * @param skin The name to convert to a file name.
   * @return The given name converted lower case, spaces replaced with
   * underscores, without the ".css" extension appended.
   */
  public static String toFilename( final String skin ) {
    return sanitize( skin ).toLowerCase().replace( ' ', '_' );
  }

  /**
   * Ensures that the given name is in the list of known skins.
   *
   * @param skin Validate this name's existence.
   * @return The given name, if valid, otherwise the default skin.
   */
  private static String sanitize( final String skin ) {
    return sSkins.contains( skin ) ? skin : SKIN_DEFAULT;
  }
}
