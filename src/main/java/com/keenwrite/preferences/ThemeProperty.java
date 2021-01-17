/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

import com.keenwrite.Constants;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.keenwrite.Constants.THEME_DEFAULT;
import static com.keenwrite.preferences.Workspace.listProperty;

/**
 * Responsible for providing a list of themes from which the user may pick.
 */
public final class ThemeProperty extends SimpleObjectProperty<String> {
  /**
   * Ordered set of available themes.
   */
  private static final Set<String> sThemes = new LinkedHashSet<>();

  static {
    sThemes.add( "Count Darcula" );
    sThemes.add( "Haunted Grey" );
    sThemes.add( "Modena Dark" );
    sThemes.add( THEME_DEFAULT );
    sThemes.add( "Silver Cavern" );
    sThemes.add( "Solarized Dark" );
    sThemes.add( "Vampire Byte" );
  }

  public ThemeProperty( final String themeName ) {
    super( themeName );
  }

  public static ObservableList<String> themeListProperty() {
    return listProperty( sThemes );
  }

  /**
   * Returns the given theme name as a sanitized file name, which must map
   * to a stylesheet file bundled with the application. This does not include
   * the path to the stylesheet. If the given theme name cannot be found in
   * the known theme list, the file name for {@link Constants#THEME_DEFAULT}
   * is returned. The extension must be added separately.
   *
   * @param theme The name to convert to a file name.
   * @return The given theme name converted lower case, spaces replaced with
   * underscores, without the ".css" extension appended.
   */
  public static String toFilename( final String theme ) {
    return sanitize( theme ).toLowerCase().replace( ' ', '_' );
  }

  /**
   * Ensures that the given theme name is in the list of known themes.
   *
   * @param theme Validate this theme name's existence.
   * @return The given theme name, if valid, otherwise the default theme name.
   */
  private static String sanitize( final String theme ) {
    return sThemes.contains( theme ) ? theme : THEME_DEFAULT;
  }
}
