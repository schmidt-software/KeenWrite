/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

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
import java.util.List;
import java.util.Locale;

import static com.keenwrite.Constants.ICON_DIALOG;
import static com.keenwrite.Messages.get;
import static com.keenwrite.preferences.Workspace.*;
import static java.util.Locale.forLanguageTag;
import static javafx.collections.FXCollections.observableArrayList;

/**
 * Provides the ability for users to configure their preferences. This links
 * the {@link Workspace} model with the {@link PreferencesFx} view, in MVC.
 */
@SuppressWarnings( "SameParameterValue" )
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
  @SuppressWarnings( "unchecked" )
  private PreferencesFx createPreferencesFx() {
    final Setting<StringField, StringProperty> scriptSetting =
      Setting.of( "Script", stringProperty( KEY_R_SCRIPT ) );
    final StringField field = scriptSetting.getElement();
    field.multiline( true );

    return PreferencesFx.of(
      new XmlStorageHandler(),
      Category.of(
        get( KEY_R ),
        Group.of(
          get( KEY_R_DIR ),
          Setting.of( label( KEY_R_DIR, false ) ),
          Setting.of( title( KEY_R_DIR ), fileProperty( KEY_R_DIR ), true )
        ),
        Group.of(
          get( KEY_R_SCRIPT ),
          Setting.of( label( KEY_R_SCRIPT ) ),
          scriptSetting
        ),
        Group.of(
          get( KEY_R_DELIM_BEGAN ),
          Setting.of( label( KEY_R_DELIM_BEGAN ) ),
          Setting.of( title( KEY_R_DELIM_BEGAN ),
                      stringProperty( KEY_R_DELIM_BEGAN ) )
        ),
        Group.of(
          get( KEY_R_DELIM_ENDED ),
          Setting.of( label( KEY_R_DELIM_ENDED ) ),
          Setting.of( title( KEY_R_DELIM_ENDED ),
                      stringProperty( KEY_R_DELIM_ENDED ) )
        )
      ),
      Category.of(
        get( KEY_IMAGES ),
        Group.of(
          get( KEY_IMAGES_DIR ),
          Setting.of( label( KEY_IMAGES_DIR ) ),
          Setting.of( title( KEY_IMAGES_DIR ),
                      fileProperty( KEY_IMAGES_DIR ),
                      true )
        ),
        Group.of(
          get( KEY_IMAGES_ORDER ),
          Setting.of( label( KEY_IMAGES_ORDER ) ),
          Setting.of( title( KEY_IMAGES_ORDER ),
                      stringProperty( KEY_IMAGES_ORDER ) )
        )
      ),
      Category.of(
        get( KEY_DEF ),
        Group.of(
          get( KEY_DEF_PATH ),
          Setting.of( label( KEY_DEF_PATH ) ),
          Setting.of( title( KEY_DEF_PATH ),
                      fileProperty( KEY_DEF_PATH ),
                      false )
        ),
        Group.of(
          get( KEY_DEF_DELIM_BEGAN ),
          Setting.of( label( KEY_DEF_DELIM_BEGAN ) ),
          Setting.of( title( KEY_DEF_DELIM_BEGAN ),
                      stringProperty( KEY_DEF_DELIM_BEGAN ) )
        ),
        Group.of(
          get( KEY_DEF_DELIM_ENDED ),
          Setting.of( label( KEY_DEF_DELIM_ENDED ) ),
          Setting.of( title( KEY_DEF_DELIM_ENDED ),
                      stringProperty( KEY_DEF_DELIM_ENDED ) )
        )
      ),
      Category.of(
        get( KEY_UI_FONT ),
        Group.of(
          get( KEY_UI_FONT_EDITOR_SIZE ),
          Setting.of( label( KEY_UI_FONT_EDITOR_SIZE ) ),
          Setting.of( title( KEY_UI_FONT_EDITOR_SIZE ),
                      doubleProperty( KEY_UI_FONT_EDITOR_SIZE ) )
        ),
        Group.of(
          get( KEY_UI_FONT_LOCALE ),
          Setting.of( label( KEY_UI_FONT_LOCALE ) ),
          Setting.of( title( KEY_UI_FONT_LOCALE ),
                      localeListProperty(),
                      localeProperty( KEY_UI_FONT_LOCALE ) )
        )
      )
    ).instantPersistent( false ).dialogIcon( ICON_DIALOG );
  }
  //KEY_UI_FONT_LOCALE

  /**
   * Creates a label for the given key after interpolating its value.
   *
   * @param key The key to find in the resource bundle.
   * @return The value of the key as a label.
   */
  private Node label( final Key key ) {
    return label( key, true );
  }

  private Node label( final Key key, final boolean interpolate ) {
    return label( key.toString() + ".desc", interpolate );
  }

  private String title( final Key key ) {
    return get( key.toString() + ".title" );
  }

  /**
   * Creates a label for the given key.
   *
   * @param key         The key to find in the resource bundle.
   * @param interpolate {@code true} means to interpolate the value.
   * @return The value of the key, interpolated if {@code interpolate} is
   * {@code true}.
   */
  private Node label( final String key, final boolean interpolate ) {
    return new Label( get( key, interpolate ) );
  }

  private ObjectProperty<File> fileProperty( final Key key ) {
    return mWorkspace.fileProperty( key );
  }

  private StringProperty stringProperty( final Key key ) {
    return mWorkspace.stringProperty( key );
  }

  @SuppressWarnings( "SameParameterValue" )
  private DoubleProperty doubleProperty( final Key key ) {
    return mWorkspace.doubleProperty( key );
  }

  private ObjectProperty<Locale> localeProperty( final Key key ) {
    return mWorkspace.localeProperty( key );
  }

  /**
   * https://www.oracle.com/java/technologies/javase/jdk12locales.html
   * @return
   */
  private ListProperty<Locale> localeListProperty() {
    //System.out.println( "DisplayName: " + locale.getDisplayName() );

    final var list = observableArrayList(
      List.of(
        forLanguageTag( "en-Latn-AU" ),
        forLanguageTag( "en-Latn-CA" ),
        forLanguageTag( "en-Latn-GB" ),
        forLanguageTag( "en-Latn-NZ" ),
        forLanguageTag( "en-Latn-US" ),
        forLanguageTag( "en-Latn-ZA" ),
        forLanguageTag( "ja-Jpan-JP" ),
        forLanguageTag( "ko-Kore-KR" ),
        forLanguageTag( "zh-Hans-CN" ),
        forLanguageTag( "zh-Hans-SG" ),
        forLanguageTag( "zh-Hant-HK" ),
        forLanguageTag( "zh-Hant-TW" )
      )
    );

    return new SimpleListProperty<>( list );
  }

  private PreferencesFx getPreferencesFx() {
    return mPreferencesFx;
  }
}
