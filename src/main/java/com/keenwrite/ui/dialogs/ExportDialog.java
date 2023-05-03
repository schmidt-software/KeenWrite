/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.dialogs;

import com.keenwrite.events.ExportFailedEvent;
import com.keenwrite.util.Diacritics;
import com.keenwrite.util.FileWalker;
import com.keenwrite.util.RangeValidator;
import com.keenwrite.util.ResourceWalker;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static com.keenwrite.Messages.get;
import static com.keenwrite.constants.Constants.THEME_NAME_LENGTH;
import static com.keenwrite.constants.Constants.UI_CONTROL_SPACING;
import static com.keenwrite.constants.GraphicsConstants.ICON_DIALOG;
import static com.keenwrite.constants.GraphicsConstants.ICON_DIALOG_NODE;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.util.FileWalker.walk;
import static java.lang.Math.max;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javafx.application.Platform.runLater;
import static javafx.geometry.Pos.CENTER;
import static javafx.scene.control.ButtonType.OK;
import static org.apache.commons.lang3.StringUtils.abbreviate;

/**
 * Provides controls for exporting to PDF, such as selecting a theme and
 * creating a subset of chapter numbers.
 */
public final class ExportDialog extends AbstractDialog<ExportSettings> {
  private record Theme( Path path, String name ) implements Comparable<Theme> {
    /**
     * Answers whether the given theme directory name matches the theme name
     * that the user selected.
     *
     * @param themeDir The user-selected directory to compare with the
     *                 corresponding path of this {@link Theme}.
     * @return {@code true} if the given directory matches the ending portion
     * of the {@link Path} associated with this {@link Theme} instance.
     */
    public boolean matches( final String themeDir ) {
      final var path = path().getFileName().toString();

      return path.equalsIgnoreCase( Diacritics.remove( themeDir ) );
    }

    /**
     * Returns the theme's display name.
     *
     * @return The name of the theme presented to users.
     */
    @Override
    public String toString() {
      return abbreviate( name(), THEME_NAME_LENGTH );
    }

    @Override
    public int compareTo( final Theme o ) {
      return name().compareTo( o.name() );
    }
  }

  private final File mThemes;
  private final ExportSettings mSettings;
  private GridPane mPane;
  private ComboBox<Theme> mComboBox;
  private TextField mChapters;
  private final boolean mMissingThemes;

  /**
   * Construction must use static method to allow caching themes in the
   * future, if needed.
   */
  private ExportDialog(
    final Window owner,
    final File themesDir,
    final ExportSettings settings,
    final boolean multiple
  ) {
    super( owner, get( "Dialog.typesetting.settings.title" ) );

    assert themesDir != null;
    assert settings != null;

    mThemes = themesDir;
    mSettings = settings;

    setResultConverter( button -> button == OK ? settings : null );

    final var themes = readThemes( themesDir );

    mMissingThemes = themes.isEmpty();

    // Typesetting installation has been corrupted. This is probably due
    // to the user's settings file gone missing. Rather than force users
    // to find the "themes" directory location, re-install the typesetter,
    if( mMissingThemes ) {
      clue( "Dialog.typesetting.settings.themes.missing",
            themesDir.getAbsolutePath() );
      ExportFailedEvent.fire();
      return;
    }

    final var previousTheme = mSettings.themeProperty().get();

    initComboBox( mComboBox, previousTheme, themes );

    mPane.add( createLabel( "Dialog.typesetting.settings.theme" ), 0, 1 );
    mPane.add( mComboBox, 1, 1 );

    var title = "Dialog.typesetting.settings.header.";
    final var focusNode = new AtomicReference<Node>( mComboBox );

    if( multiple ) {
      mPane.add( createLabel( "Dialog.typesetting.settings.chapters" ), 0, 2 );
      mPane.add( mChapters, 1, 2 );

      focusNode.set( mChapters );
      title += "multiple";
    }
    else {
      title += "single";
    }

    // Remember the chapter range regardless of text field visibility.
    mChapters.textProperty().bindBidirectional( mSettings.chaptersProperty() );

    setHeaderText( get( title ) );

    final var dialogPane = getDialogPane();
    dialogPane.setContent( mPane );

    runLater( () -> focusNode.get().requestFocus() );
  }

  /**
   * Prompts a user to select a theme, answering {@code false} if no theme
   * was selected. The themes must be on the native file system; using the
   * {@link FileWalker} is a little more optimal than {@link ResourceWalker}.
   *
   * @param owner    The parent {@link Window} responsible for the dialog.
   * @param themes   Theme directory root.
   * @param settings Configuration preferences to use when exporting.
   * @param multiple Pass {@code true} to input a chapter number subset.
   * @return {@code true} if the user accepted or selected a theme.
   */
  public static boolean choose(
    final Window owner,
    final File themes,
    final ExportSettings settings,
    final boolean multiple
  ) {
    assert themes != null;
    assert settings != null;

    return new ExportDialog( owner, themes, settings, multiple ).pick();
  }

