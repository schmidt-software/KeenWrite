/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

import com.keenwrite.io.File;
import javafx.application.Platform;
import javafx.beans.property.*;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static com.keenwrite.Constants.*;
import static com.keenwrite.Launcher.getVersion;
import static com.keenwrite.preferences.Key.key;
import static java.util.Map.entry;
import static javafx.application.Platform.runLater;

/**
 * Represents persistent preferences set both directly and indirectly.
 * <p>
 * Note the following definitions:
 * </p>
 * <dl>
 *   <dt>File</dt>
 *   <dd>References a filename (no path), path, or directory.</dd>
 *   <dt>Path</dt>
 *   <dd>Fully qualified filename, which includes all parent directories.</dd>
 *   <dt>Dir</dt>
 *   <dd>Directory without a filename ({@link File#isDirectory()} is true).</dd>
 * </dl>
 */
public class WorkspacePreferences {
  private static final Key KEY_ROOT = key( "workspace" );

  public static final Key KEY_META = key( KEY_ROOT, "meta" );
  public static final Key KEY_META_NAME = key( KEY_META, "name" );
  public static final Key KEY_META_VERSION = key( KEY_META, "version" );

  public static final Key KEY_R = key( KEY_ROOT, "r" );
  public static final Key KEY_R_SCRIPT = key( KEY_R, "script" );
  public static final Key KEY_R_DIR = key( KEY_R, "dir" );
  public static final Key KEY_R_DELIM = key( KEY_R, "delimiter" );
  public static final Key KEY_R_DELIM_BEGAN = key( KEY_R_DELIM, "began" );
  public static final Key KEY_R_DELIM_ENDED = key( KEY_R_DELIM, "ended" );

  public static final Key KEY_IMAGES = key( KEY_ROOT, "images" );
  public static final Key KEY_IMAGES_DIR = key( KEY_IMAGES, "dir" );
  public static final Key KEY_IMAGES_ORDER = key( KEY_IMAGES, "order" );

  public static final Key KEY_DEF = key( KEY_ROOT, "definition" );
  public static final Key KEY_DEF_PATH = key( KEY_DEF, "path" );
  public static final Key KEY_DEF_DELIM = key( KEY_DEF, "delimiter" );
  public static final Key KEY_DEF_DELIM_BEGAN = key( KEY_DEF_DELIM, "began" );
  public static final Key KEY_DEF_DELIM_ENDED = key( KEY_DEF_DELIM, "ended" );

  //@formatter:off
  public static final Key KEY_UI = key( KEY_ROOT, "ui" );

  public static final Key KEY_UI_RECENT = key( KEY_UI, "recent" );
  public static final Key KEY_UI_RECENT_DIR = key( KEY_UI_RECENT, "dir" );
  public static final Key KEY_UI_RECENT_DOCUMENT = key( KEY_UI_RECENT,"document" );
  public static final Key KEY_UI_RECENT_DEFINITION = key( KEY_UI_RECENT, "definition" );

  public static final Key KEY_UI_FILES = key( KEY_UI, "files" );
  public static final Key KEY_UI_FILES_PATH = key( KEY_UI_FILES, "path" );

  public static final Key KEY_UI_FONT = key( KEY_UI, "font" );
  public static final Key KEY_UI_FONT_LOCALE = key( KEY_UI_FONT, "locale" );
  public static final Key KEY_UI_FONT_EDITOR = key( KEY_UI_FONT, "editor" );
  public static final Key KEY_UI_FONT_EDITOR_SIZE = key( KEY_UI_FONT_EDITOR, "size" );
  public static final Key KEY_UI_FONT_PREVIEW = key( KEY_UI_FONT, "preview" );
  public static final Key KEY_UI_FONT_PREVIEW_SIZE = key( KEY_UI_FONT_PREVIEW, "size" );

