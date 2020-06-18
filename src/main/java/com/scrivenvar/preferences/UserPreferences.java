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
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import com.scrivenvar.Services;
import com.scrivenvar.service.Settings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;

import java.io.File;
import java.nio.file.Path;

import static com.scrivenvar.Constants.PERSIST_IMAGES_DEFAULT;
import static com.scrivenvar.Constants.USER_DIRECTORY;
import static com.scrivenvar.Messages.get;

public class UserPreferences {
  private final Settings SETTINGS = Services.load( Settings.class );

  private final ObjectProperty<File> mPropRDirectory;
  private final StringProperty mPropRScript;
  private final ObjectProperty<File> mPropImagesDirectory;
  private final StringProperty mPropImagesOrder;
  private final ObjectProperty<File> mPropDefinitionPath;

  private final PreferencesFx mPreferencesFx;

  public UserPreferences() {
    mPropRDirectory = simpleFile( USER_DIRECTORY );
    mPropRScript = new SimpleStringProperty( "" );

    mPropImagesDirectory = simpleFile( USER_DIRECTORY );
    mPropImagesOrder = new SimpleStringProperty( PERSIST_IMAGES_DEFAULT );

    mPropDefinitionPath = simpleFile( getSetting(
        "file.definition.default", "variables.yaml" )
    );

    mPreferencesFx = createPreferencesFx();
  }

  /**
   * Display the user preferences settings dialog (non-modal).
   */
  public void show() {
    mPreferencesFx.show( false );
  }

  /**
   * Call to persist the settings. Strictly speaking, this could watch on
   * all values for external changes then save automatically.
   */
  public void save() {
    mPreferencesFx.saveSettings();
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
                Setting.of( "Path", mPropDefinitionPath, true )
            )
        )
    );
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

  private ObjectProperty<File> rDirectoryProperty() {
    return mPropRDirectory;
  }

  public File getRDirectory() {
    return rDirectoryProperty().getValue();
  }

  private StringProperty rScriptProperty() {
    return mPropRScript;
  }

  public String getRScript() {
    return rScriptProperty().getValue();
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
}
