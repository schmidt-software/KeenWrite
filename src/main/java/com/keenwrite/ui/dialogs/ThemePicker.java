/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.dialogs;

import com.keenwrite.util.FileWalker;
import com.keenwrite.util.ResourceWalker;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;
import java.util.TreeMap;

import static com.keenwrite.Messages.get;
import static com.keenwrite.constants.Constants.THEME_NAME_LENGTH;
import static com.keenwrite.constants.GraphicsConstants.ICON_DIALOG;
import static com.keenwrite.constants.GraphicsConstants.ICON_DIALOG_NODE;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.util.FileWalker.walk;
import static java.lang.Math.max;
import static org.codehaus.plexus.util.StringUtils.abbreviate;

/**
 * Responsible for allowing the user to pick from the available themes found
 * in the system.
 */
public class ThemePicker extends ChoiceDialog<String> {
  private final File mThemes;
  private final StringProperty mTheme;

  /**
   * Construction must use static method to allow caching themes in the
   * future, if needed.
   *
   * @see #choose(File, StringProperty)
   */
  @SuppressWarnings( "rawtypes" )
  private ThemePicker( final File themes, final StringProperty theme ) {
    assert themes != null;
    assert theme != null;

    mThemes = themes;
    mTheme = theme;
    initIcon();
    setTitle( get( "Dialog.theme.title" ) );
    setHeaderText( get( "Dialog.theme.header" ) );

    final var options = (ComboBox) getDialogPane().lookup( ".combo-box" );
    options.setOnKeyPressed( ( event ) -> {
      // When the user presses the down arrow, open the drop-down. This prevents
      // navigating to the cancel button.
      if( event.getCode() == KeyCode.DOWN && !options.isShowing() ) {
        options.show();
        event.consume();
      }
    } );
  }

  private void initIcon() {
    setGraphic( ICON_DIALOG_NODE );
    setStageGraphic( ICON_DIALOG );
  }

  @SuppressWarnings( "SameParameterValue" )
  private void setStageGraphic( final Image icon ) {
    if( getDialogPane().getScene().getWindow() instanceof final Stage stage ) {
      stage.getIcons().add( icon );
    }
  }

  /**
   * Prompts a user to select a theme, answering {@code false} if no theme
   * was selected. The themes must be on the native file system; using the
   * {@link FileWalker} is a little more optimal than {@link ResourceWalker}.
   *
   * @param themes Theme directory root.
   * @param theme  Selected theme property name.
   * @return {@code true} if the user accepted or selected a theme.
   */
  public static boolean choose(
    final File themes, final StringProperty theme ) {
    assert themes != null;
    assert theme != null;

    return new ThemePicker( themes, theme ).pick();
  }

  /**
   * @return {@code true} if the user accepted or selected a theme.
   * @see #choose(File, StringProperty)
   */
  private boolean pick() {
    try {
      // List themes in alphabetical order (human readable by directory name).
      final var choices = new TreeMap<String, String>();
      final String[] selection = new String[]{""};

      // Populate the choices with themes detected on the system.
      walk( mThemes.toPath(), "**/theme.properties", ( path ) -> {
        try {
          final var displayed = readThemeName( path );
          final var themeName = path.getParent().toFile().getName();
          choices.put( abbreviate( displayed, THEME_NAME_LENGTH ), themeName );

          // Set the selected item to user's settings value.
          if( themeName.equals( mTheme.get() ) ) {
            selection[ 0 ] = displayed;
          }
        } catch( final Exception ex ) {
          clue( "Main.status.error.theme.name", path );
        }
      } );

      final var items = getItems();
      items.addAll( choices.keySet() );
      setSelectedItem( items.get( max( items.indexOf( selection[ 0 ] ), 0 ) ) );

      final var result = showAndWait();

      if( result.isPresent() ) {
        mTheme.set( choices.get( result.get() ) );
        return true;
      }
    } catch( final Exception ex ) {
      clue( get( "Main.status.error.theme.missing", mThemes ), ex );
    }

    return false;
  }

  /**
   * Returns the theme's human-friendly name from a file conforming to
   * {@link Properties}.
   *
   * @param file A fully qualified file name readable using {@link Properties}.
   * @return The human-friendly theme name.
   * @throws IOException          The {@link Properties} file cannot be read.
   * @throws NullPointerException The name field is not defined.
   */
  private String readThemeName( final Path file ) throws Exception {
    return read( file ).get( "name" ).toString();
  }

  private Properties read( final Path file ) throws IOException {
    final var properties = new Properties();

    try( final var in = new InputStreamReader(
      new FileInputStream( file.toFile() ), StandardCharsets.UTF_8 ) ) {
      properties.load( in );
    }

    return properties;
  }
}
