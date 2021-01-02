/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

import com.dlsc.formsfx.model.structure.StringField;
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleControl;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.controlsfx.dialog.FontSelectorDialog;

import static com.keenwrite.Constants.ICON_DIALOG;
import static com.keenwrite.StatusBarNotifier.clue;
import static java.lang.System.currentTimeMillis;
import static javafx.geometry.Pos.CENTER_LEFT;
import static javafx.scene.control.ButtonType.CANCEL;
import static javafx.scene.control.ButtonType.OK;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.layout.Priority.ALWAYS;
import static javafx.scene.text.Font.font;
import static javafx.scene.text.Font.getDefault;

/**
 * Responsible for provide users the ability to select a font using a friendly
 * font dialog.
 */
public class SimpleFontControl extends SimpleControl<StringField, StackPane> {
  private final Button mButton = new Button();
  private final String mButtonText;
  private final DoubleProperty mFontSize = new SimpleDoubleProperty();
  private final TextField mFontName = new TextField();

  public SimpleFontControl( final String buttonText ) {
    mButtonText = buttonText;
  }

  @Override
  public void initializeParts() {
    super.initializeParts();

    mFontName.setText( field.getValue() );
    mFontName.setPromptText( field.placeholderProperty().getValue() );

    final var fieldProperty = field.valueProperty();
    if( fieldProperty.get().equals( "null" ) ) {
      fieldProperty.set( "" );
    }

    mButton.setText( mButtonText );
    mButton.setOnAction( event -> {
      final var selected = !fieldProperty.get().trim().isEmpty();
      var initialFont = getDefault();
      if( selected ) {
        final var previousValue = fieldProperty.get();
        initialFont = font( previousValue );
      }

      createFontSelectorDialog( initialFont )
        .showAndWait()
        .ifPresent( ( font ) -> {
          mFontName.setText( font.getName() );
          mFontSize.set( font.getSize() );
        } );
    } );

    node = new StackPane();
  }

  @Override
  public void layoutParts() {
    node.getStyleClass().add( "simple-text-control" );
    fieldLabel.getStyleClass().addAll( field.getStyleClass() );
    fieldLabel.getStyleClass().add( "read-only-label" );

    final var box = new HBox();
    HBox.setHgrow( mFontName, ALWAYS );
    box.setAlignment( CENTER_LEFT );
    box.getChildren().addAll( fieldLabel, mFontName, mButton );

    node.getChildren().add( box );
  }

  @Override
  public void setupBindings() {
    super.setupBindings();
    mFontName.textProperty().bindBidirectional( field.userInputProperty() );
  }

  public DoubleProperty fontSizeProperty() {
    return mFontSize;
  }

  /**
   * Creates a dialog that displays a list of available font families,
   * sizes, and a button for font selection.
   *
   * @param font The default font to select initially.
   * @return A dialog to help the user select a different {@link Font}.
   */
  private FontSelectorDialog createFontSelectorDialog( final Font font ) {
    final var dialog = new FontSelectorDialog( font );
    final var pane = dialog.getDialogPane();
    final var buttonOk = ((Button) pane.lookupButton( OK ));
    final var buttonCancel = ((Button) pane.lookupButton( CANCEL ));

    buttonOk.setDefaultButton( true );
    buttonCancel.setCancelButton( true );
    pane.setOnKeyReleased( ( keyEvent ) -> {
      switch( keyEvent.getCode() ) {
        case ENTER -> buttonOk.fire();
        case ESCAPE -> buttonCancel.fire();
      }
    } );

    final var stage = (Stage) pane.getScene().getWindow();
    stage.getIcons().add( ICON_DIALOG );

    final var frontPanel = (Region) pane.getContent();
    for( final var node : frontPanel.getChildrenUnmodifiable() ) {
      if( node instanceof ListView ) {
        final var listView = (ListView<?>) node;
        final var handler = new ListViewHandler<>( listView );
        listView.setOnKeyPressed( handler::handle );
      }
    }

    return dialog;
  }

  /**
   * Responsible for handling key presses when selecting a font. Based on
   * <a href="https://stackoverflow.com/a/43604223/59087">Martin Široký</a>'s
   * answer.
   *
   * @param <T> The type of {@link ListView} to search.
   */
  private static final class ListViewHandler<T> {
    /**
     * Amount of time to wait between key presses that typing a subsequent
     * key is considered part of the same search, in milliseconds.
     */
    private static final int RESET_DELAY_MS = 1250;

    private String mNeedle = "";
    private int mSearchSkip = 0;
    private long mLastTyped = currentTimeMillis();
    private final ListView<T> mHaystack;

    private ListViewHandler( final ListView<T> listView ) {
      mHaystack = listView;
    }

    private void handle( final KeyEvent key ) {
      var ch = key.getText();
      final var code = key.getCode();

      if( ch == null || ch.isEmpty() || code == ESCAPE || code == ENTER ) {
        return;
      }

      ch = ch.toUpperCase();

      if( mNeedle.equals( ch ) ) {
        mSearchSkip++;
      }
      else {
        mNeedle = currentTimeMillis() - mLastTyped > RESET_DELAY_MS
          ? ch : mNeedle + ch;
      }

      mLastTyped = currentTimeMillis();

      boolean found = false;
      int skipped = 0;

      for( final T item : mHaystack.getItems() ) {
        final var straw = item.toString().toUpperCase();

        if( straw.startsWith( mNeedle ) ) {
          if( mSearchSkip > skipped ) {
            skipped++;
            continue;
          }

          mHaystack.getSelectionModel().select( item );
          final int index = mHaystack.getSelectionModel().getSelectedIndex();
          mHaystack.getFocusModel().focus( index );
          mHaystack.scrollTo( index );
          found = true;
          break;
        }
      }

      if( !found ) {
        clue( "Main.status.font.search.missing", mNeedle );
        mSearchSkip = 0;
      }
    }
  }
}
