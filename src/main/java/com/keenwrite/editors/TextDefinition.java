/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.editors;

import com.keenwrite.TextResource;
import com.keenwrite.definition.DefinitionEditor;
import com.keenwrite.editors.markdown.MarkdownEditor;

import java.util.Map;

/**
 * Differentiates an instance of {@link TextResource} from an instance of
 * {@link DefinitionEditor} or {@link MarkdownEditor}.
 */
public interface TextDefinition extends TextResource {
  /**
   * Converts the definitions into a map, ready for interpolation.
   *
   * @return The list of key value pairs delimited with tokens.
   */
  Map<String, String> toMap();
}
