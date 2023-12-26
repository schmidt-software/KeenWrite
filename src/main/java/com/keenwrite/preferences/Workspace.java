/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

import com.keenwrite.events.workspace.WorkspaceLoadedEvent;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.ObservableList;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import static com.keenwrite.Bootstrap.*;
import static com.keenwrite.Launcher.getVersion;
import static com.keenwrite.constants.Constants.*;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.preferences.AppKeys.*;
import static java.util.Map.entry;
import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.observableSet;

/**
 * Responsible for defining behaviours for separate projects. A workspace has
 * the ability to save and restore a session, including the window dimensions,
 * tab setup, files, and user preferences.
 * <p>
 * The configuration must support hierarchical (nested) configuration nodes
 * to persist the user interface state. Although possible with a flat
 * configuration file, it's not nearly as simple or elegant.
 * </p>
 * <p>
 * Neither JSON nor HOCON support schema validation and versioning, which makes
 * XML the more suitable configuration file format. Schema validation and
 * versioning provide future-proofing and ease of reading and upgrading previous
 * versions of the configuration file.
 * </p>
 * <p>
 * Persistent preferences may be set directly by the user or indirectly by
 * the act of using the application.
 * </p>
 * <p>
 * Note the following definitions:
 * </p>
 * <dl>
 *   <dt>File</dt>
 *   <dd>References a file name (no path), path, or directory.</dd>
 *   <dt>Path</dt>
 *   <dd>Fully qualified file name, which includes all parent directories.</dd>
 *   <dt>Dir</dt>
 *   <dd>Directory without file name ({@link File#isDirectory()} is true).</dd>
 * </dl>
 */
public final class Workspace {

