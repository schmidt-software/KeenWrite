/* Copyright 2020 White Magic Software, Ltd.
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
package com.keenwrite.ui.actions;

import com.keenwrite.Messages;
import com.keenwrite.util.GenericBuilder;
import de.jensd.fx.glyphs.GlyphIcons;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
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
 * Defines actions the user can take through GUI interactions
 */
public class Action implements MenuAction {
  private final String mText;
  private final KeyCombination mAccelerator;
  private final GlyphIcons mIcon;
  private final EventHandler<ActionEvent> mHandler;
  private final List<MenuAction> mSubActions = new ArrayList<>();

  public Action(
      final String text,
      final String accelerator,
      final GlyphIcons icon,
      final EventHandler<ActionEvent> handler ) {
    mText = text;
    mAccelerator = accelerator == null ? null : valueOf( accelerator );
    mIcon = icon;
    mHandler = handler;
  }

  /**
   * Runs this action. Most actions are mapped to menu items, but some actions
   * (such as the Insert key to toggle overwrite mode) are not.
   */
  public void execute() {
    mHandler.handle( new ActionEvent() );
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

    if( mHandler != null ) {
      menuItem.setOnAction( mHandler );
    }

    return menuItem;
  }

  @Override
  public Button createToolBarNode() {
    final var button = createIconButton();
    var tooltip = mText;

    if( tooltip.endsWith( "..." ) ) {
      tooltip = tooltip.substring( 0, tooltip.length() - 3 );
    }

    if( mAccelerator != null ) {
      tooltip += " (" + mAccelerator.getDisplayText() + ')';
    }

    button.setTooltip( new Tooltip( tooltip ) );
    button.setFocusTraversable( false );
    button.setOnAction( mHandler );

    return button;
  }

  private Button createIconButton() {
    final var button = new Button();
    button.setGraphic( get().createIcon( mIcon, "1.2em" ) );
    return button;
  }

  /**
   * Adds subordinate actions to the menu. This is used to establish sub-menu
   * relationships. The default behaviour does not wire up any registration;
   * subclasses are responsible for handling how actions relate to one another.
   *
   * @param action Actions that only exist with respect to this action.
   */
  public MenuAction addSubActions( final MenuAction... action ) {
    mSubActions.addAll( List.of( action ) );
    return this;
  }

  /**
   * TODO: Reuse the {@link GenericBuilder}.
   *
   * @return The {@link Builder} for an instance of {@link Action}.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Provides a fluent interface around constructing actions so that duplication
   * can be avoided.
   */
  public static class Builder {
    private String mText;
    private String mAccelerator;
    private GlyphIcons mIcon;
    private EventHandler<ActionEvent> mHandler;

    /**
     * Sets the text, icon, and accelerator for a given action identifier.
     * See the "App.action" entries in the messages properties file for details.
     *
     * @param id The identifier to look up in the properties file.
     * @return An instance of {@link Builder} that can be built into an
     * instance of {@link Action}.
     */
    public Builder setId( final String id ) {
      final var prefix = "App.action." + id + ".";
      final var text = prefix + "text";
      final var icon = prefix + "icon";
      final var accelerator = prefix + "accelerator";
      final var builder = setText( text ).setIcon( icon );

      return Messages.containsKey( accelerator )
          ? builder.setAccelerator( Messages.get( accelerator ) )
          : builder;
    }

    /**
     * Sets the action text based on a resource bundle key.
     *
     * @param key The key to look up in the {@link Messages}.
     * @return The corresponding value, or the key name if none found.
     * TODO: Make private or delete and merge into setId
     */
    public Builder setText( final String key ) {
      mText = Messages.get( key, key );
      return this;
    }

    /**
     * TODO: Make private or delete and merge into setId
     */
    public Builder setAccelerator( final String accelerator ) {
      mAccelerator = accelerator;
      return this;
    }

    /**
     * TODO: Make private or delete and merge into setId
     */
    public Builder setIcon( final GlyphIcons icon ) {
      mIcon = icon;
      return this;
    }

    private Builder setIcon( final String iconKey ) {
      assert iconKey != null;

      final var iconValue = Messages.get( iconKey );

      return iconKey.equals( iconValue )
          ? this
          : setIcon( getIcon( iconValue ) );
    }

    public Builder setHandler( final EventHandler<ActionEvent> handler ) {
      mHandler = handler;
      return this;
    }

    public Action build() {
      return new Action( mText, mAccelerator, mIcon, mHandler );
    }

    private GlyphIcons getIcon( final String name ) {
      return FontAwesomeIcon.valueOf( name.toUpperCase() );
    }
  }
}
