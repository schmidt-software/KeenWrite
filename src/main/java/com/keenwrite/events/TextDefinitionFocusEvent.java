/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.events;

import com.keenwrite.editors.TextDefinition;

public class TextDefinitionFocusEvent extends FocusEvent<TextDefinition> {
  protected TextDefinitionFocusEvent( final TextDefinition editor ) {
    super( editor );
  }

  /**
   * When the {@link TextDefinition} editor has focus, fire an event so that
   * subscribers may perform an action.
   *
   * @param editor The instance of editor that has gained input focus.
   */
  public static void fire( final TextDefinition editor ) {
    new TextDefinitionFocusEvent( editor ).publish();
  }
}