  /**
   * Main configuration values, single text strings.
   */
  private final Map<Key, Property<?>> mValues = Map.ofEntries(
    entry( KEY_META_VERSION, asStringProperty( getVersion() ) ),
    entry( KEY_META_NAME, asStringProperty( "default" ) ),

    entry( KEY_EDITOR_AUTOSAVE, asIntegerProperty( 30 ) ),

    entry( KEY_R_SCRIPT, asStringProperty( "" ) ),
    entry( KEY_R_DIR, asFileProperty( USER_DIRECTORY ) ),
    entry( KEY_R_DELIM_BEGAN, asStringProperty( R_DELIM_BEGAN_DEFAULT ) ),
    entry( KEY_R_DELIM_ENDED, asStringProperty( R_DELIM_ENDED_DEFAULT ) ),

    entry( KEY_CACHE_DIR, asFileProperty( USER_CACHE_DIR ) ),
    entry( KEY_IMAGE_DIR, asFileProperty( USER_DIRECTORY ) ),
    entry( KEY_IMAGE_ORDER, asStringProperty( PERSIST_IMAGES_DEFAULT ) ),
    entry( KEY_IMAGE_RESIZE, asBooleanProperty( true ) ),
    entry( KEY_IMAGE_SERVER, asStringProperty( DIAGRAM_SERVER_NAME ) ),

    entry( KEY_DEF_PATH, asFileProperty( DEFINITION_DEFAULT ) ),
    entry( KEY_DEF_DELIM_BEGAN, asStringProperty( DEF_DELIM_BEGAN_DEFAULT ) ),
    entry( KEY_DEF_DELIM_ENDED, asStringProperty( DEF_DELIM_ENDED_DEFAULT ) ),

    entry( KEY_UI_RECENT_DIR, asFileProperty( USER_DIRECTORY ) ),
    entry( KEY_UI_RECENT_OFFSET, asIntegerProperty( DOCUMENT_OFFSET ) ),
    entry( KEY_UI_RECENT_DOCUMENT, asFileProperty( DOCUMENT_DEFAULT ) ),
    entry( KEY_UI_RECENT_DEFINITION, asFileProperty( DEFINITION_DEFAULT ) ),
    entry( KEY_UI_RECENT_EXPORT, asFileProperty( PDF_DEFAULT ) ),

    //@formatter:off
    entry(
      KEY_UI_FONT_EDITOR_NAME,
      asStringProperty( FONT_NAME_EDITOR_DEFAULT )
    ),
    entry(
     KEY_UI_FONT_EDITOR_SIZE,
     asDoubleProperty( FONT_SIZE_EDITOR_DEFAULT )
    ),
    entry(
     KEY_UI_FONT_PREVIEW_NAME,
     asStringProperty( FONT_NAME_PREVIEW_DEFAULT )
    ),
    entry(
     KEY_UI_FONT_PREVIEW_SIZE,
     asDoubleProperty( FONT_SIZE_PREVIEW_DEFAULT )
    ),
    entry(
     KEY_UI_FONT_PREVIEW_MONO_NAME,
     asStringProperty( FONT_NAME_PREVIEW_MONO_NAME_DEFAULT )
    ),
    entry(
     KEY_UI_FONT_PREVIEW_MONO_SIZE,
     asDoubleProperty( FONT_SIZE_PREVIEW_MONO_SIZE_DEFAULT )
    ),
    entry(
      KEY_UI_FONT_MATH_SIZE,
      asDoubleProperty( FONT_SIZE_MATH_DEFAULT )
    ),

    entry( KEY_UI_WINDOW_X, asDoubleProperty( WINDOW_X_DEFAULT ) ),
    entry( KEY_UI_WINDOW_Y, asDoubleProperty( WINDOW_Y_DEFAULT ) ),
    entry( KEY_UI_WINDOW_W, asDoubleProperty( WINDOW_W_DEFAULT ) ),
    entry( KEY_UI_WINDOW_H, asDoubleProperty( WINDOW_H_DEFAULT ) ),
    entry( KEY_UI_WINDOW_MAX, asBooleanProperty() ),
    entry( KEY_UI_WINDOW_FULL, asBooleanProperty() ),

    entry( KEY_UI_SKIN_SELECTION, asSkinProperty( SKIN_DEFAULT ) ),
    entry( KEY_UI_SKIN_CUSTOM, asFileProperty( SKIN_CUSTOM_DEFAULT ) ),

    entry(
      KEY_UI_PREVIEW_STYLESHEET, asFileProperty( PREVIEW_CUSTOM_DEFAULT )
    ),

    entry( KEY_LANGUAGE_LOCALE, asLocaleProperty( LOCALE_DEFAULT ) ),

    entry( KEY_TYPESET_CONTEXT_CLEAN, asBooleanProperty( true ) ),
    entry( KEY_TYPESET_CONTEXT_FONTS_DIR, asFileProperty( getFontDirectory() ) ),
    entry( KEY_TYPESET_CONTEXT_THEMES_PATH, asFileProperty( USER_DIRECTORY ) ),
    entry( KEY_TYPESET_CONTEXT_THEME_SELECTION, asStringProperty( "boschet" ) ),
    entry( KEY_TYPESET_CONTEXT_CHAPTERS, asStringProperty( "" ) ),
    entry( KEY_TYPESET_TYPOGRAPHY_QUOTES, asBooleanProperty( true ) ),
    entry( KEY_TYPESET_MODES_ENABLED, asStringProperty( "" ) )
    //@formatter:on
  );

  /**
   * Sets of configuration values, all the same type (e.g., file names),
   * where the key name doesn't change per set.
   */
  private final Map<Key, SetProperty<?>> mSets = Map.ofEntries(
    entry(
      KEY_UI_RECENT_OPEN_PATH,
      createSetProperty( new HashSet<String>() )
    )
  );

  /**
   * Lists of configuration values, such as key-value pairs where both the
   * key name and the value must be preserved per list.
   */
  private final Map<Key, ListProperty<?>> mLists = Map.ofEntries(
    entry(
      KEY_DOC_META,
      createListProperty( new LinkedList<Entry<String, String>>() )
    )
  );

  /**
   * Helps instantiate {@link Property} instances for XML configuration items.
   */
  private static final Map<Class<?>, Function<String, Object>> UNMARSHALL =
    Map.of(
      LocaleProperty.class, LocaleProperty::parseLocale,
      SimpleBooleanProperty.class, Boolean::parseBoolean,
      SimpleIntegerProperty.class, Integer::parseInt,
      SimpleDoubleProperty.class, Double::parseDouble,
      SimpleFloatProperty.class, Float::parseFloat,
      SimpleStringProperty.class, String::new,
      SimpleObjectProperty.class, String::new,
      SkinProperty.class, String::new,
      FileProperty.class, File::new
    );

