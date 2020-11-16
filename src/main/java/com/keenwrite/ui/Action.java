/*
 * Copyright 2020 White Magic Software, Ltd.
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
package com.keenwrite.ui;

import com.keenwrite.Messages;
import com.keenwrite.util.GenericBuilder;
import de.jensd.fx.glyphs.GlyphIcons;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;

/**
 * Represents a menu action that can generate {@link MenuItem} instances and
 * and {@link Node} instances for a toolbar.
 */
public abstract class Action {
  /**
   * TODO: Reuse the {@link GenericBuilder}.
   *
   * @return The {@link Builder} for an instance of {@link Action}.
   */
  public static Builder builder() {
    return new Builder();
  }

  public abstract MenuItem createMenuItem();

  public abstract Node createToolBarButton();

  /**
   * Adds subordinate actions to the menu. This is used to establish sub-menu
   * relationships. The default behaviour does not wire up any registration;
   * subclasses are responsible for handling how actions relate to one another.
   *
   * @param action Actions that only exist with respect to this action.
   */
  public void addSubActions( Action... action ) {
  }

  /**
   * Provides a fluent interface around constructing actions so that duplication
   * can be avoided.
   */
  public static class Builder {
    private String mText;
    private String mAccelerator;
    private GlyphIcons mIcon;
    private EventHandler<ActionEvent> mAction;
    private ObservableBooleanValue mDisabled;

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

    public Builder setAction( final EventHandler<ActionEvent> action ) {
      mAction = action;
      return this;
    }

    public Builder setDisabled( final ObservableBooleanValue disabled ) {
      mDisabled = disabled;
      return this;
    }

    public Action build() {
      return new MenuAction( mText, mAccelerator, mIcon, mAction, mDisabled );
    }

    private GlyphIcons getIcon( final String name ) {
      return FontAwesomeIcon.valueOf( name.toUpperCase() );
    }
  }
}
