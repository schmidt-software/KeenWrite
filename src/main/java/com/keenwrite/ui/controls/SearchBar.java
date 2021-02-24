/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.controls;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.controlsfx.control.textfield.CustomTextField;

import static com.keenwrite.Messages.get;
import static com.keenwrite.ui.fonts.IconFactory.createGraphic;
import static java.lang.StrictMath.max;
import static java.lang.String.format;
import static javafx.geometry.Orientation.VERTICAL;
import static javafx.scene.layout.Priority.ALWAYS;

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
  private final Label mMatches = new Label();
  private final IntegerProperty mMatchIndex = new SimpleIntegerProperty();
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

    mMatchIndex.addListener( ( c, o, n ) -> updateMatchText() );
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
   * Adds a listener that triggers when the input text field changes.
   *
   * @param listener The listener to notify of change events.
   */
  public void addInputListener( final ChangeListener<String> listener ) {
    mFind.textProperty().addListener( listener );
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
   * @return The index of the latest search string match.
   */
  public IntegerProperty matchIndexProperty() {
    return mMatchIndex;
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
    final var index = max( 0, mMatchIndex.get() );
    final var count = max( 0, mMatchCount.get() );
    final var suffix = count == 0 ? "none" : "some";
    final var key = getMessageValue( "match", suffix );

    mMatches.setText( get( key, index, count ) );
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
    return new Separator( VERTICAL );
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
    VBox.setVgrow( spacer, ALWAYS );
    return spacer;
  }

  private Node getIcon( final String id ) {
    return createGraphic( getMessageValue( id, "icon" ) );
  }

  private String getMessageValue( final String id, final String suffix ) {
    return get( format( MESSAGE_KEY, id, suffix ) );
  }

  private void addAll( final Node... nodes ) {
    getChildren().addAll( nodes );
  }
}
