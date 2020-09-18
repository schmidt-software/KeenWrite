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
package com.scrivenvar.preferences;

import com.dlsc.formsfx.model.structure.StringField;
import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.PreferencesFxEvent;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;

import java.io.File;
import java.nio.file.Path;

import static com.scrivenvar.Constants.*;
import static com.scrivenvar.Messages.get;

/**
 * Responsible for user preferences that can be changed from the GUI. The
 * settings are displayed and persisted using {@link PreferencesFx}.
 */
public class UserPreferences {
  /**
   * Implementation of the  initialization-on-demand holder design pattern,
   * an for a lazy-loaded singleton. In all versions of Java, the idiom enables
   * a safe, highly concurrent lazy initialization of static fields with good
   * performance. The implementation relies upon the initialization phase of
   * execution within the Java Virtual Machine (JVM) as specified by the Java
   * Language Specification. When the class {@link UserPreferencesContainer}
   * is loaded, its initialization completes trivially because there are no
   * static variables to initialize.
   * <p>
   * The static class definition {@link UserPreferencesContainer} within the
   * {@link UserPreferences} is not initialized until such time that
   * {@link UserPreferencesContainer} must be executed. The static
   * {@link UserPreferencesContainer} class executes when
   * {@link #getInstance} is called. The first call will trigger loading and
   * initialization of the {@link UserPreferencesContainer} thereby
   * instantiating the {@link #INSTANCE}.
   * </p>
   * <p>
   * This indirection is necessary because the {@link UserPreferences} class
   * references {@link PreferencesFx}, which must not be instantiated until the
   * UI is ready.
   * </p>
   */
  private static class UserPreferencesContainer {
    private static final UserPreferences INSTANCE = new UserPreferences();
  }

  public static UserPreferences getInstance() {
    return UserPreferencesContainer.INSTANCE;
  }

  private final PreferencesFx mPreferencesFx;

  private final ObjectProperty<File> mPropRDirectory;
  private final StringProperty mPropRScript;
  private final ObjectProperty<File> mPropImagesDirectory;
  private final StringProperty mPropImagesOrder;
  private final ObjectProperty<File> mPropDefinitionPath;
  private final StringProperty mRDelimiterBegan;
  private final StringProperty mRDelimiterEnded;
  private final StringProperty mDefDelimiterBegan;
  private final StringProperty mDefDelimiterEnded;
  private final IntegerProperty mPropFontsSizeEditor;

  private UserPreferences() {
    mPropRDirectory = simpleFile( USER_DIRECTORY );
    mPropRScript = new SimpleStringProperty( "" );

    mPropImagesDirectory = simpleFile( USER_DIRECTORY );
    mPropImagesOrder = new SimpleStringProperty( PERSIST_IMAGES_DEFAULT );

    mPropDefinitionPath = simpleFile(
        getSetting( "file.definition.default", DEFINITION_NAME )
    );

    mDefDelimiterBegan = new SimpleStringProperty( DEF_DELIM_BEGAN_DEFAULT );
    mDefDelimiterEnded = new SimpleStringProperty( DEF_DELIM_ENDED_DEFAULT );

    mRDelimiterBegan = new SimpleStringProperty( R_DELIM_BEGAN_DEFAULT );
    mRDelimiterEnded = new SimpleStringProperty( R_DELIM_ENDED_DEFAULT );

    mPropFontsSizeEditor = new SimpleIntegerProperty( (int) FONT_SIZE_EDITOR );

    // All properties must be initialized before creating the dialog.
    mPreferencesFx = createPreferencesFx();
  }

  /**
   * Display the user preferences settings dialog (non-modal).
   */
  public void show() {
    getPreferencesFx().show( false );
  }

  /**
   * Call to persist the settings. Strictly speaking, this could watch on
   * all values for external changes then save automatically.
   */
  public void save() {
    getPreferencesFx().saveSettings();
  }