  /**
   * The asymmetry with respect to {@link #UNMARSHALL} is because most objects
   * can simply call {@link Object#toString()} to convert the value to a string.
   */
  private static final Map<Class<?>, Function<String, Object>> MARSHALL =
    Map.of(
      LocaleProperty.class, LocaleProperty::toLanguageTag
    );

  /**
   * Converts the given {@link Property} value to a string.
   *
   * @param property The {@link Property} to convert.
   * @return A string representation of the given property, or the empty
   * string if no conversion was possible.
   */
  private static String marshall( final Property<?> property ) {
    final var v = property.getValue();

    return v == null
      ? ""
      : MARSHALL
      .getOrDefault( property.getClass(), _ -> property.getValue() )
      .apply( v.toString() )
      .toString();
  }

  private static Object unmarshall(
    final Property<?> property, final Object configValue ) {
    final var v = configValue.toString();

    return UNMARSHALL
      .getOrDefault( property.getClass(), _ -> property.getValue() )
      .apply( v );
  }

  /**
   * Creates an instance of {@link ObservableList} that is based on a
   * modifiable observable array list for the given items.
   *
   * @param items The items to wrap in an observable list.
   * @param <E>   The type of items to add to the list.
   * @return An observable property that can have its contents modified.
   */
  public static <E> ObservableList<E> listProperty( final Set<E> items ) {
    return new SimpleListProperty<>( observableArrayList( items ) );
  }

  private static <E> SetProperty<E> createSetProperty( final Set<E> set ) {
    return new SimpleSetProperty<>( observableSet( set ) );
  }

  private static <E> ListProperty<E> createListProperty( final List<E> list ) {
    return new SimpleListProperty<>( observableArrayList( list ) );
  }

  private static StringProperty asStringProperty( final String value ) {
    return new SimpleStringProperty( value );
  }

  private static BooleanProperty asBooleanProperty() {
    return new SimpleBooleanProperty();
  }

  /**
   * @param value Default value.
   */
  @SuppressWarnings( "SameParameterValue" )
  private static BooleanProperty asBooleanProperty( final boolean value ) {
    return new SimpleBooleanProperty( value );
  }

  /**
   * @param value Default value.
   */
  @SuppressWarnings( "SameParameterValue" )
  private static IntegerProperty asIntegerProperty( final int value ) {
    return new SimpleIntegerProperty( value );
  }

  /**
   * @param value Default value.
   */
  private static DoubleProperty asDoubleProperty( final double value ) {
    return new SimpleDoubleProperty( value );
  }

  /**
   * @param value Default value.
   */
  private static FileProperty asFileProperty( final File value ) {
    return new FileProperty( value );
  }

  /**
   * @param value Default value.
   */
  @SuppressWarnings( "SameParameterValue" )
  private static LocaleProperty asLocaleProperty( final Locale value ) {
    return new LocaleProperty( value );
  }

  /**
   * @param value Default value.
   */
  @SuppressWarnings( "SameParameterValue" )
  private static SkinProperty asSkinProperty( final String value ) {
    return new SkinProperty( value );
  }

  /**
   * Creates a new {@link Workspace} that will attempt to load the users'
   * preferences. If the configuration file cannot be loaded, the workspace
   * settings returns default values.
   */
  public Workspace() {
    load();
  }

  /**
   * Attempts to load the app's configuration file.
   */
  private void load() {
    final var store = createXmlStore();
    store.load( FILE_PREFERENCES );

    mValues.keySet().forEach( key -> {
      try {
        final var storeValue = store.getValue( key );
        final var property = valuesProperty( key );
        final var unmarshalled = unmarshall( property, storeValue );

        property.setValue( unmarshalled );
      } catch( final NoSuchElementException ex ) {
        // When no configuration (item), use the default value.
        clue( ex );
      }
    } );

    mSets.keySet().forEach( key -> {
      final var set = store.getSet( key );
      final SetProperty<String> property = setsProperty( key );

      property.setValue( observableSet( set ) );
    } );

    mLists.keySet().forEach( key -> {
      final var map = store.getMap( key );
      final ListProperty<Entry<String, String>> property = listsProperty( key );
      final var list = map
        .entrySet()
        .stream()
        .toList();

      property.setValue( observableArrayList( list ) );
    } );

    WorkspaceLoadedEvent.fire( this );
  }

