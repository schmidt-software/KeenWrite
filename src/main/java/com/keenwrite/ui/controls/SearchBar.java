/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.controls;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.controlsfx.control.textfield.CustomTextField;

import static com.keenwrite.Messages.get;
import static java.lang.StrictMath.max;
import static java.lang.String.format;

/**
 * Responsible for presenting user interface options for searching through
 * the document.
 */
public final class SearchBar extends HBox {

  private static final String MESSAGE_KEY = "Main.search.%s.%s";

  private final Button mButtonStop = createButtonStop();
  private final Button mButtonNext = createButton( "next" );
  private final Button mButtonPrev = createButton( "prev" );
  private final TextField mFind = createTextField();
  private final Text mMatches = new Text();
  private final IntegerProperty mMatchItem = new SimpleIntegerProperty();
  private final IntegerProperty mMatchCount = new SimpleIntegerProperty();

  public SearchBar() {
    setAlignment( Pos.CENTER );
    addAll(
        mButtonStop,
        createSpacer( 10 ),
        mFind,
        createSpacer( 10 ),
        mButtonNext,
        createSpacer( 10 ),
        mButtonPrev,
        createSpacer( 10 ),
        mMatches,
        createSpacer( 10 ),
        createSeparatorVertical(),
        createSpacer( 5 )
    );

    mMatchItem.addListener( ( c, o, n ) -> updateMatchText() );
    mMatchCount.addListener( ( c, o, n ) -> updateMatchText() );
    updateMatchText();
  }

  /**
   * Gives focus to the text field.
   */
  @Override
  public void requestFocus() {
    mFind.requestFocus();
  }

  /**
   * Sets the {@link EventHandler} to call when the user interface triggers
   * finding the next matching search string. This will wrap from the end
   * to the beginning.
   *
   * @param handler The handler requested to perform the find next action.
   */
  public void setOnNextAction( final EventHandler<ActionEvent> handler ) {
    mButtonNext.setOnAction( handler );
    mFind.setOnAction( handler );
  }

  /**
   * Sets the {@link EventHandler} to call when the user interface triggers
   * finding the previous matching search string. This will wrap from the
   * beginning to the end.
   *
   * @param handler The handler requested to perform the find next action.
   */
  public void setOnPrevAction( final EventHandler<ActionEvent> handler ) {
    mButtonPrev.setOnAction( handler );
  }

  /**
   * Sets the {@link EventHandler} to call when searching has been terminated.
   *
   * @param handler The {@link EventHandler} that will perform an action
   *                when the searching has stopped (e.g., remove from this
   *                widget from status bar).
   */
  public void setOnCancelAction( final EventHandler<ActionEvent> handler ) {
    mButtonStop.setOnAction( handler );
  }

  /**
   * When this property value changes, the match text is updated accordingly.
   * If the value is less than zero, the text will show zero.
   *
   * @return The nth item number that matches the search string.
   */
  public IntegerProperty matchItemProperty() {
    return mMatchItem;
  }

  /**
   * When this property value changes, the match text is updated accordingly.
   * If the value is less than zero, the text will show zero.
   *
   * @return The total number of items that match the search string.
   */
  public IntegerProperty matchCountProperty() {
    return mMatchCount;
  }

  /**
   * Updates the match count.
   */
  private void updateMatchText() {
    final var item = max( 0, mMatchItem.get() );
    final var total = max( 0, mMatchCount.get() );

    final var key = getMessageValue( "match", total == 0 ? "none" : "some" );
    mMatches.setText( get( key, item, total ) );
  }

  private Button createButton( final String id ) {
    final var button = new Button();
    final var tooltipText = getMessageValue( id, "tooltip" );

    button.setMnemonicParsing( false );
    button.setGraphic( getIcon( id ) );
    button.setTooltip( new Tooltip( tooltipText ) );

    return button;
  }

  private Button createButtonStop() {
    final var button = createButton( "stop" );
    button.setCancelButton( true );

    return button;
  }

  private TextField createTextField() {
    final var textField = new CustomTextField();
    textField.setLeft( getIcon( "find" ) );
    return textField;
  }

  /**
   * Creates a vertical bar, used to divide the search results from the
   * application status message.
   *
   * @return A vertical separator.
   */
  private Node createSeparatorVertical() {
    return new Separator( Orientation.VERTICAL );
  }

  /**
   * Breathing room between the search box and the application status message.
   * This could also be accomplished by using CSS.
   *
   * @param width The spacer's width.
   * @return A new {@link Node} having about 10px of space.
   */
  private Node createSpacer( final int width ) {
    final var spacer = new Region();
    spacer.setPrefWidth( width );
    VBox.setVgrow( spacer, Priority.ALWAYS );
    return spacer;
  }

  private Node getIcon( final String id ) {
    final var name = getMessageValue( id, "icon" );
    final var glyph = FontAwesomeIcon.valueOf( name.toUpperCase() );
    return FontAwesomeIconFactory.get().createIcon( glyph );
  }

  private String getMessageValue( final String id, final String suffix ) {
    return get( format( MESSAGE_KEY, id, suffix ) );
  }

  private void addAll( final Node... nodes ) {
    getChildren().addAll( nodes );
  }
}
