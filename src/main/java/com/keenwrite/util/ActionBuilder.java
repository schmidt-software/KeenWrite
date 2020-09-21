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
package com.keenwrite.util;

import com.keenwrite.Messages;
import de.jensd.fx.glyphs.GlyphIcons;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**
 * Provides a fluent interface around constructing actions so that duplication
 * can be avoided.
 */
public class ActionBuilder {
  private String mText;
  private String mAccelerator;
  private GlyphIcons mIcon;
  private EventHandler<ActionEvent> mAction;
  private ObservableBooleanValue mDisable;

  /**
   * Sets the action text based on a resource bundle key.
   *
   * @param key The key to look up in the {@link Messages}.
   * @return The corresponding value, or the key name if none found.
   */
  public ActionBuilder setText( final String key ) {
    mText = Messages.get( key, key );
    return this;
  }

  public ActionBuilder setAccelerator( final String accelerator ) {
    mAccelerator = accelerator;
    return this;
  }

  public ActionBuilder setIcon( final GlyphIcons icon ) {
    mIcon = icon;
    return this;
  }

  public ActionBuilder setAction( final EventHandler<ActionEvent> action ) {
    mAction = action;
    return this;
  }

  public ActionBuilder setDisable( final ObservableBooleanValue disable ) {
    mDisable = disable;
    return this;
  }

  public Action build() {
    return new MenuAction( mText, mAccelerator, mIcon, mAction, mDisable );
  }
}