  public static final Key KEY_UI_WINDOW = key( KEY_UI, "window" );
  public static final Key KEY_UI_WINDOW_X = key( KEY_UI_WINDOW, "x" );
  public static final Key KEY_UI_WINDOW_Y = key( KEY_UI_WINDOW, "y" );
  public static final Key KEY_UI_WINDOW_W = key( KEY_UI_WINDOW, "width" );
  public static final Key KEY_UI_WINDOW_H = key( KEY_UI_WINDOW, "height" );
  public static final Key KEY_UI_WINDOW_MAX = key( KEY_UI_WINDOW, "maximized" );
  public static final Key KEY_UI_WINDOW_FULL = key( KEY_UI_WINDOW, "full" );

  private final Map<Key, Property<?>> VALUES = Map.ofEntries(
    entry( KEY_META_VERSION, new SimpleStringProperty( getVersion() ) ),
    entry( KEY_META_NAME, new SimpleStringProperty( "defaullt" ) ),
    
    entry( KEY_R_SCRIPT, new SimpleStringProperty( "" ) ),
    entry( KEY_R_DIR, new SimpleFileProperty( USER_DIRECTORY ) ),
    entry( KEY_R_DELIM_BEGAN, new SimpleStringProperty( R_DELIM_BEGAN_DEFAULT ) ),
    entry( KEY_R_DELIM_ENDED, new SimpleStringProperty( R_DELIM_ENDED_DEFAULT ) ),
    
    entry( KEY_IMAGES_DIR, new SimpleFileProperty( USER_DIRECTORY ) ),
    entry( KEY_IMAGES_ORDER, new SimpleStringProperty( PERSIST_IMAGES_DEFAULT ) ),
    
    entry( KEY_DEF_PATH, new SimpleFileProperty( DEFINITION_DEFAULT ) ),
    entry( KEY_DEF_DELIM_BEGAN, new SimpleStringProperty( DEF_DELIM_BEGAN_DEFAULT ) ),
    entry( KEY_DEF_DELIM_ENDED, new SimpleStringProperty( DEF_DELIM_ENDED_DEFAULT ) ),
    
    entry( KEY_UI_RECENT_DIR, new SimpleFileProperty( USER_DIRECTORY ) ),
    entry( KEY_UI_RECENT_DOCUMENT, new SimpleFileProperty( DOCUMENT_DEFAULT ) ),
    entry( KEY_UI_RECENT_DEFINITION, new SimpleFileProperty( DEFINITION_DEFAULT ) ),
    
    entry( KEY_UI_FONT_LOCALE, new SimpleLocaleProperty( LOCALE_DEFAULT ) ),
    entry( KEY_UI_FONT_EDITOR_SIZE, new SimpleFloatProperty( FONT_SIZE_EDITOR_DEFAULT ) ),
    entry( KEY_UI_FONT_PREVIEW_SIZE, new SimpleFloatProperty( FONT_SIZE_PREVIEW_DEFAULT ) ),
    
    entry( KEY_UI_WINDOW_X, new SimpleDoubleProperty( WINDOW_X_DEFAULT ) ),
    entry( KEY_UI_WINDOW_Y, new SimpleDoubleProperty( WINDOW_Y_DEFAULT ) ),
    entry( KEY_UI_WINDOW_W, new SimpleDoubleProperty( WINDOW_W_DEFAULT ) ),
    entry( KEY_UI_WINDOW_H, new SimpleDoubleProperty( WINDOW_H_DEFAULT ) ),
    entry( KEY_UI_WINDOW_MAX, new SimpleBooleanProperty() ),
    entry( KEY_UI_WINDOW_FULL, new SimpleBooleanProperty() )
  );
  //@formatter:on

  private final Map<Key, SetProperty<?>> SETS = Map.ofEntries(
    entry( KEY_UI_FILES_PATH, new SimpleSetProperty<>() )
  );

  public WorkspacePreferences() {
  }

  /**
   * Returns a value that represents a setting in the application that the user
   * may configure, either directly or indirectly.
   *
   * @param key The reference to the users' preference stored in deference
   *            of app reëntrance.
   * @return An observable property to be persisted.
   */
  @SuppressWarnings("unchecked")
  public <T> Property<T> valuesProperty( final Key key ) {
    // The type that goes into the map must come out.
    return (Property<T>) VALUES.get( key );
  }

