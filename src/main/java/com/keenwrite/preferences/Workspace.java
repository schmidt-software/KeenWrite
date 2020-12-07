/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

import com.keenwrite.Constants;
import com.keenwrite.io.File;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.io.FileHandler;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.keenwrite.Bootstrap.APP_TITLE_LOWERCASE;
import static com.keenwrite.Constants.FILE_PREFERENCES;
import static com.keenwrite.Launcher.getVersion;
import static com.keenwrite.StatusBarNotifier.clue;
import static java.lang.String.format;

/**
 * Responsible for defining behaviours for separate projects. A workspace has
 * the ability to save and restore a session, including the window dimensions,
 * tab setup, files, and user preferences.
 * <p>
 * The {@link Workspace} configuration must support hierarchical (nested)
 * configuration nodes to persist the user interface state. Although possible
 * with a flat configuration file, it's not nearly as simple or elegant.
 * </p>
 * <p>
 * Neither JSON nor HOCON support schema validation and versioning, which makes
 * XML the more suitable configuration file format. Schema validation and
 * versioning provide future-proofing and ease of reading and upgrading previous
 * versions of the configuration file.
 * </p>
 */
public final class Workspace {

  private static final String KEY_META = "workspace.meta.";
  private static final String KEY_UI_FILES_PATH = "workspace.ui.files.path";

  /**
   * Application configuration file used to persist both user preferences and
   * project settings. The user preferences include items such as locale
   * and font sizes while the project settings include items such as last
   * opened directory and window sizes. That is, user preferences can be
   * changed directly by the user through the preferences dialog; whereas,
   * project settings reflect application interactions.
   */
  private final XMLConfiguration mConfig;

  /**
   * User-defined name for this workspace.
   */
  private final String mProject;

  /**
   * Constructs a new workspace with the given identifier. This will attempt
   * to read the configuration file stored in the
   *
   * @param project The unique identifier for this workspace.
   */
  public Workspace( final String project ) {
    mConfig = load();
    mProject = project;
  }

  /**
   * Saves the current workspace.
   */
  public void save() {
    try {
      mConfig.setProperty( KEY_META + "version", getVersion() );
      mConfig.setProperty( KEY_META + "name", mProject );
      new FileHandler( mConfig ).save( FILE_PREFERENCES );
    } catch( final Exception ex ) {
      clue( ex );
    }
  }

  /**
   * Returns the list of files opened for this {@link Workspace}.
   *
   * @return A non-null, possibly empty list of {@link File} instances.
   */
  public List<File> getFiles() {
    final var items = getFileList();
    final var files = new HashSet<File>( items.size() );
    items.forEach( ( item ) -> {
      final var file = new File( item );

      if( file.exists() ) {
        files.add( file );
      }
    } );

    // Remove duplicate and missing files. The configuration is re-populated
    // on saving because the UI will re-open the files in the list that's
    // returned by this method. Re-opening adds the files back to the config.
    // This ensures that the list never grows beyond a reasonable number.
    mConfig.clearProperty( KEY_UI_FILES_PATH );

    return new ArrayList<>( files );
  }

  public void putFile( final File file ) {
    mConfig.addProperty( KEY_UI_FILES_PATH, file.getAbsolutePath() );
  }

  /**
   * Removes the given file from the workspace so that when the application
   * is restarted, the file will not be automatically loaded.
   *
   * @param file The file to remove from the list files opened for editing.
   */
  public void removeFile( final File file ) {
    final var items = getFileList();
    final var index = items.indexOf( file.getAbsolutePath() );

    if( index > 0 ) {
      mConfig.clearTree( format( "%s(%d)", KEY_UI_FILES_PATH, index ) );
    }
  }

  /**
   * Restores the caret position for the given path.
   *
   * @param path The path to a file that was opened previously.
   * @return The stored caret position or 0 if the path has no associated
   * caret position persisted.
   */
  public int restoreCaretPosition( final Path path ) {
    return 0;
  }

  /**
   * Updates the dictionary to include project-specific words.
   * <p>
   * TODO: Implementation
   */
  public void restoreDictionary() {
  }

  /**
   * Attempts to load the {@link Constants#FILE_PREFERENCES} configuration file.
   * If not found, this will fall back to an empty configuration file, leaving
   * the application to fill in default values.
   *
   * @return Configuration instance representing last known state of the
   * application's user preferences and project settings.
   */
  private XMLConfiguration load() {
    try {
      return new Configurations().xml( FILE_PREFERENCES );
    } catch( final Exception ex ) {
      clue( ex );

      final var config = new XMLConfiguration();

      // The root config key can only be set for an empty configuration file.
      config.setRootElementName( APP_TITLE_LOWERCASE );
      return config;
    }
  }

  private List<String> getFileList() {
    return mConfig.getList(
        String.class, KEY_UI_FILES_PATH, new ArrayList<>() );
  }
}
