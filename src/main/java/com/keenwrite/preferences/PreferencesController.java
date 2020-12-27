/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

import com.dlsc.formsfx.model.structure.StringField;
import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.PreferencesFxEvent;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;

import java.io.File;

import static com.keenwrite.Constants.ICON_DIALOG;
import static com.keenwrite.Messages.get;
import static com.keenwrite.preferences.Workspace.*;

/**
 * Provides the ability for users to configure their preferences. This links
 * the {@link Workspace} model with the {@link PreferencesFx} view, in MVC.
 */
public class PreferencesController {

  private final Workspace mWorkspace;
  private final PreferencesFx mPreferencesFx;

  public PreferencesController( final Workspace workspace ) {
    mWorkspace = workspace;

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
      Setting.of( "Script", stringProperty( KEY_R_SCRIPT ) );
    final StringField field = scriptSetting.getElement();
    field.multiline( true );

    return PreferencesFx.of(
      new XmlStorageHandler(),
      Category.of(
        get( "Preferences.r" ),
        Group.of(
          get( "Preferences.r.directory" ),
          Setting.of( label( "Preferences.r.directory.desc", false ) ),
          Setting.of( "Directory", fileProperty( KEY_R_DIR ), true )
        ),
        Group.of(
          get( "Preferences.r.script" ),
          Setting.of( label( "Preferences.r.script.desc" ) ),
          scriptSetting
        ),
        Group.of(
          get( "Preferences.r.delimiter.began" ),
          Setting.of( label( "Preferences.r.delimiter.began.desc" ) ),
          Setting.of( "Opening", stringProperty( KEY_R_DELIM_BEGAN ) )
        ),
        Group.of(
          get( "Preferences.r.delimiter.ended" ),
          Setting.of( label( "Preferences.r.delimiter.ended.desc" ) ),
          Setting.of( "Closing", stringProperty( KEY_R_DELIM_ENDED ) )
        )
      ),
      Category.of(
        get( "Preferences.images" ),
        Group.of(
          get( "Preferences.images.directory" ),
          Setting.of( label( "Preferences.images.directory.desc" ) ),
          Setting.of( "Directory", fileProperty( KEY_IMAGES_DIR ), true )
        ),
        Group.of(
          get( "Preferences.images.suffixes" ),
          Setting.of( label( "Preferences.images.suffixes.desc" ) ),
          Setting.of( "Extensions", stringProperty( KEY_IMAGES_ORDER ) )
        )
      ),
      Category.of(
        get( "Preferences.definitions" ),
        Group.of(
          get( "Preferences.definitions.path" ),
          Setting.of( label( "Preferences.definitions.path.desc" ) ),
          Setting.of( "Path", fileProperty( KEY_DEF_PATH ), false )
        ),
        Group.of(
          get( "Preferences.definitions.delimiter.began" ),
          Setting.of( label(
            "Preferences.definitions.delimiter.began.desc" ) ),
          Setting.of( "Opening", stringProperty( KEY_DEF_DELIM_BEGAN ) )
        ),
        Group.of(
          get( "Preferences.definitions.delimiter.ended" ),
          Setting.of( label(
            "Preferences.definitions.delimiter.ended.desc" ) ),
          Setting.of( "Closing", stringProperty( KEY_DEF_DELIM_ENDED ) )
        )
      ),
      Category.of(
        get( "Preferences.fonts" ),
        Group.of(
          get( "Preferences.fonts.size_editor" ),
          Setting.of( label( "Preferences.fonts.size_editor.desc" ) ),
          Setting.of( "Points", doubleProperty( KEY_UI_FONT_EDITOR_SIZE ) )
        )
      )
    ).instantPersistent( false ).dialogIcon( ICON_DIALOG );
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

  private ObjectProperty<File> fileProperty( final Key key ) {
    return mWorkspace.fileProperty( key );
  }

  private StringProperty stringProperty( final Key key ) {
    return mWorkspace.stringProperty( key );
  }

  @SuppressWarnings("SameParameterValue")
  private DoubleProperty doubleProperty( final Key key ) {
    return mWorkspace.doubleProperty( key );
  }

  private PreferencesFx getPreferencesFx() {
    return mPreferencesFx;
  }
}