  /**
   * Returns a list of values that represent a setting in the application that
   * the user may configure, either directly or indirectly.
   *
   * @param key The {@link Key} associated with a preference value.
   * @return An observable property to be persisted.
   */
  @SuppressWarnings("unchecked")
  public <T> SetProperty<T> setsProperty( final Key key ) {
    // The type that goes into the map must come out.
    return (SetProperty<T>) SETS.get( key );
  }

  /**
   * Returns the {@link Double} preference value associated with the given
   * {@link Key}. The caller must be sure that the given {@link Key} is
   * associated with a value that matches the return type.
   *
   * @param key The {@link Key} associated with a preference value.
   * @return The value associated with the given {@link Key}.
   */
  public double toDouble( final Key key ) {
    return (double) valuesProperty( key ).getValue();
  }

  /**
   * Returns the {@link Boolean} preference value associated with the given
   * {@link Key}. The caller must be sure that the given {@link Key} is
   * associated with a value that matches the return type.
   *
   * @param key The {@link Key} associated with a preference value.
   * @return The value associated with the given {@link Key}.
   */
  public boolean toBoolean( final Key key ) {
    return (boolean) valuesProperty( key ).getValue();
  }

  /**
   * Returns the {@link File} {@link Property} associated with the given
   * {@link Key} from the internal list of preference values. The caller
   * must be sure that the given {@link Key} is associated with a {@link File}
   * {@link Property}.
   *
   * @param key The {@link Key} associated with a preference value.
   * @return The value associated with the given {@link Key}.
   */
  public Property<File> fileProperty( final Key key ) {
    return valuesProperty( key );
  }

  /**
   * Calls the given consumer for all single-value keys. For lists, see
   * {@link #consumeSets(BiConsumer)}.
   *
   * @param consumer Called to accept each preference key value.
   */
  public void consumeValues( final BiConsumer<Key, Property<?>> consumer ) {
    VALUES.forEach( consumer );
  }

  /**
   * Calls the given consumer for all multi-value keys. For single items, see
   * {@link #consumeValues(BiConsumer)}. Callers are responsible for iterating
   * over the list of items retrieved through this method.
   *
   * @param consumer Called to accept each preference key list.
   */
  public void consumeSets( final BiConsumer<Key, SetProperty<?>> consumer ) {
    SETS.forEach( consumer );
  }

  public void consumeValueKeys( final Consumer<Key> consumer ) {
    VALUES.keySet().forEach( consumer );
  }

  public void consumeSetKeys( final Consumer<Key> consumer ) {
    SETS.keySet().forEach( consumer );
  }

  /**
   * Delegates to {@link #listen(Key, ReadOnlyProperty, BooleanSupplier)},
   * providing a value of {@code true} for the {@link BooleanSupplier} to
   * indicate the property changes always take effect.
   *
   * @param key      The value to bind to the internal key property.
   * @param property The external property value that sets the internal value.
   */
  public <T> void listen( final Key key, final ReadOnlyProperty<T> property ) {
    listen( key, property, () -> true );
  }

  /**
   * Binds a read-only property to a value in the preferences. This allows
   * user interface properties to change and the preferences will be
   * synchronized automatically.
   * <p>
   * This calls {@link Platform#runLater(Runnable)} to ensure that all pending
   * application window states are finished before assessing whether property
   * changes should be applied. Without this, exiting the application while the
   * window is maximized would persist the window's maximum dimensions,
   * preventing restoration to its prior, non-maximum size.
   * </p>
   *
   * @param key      The value to bind to the internal key property.
   * @param property The external property value that sets the internal value.
   * @param enabled  Indicates whether property changes should be applied.
   */
  public <T> void listen(
    final Key key,
    final ReadOnlyProperty<T> property,
    final BooleanSupplier enabled ) {
    property.addListener(
      ( c, o, n ) ->
        runLater( () -> {
          if( enabled.getAsBoolean() ) {
            valuesProperty( key ).setValue( n );
          }
        } )
    );
  }
}
