/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

import com.dlsc.preferencesfx.PreferencesFx;
import javafx.beans.property.*;

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;

import static com.keenwrite.Constants.*;

/**
 * Responsible for user preferences that can be changed from the GUI. The
 * settings are displayed and persisted using {@link PreferencesFx}.
 */
public final class UserPreferences {
  /**
   * Implementation of the initialization-on-demand holder design pattern
   * for a lazily-loaded singleton. In all versions of Java, the idiom enables
   * a safe, highly concurrent lazy initialization of static fields with good
   * performance. The implementation relies upon the initialization phase of
   * execution within the Java Virtual Machine (JVM) as specified by the Java
   * Language Specification.
   */
  private static class UserPreferencesContainer {
    private static final UserPreferences INSTANCE = new UserPreferences();
  }

  /**
   * Returns the singleton instance for rendering math symbols.
   *
   * @return A non-null instance, loaded, configured, and ready to render math.
   */
  public static UserPreferences getInstance() {
    return UserPreferencesContainer.INSTANCE;
  }

  /**
   * Initializes the user preferences from a file resource.
   */
  public static void initPreferences() {
    System.setProperty(
        "java.util.prefs.PreferencesFactory",
        FilePreferencesFactory.class.getName()
    );
  }

  private final ObjectProperty<File> mPropRDirectory;
  private final StringProperty mPropRScript;
  private final ObjectProperty<File> mPropImagesDirectory;
  private final StringProperty mPropImagesOrder;
  private final ObjectProperty<File> mPropDefinitionPath;
  private final StringProperty mPropRDelimBegan;
  private final StringProperty mPropRDelimEnded;
  private final StringProperty mPropDefDelimBegan;
  private final StringProperty mPropDefDelimEnded;
  private final IntegerProperty mPropFontsSizeEditor;
  private final ObjectProperty<Locale> mPropFontsLocale;

  private UserPreferences() {
    mPropImagesDirectory = new SimpleObjectProperty<>( USER_DIRECTORY );
    mPropImagesOrder = new SimpleStringProperty( PERSIST_IMAGES_DEFAULT );

    mPropDefinitionPath = new SimpleObjectProperty<>( DEFAULT_DEFINITION );
    mPropDefDelimBegan = new SimpleStringProperty( DEF_DELIM_BEGAN_DEFAULT );
    mPropDefDelimEnded = new SimpleStringProperty( DEF_DELIM_ENDED_DEFAULT );

    mPropRDirectory = new SimpleObjectProperty<>( USER_DIRECTORY );
    mPropRScript = new SimpleStringProperty( "" );
    mPropRDelimBegan = new SimpleStringProperty( R_DELIM_BEGAN_DEFAULT );
    mPropRDelimEnded = new SimpleStringProperty( R_DELIM_ENDED_DEFAULT );

    mPropFontsLocale = new SimpleObjectProperty<>( DEFAULT_LOCALE );
    mPropFontsSizeEditor = new SimpleIntegerProperty( (int) FONT_SIZE_EDITOR );
  }

  public ObjectProperty<File> definitionPathProperty() {
    return mPropDefinitionPath;
  }

  public Path getDefinitionPath() {
    return definitionPathProperty().getValue().toPath();
  }

  public StringProperty defDelimiterBeganProperty() {
    return mPropDefDelimBegan;
  }

  public String getDefDelimiterBegan() {
    return defDelimiterBeganProperty().get();
  }

  public StringProperty defDelimiterEndedProperty() {
    return mPropDefDelimEnded;
  }

  public String getDefDelimiterEnded() {
    return defDelimiterEndedProperty().get();
  }

  public ObjectProperty<File> rDirectoryProperty() {
    return mPropRDirectory;
  }

  public File getRDirectory() {
    return rDirectoryProperty().get();
  }

  public StringProperty rScriptProperty() {
    return mPropRScript;
  }

  public String getRScript() {
    return rScriptProperty().get();
  }

  public StringProperty rDelimiterBeganProperty() {
    return mPropRDelimBegan;
  }

  public String getRDelimiterBegan() {
    return rDelimiterBeganProperty().get();
  }

  public StringProperty rDelimiterEndedProperty() {
    return mPropRDelimEnded;
  }

  public String getRDelimiterEnded() {
    return rDelimiterEndedProperty().get();
  }

  public ObjectProperty<File> imagesDirectoryProperty() {
    return mPropImagesDirectory;
  }

  public File getImagesDirectory() {
    return imagesDirectoryProperty().get();
  }

  StringProperty imagesOrderProperty() {
    return mPropImagesOrder;
  }

  public String getImagesOrder() {
    return imagesOrderProperty().get();
  }

  public IntegerProperty fontsSizeEditorProperty() {
    return mPropFontsSizeEditor;
  }

  /**
   * Returns the preferred font size of the text editor.
   *
   * @return A non-negative integer, in points.
   */
  public int getFontsSizeEditor() {
    return mPropFontsSizeEditor.intValue();
  }

  public ObjectProperty<Locale> fontsLocaleProperty() {
    return mPropFontsLocale;
  }

  /**
   * Returns the user's preferred locale.
   *
   * @return A non-null {@link Locale} instance.
   */
  public Locale getFontsLocale() {
    return mPropFontsLocale.get();
  }
}
