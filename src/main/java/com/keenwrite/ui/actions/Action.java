/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.actions;

import com.keenwrite.Messages;
import com.keenwrite.util.GenericBuilder;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCombination;

import java.util.ArrayList;
import java.util.List;

import static com.keenwrite.constants.Constants.ACTION_PREFIX;
import static com.keenwrite.ui.fonts.IconFactory.createGraphic;
import static javafx.scene.input.KeyCombination.valueOf;

/**
 * Defines actions the user can take through GUI interactions
 */
public final class Action implements MenuAction {
  private final String mText;
  private final KeyCombination mAccelerator;
  private final String mIcon;
  private final EventHandler<ActionEvent> mHandler;
  private final List<MenuAction> mSubActions = new ArrayList<>();

  /**
   * Provides a fluent interface around constructing actions so that duplication
   * can be avoided.
   */
  public static class Builder {
    private String mText;
    private String mAccelerator;
    private String mIcon;
    private EventHandler<ActionEvent> mHandler;

    /**
     * Sets the text, icon, and accelerator for a given action identifier.
     * See the messages properties file for details.
     *
     * @param id The identifier to look up in the properties file.
     * @return An instance of {@link Builder} that can be built into an
     * instance of {@link Action}.
     */
    public Builder setId( final String id ) {
      final var prefix = STR."\{ACTION_PREFIX}\{id}.";
      final var text = STR."\{prefix}text";
      final var icon = STR."\{prefix}icon";
      final var accelerator = STR."\{prefix}accelerator";
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
     */
    private Builder setText( final String key ) {
      mText = Messages.get( key, key );
      return this;
    }

    private Builder setAccelerator( final String accelerator ) {
      mAccelerator = accelerator;
      return this;
    }

    private Builder setIcon( final String iconKey ) {
      assert iconKey != null;

      // If there's no icon associated with the icon key name, don't attempt
      // to create a graphic for the icon, because it won't exist.
      final var iconName = Messages.get( iconKey );
      mIcon = iconKey.equals( iconName ) ? "" : iconName;

      return this;
    }

    public Builder setHandler( final EventHandler<ActionEvent> handler ) {
      mHandler = handler;
      return this;
    }

    public Action build() {
      return new Action( mText, mAccelerator, mIcon, mHandler );
    }
  }

  /**
   * TODO: Reuse the {@link GenericBuilder}.
   *
   * @return The {@link Builder} for an instance of {@link Action}.
   */
  public static Builder builder() {
    return new Builder();
  }

  private static Button createIconButton( final String icon ) {
    return new Button( null, createGraphic( icon ) );
  }

  public Action(
    final String text,
    final String accelerator,
    final String icon,
    final EventHandler<ActionEvent> handler ) {
    assert text != null;
    assert handler != null;

    mText = text;
    mAccelerator = accelerator == null ? null : valueOf( accelerator );
    mIcon = icon;
    mHandler = handler;
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
      menuItem.setGraphic( createGraphic( mIcon ) );
    }

    menuItem.setOnAction( mHandler );

    return menuItem;
  }

  @Override
  public Button createToolBarNode() {
    final var button = createIconButton( mIcon );
    var tooltip = mText;

    if( tooltip.endsWith( "..." ) ) {
      tooltip = tooltip.substring( 0, tooltip.length() - 3 );
    }

    // Do not display mnemonic accelerator character in tooltip text.
    // The accelerator key will still be available, this is display-only.
    tooltip = tooltip.replace( "_", "" );

    if( mAccelerator != null ) {
      tooltip += STR." (\{mAccelerator.getDisplayText()}\{')'}";
    }

    button.setTooltip( new Tooltip( tooltip ) );
    button.setFocusTraversable( false );
    button.setOnAction( mHandler );

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
}