  /**
   * @return {@code true} if the user accepted or selected a theme.
   * @see #choose(Window, File, ExportSettings, boolean)
   */
  private boolean pick() {
    try {
      if( !mMissingThemes ) {
        final var result = showAndWait();

        // The result will only be set if the OK button is pressed.
        if( result.isPresent() ) {
          final var theme = mComboBox.getSelectionModel().getSelectedItem();
          final var path = theme.path();
          final var filename = path.getFileName().toString();
          mSettings.themeProperty().setValue( filename );

          return true;
        }
      }
    } catch( final Exception ex ) {
      clue( get( "Main.status.error.theme.missing", mThemes ), ex );
    }

    return false;
  }

  @Override
  protected void initComponents() {
    initIcon();
    setResizable( true );

    mPane = createContentPane();
    mComboBox = createComboBox();
    mComboBox.setOnKeyPressed( event -> {
      // When the user presses the down arrow, open the drop-down. This
      // prevents navigating to the cancel button.
      if( event.getCode() == KeyCode.DOWN && !mComboBox.isShowing() ) {
        mComboBox.show();
        event.consume();
      }
    } );

    mChapters = createNumericTextField();
  }

  private void initIcon() {
    setGraphic( ICON_DIALOG_NODE );
    setStageGraphic( ICON_DIALOG );
  }

  @SuppressWarnings( "SameParameterValue" )
  private void setStageGraphic( final Image icon ) {
    if( getDialogPane().getScene().getWindow() instanceof final Stage stage ) {
      stage.getIcons().add( icon );
    }
  }

  private void initComboBox(
    final ComboBox<Theme> comboBox,
    final String previousTheme,
    final List<Theme> choices
  ) {
    assert comboBox != null;
    assert previousTheme != null;
    assert choices != null;

    final var items = comboBox.getItems();
    items.clear();
    items.addAll( choices );

    // Set the selected item to user's settings value.
    for( final var choice : choices ) {
      if( choice.matches( previousTheme ) ) {
        comboBox.getSelectionModel().select(
          items.get( max( items.indexOf( choice ), 0 ) )
        );

        break;
      }
    }
  }

  private List<Theme> readThemes( final File themesDir ) {
    try {
      // List themes in alphabetical order (human-readable by directory name).
      final var choices = new LinkedList<Theme>();

      // Populate the choices with themes detected on the system.
      walk( themesDir.toPath(), "**/theme.properties", path -> {
        try {
          final var themeName = readThemeName( path );
          final var themePath = path.getParent();
          choices.add( new Theme( themePath, themeName ) );
        } catch( final Exception ex ) {
          clue( "Main.status.error.theme.name", path );
        }
      } );

      Collections.sort( choices );

      return choices;
    } catch( final Exception ex ) {
      clue( ex );
    }

    return Collections.emptyList();
  }

  private ComboBox<Theme> createComboBox() {
    return new ComboBox<>();
  }

  private GridPane createContentPane() {
    final var grid = new GridPane();

    grid.setAlignment( CENTER );
    grid.setHgap( UI_CONTROL_SPACING );
    grid.setVgap( UI_CONTROL_SPACING );
    grid.setPadding( new Insets( 25, 25, 25, 25 ) );

    return grid;
  }

  /**
   * Creates an input field that only accepts whole numbers. This allows users
   * to enter in chapter ranges such as: <code>1-5, 7, 9-10</code>.
   *
   * @return A {@link TextField} that censors non-conforming characters.
   */
  private TextField createNumericTextField() {
    final var textField = new TextField();

    textField.textProperty().addListener(
      ( c, o, n ) -> textField.setText( RangeValidator.normalize( n ) )
    );

    return textField;
  }

  private Label createLabel( final String key ) {
    final var label = new Label( get( key ) + ":" );
    final var font = label.getFont();
    final var upscale = new Font( font.getName(), 14 );

    label.setFont( upscale );

    return label;
  }

  /**
   * Returns the theme's human-friendly name from a file conforming to
   * {@link Properties}.
   *
   * @param file A fully qualified file name readable using {@link Properties}.
   * @return The human-friendly theme name.
   * @throws IOException          The {@link Properties} file cannot be read.
   * @throws NullPointerException The name field is not defined.
   */
  private String readThemeName( final Path file ) throws Exception {
    return read( file ).get( "name" ).toString();
  }

  /**
   * Reads an instance of {@link Properties} from the given {@link Path} using
   * {@link StandardCharsets#UTF_8} encoding.
   *
   * @param path The fully qualified path to the file.
   * @return The path to the file to read.
   * @throws IOException Could not open the file for reading.
   */
  private Properties read( final Path path ) throws IOException {
    final var properties = new Properties();

    try(
      final var f = new FileInputStream( path.toFile() );
      final var in = new InputStreamReader( f, UTF_8 )
    ) {
      properties.load( in );
    }

    return properties;
  }
}