  /**
   * Saves the current workspace.
   */
  public void save() {
    final var store = createXmlStore();

    try {
      // Update the string values to include the application version.
      valuesProperty( KEY_META_VERSION ).setValue( getVersion() );

      mValues.forEach( ( k, v ) -> store.setValue( k, marshall( v ) ) );
      mSets.forEach( store::setSet );
      mLists.forEach( store::setMap );

      store.save( FILE_PREFERENCES );
    } catch( final Exception ex ) {
      clue( ex );
    }
  }

  /**
   * Returns a value that represents a setting in the application that the user
   * may configure, either directly or indirectly.
   *
   * @param key The reference to the users' preference stored in deference
   *            of app reëntrance.
   * @return An observable property to be persisted.
   */
  @SuppressWarnings( "unchecked" )
  public <T, U extends Property<T>> U valuesProperty( final Key key ) {
    assert key != null;
    return (U) mValues.get( key );
  }

  /**
   * Returns a set of values that represent a setting in the application that
   * the user may configure, either directly or indirectly. The property
   * returned is backed by a {@link Set}.
   *
   * @param key The {@link Key} associated with a preference value.
   * @return An observable property to be persisted.
   */
  @SuppressWarnings( "unchecked" )
  public <T> SetProperty<T> setsProperty( final Key key ) {
    assert key != null;
    return (SetProperty<T>) mSets.get( key );
  }

  /**
   * Returns a list of values that represent a setting in the application that
   * the user may configure, either directly or indirectly. The property
   * returned is backed by a mutable {@link List}.
   *
   * @param key The {@link Key} associated with a preference value.
   * @return An observable property to be persisted.
   */
  @SuppressWarnings( "unchecked" )
  public <K, V> ListProperty<Entry<K, V>> listsProperty( final Key key ) {
    assert key != null;
    return (ListProperty<Entry<K, V>>) mLists.get( key );
  }

  /**
   * Returns the {@link String} {@link Property} associated with the given
   * {@link Key} from the internal list of preference values. The caller
   * must be sure that the given {@link Key} is associated with a {@link File}
   * {@link Property}.
   *
   * @param key The {@link Key} associated with a preference value.
   * @return The value associated with the given {@link Key}.
   */
  public StringProperty stringProperty( final Key key ) {
    assert key != null;
    return valuesProperty( key );
  }

  /**
   * Returns the {@link Boolean} {@link Property} associated with the given
   * {@link Key} from the internal list of preference values. The caller
   * must be sure that the given {@link Key} is associated with a {@link File}
   * {@link Property}.
   *
   * @param key The {@link Key} associated with a preference value.
   * @return The value associated with the given {@link Key}.
   */
  public BooleanProperty booleanProperty( final Key key ) {
    assert key != null;
    return valuesProperty( key );
  }

  /**
   * Returns the {@link Integer} {@link Property} associated with the given
   * {@link Key} from the internal list of preference values. The caller
   * must be sure that the given {@link Key} is associated with a {@link File}
   * {@link Property}.
   *
   * @param key The {@link Key} associated with a preference value.
   * @return The value associated with the given {@link Key}.
   */
  public IntegerProperty integerProperty( final Key key ) {
    assert key != null;
    return valuesProperty( key );
  }

