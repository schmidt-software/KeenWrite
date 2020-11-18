/*
 * Copyright 2020 Karl Tauber and White Magic Software, Ltd.
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
package com.keenwrite.ui;

import com.keenwrite.ui.Action;
import de.jensd.fx.glyphs.GlyphIcons;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCombination;

import java.util.ArrayList;
import java.util.List;

import static de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory.get;
import static javafx.scene.input.KeyCombination.valueOf;

/**
 * Defines actions the user can take by interacting with the GUI.
 */
public class MenuAction extends Action {
  private final String mText;
  private final KeyCombination mAccelerator;
  private final GlyphIcons mIcon;
  private final EventHandler<ActionEvent> mAction;
  private final ObservableBooleanValue mDisabled;
  private final List<Action> mSubActions = new ArrayList<>();

  public MenuAction(
      final String text,
      final String accelerator,
      final GlyphIcons icon,
      final EventHandler<ActionEvent> action,
      final ObservableBooleanValue disabled ) {

    mText = text;
    mAccelerator = accelerator == null ? null : valueOf( accelerator );
    mIcon = icon;
    mAction = action;
    mDisabled = disabled;
  }

  @Override
  public MenuItem createMenuItem() {
    // This will either become a menu or a menu item, depending on whether
    // sub-actions are defined.
    final MenuItem menuItem;

    if( mSubActions.isEmpty() ) {
      // Regular menu item has no sub-menus.
      menuItem = new MenuItem( mText );
    }
    else {
      // Sub-actions are translated into sub-menu items beneath this action.
      final var submenu = new Menu( mText );

      for( final var action : mSubActions ) {
        // Recursive call that creates a sub-menu hierarchy.
        submenu.getItems().add( action.createMenuItem() );
      }

      menuItem = submenu;
    }

    if( mAccelerator != null ) {
      menuItem.setAccelerator( mAccelerator );
    }

    if( mIcon != null ) {
      menuItem.setGraphic( get().createIcon( mIcon ) );
    }

    if( mAction != null ) {
      menuItem.setOnAction( mAction );
    }

    if( mDisabled != null ) {
      menuItem.disableProperty().bind( mDisabled );
    }

    menuItem.setMnemonicParsing( true );

    return menuItem;
  }

  @Override
  public Button createToolBarButton() {
    final Button button = new Button();
    button.setGraphic(
        get().createIcon( mIcon, "1.2em" ) );

    String tooltip = mText;

    if( tooltip.endsWith( "..." ) ) {
      tooltip = tooltip.substring( 0, tooltip.length() - 3 );
    }

    if( mAccelerator != null ) {
      tooltip += " (" + mAccelerator.getDisplayText() + ')';
    }

    button.setTooltip( new Tooltip( tooltip ) );
    button.setFocusTraversable( false );
    button.setOnAction( mAction );

    if( mDisabled != null ) {
      button.disableProperty().bind( mDisabled );
    }

    return button;
  }

  @Override
  public Action addSubActions( final Action... action ) {
    mSubActions.addAll( List.of( action ) );
    return this;
  }
}
