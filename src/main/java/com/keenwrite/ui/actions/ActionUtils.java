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
package com.keenwrite.ui.actions;

import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;

/**
 * Responsible for creating menu items and toolbar buttons.
 */
public class ActionUtils {

  public static Menu createMenu( final String text, final Action... actions ) {
    return new Menu( text, null, createMenuItems( actions ) );
  }

  public static MenuItem[] createMenuItems( final Action... actions ) {
    final var menuItems = new MenuItem[ actions.length ];

    for( int i = 0; i < actions.length; i++ ) {
      menuItems[ i ] = actions[ i ].createMenuItem();
    }

    return menuItems;
  }

  /**
   * TODO: Delete
   * @deprecated Moved into ApplicationMenuBar
   */
  public static ToolBar createToolBar( final Action... actions ) {
    return new ToolBar( createToolBarButtons( actions ) );
  }

  /**
   * TODO: Delete
   * @deprecated Moved into ApplicationMenuBar
   */
  public static Node[] createToolBarButtons( final Action... actions ) {
    final var buttons = new Node[ actions.length ];

    for( int i = 0; i < actions.length; i++ ) {
      buttons[ i ] = actions[ i ].createToolBarButton();
    }

    return buttons;
  }
}