  /**
   * Returns the {@link Double} {@link Property} associated with the given
   * {@link Key} from the internal list of preference values. The caller
   * must be sure that the given {@link Key} is associated with a {@link File}
   * {@link Property}.
   *
   * @param key The {@link Key} associated with a preference value.
   * @return The value associated with the given {@link Key}.
   */
  public DoubleProperty doubleProperty( final Key key ) {
    assert key != null;
    return valuesProperty( key );
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
  public ObjectProperty<File> fileProperty( final Key key ) {
    assert key != null;
    return valuesProperty( key );
  }

  /**
   * Returns the {@link Locale} {@link Property} associated with the given
   * {@link Key} from the internal list of preference values. The caller
   * must be sure that the given {@link Key} is associated with a {@link File}
   * {@link Property}.
   *
   * @param key The {@link Key} associated with a preference value.
   * @return The value associated with the given {@link Key}.
   */
  public LocaleProperty localeProperty( final Key key ) {
    assert key != null;
    return valuesProperty( key );
  }

  public ObjectProperty<String> skinProperty( final Key key ) {
    assert key != null;
    return valuesProperty( key );
  }

  public String getString( final Key key ) {
    assert key != null;
    return stringProperty( key ).get();
  }

  /**
   * Returns the {@link Boolean} preference value associated with the given
   * {@link Key}. The caller must be sure that the given {@link Key} is
   * associated with a value that matches the return type.
   *
   * @param key The {@link Key} associated with a preference value.
   * @return The value associated with the given {@link Key}.
   */
  public boolean getBoolean( final Key key ) {
    assert key != null;
    return booleanProperty( key ).get();
  }

  /**
   * Returns the {@link Integer} preference value associated with the given
   * {@link Key}. The caller must be sure that the given {@link Key} is
   * associated with a value that matches the return type.
   *
   * @param key The {@link Key} associated with a preference value.
   * @return The value associated with the given {@link Key}.
   */
  @SuppressWarnings( "unused" )
  public int getInteger( final Key key ) {
    assert key != null;
    return integerProperty( key ).get();
  }

  /**
   * Returns the {@link Double} preference value associated with the given
   * {@link Key}. The caller must be sure that the given {@link Key} is
   * associated with a value that matches the return type.
   *
   * @param key The {@link Key} associated with a preference value.
   * @return The value associated with the given {@link Key}.
   */
  public double getDouble( final Key key ) {
    assert key != null;
    return doubleProperty( key ).get();
  }

  /**
   * Returns the {@link File} preference value associated with the given
   * {@link Key}. The caller must be sure that the given {@link Key} is
   * associated with a value that matches the return type.
   *
   * @param key The {@link Key} associated with a preference value.
   * @return The value associated with the given {@link Key}.
   */
  public File getFile( final Key key ) {
    assert key != null;
    return fileProperty( key ).get();
  }

  /**
   * Returns the language locale setting for the
   * {@link AppKeys#KEY_LANGUAGE_LOCALE} key.
   *
   * @return The user's current locale setting.
   */
  public Locale getLocale() {
    return localeProperty( KEY_LANGUAGE_LOCALE ).toLocale();
  }

  @SuppressWarnings( "unchecked" )
  public <K, V> Map<K, V> getMetadata() {
    final var metadata = listsProperty( KEY_DOC_META );
    final HashMap<K, V> map;

    if( metadata != null ) {
      map = new HashMap<>( metadata.size() );

      metadata.forEach(
        entry -> map.put( (K) entry.getKey(), (V) entry.getValue() )
      );
    }
    else {
      map = new HashMap<>();
    }

    return map;
  }

  public Path getThemesPath() {
    final var dir = getFile( KEY_TYPESET_CONTEXT_THEMES_PATH );
    final var name = getString( KEY_TYPESET_CONTEXT_THEME_SELECTION );

    return Path.of( dir.toString(), name );
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
    assert key != null;
    assert property != null;

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
   *
   * @param key      The value to bind to the internal key property.
   * @param property The external property value that sets the internal value.
   * @param enabled  Indicates whether property changes should be applied.
   */
  public <T> void listen(
    final Key key,
    final ReadOnlyProperty<T> property,
    final BooleanSupplier enabled ) {
    assert key != null;
    assert property != null;
    assert enabled != null;

    property.addListener(
      ( _, _, n ) -> runLater( () -> {
        if( enabled.getAsBoolean() ) {
          valuesProperty( key ).setValue( n );
        }
      } )
    );
  }

  /**
   * Creates a lightweight persistence mechanism for user preferences.
   *
   * @return The {@link XmlStore} that helps with persisting application state.
   */
  private XmlStore createXmlStore() {
    // Root-level configuration item is the application name.
    return new XmlStore( APP_TITLE_LOWERCASE );
  }
}