  /**
   * Creates the preferences dialog.
   * <p>
   * TODO: Make this dynamic by iterating over all "Preferences.*" values
   * that follow a particular naming pattern.
   * </p>
   *
   * @return A new instance of preferences for users to edit.
   */
  @SuppressWarnings("unchecked")
  private PreferencesFx createPreferencesFx() {
    final Setting<StringField, StringProperty> scriptSetting =
        Setting.of( "Script", mPropRScript );
    final StringField field = scriptSetting.getElement();
    field.multiline( true );

    return PreferencesFx.of(
        UserPreferences.class,
        Category.of(
            get( "Preferences.r" ),
            Group.of(
                get( "Preferences.r.directory" ),
                Setting.of( label( "Preferences.r.directory.desc", false ) ),
                Setting.of( "Directory", mPropRDirectory, true )
            ),
            Group.of(
                get( "Preferences.r.script" ),
                Setting.of( label( "Preferences.r.script.desc" ) ),
                scriptSetting
            ),
            Group.of(
                get( "Preferences.r.delimiter.began" ),
                Setting.of( label( "Preferences.r.delimiter.began.desc" ) ),
                Setting.of( "Opening", mRDelimiterBegan )
            ),
            Group.of(
                get( "Preferences.r.delimiter.ended" ),
                Setting.of( label( "Preferences.r.delimiter.ended.desc" ) ),
                Setting.of( "Closing", mRDelimiterEnded )
            )
        ),
        Category.of(
            get( "Preferences.images" ),
            Group.of(
                get( "Preferences.images.directory" ),
                Setting.of( label( "Preferences.images.directory.desc" ) ),
                Setting.of( "Directory", mPropImagesDirectory, true )
            ),
            Group.of(
                get( "Preferences.images.suffixes" ),
                Setting.of( label( "Preferences.images.suffixes.desc" ) ),
                Setting.of( "Extensions", mPropImagesOrder )
            )
        ),
        Category.of(
            get( "Preferences.definitions" ),
            Group.of(
                get( "Preferences.definitions.path" ),
                Setting.of( label( "Preferences.definitions.path.desc" ) ),
                Setting.of( "Path", mPropDefinitionPath, false )
            ),
            Group.of(
                get( "Preferences.definitions.delimiter.began" ),
                Setting.of( label(
                    "Preferences.definitions.delimiter.began.desc" ) ),
                Setting.of( "Opening", mDefDelimiterBegan )
            ),
            Group.of(
                get( "Preferences.definitions.delimiter.ended" ),
                Setting.of( label(
                    "Preferences.definitions.delimiter.ended.desc" ) ),
                Setting.of( "Closing", mDefDelimiterEnded )
            )
        ),
        Category.of(
            get( "Preferences.fonts" ),
            Group.of(
                get( "Preferences.fonts.size_editor" ),
                Setting.of( label( "Preferences.fonts.size_editor.desc" ) ),
                Setting.of( "Points", mPropFontsSizeEditor )
            )
        )
    ).instantPersistent( false );
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
   * Creates a label for the given key after interpolating its value.
   *
   * @param key The key to find in the resource bundle.
   * @return The value of the key as a label.
   */
  private Node label( final String key ) {
    return new Label( get( key, true ) );
  }

  /**
   * Creates a label for the given key.
   *
   * @param key         The key to find in the resource bundle.
   * @param interpolate {@code true} means to interpolate the value.
   * @return The value of the key, interpolated if {@code interpolate} is
   * {@code true}.
   */
  @SuppressWarnings("SameParameterValue")
  private Node label( final String key, final boolean interpolate ) {
    return new Label( get( key, interpolate ) );
  }

  /**
   * Delegates to the {@link PreferencesFx} event handler for monitoring
   * save events.
   *
   * @param eventHandler The handler to call when the preferences are saved.
   */
  public void addSaveEventHandler(
      final EventHandler<? super PreferencesFxEvent> eventHandler ) {
    final var eventType = PreferencesFxEvent.EVENT_PREFERENCES_SAVED;
    getPreferencesFx().addEventHandler( eventType, eventHandler );
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

  private StringProperty defDelimiterBegan() {
    return mDefDelimiterBegan;
  }

  public String getDefDelimiterBegan() {
    return defDelimiterBegan().get();
  }

  private StringProperty defDelimiterEnded() {
    return mDefDelimiterEnded;
  }

  public String getDefDelimiterEnded() {
    return defDelimiterEnded().get();
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

  private StringProperty rDelimiterBegan() {
    return mRDelimiterBegan;
  }

  public String getRDelimiterBegan() {
    return rDelimiterBegan().get();
  }

  private StringProperty rDelimiterEnded() {
    return mRDelimiterEnded;
  }

  public String getRDelimiterEnded() {
    return rDelimiterEnded().get();
  }

  private ObjectProperty<File> imagesDirectoryProperty() {
    return mPropImagesDirectory;
  }

  public File getImagesDirectory() {
    return imagesDirectoryProperty().getValue();
  }

  private StringProperty imagesOrderProperty() {
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

  private PreferencesFx getPreferencesFx() {
    return mPreferencesFx;
  }
}
