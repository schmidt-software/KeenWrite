package com.keenwrite.preferences;

import com.dlsc.formsfx.model.structure.StringField;
import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.PreferencesFxEvent;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;

import java.io.File;

import static com.keenwrite.Constants.ICON_DIALOG;
import static com.keenwrite.Messages.get;

public class UserPreferencesView {
  /**
   * Implementation of the initialization-on-demand holder design pattern,
   * an for a lazy-loaded singleton. In all versions of Java, the idiom enables
   * a safe, highly concurrent lazy initialization of static fields with good
   * performance. The implementation relies upon the initialization phase of
   * execution within the Java Virtual Machine (JVM) as specified by the Java
   * Language Specification.
   */
  private static class UserPreferencesViewContainer {
    private static final UserPreferencesView INSTANCE =
        new UserPreferencesView();
  }

  /**
   * Returns the singleton instance for rendering math symbols.
   *
   * @return A non-null instance, loaded, configured, and ready to render math.
   */
  public static UserPreferencesView getInstance() {
    return UserPreferencesViewContainer.INSTANCE;
  }

  private final PreferencesFx mPreferencesFx;

  public UserPreferencesView() {
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
        Setting.of( "Script", rScriptProperty() );
    final StringField field = scriptSetting.getElement();
    field.multiline( true );

    return PreferencesFx.of(
        UserPreferences.class,
        Category.of(
            get( "Preferences.r" ),
            Group.of(
                get( "Preferences.r.directory" ),
                Setting.of( label( "Preferences.r.directory.desc", false ) ),
                Setting.of( "Directory", rDirectoryProperty(), true )
            ),
            Group.of(
                get( "Preferences.r.script" ),
                Setting.of( label( "Preferences.r.script.desc" ) ),
                scriptSetting
            ),
            Group.of(
                get( "Preferences.r.delimiter.began" ),
                Setting.of( label( "Preferences.r.delimiter.began.desc" ) ),
                Setting.of( "Opening", rDelimiterBeganProperty() )
            ),
            Group.of(
                get( "Preferences.r.delimiter.ended" ),
                Setting.of( label( "Preferences.r.delimiter.ended.desc" ) ),
                Setting.of( "Closing", rDelimiterEndedProperty() )
            )
        ),
        Category.of(
            get( "Preferences.images" ),
            Group.of(
                get( "Preferences.images.directory" ),
                Setting.of( label( "Preferences.images.directory.desc" ) ),
                Setting.of( "Directory", imagesDirectoryProperty(), true )
            ),
            Group.of(
                get( "Preferences.images.suffixes" ),
                Setting.of( label( "Preferences.images.suffixes.desc" ) ),
                Setting.of( "Extensions", imagesOrderProperty() )
            )
        ),
        Category.of(
            get( "Preferences.definitions" ),
            Group.of(
                get( "Preferences.definitions.path" ),
                Setting.of( label( "Preferences.definitions.path.desc" ) ),
                Setting.of( "Path", definitionPathProperty(), false )
            ),
            Group.of(
                get( "Preferences.definitions.delimiter.began" ),
                Setting.of( label(
                    "Preferences.definitions.delimiter.began.desc" ) ),
                Setting.of( "Opening", defDelimiterBeganProperty() )
            ),
            Group.of(
                get( "Preferences.definitions.delimiter.ended" ),
                Setting.of( label(
                    "Preferences.definitions.delimiter.ended.desc" ) ),
                Setting.of( "Closing", defDelimiterEnded() )
            )
        ),
        Category.of(
            get( "Preferences.fonts" ),
            Group.of(
                get( "Preferences.fonts.size_editor" ),
                Setting.of( label( "Preferences.fonts.size_editor.desc" ) ),
                Setting.of( "Points", fontsSizeEditorProperty() )
            )
        )
    ).instantPersistent( false )
                        .dialogIcon( ICON_DIALOG );
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

  private UserPreferences getUserPreferences() {
    return UserPreferences.getInstance();
  }

  private PreferencesFx getPreferencesFx() {
    return mPreferencesFx;
  }

  public ObjectProperty<File> definitionPathProperty() {
    return getUserPreferences().definitionPathProperty();
  }

  private StringProperty defDelimiterBeganProperty() {
    return getUserPreferences().defDelimiterBeganProperty();
  }

  private StringProperty defDelimiterEnded() {
    return getUserPreferences().defDelimiterEndedProperty();
  }

  public ObjectProperty<File> rDirectoryProperty() {
    return getUserPreferences().rDirectoryProperty();
  }

  public StringProperty rScriptProperty() {
    return getUserPreferences().rScriptProperty();
  }

  private StringProperty rDelimiterBeganProperty() {
    return getUserPreferences().rDelimiterBeganProperty();
  }

  private StringProperty rDelimiterEndedProperty() {
    return getUserPreferences().rDelimiterEndedProperty();
  }

  private ObjectProperty<File> imagesDirectoryProperty() {
    return getUserPreferences().imagesDirectoryProperty();
  }

  private StringProperty imagesOrderProperty() {
    return getUserPreferences().imagesOrderProperty();
  }

  public IntegerProperty fontsSizeEditorProperty() {
    return getUserPreferences().fontsSizeEditorProperty();
  }
}
