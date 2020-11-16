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
package com.keenwrite.preferences;

import com.dlsc.preferencesfx.PreferencesFx;
import javafx.beans.property.*;

import java.io.File;
import java.nio.file.Path;

import static com.keenwrite.Constants.*;

/**
 * Responsible for user preferences that can be changed from the GUI. The
 * settings are displayed and persisted using {@link PreferencesFx}.
 */
public class UserPreferences {
  /**
   * Implementation of the initialization-on-demand holder design pattern,
   * an for a lazy-loaded singleton. In all versions of Java, the idiom enables
   * a safe, highly concurrent lazy initialization of static fields with good
   * performance. The implementation relies upon the initialization phase of
   * execution within the Java Virtual Machine (JVM) as specified by the Java
   * Language Specification.
   */
  private static class UserPreferencesContainer {
    private final static UserPreferences INSTANCE = new UserPreferences();
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

  private UserPreferences() {
    mPropRDirectory = simpleFile( USER_DIRECTORY );
    mPropRScript = new SimpleStringProperty( "" );

    mPropImagesDirectory = simpleFile( USER_DIRECTORY );
    mPropImagesOrder = new SimpleStringProperty( PERSIST_IMAGES_DEFAULT );

    mPropDefinitionPath = simpleFile(
        getSetting( "file.definition.default", DEFINITION_NAME )
    );

    mPropDefDelimBegan = new SimpleStringProperty( DEF_DELIM_BEGAN_DEFAULT );
    mPropDefDelimEnded = new SimpleStringProperty( DEF_DELIM_ENDED_DEFAULT );

    mPropRDelimBegan = new SimpleStringProperty( R_DELIM_BEGAN_DEFAULT );
    mPropRDelimEnded = new SimpleStringProperty( R_DELIM_ENDED_DEFAULT );

    mPropFontsSizeEditor = new SimpleIntegerProperty( (int) FONT_SIZE_EDITOR );
  }

  /**
   * Wraps a {@link File} inside a {@link SimpleObjectProperty}.
   *
   * @param path The file name to use when constructing the {@link File}.
   * @return A new {@link SimpleObjectProperty} instance with a {@link File}
   * that references the given {@code path}.
   */
  private SimpleObjectProperty<File> simpleFile( final String path ) {
    return new SimpleObjectProperty<>( new File( path ) );
  }

  /**
   * Returns the value for a key from the settings properties file.
   *
   * @param key   Key within the settings properties file to find.
   * @param value Default value to return if the key is not found.
   * @return The value for the given key from the settings file, or the
   * given {@code value} if no key found.
   */
  @SuppressWarnings("SameParameterValue")
  private String getSetting( final String key, final String value ) {
    return SETTINGS.getSetting( key, value );
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
    return rDirectoryProperty().getValue();
  }

  public StringProperty rScriptProperty() {
    return mPropRScript;
  }

  public String getRScript() {
    return rScriptProperty().getValue();
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
    return imagesDirectoryProperty().getValue();
  }

  StringProperty imagesOrderProperty() {
    return mPropImagesOrder;
  }

  public String getImagesOrder() {
    return imagesOrderProperty().getValue();
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
}
