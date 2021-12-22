/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

import com.dlsc.formsfx.model.structure.StringField;
import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.PreferencesFxEvent;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import com.dlsc.preferencesfx.util.StorageHandler;
import com.dlsc.preferencesfx.view.NavigationView;
import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import org.controlsfx.control.MasterDetailPane;

import java.io.File;

import static com.dlsc.formsfx.model.structure.Field.ofStringType;
import static com.dlsc.preferencesfx.PreferencesFxEvent.EVENT_PREFERENCES_SAVED;
import static com.keenwrite.Messages.get;
import static com.keenwrite.constants.GraphicsConstants.ICON_DIALOG;
import static com.keenwrite.preferences.AppKeys.*;
import static com.keenwrite.preferences.LocaleProperty.localeListProperty;
import static com.keenwrite.preferences.MapField.ofMapType;
import static com.keenwrite.preferences.SkinProperty.skinListProperty;
import static javafx.scene.control.ButtonType.CANCEL;
import static javafx.scene.control.ButtonType.OK;

/**
 * Provides the ability for users to configure their preferences. This links
 * the {@link Workspace} model with the {@link PreferencesFx} view, in MVC.
 */
@SuppressWarnings( "SameParameterValue" )
public final class PreferencesController {

  private final Workspace mWorkspace;
  private final PreferencesFx mPreferencesFx;

  public PreferencesController( final Workspace workspace ) {
    mWorkspace = workspace;

    // All properties must be initialized before creating the dialog.
    mPreferencesFx = createPreferencesFx();

    initKeyEventHandler( mPreferencesFx );
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
    getPreferencesFx().addEventHandler( EVENT_PREFERENCES_SAVED, eventHandler );
  }

  private StringField createFontNameField(
    final StringProperty fontName, final DoubleProperty fontSize ) {
    final var control = new SimpleFontControl( "Change" );
    control.fontSizeProperty().addListener( ( c, o, n ) -> {
      if( n != null ) {
        fontSize.set( n.doubleValue() );
      }
    } );
    return ofStringType( fontName ).render( control );
  }

  private <K, V> MapField<K, V> createMapField( final MapProperty<K, V> map ) {
    return ofMapType( map ).render( new SimpleTableControl<>() );
  }

  /**
   * Creates the preferences dialog based using {@link XmlStorageHandler} and
   * numerous {@link Category} objects.
   *
   * @return A component for editing preferences.
   * @throws RuntimeException Could not construct the {@link PreferencesFx}
   *                          object (e.g., illegal access permissions,
   *                          unmapped XML resource).
   */
  private PreferencesFx createPreferencesFx() {
    return PreferencesFx.of( createStorageHandler(), createCategories() )
                        .instantPersistent( false )
                        .dialogIcon( ICON_DIALOG );
  }

  private StorageHandler createStorageHandler() {
    return new XmlStorageHandler();
  }

  private Category[] createCategories() {
    return new Category[]{
      Category.of(
        get( KEY_DOC ),
        Group.of(
          get( KEY_DOC_META ),
          Setting.of( label( KEY_DOC_META ) ),
          Setting.of( title( KEY_DOC_META ),
                      createMapField( mapProperty( KEY_DOC_META ) ),
                      mapProperty( KEY_DOC_META ) )
        ),
        Group.of(
          get( KEY_DOC_TITLE ),
          Setting.of( label( KEY_DOC_TITLE ) ),
          Setting.of( title( KEY_DOC_TITLE ),
                      stringProperty( KEY_DOC_TITLE ) )
        ),
        Group.of(
          get( KEY_DOC_AUTHOR ),
          Setting.of( label( KEY_DOC_AUTHOR ) ),
          Setting.of( title( KEY_DOC_AUTHOR ),
                      stringProperty( KEY_DOC_AUTHOR ) )
        ),
        Group.of(
          get( KEY_DOC_BYLINE ),
          Setting.of( label( KEY_DOC_BYLINE ) ),
          Setting.of( title( KEY_DOC_BYLINE ),
                      stringProperty( KEY_DOC_BYLINE ) )
        ),
        Group.of(
          get( KEY_DOC_ADDRESS ),
          Setting.of( label( KEY_DOC_ADDRESS ) ),
          createMultilineSetting( "Address", KEY_DOC_ADDRESS )
        ),
        Group.of(
          get( KEY_DOC_PHONE ),
          Setting.of( label( KEY_DOC_PHONE ) ),
          Setting.of( title( KEY_DOC_PHONE ),
                      stringProperty( KEY_DOC_PHONE ) )
        ),
        Group.of(
          get( KEY_DOC_EMAIL ),
          Setting.of( label( KEY_DOC_EMAIL ) ),
          Setting.of( title( KEY_DOC_EMAIL ),
                      stringProperty( KEY_DOC_EMAIL ) )
        ),
        Group.of(
          get( KEY_DOC_KEYWORDS ),
          Setting.of( label( KEY_DOC_KEYWORDS ) ),
          Setting.of( title( KEY_DOC_KEYWORDS ),
                      stringProperty( KEY_DOC_KEYWORDS ) )
        ),
        Group.of(
          get( KEY_DOC_COPYRIGHT ),
          Setting.of( label( KEY_DOC_COPYRIGHT ) ),
          Setting.of( title( KEY_DOC_COPYRIGHT ),
                      stringProperty( KEY_DOC_COPYRIGHT ) )
        ),
        Group.of(
          get( KEY_DOC_DATE ),
          Setting.of( label( KEY_DOC_DATE ) ),
          Setting.of( title( KEY_DOC_DATE ),
                      stringProperty( KEY_DOC_DATE ) )
        )
      ),
      Category.of(
        get( KEY_TYPESET ),
        Group.of(
          get( KEY_TYPESET_CONTEXT ),
          Setting.of( label( KEY_TYPESET_CONTEXT_THEMES_PATH ) ),
          Setting.of( title( KEY_TYPESET_CONTEXT_THEMES_PATH ),
                      fileProperty( KEY_TYPESET_CONTEXT_THEMES_PATH ), true ),
          Setting.of( label( KEY_TYPESET_CONTEXT_CLEAN ) ),
          Setting.of( title( KEY_TYPESET_CONTEXT_CLEAN ),
                      booleanProperty( KEY_TYPESET_CONTEXT_CLEAN ) )
        ),
        Group.of(
          get( KEY_TYPESET_TYPOGRAPHY ),
          Setting.of( label( KEY_TYPESET_TYPOGRAPHY_QUOTES ) ),
          Setting.of( title( KEY_TYPESET_TYPOGRAPHY_QUOTES ),
                      booleanProperty( KEY_TYPESET_TYPOGRAPHY_QUOTES ) )
        )
      ),
      Category.of(
        get( KEY_EDITOR ),
        Group.of(
          get( KEY_EDITOR_AUTOSAVE ),
          Setting.of( label( KEY_EDITOR_AUTOSAVE ) ),
          Setting.of( title( KEY_EDITOR_AUTOSAVE ),
                      integerProperty( KEY_EDITOR_AUTOSAVE ) )
        )
      ),
      Category.of(
        get( KEY_R ),
        Group.of(
          get( KEY_R_DIR ),
          Setting.of( label( KEY_R_DIR,
                             stringProperty( KEY_DEF_DELIM_BEGAN ).get(),
                             stringProperty( KEY_DEF_DELIM_ENDED ).get() ) ),
          Setting.of( title( KEY_R_DIR ),
                      fileProperty( KEY_R_DIR ), true )
        ),
        Group.of(
          get( KEY_R_SCRIPT ),
          Setting.of( label( KEY_R_SCRIPT ) ),
          createMultilineSetting( "Script", KEY_R_SCRIPT )
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
                      fileProperty( KEY_IMAGES_DIR ), true )
        ),
        Group.of(
          get( KEY_IMAGES_ORDER ),
          Setting.of( label( KEY_IMAGES_ORDER ) ),
          Setting.of( title( KEY_IMAGES_ORDER ),
                      stringProperty( KEY_IMAGES_ORDER ) )
        ),
        Group.of(
          get( KEY_IMAGES_RESIZE ),
          Setting.of( label( KEY_IMAGES_RESIZE ) ),
          Setting.of( title( KEY_IMAGES_RESIZE ),
                      booleanProperty( KEY_IMAGES_RESIZE ) )
        ),
        Group.of(
          get( KEY_IMAGES_SERVER ),
          Setting.of( label( KEY_IMAGES_SERVER ) ),
          Setting.of( title( KEY_IMAGES_SERVER ),
                      stringProperty( KEY_IMAGES_SERVER ) )
        )
      ),
      Category.of(
        get( KEY_DEF ),
        Group.of(
          get( KEY_DEF_PATH ),
          Setting.of( label( KEY_DEF_PATH ) ),
          Setting.of( title( KEY_DEF_PATH ),
                      fileProperty( KEY_DEF_PATH ), false )
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
          get( KEY_UI_FONT_EDITOR ),
          Setting.of( label( KEY_UI_FONT_EDITOR_NAME ) ),
          Setting.of( title( KEY_UI_FONT_EDITOR_NAME ),
                      createFontNameField(
                        stringProperty( KEY_UI_FONT_EDITOR_NAME ),
                        doubleProperty( KEY_UI_FONT_EDITOR_SIZE ) ),
                      stringProperty( KEY_UI_FONT_EDITOR_NAME ) ),
          Setting.of( label( KEY_UI_FONT_EDITOR_SIZE ) ),
          Setting.of( title( KEY_UI_FONT_EDITOR_SIZE ),
                      doubleProperty( KEY_UI_FONT_EDITOR_SIZE ) )
        ),
        Group.of(
          get( KEY_UI_FONT_PREVIEW ),
          Setting.of( label( KEY_UI_FONT_PREVIEW_NAME ) ),
          Setting.of( title( KEY_UI_FONT_PREVIEW_NAME ),
                      createFontNameField(
                        stringProperty( KEY_UI_FONT_PREVIEW_NAME ),
                        doubleProperty( KEY_UI_FONT_PREVIEW_SIZE ) ),
                      stringProperty( KEY_UI_FONT_PREVIEW_NAME ) ),
          Setting.of( label( KEY_UI_FONT_PREVIEW_SIZE ) ),
          Setting.of( title( KEY_UI_FONT_PREVIEW_SIZE ),
                      doubleProperty( KEY_UI_FONT_PREVIEW_SIZE ) ),
          Setting.of( label( KEY_UI_FONT_PREVIEW_MONO_NAME ) ),
          Setting.of( title( KEY_UI_FONT_PREVIEW_MONO_NAME ),
                      createFontNameField(
                        stringProperty( KEY_UI_FONT_PREVIEW_MONO_NAME ),
                        doubleProperty( KEY_UI_FONT_PREVIEW_MONO_SIZE ) ),
                      stringProperty( KEY_UI_FONT_PREVIEW_MONO_NAME ) ),
          Setting.of( label( KEY_UI_FONT_PREVIEW_MONO_SIZE ) ),
          Setting.of( title( KEY_UI_FONT_PREVIEW_MONO_SIZE ),
                      doubleProperty( KEY_UI_FONT_PREVIEW_MONO_SIZE ) )
        )
      ),
      Category.of(
        get( KEY_UI_SKIN ),
        Group.of(
          get( KEY_UI_SKIN_SELECTION ),
          Setting.of( label( KEY_UI_SKIN_SELECTION ) ),
          Setting.of( title( KEY_UI_SKIN_SELECTION ),
                      skinListProperty(),
                      skinProperty( KEY_UI_SKIN_SELECTION ) )
        ),
        Group.of(
          get( KEY_UI_SKIN_CUSTOM ),
          Setting.of( label( KEY_UI_SKIN_CUSTOM ) ),
          Setting.of( title( KEY_UI_SKIN_CUSTOM ),
                      fileProperty( KEY_UI_SKIN_CUSTOM ), false )
        )
      ),
      Category.of(
        get( KEY_UI_PREVIEW ),
        Group.of(
          get( KEY_UI_PREVIEW_STYLESHEET ),
          Setting.of( label( KEY_UI_PREVIEW_STYLESHEET ) ),
          Setting.of( title( KEY_UI_PREVIEW_STYLESHEET ),
                      fileProperty( KEY_UI_PREVIEW_STYLESHEET ), false )
        )
      ),
      Category.of(
        get( KEY_LANGUAGE ),
        Group.of(
          get( KEY_LANGUAGE_LOCALE ),
          Setting.of( label( KEY_LANGUAGE_LOCALE ) ),
          Setting.of( title( KEY_LANGUAGE_LOCALE ),
                      localeListProperty(),
                      localeProperty( KEY_LANGUAGE_LOCALE ) )
        )
      )};
  }

  @SuppressWarnings( "unchecked" )
  private Setting<StringField, StringProperty> createMultilineSetting(
    final String description, final Key property ) {
    final Setting<StringField, StringProperty> setting =
      Setting.of( description, stringProperty( property ) );
    final var field = setting.getElement();
    field.multiline( true );

    return setting;
  }

  /**
   * Map ENTER and ESCAPE keys to OK and CANCEL buttons, respectively.
   */
  private void initKeyEventHandler( final PreferencesFx preferences ) {
    final var view = preferences.getView();
    final var nodes = view.getChildrenUnmodifiable();
    final var master = (MasterDetailPane) nodes.get( 0 );
    final var detail = (NavigationView) master.getDetailNode();
    final var pane = (DialogPane) view.getParent();

    detail.setOnKeyReleased( key -> {
      switch( key.getCode() ) {
        case ENTER -> ((Button) pane.lookupButton( OK )).fire();
        case ESCAPE -> ((Button) pane.lookupButton( CANCEL )).fire();
      }
    } );
  }

  /**
   * Creates a label for the given key after interpolating its value.
   *
   * @param key The key to find in the resource bundle.
   * @return The value of the key as a label.
   */
  private Node label( final Key key ) {
    return label( key, (String[]) null );
  }

  private Node label( final Key key, final String... values ) {
    return new Label( get( key.toString() + ".desc", (Object[]) values ) );
  }

  private String title( final Key key ) {
    return get( key.toString() + ".title" );
  }

  private ObjectProperty<File> fileProperty( final Key key ) {
    return mWorkspace.fileProperty( key );
  }

  private StringProperty stringProperty( final Key key ) {
    return mWorkspace.stringProperty( key );
  }

  private BooleanProperty booleanProperty( final Key key ) {
    return mWorkspace.booleanProperty( key );
  }

  @SuppressWarnings( "SameParameterValue" )
  private IntegerProperty integerProperty( final Key key ) {
    return mWorkspace.integerProperty( key );
  }

  @SuppressWarnings( "SameParameterValue" )
  private DoubleProperty doubleProperty( final Key key ) {
    return mWorkspace.doubleProperty( key );
  }

  private ObjectProperty<String> skinProperty( final Key key ) {
    return mWorkspace.skinProperty( key );
  }

  private ObjectProperty<String> localeProperty( final Key key ) {
    return mWorkspace.localeProperty( key );
  }

  private <K, V> MapProperty<K, V> mapProperty( final Key key ) {
    return mWorkspace.mapProperty( key );
  }

  private PreferencesFx getPreferencesFx() {
    return mPreferencesFx;
  }
}
